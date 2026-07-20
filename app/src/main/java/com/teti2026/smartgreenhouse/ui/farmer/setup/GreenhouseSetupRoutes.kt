package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
        onLocationChanged = { position ->
            // TODO: label lokasi sungguhan butuh reverse geocoding (Geocoder/Places API, MOB-T04).
            // Untuk sekarang label memakai koordinat yang dibulatkan sebagai placeholder.
            stateHolder.updateLocation(position, "%.4f, %.4f".format(position.latitude, position.longitude))
        },
        onBackClick = onBackClick,
        onNextClick = onNextClick
    )
}

/**
 * Route tipis langkah 3/3 — menghubungkan [GreenhouseSetupStateHolder] dengan
 * [GreenhouseSetupPairingScreen]. [onFinishClick] (caller, NavGraph) dipanggil PERSIS SEKALI,
 * hanya setelah [GreenhouseSetupStateHolder.submitState] menjadi [SetupSubmitState.Success]
 * (dokumen farms+plots sudah tersimpan) — bukan langsung saat tombol ditekan.
 */
@Composable
fun GreenhouseSetupPairingRoute(
    stateHolder: GreenhouseSetupStateHolder,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val submitState by stateHolder.submitState.collectAsStateWithLifecycle()

    LaunchedEffect(submitState) {
        if (submitState is SetupSubmitState.Success) {
            onFinishClick()
        }
    }

    val errorMessage = (submitState as? SetupSubmitState.Error)?.let { stringResource(it.messageResId) }

    GreenhouseSetupPairingScreen(
        pairingCode = stateHolder.state.pairingCode,
        onPairingCodeChange = stateHolder::updatePairingCode,
        onBackClick = onBackClick,
        onFinishClick = stateHolder::submit,
        onHelpClick = { /* TODO: navigasi ke bantuan/FAQ pairing perangkat */ },
        isLoading = submitState is SetupSubmitState.Loading,
        errorMessage = errorMessage
    )
}
