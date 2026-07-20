package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.MapFarmItem
import com.teti2026.smartgreenhouse.util.toMapFarmItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(val farms: List<MapFarmItem>) : MapUiState
    data class Error(@param:StringRes val messageResId: Int) : MapUiState
}

/**
 * Kebun yang muncul di Peta Marketplace = kebun yang punya minimal satu listing berstatus
 * "available" (bukan seluruh dokumen `farms`) — selaras sudut pandang pembeli (peta ini untuk
 * menemukan produk yang bisa dibeli, bukan direktori kebun kosong). Gabungan `listings`+`farms`
 * (data-contracts.md §3.2/§3.7) **client-side**, padanan [MarketplaceViewModel] (N+1 query per
 * kebun, diterima untuk skala demo bootcamp — Firestore tidak punya join server-side).
 */
class MapViewModel @JvmOverloads constructor(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = MapUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getAvailableListings()
                .mapCatching { listings ->
                    listings.groupBy { it.farmId }.mapNotNull { (farmId, farmListings) ->
                        firestoreRepository.getFarmById(farmId).getOrNull()?.toMapFarmItem(farmListings)
                    }
                }
                .onSuccess { farms -> _state.value = MapUiState.Success(farms) }
                .onFailure { _state.value = MapUiState.Error(R.string.map_error_load_failed) }
        }
    }
}
