package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.GreenhouseTextField
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Langkah 3/3 — Screen "Setup Greenhouse - Pairing Perangkat". [pairingCode] dipetakan langsung
 * ke `plots.device_id` (`docs/data-contracts.md §1.1/§3.3`). `device_id` di kontrak resmi adalah
 * STRING BEBAS FORMAT (contoh persis di kontrak: `"gh-esp32-01"`, 11 karakter dengan strip) —
 * BUKAN 6-digit kode pairing numerik. Field ini SEBELUMNYA berupa 6 kotak OTP-style 1-karakter
 * (asumsi "kode pairing 6 digit" yang TIDAK PERNAH ada di `data-contracts.md`), yang membuat
 * device_id asli firmware (`config.example.h` → `DEVICE_ID "gh-esp32-01"`) mustahil diketik utuh
 * — ditemukan saat testing manual dengan device_id sungguhan dari Serial Monitor. Diganti text
 * field bebas biasa ([GreenhouseTextField], pola sama seperti field email/password Login) supaya
 * device_id APAPUN sesuai kontrak bisa dimasukkan apa adanya. Stateless: [pairingCode] di-hoist
 * ke caller.
 *
 * [plotId] adalah field TAMBAHAN (bukan bagian layar asli) — dipakai LANGSUNG sebagai Firestore
 * document ID `plots/{plotId}` (lihat `FirestoreRepository.createFarmWithPlot`), BUKAN
 * auto-generate. Firmware IoT mengirim `plot_id` TETAP (hardcoded di `config.h`), jadi user harus
 * memasukkan nilai yang SAMA persis di sini (juga dari Serial Monitor) supaya Dashboard langsung
 * menerima `sensor_readings` tanpa perlu menyamakan ID manual lewat Firestore Console setelahnya.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GreenhouseSetupPairingScreen(
    pairingCode: String,
    onPairingCodeChange: (String) -> Unit,
    plotId: String,
    onPlotIdChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit,
    onHelpClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.setup_greenhouse_top_bar_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.setup_greenhouse_back_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SetupStepIndicator(
                currentStep = 3,
                totalSteps = 3,
                stepLabel = stringResource(R.string.setup_greenhouse_step_pairing)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.setup_greenhouse_pairing_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.setup_greenhouse_pairing_step_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SensorsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.setup_greenhouse_pairing_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            GreenhouseTextField(
                value = pairingCode,
                onValueChange = onPairingCodeChange,
                label = stringResource(R.string.setup_greenhouse_pairing_device_id_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
            )

            GreenhouseTextField(
                value = plotId,
                onValueChange = onPlotIdChange,
                label = stringResource(R.string.setup_greenhouse_pairing_plot_id_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onFinishClick,
                    enabled = pairingCode.isNotBlank() && plotId.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.setup_greenhouse_pairing_finish_button),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                OutlinedButton(
                    onClick = onHelpClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.setup_greenhouse_pairing_help_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GreenhouseSetupPairingScreenPreview() {
    SmartgreenhousemobileTheme {
        GreenhouseSetupPairingScreen(
            pairingCode = "gh-esp32-01",
            onPairingCodeChange = {},
            plotId = "plot-abc123",
            onPlotIdChange = {},
            onBackClick = {},
            onFinishClick = {},
            onHelpClick = {}
        )
    }
}
