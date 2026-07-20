package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.OrderStatus
import com.teti2026.smartgreenhouse.ui.farmer.orders.FarmerOrderItem
import com.teti2026.smartgreenhouse.util.formatOrderDate
import com.teti2026.smartgreenhouse.util.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface FarmerOrdersUiState {
    data object Loading : FarmerOrdersUiState
    data class Success(val orders: List<FarmerOrderItem>) : FarmerOrdersUiState
    data class Error(@param:StringRes val messageResId: Int) : FarmerOrdersUiState
}

/**
 * Daftar `orders` MASUK milik petani yang login (data-contracts.md §3.8, filter `seller_uid`),
 * digabung `listings` (nama produk & foto) + `users` (nama pembeli), N+1 client-side, padanan
 * [OrderHistoryViewModel] sisi Pembeli. [updateStatus] memanggil
 * [FirestoreRepository.updateOrderStatus] lalu memuat ULANG daftar (bukan realtime — konsisten
 * dengan [OrderHistoryViewModel], beda dari Chat yang memang butuh realtime untuk UX percakapan).
 */
class FarmerOrdersViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<FarmerOrdersUiState>(FarmerOrdersUiState.Loading)
    val state: StateFlow<FarmerOrdersUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = FarmerOrdersUiState.Error(R.string.auth_error_generic)
            return
        }
        _state.value = FarmerOrdersUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getOrdersForSeller(uid)
                .mapCatching { orders ->
                    orders.sortedByDescending { it.createdAt }.mapNotNull { order ->
                        val listing = firestoreRepository.getListingById(order.listingId).getOrNull()
                            ?: return@mapNotNull null
                        val buyer = firestoreRepository.getUser(order.buyerUid).getOrNull()
                        FarmerOrderItem(
                            id = order.id,
                            listingId = order.listingId,
                            cropName = listing.productName.ifBlank { listing.cropType },
                            imageUrl = listing.imageUrls.firstOrNull().orEmpty(),
                            imageContentDescription = listing.productName,
                            buyerName = buyer?.name.orEmpty(),
                            quantityLabel = "${order.quantityKg.toInt()} Kg",
                            totalPriceLabel = formatRupiah(order.totalPrice),
                            dateLabel = formatOrderDate(order.createdAt),
                            status = OrderStatus.entries.firstOrNull { it.wireValue == order.status }
                                ?: OrderStatus.PENDING
                        )
                    }
                }
                .onSuccess { orders -> _state.value = FarmerOrdersUiState.Success(orders) }
                .onFailure { _state.value = FarmerOrdersUiState.Error(R.string.farmer_orders_error_load_failed) }
        }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            firestoreRepository.updateOrderStatus(orderId, newStatus.wireValue)
                .onSuccess { load() }
        }
    }
}
