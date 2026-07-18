package com.teti2026.smartgreenhouse.ui.buyer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan state & data kebun ke MapViewModel (StateFlow<UiState<List<MapFarmItem>>>)
// yang mengambil data dari FirestoreRepository.getFarmsForMap() begitu MOB-T19 dikerjakan.
@Composable
fun MapRoute(
    onFarmClick: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedQuickFilter by remember { mutableStateOf<String?>(null) }

    val quickFilters = listOf(
        stringResource(R.string.map_quick_filter_vegetable),
        stringResource(R.string.map_quick_filter_fruit),
        stringResource(R.string.map_quick_filter_nearest)
    )

    MapScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        quickFilters = quickFilters,
        selectedQuickFilter = selectedQuickFilter,
        onQuickFilterSelected = { filter ->
            selectedQuickFilter = if (selectedQuickFilter == filter) null else filter
        },
        farms = sampleNearbyFarms,
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
