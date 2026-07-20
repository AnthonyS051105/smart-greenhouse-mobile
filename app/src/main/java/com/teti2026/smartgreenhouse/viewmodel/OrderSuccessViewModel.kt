package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.OrderSuccessItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface OrderSuccessUiState {
    data object Loading : OrderSuccessUiState
    data class Success(val item: OrderSuccessItem) : OrderSuccessUiState
    data class Error(@param:StringRes val messageResId: Int) : OrderSuccessUiState
}

/**
 * "Konfirmasi Pesanan - Berhasil" — baca order SUNGGUHAN (data-contracts.md §3.8) by-id (id
 * dokumen `orders` nyata dari `CheckoutViewModel.submitOrder`, bukan lagi id sample/turunan hash
 * seperti sebelum sesi ini). [OrderSuccessItem.orderId] diformat "AGR-<8 karakter terakhir id
 * Firestore, huruf besar>" — tetap ringkas & mudah dibaca di layar, tapi kini benar-benar
 * merujuk dokumen nyata (id lengkap tetap bisa dicek di Firebase Console bila perlu).
 */
class OrderSuccessViewModel @JvmOverloads constructor(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<OrderSuccessUiState>(OrderSuccessUiState.Loading)
    val state: StateFlow<OrderSuccessUiState> = _state.asStateFlow()

    fun load(orderId: String) {
        _state.value = OrderSuccessUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getOrderById(orderId)
                .mapCatching { order ->
                    val listing = firestoreRepository.getListingById(order.listingId).getOrNull()
                    val farm = listing?.let { firestoreRepository.getFarmById(it.farmId).getOrNull() }
                    val seller = farm?.let { firestoreRepository.getUser(it.ownerUid).getOrNull() }
                    OrderSuccessItem(
                        orderId = "AGR-${order.id.takeLast(8).uppercase()}",
                        sellerName = seller?.name.orEmpty()
                    )
                }
                .onSuccess { _state.value = OrderSuccessUiState.Success(it) }
                .onFailure { _state.value = OrderSuccessUiState.Error(R.string.order_success_error_load_failed) }
        }
    }
}
