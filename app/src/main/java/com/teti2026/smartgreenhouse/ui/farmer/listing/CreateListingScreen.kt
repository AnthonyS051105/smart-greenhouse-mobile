package com.teti2026.smartgreenhouse.ui.farmer.listing

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.DisabledBg
import com.teti2026.smartgreenhouse.ui.theme.MintTint
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

private const val MAX_PHOTOS = 3

/**
 * Screen "Buat Listing - Petani" dari Stitch. Stateless: seluruh nilai form di-hoist ke
 * caller ([CreateListingRoute]), sesuai pola MVVM+UDF di `docs/SDD.md §5`.
 *
 * [productName] & [healthScore] read-only (diisi otomatis dari AI/plot aktif, lihat
 * `docs/data-contracts.md §4.6` `POST /listings/auto-fill-health-score`) — app tidak
 * menghitung skor sendiri.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    productName: String,
    healthScore: Double,
    photoUris: List<Uri>,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    pricePerKg: String,
    onPricePerKgChange: (String) -> Unit,
    quantityKg: String,
    onQuantityKgChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    preOrderEnabled: Boolean,
    onPreOrderToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_listing_top_bar_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.create_listing_back_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onPublishClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.create_listing_publish_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            HealthScoreReadOnlyCard(healthScore = healthScore)
            PhotoUploadSection(
                photoUris = photoUris,
                onAddPhotoClick = onAddPhotoClick,
                onRemovePhoto = onRemovePhoto
            )
            ProductDetailSection(
                productName = productName,
                pricePerKg = pricePerKg,
                onPricePerKgChange = onPricePerKgChange,
                quantityKg = quantityKg,
                onQuantityKgChange = onQuantityKgChange,
                description = description,
                onDescriptionChange = onDescriptionChange,
                preOrderEnabled = preOrderEnabled,
                onPreOrderToggle = onPreOrderToggle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HealthScoreReadOnlyCard(
    healthScore: Double,
    modifier: Modifier = Modifier
) {
    val (badgeLabelRes, badgeColor) = when {
        healthScore >= 70 -> R.string.health_score_label_good to MaterialTheme.colorScheme.primary
        healthScore >= 40 -> R.string.health_score_label_medium to MaterialTheme.colorScheme.secondary
        else -> R.string.health_score_label_low to MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MintTint,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.create_listing_health_score_locked_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.create_listing_health_score_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = healthScore.toInt().toString(),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            modifier = Modifier.padding(start = 12.dp),
                            shape = CircleShape,
                            color = badgeColor
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(badgeLabelRes),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoUploadSection(
    photoUris: List<Uri>,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.create_listing_photo_section_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        // MAX_PHOTOS = 3 selalu muat dalam satu baris (padanan grid 3-kolom di desain Stitch) —
        // Row lebih pas daripada LazyVerticalGrid karena tidak perlu scroll & tidak butuh tinggi
        // tetap yang rawan memotong konten (LazyVerticalGrid tidak bisa wrap-content secara alami
        // di dalam Column yang sudah scrollable).
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            photoUris.forEach { uri ->
                PhotoThumbnail(
                    uri = uri,
                    onRemoveClick = { onRemovePhoto(uri) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (photoUris.size < MAX_PHOTOS) {
                AddPhotoButton(onClick = onAddPhotoClick, modifier = Modifier.weight(1f))
            }
            repeat(MAX_PHOTOS - photoUris.size - if (photoUris.size < MAX_PHOTOS) 1 else 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    uri: Uri,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        )
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.create_listing_remove_photo_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.AddAPhoto,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.create_listing_add_photo_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProductDetailSection(
    productName: String,
    pricePerKg: String,
    onPricePerKgChange: (String) -> Unit,
    quantityKg: String,
    onQuantityKgChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    preOrderEnabled: Boolean,
    onPreOrderToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.create_listing_detail_section_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column {
            Text(
                text = stringResource(R.string.create_listing_product_name_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = DisabledBg,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LabeledOutlinedField(
                label = stringResource(R.string.create_listing_price_label),
                value = pricePerKg,
                onValueChange = onPricePerKgChange,
                keyboardType = KeyboardType.Number,
                prefix = stringResource(R.string.create_listing_price_prefix),
                modifier = Modifier.weight(1f)
            )
            LabeledOutlinedField(
                label = stringResource(R.string.create_listing_quantity_label),
                value = quantityKg,
                onValueChange = onQuantityKgChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Column {
            Text(
                text = stringResource(R.string.create_listing_description_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text(stringResource(R.string.create_listing_description_placeholder)) },
                modifier = Modifier.fillMaxWidth().height(112.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.create_listing_preorder_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.create_listing_preorder_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = preOrderEnabled,
                    onCheckedChange = onPreOrderToggle,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun LabeledOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    prefix: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            prefix = prefix?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun CreateListingScreenPreview() {
    SmartgreenhousemobileTheme {
        CreateListingScreen(
            productName = "Cabai Rawit Merah",
            healthScore = 87.0,
            photoUris = emptyList(),
            onAddPhotoClick = {},
            onRemovePhoto = {},
            pricePerKg = "25000",
            onPricePerKgChange = {},
            quantityKg = "50",
            onQuantityKgChange = {},
            description = "",
            onDescriptionChange = {},
            preOrderEnabled = false,
            onPreOrderToggle = {},
            onBackClick = {},
            onPublishClick = {}
        )
    }
}
