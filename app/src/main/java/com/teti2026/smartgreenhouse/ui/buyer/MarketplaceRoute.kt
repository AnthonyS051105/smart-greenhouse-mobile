package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.MarketplaceUiState
import com.teti2026.smartgreenhouse.viewmodel.MarketplaceViewModel

private val MARKETPLACE_FILTERS = listOf("Jenis", "Lokasi", "Harga", "Skor Min.")

// TODO: [searchQuery]/[selectedFilter] masih tampilan saja, belum benar-benar memfilter
// [listings] sungguhan (lihat MarketplaceViewModel) — filter fungsional menyusul terpisah.
@Composable
fun MarketplaceRoute(
    onListingClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    viewModel: MarketplaceViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MARKETPLACE_FILTERS.first()) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is MarketplaceUiState.Loading -> ProfileLoadingIndicator()
        is MarketplaceUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load
        )
        is MarketplaceUiState.Success -> {
            MarketplaceScreen(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                filters = MARKETPLACE_FILTERS,
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                listings = s.listings,
                onListingClick = onListingClick,
                onNotificationsClick = onNotificationsClick,
                currentBottomNavRoute = Routes.BUYER_MARKETPLACE,
                onBottomNavigate = onBottomNavigate
            )
        }
    }
}
