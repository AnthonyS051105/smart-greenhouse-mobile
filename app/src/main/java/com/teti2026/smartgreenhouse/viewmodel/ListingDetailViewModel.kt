package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailItem
import com.teti2026.smartgreenhouse.util.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface ListingDetailUiState {
    data object Loading : ListingDetailUiState
    data class Success(val listing: ListingDetailItem) : ListingDetailUiState
    data class Error(@param:StringRes val messageResId: Int) : ListingDetailUiState
}

/**
 * Gabungan `listings` + `farms` + `users` (data-contracts.md §3.2/§3.7/§3.1), padanan
 * [MarketplaceViewModel] untuk satu listing. [ListingDetailItem.sensorHistory] SELALU kosong &
 * [ListingDetailItem.sellerRatingLabel] SELALU null — belum ada `sensor_readings` sungguhan
 * (butuh backend/IoT) maupun agregasi `reviews` (menyusul terpisah). [ListingDetailItem
 * .minOrderKg]/[minOrderLabel] didefaultkan 1 kg — form Buat Listing tidak punya input untuk
 * ini sama sekali (bukan keputusan skema, murni default UI, beda dari `description`/
 * `pre_order_enabled`/`product_name` yang memang berasal dari input form nyata).
 */
class ListingDetailViewModel @JvmOverloads constructor(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ListingDetailUiState>(ListingDetailUiState.Loading)
    val state: StateFlow<ListingDetailUiState> = _state.asStateFlow()

    fun load(listingId: String) {
        _state.value = ListingDetailUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getListingById(listingId)
                .mapCatching { listing ->
                    val farm = firestoreRepository.getFarmById(listing.farmId).getOrNull()
                    val seller = farm?.let { firestoreRepository.getUser(it.ownerUid).getOrNull() }
                    ListingDetailItem(
                        id = listing.id,
                        farmId = listing.farmId,
                        cropName = listing.productName.ifBlank { listing.cropType },
                        locationLabel = farm?.farmName.orEmpty(),
                        harvestLabel = listing.harvestDate,
                        imageUrls = listing.imageUrls,
                        imageContentDescription = listing.productName,
                        healthScore = listing.healthScore,
                        pricePerKg = listing.pricePerKg,
                        pricePerKgLabel = formatRupiah(listing.pricePerKg),
                        quantityAvailableKg = listing.quantityKg,
                        quantityAvailableLabel = "${listing.quantityKg.toInt()} Kg",
                        minOrderKg = 1.0,
                        minOrderLabel = "1 Kg",
                        unitLabel = "Kg",
                        description = listing.description,
                        sensorHistoryStatusLabel = "Belum ada data sensor",
                        sensorHistory = emptyList(),
                        sellerName = seller?.name.orEmpty(),
                        sellerAvatarUrl = null,
                        sellerRatingLabel = null,
                        sellerActivityLabel = ""
                    )
                }
                .onSuccess { item -> _state.value = ListingDetailUiState.Success(item) }
                .onFailure { _state.value = ListingDetailUiState.Error(R.string.listing_detail_error_load_failed) }
        }
    }
}
