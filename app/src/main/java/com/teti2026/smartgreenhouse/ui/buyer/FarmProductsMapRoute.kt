package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.FarmProductsMapUiState
import com.teti2026.smartgreenhouse.viewmodel.FarmProductsMapViewModel

@Composable
fun FarmProductsMapRoute(
    farmId: String,
    onBackClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onSeeAllProductsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    viewModel: FarmProductsMapViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(farmId) {
        viewModel.load(farmId)
    }

    when (val s = state) {
        is FarmProductsMapUiState.Loading -> ProfileLoadingIndicator()
        is FarmProductsMapUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(farmId) }
        )
        is FarmProductsMapUiState.Success -> {
            // Lokasi pembeli dibaca best-effort SEKALI (bukan lewat alur minta izin di sini —
            // izin lokasi sudah ditangani MapScreen sebelumnya) untuk mengisi jarak, sama seperti
            // MapRoute. remember(farmId): tidak perlu baca ulang tiap recomposition biasa.
            val userLocation = remember(farmId) { lastKnownLocation(context) }
            val farm = s.farm.copy(distanceLabel = distanceLabelFrom(userLocation, s.farm.position))
            FarmProductsMapScreen(
                farm = farm,
                products = s.products,
                onBackClick = onBackClick,
                onProductClick = onProductClick,
                onSeeAllProductsClick = onSeeAllProductsClick,
                currentBottomNavRoute = Routes.BUYER_MAP,
                onBottomNavigate = onBottomNavigate
            )
        }
    }
}
