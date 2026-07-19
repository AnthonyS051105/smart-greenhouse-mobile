package com.teti2026.smartgreenhouse.ui.farmer.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.MintTint
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Kontrol Ventilasi - Petani" dari Stitch. Stateless, sesuai pola `docs/SDD.md §5`.
 *
 * Diminta eksplisit oleh user dirender sebagai [ModalBottomSheet] (bukan screen penuh), sama
 * seperti pola [com.teti2026.smartgreenhouse.ui.farmer.history.ImageAnalysisDetailScreen].
 *
 * Ilustrasi kipas berputar (`animate-fan`) pada desain Stitch asli dihapus atas permintaan
 * eksplisit user — awalnya dihapus total, lalu user meralat dan meminta [LouverIllustration]
 * (ikon louver statis, padanan `FaucetIllustration` di Kontrol Irigasi) sebagai gantinya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentilationControlScreen(
    uiState: ActuatorControlUiState,
    onModeSelected: (ActuatorControlMode) -> Unit,
    onPowerToggle: () -> Unit,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        VentilationControlContent(
            uiState = uiState,
            onModeSelected = onModeSelected,
            onPowerToggle = onPowerToggle
        )
    }
}

@Composable
private fun VentilationControlContent(
    uiState: ActuatorControlUiState,
    onModeSelected: (ActuatorControlMode) -> Unit,
    onPowerToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var showManualModeInfo by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.ventilation_control_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (uiState.mode == ActuatorControlMode.MANUAL) {
                ManualModeTitleIcon(
                    onClick = { showManualModeInfo = true },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (showManualModeInfo) {
            ManualModeInfoDialog(onDismiss = { showManualModeInfo = false })
        }

        LouverIllustration(modifier = Modifier.padding(top = 16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ActuatorModeSegmentedControl(
                selectedMode = uiState.mode,
                onModeSelected = onModeSelected
            )

            ActuatorPowerButton(isOn = uiState.isOn, onClick = onPowerToggle)

            if (uiState.mode == ActuatorControlMode.AUTOMATIC && uiState.isOn && uiState.activeSinceLabel != null) {
                AiActiveSinceCaption(activeSinceLabel = uiState.activeSinceLabel)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            VentilationSpecGrid()
        }
    }
}

@Composable
private fun AiActiveSinceCaption(activeSinceLabel: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        color = MintTint.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Memory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(R.string.ventilation_control_active_since, activeSinceLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VentilationSpecGrid(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActuatorSpecItem(
            labelRes = R.string.ventilation_control_spec_speed_label,
            valueText = "1.200",
            unitText = stringResource(R.string.ventilation_control_spec_speed_unit),
            modifier = Modifier.weight(1f)
        )
        ActuatorSpecItem(
            labelRes = R.string.ventilation_control_spec_target_temp_label,
            valueText = "24.5",
            unitText = stringResource(R.string.ventilation_control_spec_target_temp_unit),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActuatorSpecItem(
    labelRes: Int,
    valueText: String,
    unitText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unitText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun VentilationControlScreenPreview() {
    SmartgreenhousemobileTheme {
        VentilationControlContent(
            uiState = ActuatorControlUiState(
                mode = ActuatorControlMode.AUTOMATIC,
                isOn = true,
                activeSinceLabel = "15 menit lalu"
            ),
            onModeSelected = {},
            onPowerToggle = {}
        )
    }
}
