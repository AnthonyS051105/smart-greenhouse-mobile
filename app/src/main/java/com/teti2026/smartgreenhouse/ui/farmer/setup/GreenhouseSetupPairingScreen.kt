package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

private const val PAIRING_CODE_LENGTH = 6

/**
 * Langkah 3/3 — Screen "Setup Greenhouse - Pairing Perangkat" dari Stitch. 6 digit kode
 * pairing digabung menjadi [pairingCode] (nantinya dipetakan ke `plots.device_id`, lihat
 * `shared/data-contracts.md §3.3`). Stateless: [pairingCode] di-hoist ke caller.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GreenhouseSetupPairingScreen(
    pairingCode: String,
    onPairingCodeChange: (String) -> Unit,
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

            PairingCodeInput(
                pairingCode = pairingCode,
                onPairingCodeChange = onPairingCodeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp)
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
                    enabled = pairingCode.length == PAIRING_CODE_LENGTH && !isLoading,
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

/**
 * 6 kotak digit tunggal bergaya OTP. State digabung jadi satu [String] agar mudah di-hoist,
 * bukan 6 state terpisah. Fokus otomatis maju ke kotak berikutnya saat digit terisi, dan
 * mundur ke kotak sebelumnya saat Backspace ditekan pada kotak yang sudah kosong — meniru
 * skrip auto-advance pada desain asli Stitch (HTML/JS), diterjemahkan ke `FocusRequester`.
 */
@Composable
private fun PairingCodeInput(
    pairingCode: String,
    onPairingCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { List(PAIRING_CODE_LENGTH) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
    ) {
        val paddedCode = pairingCode.padEnd(PAIRING_CODE_LENGTH, ' ')
        repeat(PAIRING_CODE_LENGTH) { index ->
            val digit = paddedCode[index].takeIf { it != ' ' }?.toString().orEmpty()
            PairingDigitBox(
                digit = digit,
                focusRequester = focusRequesters[index],
                onDigitChange = { newDigit ->
                    val updatedChars = paddedCode.toCharArray()
                    updatedChars[index] = newDigit.firstOrNull() ?: ' '
                    val updated = String(updatedChars).trimEnd()
                    onPairingCodeChange(updated.take(PAIRING_CODE_LENGTH))
                    if (newDigit.isNotEmpty() && index < PAIRING_CODE_LENGTH - 1) {
                        focusRequesters[index + 1].requestFocus()
                    }
                },
                onBackspaceOnEmpty = {
                    if (index > 0) {
                        focusRequesters[index - 1].requestFocus()
                    }
                }
            )
        }
    }
}

@Composable
private fun PairingDigitBox(
    digit: String,
    focusRequester: FocusRequester,
    onDigitChange: (String) -> Unit,
    onBackspaceOnEmpty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(44.dp)
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = digit,
            onValueChange = { value ->
                val singleDigit = value.filter { it.isLetterOrDigit() }.takeLast(1)
                onDigitChange(singleDigit)
            },
            singleLine = true,
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type == androidx.compose.ui.input.key.KeyEventType.KeyUp &&
                        event.key == Key.Backspace &&
                        digit.isEmpty()
                    ) {
                        onBackspaceOnEmpty()
                        true
                    } else {
                        false
                    }
                }
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GreenhouseSetupPairingScreenPreview() {
    SmartgreenhousemobileTheme {
        GreenhouseSetupPairingScreen(
            pairingCode = "A1B2",
            onPairingCodeChange = {},
            onBackClick = {},
            onFinishClick = {},
            onHelpClick = {}
        )
    }
}
