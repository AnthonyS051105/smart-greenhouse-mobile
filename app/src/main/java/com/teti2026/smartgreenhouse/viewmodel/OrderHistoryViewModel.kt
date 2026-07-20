package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryItem
import com.teti2026.smartgreenhouse.ui.buyer.OrderStatus
import com.teti2026.smartgreenhouse.util.formatOrderDate
import com.teti2026.smartgreenhouse.util.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface OrderHistoryUiState {
    data object Loading : OrderHistoryUiState
    data class Success(val orders: List<OrderHistoryItem>) : OrderHistoryUiState
    data class Error(@param:StringRes val messageResId: Int) : OrderHistoryUiState
}

/**
 * Daftar `orders` milik pembeli yang login (data-contracts.md §3.8), digabung `listings` untuk
 * nama produk & foto (N+1 client-side, padanan pola lain di app ini). Pesanan yang listing-nya
 * sudah tidak ada (dihapus) dilewati (`mapNotNull`) — kasus tepi, bukan error. `status` mentah
 * Firestore dipetakan ke [OrderStatus] via [OrderStatus.wireValue]; fallback [OrderStatus.PENDING]
 * bila nilainya tidak dikenal (seharusnya tidak terjadi — `createOrder` hanya pernah menulis
 * "pending", belum ada UI ubah status apapun).
 */
class OrderHistoryViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val state: StateFlow<OrderHistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = OrderHistoryUiState.Error(R.string.auth_error_generic)
            return
        }
        _state.value = OrderHistoryUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getOrdersForBuyer(uid)
                .mapCatching { orders ->
                    orders.sortedByDescending { it.createdAt }.mapNotNull { order ->
                        val listing = firestoreRepository.getListingById(order.listingId).getOrNull()
                            ?: return@mapNotNull null
                        OrderHistoryItem(
                            id = order.id,
                            listingId = order.listingId,
                            cropName = listing.productName.ifBlank { listing.cropType },
                            imageUrl = listing.imageUrls.firstOrNull().orEmpty(),
                            imageContentDescription = listing.productName,
                            totalPriceLabel = formatRupiah(order.totalPrice),
                            dateLabel = formatOrderDate(order.createdAt),
                            status = OrderStatus.entries.firstOrNull { it.wireValue == order.status }
                                ?: OrderStatus.PENDING
                        )
                    }
                }
                .onSuccess { orders -> _state.value = OrderHistoryUiState.Success(orders) }
                .onFailure { _state.value = OrderHistoryUiState.Error(R.string.order_history_error_load_failed) }
        }
    }
}
