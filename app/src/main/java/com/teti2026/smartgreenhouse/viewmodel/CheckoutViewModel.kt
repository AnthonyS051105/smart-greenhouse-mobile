package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailItem
import com.teti2026.smartgreenhouse.util.toListingDetailItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5` — untuk pemuatan data listing yang di-checkout. */
sealed interface CheckoutUiState {
    data object Loading : CheckoutUiState
    data class Success(val listing: ListingDetailItem) : CheckoutUiState
    data class Error(@param:StringRes val messageResId: Int) : CheckoutUiState
}

/** State TERPISAH untuk aksi "Konfirmasi Pesanan" (submit) — beda siklus hidup dari [CheckoutUiState] pemuatan data, pola sama seperti `ListingPublishState`/`SetupSubmitState`. */
sealed interface CheckoutSubmitState {
    data object Idle : CheckoutSubmitState
    data object Submitting : CheckoutSubmitState
    data class Error(@param:StringRes val messageResId: Int) : CheckoutSubmitState
}

/**
 * Checkout — [load] baca listing yang mau dibeli (padanan [ListingDetailViewModel]/[ChatViewModel]),
 * [submitOrder] tulis dokumen `orders` sungguhan (data-contracts.md §3.8) via
 * [FirestoreRepository.createOrder]. Kuantitas/metode pengiriman/alamat TETAP `remember` lokal di
 * `CheckoutRoute` (state UI form, bukan data bisnis) — hanya dikirim ke ViewModel saat submit.
 */
class CheckoutViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    private val _submitState = MutableStateFlow<CheckoutSubmitState>(CheckoutSubmitState.Idle)
    val submitState: StateFlow<CheckoutSubmitState> = _submitState.asStateFlow()

    private var sellerUid: String? = null

    fun load(listingId: String) {
        _state.value = CheckoutUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getListingById(listingId)
                .mapCatching { listing ->
                    val farm = firestoreRepository.getFarmById(listing.farmId).getOrNull()
                    sellerUid = farm?.ownerUid
                    val seller = farm?.let { firestoreRepository.getUser(it.ownerUid).getOrNull() }
                    listing.toListingDetailItem(farm, seller)
                }
                .onSuccess { _state.value = CheckoutUiState.Success(it) }
                .onFailure { _state.value = CheckoutUiState.Error(R.string.checkout_error_load_failed) }
        }
    }

    /** [totalPrice] = subtotal + biaya-biaya, sudah dihitung penuh oleh caller (`CheckoutRoute`) sesuai yang ditampilkan di layar. */
    fun submitOrder(quantityKg: Double, totalPrice: Long, onOrderCreated: (orderId: String) -> Unit) {
        val uid = authRepository.currentUser()?.uid
        val listingId = (_state.value as? CheckoutUiState.Success)?.listing?.id
        val seller = sellerUid
        if (uid == null || listingId == null || seller == null) {
            _submitState.value = CheckoutSubmitState.Error(R.string.checkout_error_submit_failed)
            return
        }
        _submitState.value = CheckoutSubmitState.Submitting
        viewModelScope.launch {
            firestoreRepository.createOrder(uid, seller, listingId, quantityKg, totalPrice)
                .onSuccess { order ->
                    _submitState.value = CheckoutSubmitState.Idle
                    onOrderCreated(order.id)
                }
                .onFailure { _submitState.value = CheckoutSubmitState.Error(R.string.checkout_error_submit_failed) }
        }
    }
}
