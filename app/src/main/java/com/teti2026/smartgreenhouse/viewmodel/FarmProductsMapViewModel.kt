package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailItem
import com.teti2026.smartgreenhouse.ui.buyer.MapFarmItem
import com.teti2026.smartgreenhouse.util.toListingDetailItem
import com.teti2026.smartgreenhouse.util.toMapFarmItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface FarmProductsMapUiState {
    data object Loading : FarmProductsMapUiState
    data class Success(val farm: MapFarmItem, val products: List<ListingDetailItem>) : FarmProductsMapUiState
    data class Error(@param:StringRes val messageResId: Int) : FarmProductsMapUiState
}

/**
 * Padanan [MapViewModel] untuk satu kebun spesifik (screen "Produk Lahan - Peta", dijangkau dari
 * tap kebun/marker di `MapScreen`). [seller] (pemilik kebun) hanya di-resolve SEKALI (bukan per
 * listing seperti [MarketplaceViewModel]/[ListingDetailViewModel]) karena seluruh produk di
 * screen ini pasti berasal dari kebun & pemilik yang sama.
 */
class FarmProductsMapViewModel @JvmOverloads constructor(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<FarmProductsMapUiState>(FarmProductsMapUiState.Loading)
    val state: StateFlow<FarmProductsMapUiState> = _state.asStateFlow()

    fun load(farmId: String) {
        _state.value = FarmProductsMapUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getFarmById(farmId)
                .mapCatching { farm ->
                    val listings = firestoreRepository.getListingsForFarm(farmId).getOrThrow()
                    val seller = firestoreRepository.getUser(farm.ownerUid).getOrNull()
                    FarmProductsMapUiState.Success(
                        farm = farm.toMapFarmItem(listings),
                        products = listings.map { it.toListingDetailItem(farm, seller) }
                    )
                }
                .onSuccess { _state.value = it }
                .onFailure { _state.value = FarmProductsMapUiState.Error(R.string.farm_products_error_load_failed) }
        }
    }
}
