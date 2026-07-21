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
 * Route "Kontrol Ventilasi - Petani". Pola identik [IrrigationControlRoute] termasuk
 * [onManualModeActivated]/[onTriggerError] dan loop polling mode OTOMATIS — bedanya
 * `actuator = "ventilation"` dipetakan ke `Fan_actuator_status` model AI (`BackendRepository.
 * getIrrigationRecommendation`).
 *
 * Catatan hardware: saat ini hanya ada 1 servo fisik (lihat `iot/CLAUDE.md`) — servo yang sama
 * akan bergerak baik dari Kontrol Irigasi maupun Kontrol Ventilasi sampai servo ke-2 tersedia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentilationControlRoute(
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
                isOn = false,
                activeSinceLabel = null
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

    fun triggerVentilation(turnOn: Boolean) {
        scope.launch {
            backendRepository.triggerActuator(
                plotId = plotId,
                deviceId = deviceId,
                actuator = "ventilation",
                action = if (turnOn) "open" else "close"
            ).onSuccess {
                uiState = uiState.copy(isOn = turnOn)
            }.onFailure {
                onTriggerError(triggerErrorMessage)
            }
        }
    }

    LaunchedEffect(uiState.mode, plotId) {
        if (uiState.mode != ActuatorControlMode.AUTOMATIC) return@LaunchedEffect
        while (true) {
            backendRepository.getIrrigationRecommendation(plotId)
                .onSuccess { recommendation ->
                    val recommended = recommendation.ventilationOn
                    if (recommended != null && recommended != uiState.isOn) {
                        triggerVentilation(recommended)
                    }
                }
            delay(AUTOMATIC_POLL_INTERVAL_MS)
        }
    }

    VentilationControlScreen(
        uiState = uiState,
        onModeSelected = { mode ->
            uiState = uiState.copy(mode = mode)
            if (mode == ActuatorControlMode.MANUAL) onManualModeActivated()
        },
        onPowerToggle = { triggerVentilation(!uiState.isOn) },
        sheetState = sheetState,
        onDismiss = { dismissThenNavigate(onDismiss) }
    )
}
