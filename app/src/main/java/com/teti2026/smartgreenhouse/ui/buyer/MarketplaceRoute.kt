package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.teti2026.smartgreenhouse.ui.navigation.Routes

private val MARKETPLACE_FILTERS = listOf("Jenis", "Lokasi", "Harga", "Skor Min.")

// TODO: pindahkan state & data listing ke MarketplaceViewModel (StateFlow<UiState<List<Listing>>>)
// yang mengambil data dari FirestoreRepository.getListings(filter) begitu MOB-T05/T17 dikerjakan.
// [onBottomNavigate] untuk sementara hanya menangani rute yang sudah punya destination.
@Composable
fun MarketplaceRoute(
    onListingClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MARKETPLACE_FILTERS.first()) }

    MarketplaceScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        filters = MARKETPLACE_FILTERS,
        selectedFilter = selectedFilter,
        onFilterSelected = { selectedFilter = it },
        listings = sampleMarketplaceListings,
        onListingClick = onListingClick,
        onNotificationsClick = onNotificationsClick,
        currentBottomNavRoute = Routes.BUYER_MARKETPLACE,
        onBottomNavigate = onBottomNavigate
    )
}
