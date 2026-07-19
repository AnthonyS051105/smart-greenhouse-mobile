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
 * Route "Kontrol Ventilasi - Petani". State [ActuatorControlUiState] di-hoist di sini, pola
 * identik [IrrigationControlRoute] termasuk [onManualModeActivated].
 *
 * TODO (MOB-T11): [onPowerToggle] harus memanggil
 * `BackendRepository.triggerActuator(plotId, actuator = "ventilation", action = "open"/"close",
 * durationSeconds)` begitu dikerjakan, bukan hanya state lokal seperti sekarang.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentilationControlRoute(
    onDismiss: () -> Unit,
    onManualModeActivated: () -> Unit = {}
) {
    var uiState by remember {
        mutableStateOf(
            ActuatorControlUiState(
                mode = ActuatorControlMode.AUTOMATIC,
                isOn = false,
                activeSinceLabel = null
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

    VentilationControlScreen(
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
