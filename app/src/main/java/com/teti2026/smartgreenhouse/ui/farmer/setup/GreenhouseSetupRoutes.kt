package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.compose.runtime.Composable

/** Route tipis langkah 1/3 — menghubungkan [GreenhouseSetupStateHolder] dengan [GreenhouseSetupDataScreen]. */
@Composable
fun GreenhouseSetupDataRoute(
    stateHolder: GreenhouseSetupStateHolder,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    GreenhouseSetupDataScreen(
        greenhouseName = stateHolder.state.greenhouseName,
        onGreenhouseNameChange = stateHolder::updateGreenhouseName,
        sizeM2 = stateHolder.state.sizeM2,
        onSizeM2Change = stateHolder::updateSizeM2,
        cropType = stateHolder.state.cropType,
        onCropTypeSelected = stateHolder::updateCropType,
        onBackClick = onBackClick,
        onNextClick = onNextClick
    )
}

/** Route tipis langkah 2/3 — menghubungkan [GreenhouseSetupStateHolder] dengan [GreenhouseSetupLocationScreen]. */
@Composable
fun GreenhouseSetupLocationRoute(
    stateHolder: GreenhouseSetupStateHolder,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    GreenhouseSetupLocationScreen(
        selectedLocation = stateHolder.state.location,
        locationLabel = stateHolder.state.locationLabel,
        searchQuery = stateHolder.searchQuery,
        onSearchQueryChange = stateHolder::updateSearchQuery,
        onLocationChanged = { latLng ->
            // TODO: label lokasi sungguhan butuh reverse geocoding (Geocoder/Places API, MOB-T04).
            // Untuk sekarang label memakai koordinat yang dibulatkan sebagai placeholder.
            stateHolder.updateLocation(latLng, "%.4f, %.4f".format(latLng.latitude, latLng.longitude))
        },
        onBackClick = onBackClick,
        onNextClick = onNextClick
    )
}

/** Route tipis langkah 3/3 — menghubungkan [GreenhouseSetupStateHolder] dengan [GreenhouseSetupPairingScreen]. */
@Composable
fun GreenhouseSetupPairingRoute(
    stateHolder: GreenhouseSetupStateHolder,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    GreenhouseSetupPairingScreen(
        pairingCode = stateHolder.state.pairingCode,
        onPairingCodeChange = stateHolder::updatePairingCode,
        onBackClick = onBackClick,
        onFinishClick = onFinishClick,
        onHelpClick = { /* TODO: navigasi ke bantuan/FAQ pairing perangkat */ }
    )
}
