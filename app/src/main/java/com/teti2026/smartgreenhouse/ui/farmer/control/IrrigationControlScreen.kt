package com.teti2026.smartgreenhouse.ui.farmer.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.WaterDrop
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
 * Layar "Kontrol Irigasi - Petani" dari Stitch. Stateless, sesuai pola `docs/SDD.md §5`.
 *
 * Diminta eksplisit oleh user dirender sebagai [ModalBottomSheet] (bukan screen penuh), sama
 * seperti pola [com.teti2026.smartgreenhouse.ui.farmer.history.ImageAnalysisDetailScreen] —
 * caller ([IrrigationControlRoute]) menghoist [sheetState]/[onDismiss] agar animasi collapse
 * selesai dulu sebelum ditutup.
 *
 * Ilustrasi lingkaran ikon keran + tetes air dipertahankan APA ADANYA dari desain asli (statis,
 * tidak ada animasi) — berbeda dari Kontrol Ventilasi yang ilustrasi kipas berputarnya dihapus
 * atas permintaan eksplisit user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationControlScreen(
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
        IrrigationControlContent(
            uiState = uiState,
            onModeSelected = onModeSelected,
            onPowerToggle = onPowerToggle
        )
    }
}

@Composable
private fun IrrigationControlContent(
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

        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.irrigation_control_title),
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

        FaucetIllustration(modifier = Modifier.padding(top = 16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.actuator_control_status_current),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    if (uiState.isOn) {
                        R.string.irrigation_control_status_active
                    } else {
                        R.string.irrigation_control_status_inactive
                    }
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

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

            if (uiState.mode == ActuatorControlMode.MANUAL) {
                Text(
                    text = stringResource(R.string.irrigation_control_power_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            if (uiState.mode == ActuatorControlMode.AUTOMATIC && uiState.isOn && uiState.activeSinceLabel != null) {
                IrrigationAiActiveSinceCaption(activeSinceLabel = uiState.activeSinceLabel)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            IrrigationSpecGrid()
        }
    }
}

@Composable
private fun IrrigationAiActiveSinceCaption(activeSinceLabel: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        color = MintTint.copy(alpha = 0.5f)
    ) {
        androidx.compose.foundation.layout.Row(
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
                text = stringResource(R.string.irrigation_control_active_since, activeSinceLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun IrrigationSpecGrid(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IrrigationSpecItem(
            labelRes = R.string.irrigation_control_spec_flow_label,
            valueText = "2,4",
            unitText = stringResource(R.string.irrigation_control_spec_flow_unit),
            modifier = Modifier.weight(1f)
        )
        IrrigationSpecItem(
            labelRes = R.string.irrigation_control_spec_last_duration_label,
            valueText = "3",
            unitText = stringResource(R.string.irrigation_control_spec_last_duration_unit),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IrrigationSpecItem(
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
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 4.dp)
        ) {
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

@Composable
private fun FaucetIllustration(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(180.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(88.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IrrigationControlScreenPreview() {
    SmartgreenhousemobileTheme {
        IrrigationControlContent(
            uiState = ActuatorControlUiState(
                mode = ActuatorControlMode.AUTOMATIC,
                isOn = true,
                activeSinceLabel = "3 menit lalu"
            ),
            onModeSelected = {},
            onPowerToggle = {}
        )
    }
}
