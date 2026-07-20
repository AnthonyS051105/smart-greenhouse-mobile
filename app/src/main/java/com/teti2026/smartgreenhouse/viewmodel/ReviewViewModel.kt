package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.ReviewTargetItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5` — untuk pemuatan kartu ringkasan transaksi yang direview. */
sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data class Success(val target: ReviewTargetItem) : ReviewUiState
    data class Error(@param:StringRes val messageResId: Int) : ReviewUiState
}

/** State TERPISAH untuk aksi "Kirim Ulasan" (submit) — pola sama `CheckoutSubmitState`. */
sealed interface ReviewSubmitState {
    data object Idle : ReviewSubmitState
    data object Submitting : ReviewSubmitState
    data class Error(@param:StringRes val messageResId: Int) : ReviewSubmitState
}

/**
 * "Beri Rating & Ulasan" — [load] baca order+listing+petani sungguhan (padanan
 * [OrderSuccessViewModel]), [submitReview] tulis dokumen `reviews` sungguhan (data-contracts.md
 * §3.10) via [FirestoreRepository.createReview]. `rating`/`comment` TETAP `remember` lokal di
 * `ReviewRoute` (state form UI, bukan data bisnis) — hanya dikirim ke ViewModel saat submit.
 */
class ReviewViewModel @JvmOverloads constructor(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private val _submitState = MutableStateFlow<ReviewSubmitState>(ReviewSubmitState.Idle)
    val submitState: StateFlow<ReviewSubmitState> = _submitState.asStateFlow()

    fun load(orderId: String) {
        _state.value = ReviewUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getOrderById(orderId)
                .mapCatching { order ->
                    val listing = firestoreRepository.getListingById(order.listingId).getOrNull()
                    val farm = listing?.let { firestoreRepository.getFarmById(it.farmId).getOrNull() }
                    val seller = farm?.let { firestoreRepository.getUser(it.ownerUid).getOrNull() }
                    ReviewTargetItem(
                        orderId = order.id,
                        cropName = listing?.productName?.ifBlank { listing.cropType }.orEmpty(),
                        imageUrl = listing?.imageUrls?.firstOrNull().orEmpty(),
                        imageContentDescription = listing?.productName.orEmpty(),
                        sellerName = seller?.name.orEmpty()
                    )
                }
                .onSuccess { _state.value = ReviewUiState.Success(it) }
                .onFailure { _state.value = ReviewUiState.Error(R.string.review_error_load_failed) }
        }
    }

    fun submitReview(rating: Int, comment: String, onSubmitted: () -> Unit) {
        val orderId = (_state.value as? ReviewUiState.Success)?.target?.orderId
        if (orderId == null) {
            _submitState.value = ReviewSubmitState.Error(R.string.review_error_submit_failed)
            return
        }
        _submitState.value = ReviewSubmitState.Submitting
        viewModelScope.launch {
            firestoreRepository.createReview(orderId, rating, comment)
                .onSuccess {
                    _submitState.value = ReviewSubmitState.Idle
                    onSubmitted()
                }
                .onFailure { _submitState.value = ReviewSubmitState.Error(R.string.review_error_submit_failed) }
        }
    }
}
