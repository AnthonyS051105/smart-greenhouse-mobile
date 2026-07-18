package com.teti2026.smartgreenhouse.ui.farmer.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.GreenhouseTextField
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Langkah 1/3 — Screen "Setup Greenhouse - Data Utama" dari Stitch. Stateless: seluruh nilai
 * form di-hoist ke caller ([GreenhouseSetupRoute]), sesuai pola MVVM+UDF di `docs/SDD.md §5`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenhouseSetupDataScreen(
    greenhouseName: String,
    onGreenhouseNameChange: (String) -> Unit,
    sizeM2: String,
    onSizeM2Change: (String) -> Unit,
    cropType: CropTypeOption,
    onCropTypeSelected: (CropTypeOption) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
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
        },
        bottomBar = {
            SetupBottomActionBar {
                Button(
                    onClick = onNextClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.setup_greenhouse_data_next_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp).height(20.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SetupStepIndicator(
                currentStep = 1,
                totalSteps = 3,
                stepLabel = stringResource(R.string.setup_greenhouse_step_data)
            )

            Column(modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)) {
                Text(
                    text = stringResource(R.string.setup_greenhouse_data_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.setup_greenhouse_data_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                GreenhouseTextField(
                    value = greenhouseName,
                    onValueChange = onGreenhouseNameChange,
                    label = stringResource(R.string.setup_greenhouse_name_label)
                )
                GreenhouseTextField(
                    value = sizeM2,
                    onValueChange = onSizeM2Change,
                    label = stringResource(R.string.setup_greenhouse_size_label),
                    keyboardType = KeyboardType.Number
                )
                CropTypeDropdown(
                    selected = cropType,
                    onSelected = onCropTypeSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CropTypeDropdown(
    selected: CropTypeOption,
    onSelected: (CropTypeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.setup_greenhouse_crop_type_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = cropTypeLabel(selected),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CropTypeOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(cropTypeLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun cropTypeLabel(option: CropTypeOption): String = when (option) {
    CropTypeOption.CABAI_RAWIT -> stringResource(R.string.setup_greenhouse_crop_cabai_rawit)
    CropTypeOption.TOMAT -> stringResource(R.string.setup_greenhouse_crop_tomat)
    CropTypeOption.SELADA -> stringResource(R.string.setup_greenhouse_crop_selada)
    CropTypeOption.PAPRIKA -> stringResource(R.string.setup_greenhouse_crop_paprika)
}

/** Indikator langkah 3-tahap (progress bar + label), dipakai di ketiga screen setup. */
@Composable
fun SetupStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.setup_greenhouse_step_progress, currentStep, totalSteps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stepLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            color = if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(percent = 50)
                        )
                )
            }
        }
    }
}

/** Bottom bar tetap (floating action) yang dipakai seragam di ketiga screen setup. */
@Composable
fun SetupBottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GreenhouseSetupDataScreenPreview() {
    var name by remember { mutableStateOf("Greenhouse Cabai Boyolali") }
    var size by remember { mutableStateOf("24") }
    var crop by remember { mutableStateOf(CropTypeOption.CABAI_RAWIT) }

    SmartgreenhousemobileTheme {
        GreenhouseSetupDataScreen(
            greenhouseName = name,
            onGreenhouseNameChange = { name = it },
            sizeM2 = size,
            onSizeM2Change = { size = it },
            cropType = crop,
            onCropTypeSelected = { crop = it },
            onBackClick = {},
            onNextClick = {}
        )
    }
}
