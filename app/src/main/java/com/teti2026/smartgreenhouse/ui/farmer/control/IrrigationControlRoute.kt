package com.teti2026.smartgreenhouse.ui.farmer.control

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.BackendRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTOMATIC_POLL_INTERVAL_MS = 60_000L

/**
 * Route "Kontrol Irigasi - Petani". State [ActuatorControlUiState] di-hoist di sini (pola sama
 * seperti [com.teti2026.smartgreenhouse.ui.farmer.history.ImageAnalysisDetailRoute]).
 *
 * [onPowerToggle] memanggil `POST /irrigation/trigger` sungguhan lewat [BackendRepository]
 * (aktuator = "irrigation" → dipetakan ke `Water_pump_actuator_status` model AI, lihat
 * `BackendRepository.getIrrigationRecommendation`). Saat [ActuatorControlMode.AUTOMATIC] aktif,
 * loop polling `GET /plots/{id}/irrigation/recommendation` tiap ~60 detik menggerakkan servo
 * SENDIRI tanpa konfirmasi petani begitu rekomendasi AI berbeda dari state saat ini — sesuai
 * keputusan eksplisit (lihat plan MOB wiring IoT↔Mobile).
 *
 * [onManualModeActivated] dipanggil SEKALI setiap kali mode berubah ke [ActuatorControlMode.MANUAL]
 * (bukan tiap recomposition) — dipakai caller ([com.teti2026.smartgreenhouse.ui.farmer.
 * DashboardFarmerRoute]) untuk memicu toast di puncak layar HP (BUKAN di dalam sheet ini, lihat
 * catatan [ManualModeTopToast]). [onTriggerError] dipakai caller yang sama untuk toast error.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationControlRoute(
    plotId: String,
    deviceId: String,
    onDismiss: () -> Unit,
    onManualModeActivated: () -> Unit = {},
    onTriggerError: (String) -> Unit = {},
    backendRepository: BackendRepository = BackendRepository()
) {
    var uiState by remember {
        mutableStateOf(
            ActuatorControlUiState(
                mode = ActuatorControlMode.AUTOMATIC,
                isOn = true,
                activeSinceLabel = "3 menit lalu"
            )
        )
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val triggerErrorMessage = stringResource(R.string.actuator_control_trigger_error)

    fun dismissThenNavigate(action: () -> Unit) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
            }
        }
    }

    fun triggerIrrigation(turnOn: Boolean) {
        scope.launch {
            backendRepository.triggerActuator(
                plotId = plotId,
                deviceId = deviceId,
                actuator = "irrigation",
                action = if (turnOn) "open" else "close"
            ).onSuccess {
                uiState = uiState.copy(isOn = turnOn)
            }.onFailure {
                onTriggerError(triggerErrorMessage)
            }
        }
    }

    // Mode OTOMATIS: polling rekomendasi AI, auto-trigger servo sendiri tanpa konfirmasi petani
    // saat rekomendasi berbeda dari state [isOn] saat ini. Berhenti otomatis (LaunchedEffect
    // cancel) begitu mode berubah ke MANUAL atau sheet ditutup.
    LaunchedEffect(uiState.mode, plotId) {
        if (uiState.mode != ActuatorControlMode.AUTOMATIC) return@LaunchedEffect
        while (true) {
            backendRepository.getIrrigationRecommendation(plotId)
                .onSuccess { recommendation ->
                    val recommended = recommendation.irrigationOn
                    if (recommended != null && recommended != uiState.isOn) {
                        triggerIrrigation(recommended)
                    }
                }
            delay(AUTOMATIC_POLL_INTERVAL_MS)
        }
    }

    IrrigationControlScreen(
        uiState = uiState,
        onModeSelected = { mode ->
            uiState = uiState.copy(mode = mode)
            if (mode == ActuatorControlMode.MANUAL) onManualModeActivated()
        },
        onPowerToggle = { triggerIrrigation(!uiState.isOn) },
        sheetState = sheetState,
        onDismiss = { dismissThenNavigate(onDismiss) }
    )
}
