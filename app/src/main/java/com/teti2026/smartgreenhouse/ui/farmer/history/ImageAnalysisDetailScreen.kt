package com.teti2026.smartgreenhouse.ui.farmer.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.MintTint
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Screen "Detail Analisis Citra - AgriSmart" dari Stitch: judul nama tanaman, hero image, badge
 * kategori + skor, kartu "Catatan Analisis AI", metadata lokasi/perangkat, dan 2 tombol aksi
 * tetap di bawah ("Buat Listing dari Data Ini" / "Tutup"). Stateless, sesuai pola `docs/SDD.md §5`.
 *
 * Dirender sebagai [ModalBottomSheet] sungguhan (bukan screen penuh) sesuai desain Stitch asli —
 * muncul mengambang setengah layar di atas grid Riwayat Citra dengan drag handle, bisa digeser ke
 * atas untuk full-screen atau digeser ke bawah/tap scrim untuk dismiss. [sheetState] & [onDismiss]
 * di-hoist ke caller ([ImageAnalysisDetailRoute]) supaya animasi collapse selesai dulu sebelum
 * `NavController.popBackStack()` dipanggil (pola standar `ModalBottomSheet` + Navigation Compose).
 *
 * Hero image memakai rasio [HERO_IMAGE_ASPECT_RATIO] tetap (BUKAN animasi mengikuti posisi
 * sheet) — percobaan awal mengubah rasio secara animated (16:9 saat partial, membesar saat full)
 * terlihat kasar karena animasi `animateFloatAsState` terpisah berjalan tidak sinkron dengan
 * animasi native drag `ModalBottomSheet`, menyebabkan area kosong sekilas terlihat di bawah
 * panel selama transisi. Satu rasio tetap (16:9) sudah cukup: cukup pendek untuk menyisakan
 * deskripsi terlihat saat sheet baru terbuka partial, dan tetap proporsional wajar saat full.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageAnalysisDetailScreen(
    detail: ImageAnalysisDetail,
    sheetState: SheetState,
    onCreateListingClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ImageAnalysisDetailContent(
            detail = detail,
            onCreateListingClick = onCreateListingClick,
            onCloseClick = onDismiss
        )
    }
}

private val HERO_IMAGE_ASPECT_RATIO = 16f / 9f

@Composable
private fun ImageAnalysisDetailContent(
    detail: ImageAnalysisDetail,
    onCreateListingClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = detail.productName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            AsyncImage(
                model = detail.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(HERO_IMAGE_ASPECT_RATIO)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DetailHeader(detail = detail)
            AiAnalysisCard(aiNote = detail.aiNote)
            MetadataGrid(detail = detail)
        }
        BottomActionBar(
            onCreateListingClick = onCreateListingClick,
            onCloseClick = onCloseClick,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun DetailHeader(
    detail: ImageAnalysisDetail,
    modifier: Modifier = Modifier
) {
    val categoryLabelRes = when (detail.category) {
        ImageHealthCategory.GOOD -> R.string.image_analysis_category_healthy
        ImageHealthCategory.MEDIUM -> R.string.health_score_label_medium
        ImageHealthCategory.SICK -> R.string.image_history_category_sick
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = stringResource(categoryLabelRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = detail.timestampLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MintTint,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(
                        R.string.image_analysis_score_label,
                        detail.healthScore.toInt()
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AiAnalysisCard(
    aiNote: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.image_analysis_ai_note_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = aiNote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun MetadataGrid(
    detail: ImageAnalysisDetail,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MetadataCard(
            labelRes = R.string.image_analysis_location_label,
            valueText = detail.detectionLocationLabel,
            icon = Icons.Filled.LocationOn,
            modifier = Modifier.weight(1f)
        )
        MetadataCard(
            labelRes = R.string.image_analysis_device_label,
            valueText = detail.deviceLabel,
            icon = Icons.Filled.Photo,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetadataCard(
    labelRes: Int,
    valueText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    onCreateListingClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCreateListingClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.AddTask,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.image_analysis_create_listing_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            OutlinedButton(
                onClick = onCloseClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(percent = 50),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.image_analysis_close_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ImageAnalysisDetailScreenPreview() {
    SmartgreenhousemobileTheme {
        ImageAnalysisDetailContent(
            detail = sampleImageAnalysisDetails.getValue("img-001"),
            onCreateListingClick = {},
            onCloseClick = {}
        )
    }
}
