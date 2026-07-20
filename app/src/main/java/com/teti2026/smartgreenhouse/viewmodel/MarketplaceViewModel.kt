package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.MarketplaceListingItem
import com.teti2026.smartgreenhouse.util.formatRupiahPerKg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface MarketplaceUiState {
    data object Loading : MarketplaceUiState
    data class Success(val listings: List<MarketplaceListingItem>) : MarketplaceUiState
    data class Error(@param:StringRes val messageResId: Int) : MarketplaceUiState
}

/**
 * Gabungan `listings` (status "available") + `farms` + `users` (data-contracts.md §3.2/§3.7/
 * §3.1) — Firestore tidak mendukung join server-side, jadi dilakukan per-listing di sini
 * (N+1 query, cukup untuk skala demo bootcamp). Filter/search di UI (`MarketplaceRoute`) MASIH
 * tampilan saja, belum benar-benar memfilter [listings] — di luar lingkup sesi menyambungkan data.
 */
class MarketplaceViewModel @JvmOverloads constructor(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<MarketplaceUiState>(MarketplaceUiState.Loading)
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = MarketplaceUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getAvailableListings()
                .mapCatching { listings ->
                    listings.map { listing ->
                        val farm = firestoreRepository.getFarmById(listing.farmId).getOrNull()
                        val sellerName = farm?.let {
                            firestoreRepository.getUser(it.ownerUid).getOrNull()?.name
                        }.orEmpty()
                        MarketplaceListingItem(
                            id = listing.id,
                            cropName = listing.productName.ifBlank { listing.cropType },
                            priceLabel = formatRupiahPerKg(listing.pricePerKg),
                            locationLabel = farm?.farmName.orEmpty(),
                            imageUrl = listing.imageUrls.firstOrNull().orEmpty(),
                            imageContentDescription = listing.productName,
                            healthScore = listing.healthScore,
                            sellerName = sellerName
                        )
                    }
                }
                .onSuccess { items -> _state.value = MarketplaceUiState.Success(items) }
                .onFailure { _state.value = MarketplaceUiState.Error(R.string.marketplace_error_load_failed) }
        }
    }
}
