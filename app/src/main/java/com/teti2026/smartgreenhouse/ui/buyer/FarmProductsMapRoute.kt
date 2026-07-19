package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan resolusi farm & produk ke FarmProductsMapViewModel (StateFlow<UiState<...>>)
// yang mengambil data dari FirestoreRepository.getFarmsForMap() + getListings(farmId) begitu
// MOB-T19 dikerjakan — sama seperti TODO di MapRoute.
@Composable
fun FarmProductsMapRoute(
    farmId: String,
    onBackClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onSeeAllProductsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
) {
    val farm = sampleNearbyFarms.firstOrNull { it.id == farmId } ?: return
    val products = listingsForFarm(farmId)

    FarmProductsMapScreen(
        farm = farm,
        products = products,
        onBackClick = onBackClick,
        onProductClick = onProductClick,
        onSeeAllProductsClick = onSeeAllProductsClick,
        currentBottomNavRoute = Routes.BUYER_MAP,
        onBottomNavigate = onBottomNavigate
    )
}
