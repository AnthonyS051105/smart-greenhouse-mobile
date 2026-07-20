package com.teti2026.smartgreenhouse.ui.buyer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.MapUiState
import com.teti2026.smartgreenhouse.viewmodel.MapViewModel
import org.maplibre.spatialk.geojson.Position

// TODO: searchQuery/selectedQuickFilter masih tampilan saja, belum benar-benar memfilter [farms]
// sungguhan — sama seperti TODO di MarketplaceRoute.
@Composable
fun MapRoute(
    onFarmClick: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    // Lokasi pembeli dibaca ULANG setiap kali izin berubah jadi granted (bukan bagian data
    // Firestore) — dipakai untuk mengisi MapFarmItem.distanceLabel di layer UI, lihat
    // distanceLabelFrom (MapScreen.kt).
    var userLocation by remember { mutableStateOf<Position?>(null) }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            userLocation = lastKnownLocation(context)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedQuickFilter by remember { mutableStateOf<String?>(null) }

    val quickFilters = listOf(
        stringResource(R.string.map_quick_filter_vegetable),
        stringResource(R.string.map_quick_filter_fruit),
        stringResource(R.string.map_quick_filter_nearest)
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is MapUiState.Loading -> ProfileLoadingIndicator()
        is MapUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load
        )
        is MapUiState.Success -> {
            val farmsWithDistance = s.farms.map { farm ->
                farm.copy(distanceLabel = distanceLabelFrom(userLocation, farm.position))
            }
            MapScreen(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                quickFilters = quickFilters,
                selectedQuickFilter = selectedQuickFilter,
                onQuickFilterSelected = { filter ->
                    selectedQuickFilter = if (selectedQuickFilter == filter) null else filter
                },
                farms = farmsWithDistance,
                hasLocationPermission = hasLocationPermission,
                onRequestLocationPermission = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onFarmClick = onFarmClick,
                onSeeAllClick = onSeeAllClick,
                currentBottomNavRoute = Routes.BUYER_MAP,
                onBottomNavigate = onBottomNavigate
            )
        }
    }
}
