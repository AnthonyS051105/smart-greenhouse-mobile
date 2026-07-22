package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.farmer.history.CropImageHistoryItem
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageAnalysisDetail
import com.teti2026.smartgreenhouse.util.toCropImageHistoryItem
import com.teti2026.smartgreenhouse.util.toImageAnalysisDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface ImageHistoryUiState {
    data object Loading : ImageHistoryUiState
    data class Success(
        val items: List<CropImageHistoryItem>,
        val details: Map<String, ImageAnalysisDetail>
    ) : ImageHistoryUiState
    data class Error(@param:StringRes val messageResId: Int) : ImageHistoryUiState
}

/**
 * Sumber data "Riwayat Citra - Petani" — menggantikan [com.teti2026.smartgreenhouse.ui.farmer
 * .history.sampleCropImageHistoryItems] statis. Asumsi MVP sama seperti [DashboardViewModel]:
 * satu petani = satu farm = satu plot, jadi seluruh `crop_images` milik plot itu diambil sekaligus
 * (bukan realtime listener seperti `sensor_readings` — riwayat citra tidak butuh update live
 * sedetik itu, cukup dimuat ulang lewat [load]/tombol "Coba Lagi").
 */
class ImageHistoryViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ImageHistoryUiState>(ImageHistoryUiState.Loading)
    val state: StateFlow<ImageHistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = ImageHistoryUiState.Error(R.string.auth_error_generic)
            return
        }
        _state.value = ImageHistoryUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getFarmAndPlotForOwner(uid)
                .mapCatching { (farm, plot) ->
                    val cropImages = firestoreRepository.getCropImages(plot.id).getOrThrow()
                    val items = cropImages.map { it.toCropImageHistoryItem(farm, plot) }
                    val details = cropImages.associate { it.id to it.toImageAnalysisDetail(farm, plot) }
                    items to details
                }
                .onSuccess { (items, details) ->
                    _state.value = ImageHistoryUiState.Success(items, details)
                }
                .onFailure {
                    _state.value = ImageHistoryUiState.Error(R.string.image_history_error_load_failed)
                }
        }
    }
}
