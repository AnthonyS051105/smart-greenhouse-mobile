package com.teti2026.smartgreenhouse.ui.farmer.control

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

/**
 * Route "Kontrol Irigasi - Petani". State [ActuatorControlUiState] di-hoist di sini (pola sama
 * seperti [com.teti2026.smartgreenhouse.ui.farmer.history.ImageAnalysisDetailRoute]) — [onPowerToggle]
 * untuk sekarang hanya mengubah state lokal.
 *
 * [onManualModeActivated] dipanggil SEKALI setiap kali mode berubah ke [ActuatorControlMode.MANUAL]
 * (bukan tiap recomposition) — dipakai caller ([com.teti2026.smartgreenhouse.ui.farmer.
 * DashboardFarmerRoute]) untuk memicu toast di puncak layar HP (BUKAN di dalam sheet ini, lihat
 * catatan [ManualModeTopToast]).
 *
 * TODO (MOB-T11): [onPowerToggle] harus memanggil
 * `BackendRepository.triggerActuator(plotId, actuator = "irrigation", action = "open"/"close",
 * durationSeconds)` (`POST /irrigation/trigger`, override manual — `docs/data-contracts.md
 * §4.4`) begitu dikerjakan, bukan hanya state lokal seperti sekarang.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationControlRoute(
    onDismiss: () -> Unit,
    onManualModeActivated: () -> Unit = {}
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

    fun dismissThenNavigate(action: () -> Unit) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
            }
        }
    }

    IrrigationControlScreen(
        uiState = uiState,
        onModeSelected = { mode ->
            uiState = uiState.copy(mode = mode)
            if (mode == ActuatorControlMode.MANUAL) onManualModeActivated()
        },
        onPowerToggle = { uiState = uiState.copy(isOn = !uiState.isOn) },
        sheetState = sheetState,
        onDismiss = { dismissThenNavigate(onDismiss) }
    )
}
