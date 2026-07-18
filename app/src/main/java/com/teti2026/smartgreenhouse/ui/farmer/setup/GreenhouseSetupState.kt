package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

/**
 * State holder form 3-langkah Setup Greenhouse (Data Utama/Lokasi Lahan/Pairing Perangkat).
 * Diambil sebagai [ViewModel] yang di-scope ke [NavBackStackEntry] langkah 1 (lihat
 * [com.teti2026.smartgreenhouse.ui.navigation.GreenhouseNavGraph]) sehingga instance yang SAMA
 * dipakai bersama oleh ketiga step selama flow berjalan — pola standar Navigation Compose untuk
 * berbagi state antar-destination dalam satu alur (bukan lintas seluruh app).
 *
 * TODO: saat MOB-T07 (backend) dikerjakan, [state] dipetakan ke [com.teti2026.smartgreenhouse.data.model.Farm]
 * + [com.teti2026.smartgreenhouse.data.model.Plot] lalu disimpan via FirestoreRepository — untuk
 * sekarang [onFinish] di caller (NavGraph) hanya menavigasi ke Dashboard tanpa menyimpan apa pun.
 */
class GreenhouseSetupStateHolder : ViewModel() {
    var state by mutableStateOf(GreenhouseSetupFormState())
        private set

    fun updateGreenhouseName(value: String) {
        state = state.copy(greenhouseName = value)
    }

    fun updateSizeM2(value: String) {
        state = state.copy(sizeM2 = value)
    }

    fun updateCropType(value: CropTypeOption) {
        state = state.copy(cropType = value)
    }

    fun updateLocation(location: com.google.android.gms.maps.model.LatLng, label: String) {
        state = state.copy(location = location, locationLabel = label)
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
    }

    var searchQuery by mutableStateOf("")
        private set

    fun updatePairingCode(value: String) {
        state = state.copy(pairingCode = value)
    }
}

/**
 * Ambil [GreenhouseSetupStateHolder] yang di-scope ke [backStackEntry] milik langkah 1
 * (Data Utama) — dipanggil dari ketiga sub-route setup dengan [backStackEntry] yang sama
 * (langkah 1) supaya mendapat instance identik, bukan `remember` biasa yang baru per-composable.
 */
@Composable
fun rememberGreenhouseSetupStateHolder(backStackEntry: NavBackStackEntry): GreenhouseSetupStateHolder =
    viewModel(viewModelStoreOwner = backStackEntry)
