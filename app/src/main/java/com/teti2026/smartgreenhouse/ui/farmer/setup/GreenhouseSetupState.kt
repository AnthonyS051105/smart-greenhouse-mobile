package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.spatialk.geojson.Position

/** Sesuai pola `UiState` di `docs/SDD.md §5`, dikhususkan untuk hasil submit "Selesai" langkah 3/3. */
sealed interface SetupSubmitState {
    data object Idle : SetupSubmitState
    data object Loading : SetupSubmitState
    data object Success : SetupSubmitState
    data class Error(@param:StringRes val messageResId: Int) : SetupSubmitState
}

/**
 * State holder form 3-langkah Setup Greenhouse (Data Utama/Lokasi Lahan/Pairing Perangkat).
 * Diambil sebagai [ViewModel] yang di-scope ke [NavBackStackEntry] langkah 1 (lihat
 * [com.teti2026.smartgreenhouse.ui.navigation.GreenhouseNavGraph]) sehingga instance yang SAMA
 * dipakai bersama oleh ketiga step selama flow berjalan — pola standar Navigation Compose untuk
 * berbagi state antar-destination dalam satu alur (bukan lintas seluruh app).
 */
class GreenhouseSetupStateHolder @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    var state by mutableStateOf(GreenhouseSetupFormState())
        private set

    private val _submitState = MutableStateFlow<SetupSubmitState>(SetupSubmitState.Idle)
    val submitState: StateFlow<SetupSubmitState> = _submitState.asStateFlow()

    fun updateGreenhouseName(value: String) {
        state = state.copy(greenhouseName = value)
    }

    fun updateSizeM2(value: String) {
        state = state.copy(sizeM2 = value)
    }

    fun updateCropType(value: CropTypeOption) {
        state = state.copy(cropType = value)
    }

    fun updateLocation(location: Position, label: String) {
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

    /**
     * Simpan [state] terkumpul dari ketiga langkah sebagai dokumen `farms` + `plots` sungguhan.
     * [onFinishClick] caller (NavGraph, lewat [GreenhouseSetupPairingRoute]) hanya dipanggil
     * setelah [submitState] menjadi [SetupSubmitState.Success] — lihat efek di Route.
     */
    fun submit() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _submitState.value = SetupSubmitState.Error(R.string.auth_error_generic)
            return
        }
        val sizeM2 = state.sizeM2.toDoubleOrNull()
        val location = state.location
        if (sizeM2 == null || location == null) {
            _submitState.value = SetupSubmitState.Error(R.string.setup_greenhouse_error_incomplete)
            return
        }
        _submitState.value = SetupSubmitState.Loading
        viewModelScope.launch {
            firestoreRepository.createFarmWithPlot(
                ownerUid = uid,
                farmName = state.greenhouseName,
                farmSizeM2 = sizeM2,
                locationLat = location.latitude,
                locationLng = location.longitude,
                cropType = state.cropType.value,
                deviceId = state.pairingCode
            )
                .onSuccess { _submitState.value = SetupSubmitState.Success }
                .onFailure { _submitState.value = SetupSubmitState.Error(R.string.setup_greenhouse_error_save_failed) }
        }
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
