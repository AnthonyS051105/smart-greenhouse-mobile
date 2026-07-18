package com.teti2026.smartgreenhouse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tingkat kesehatan lahan untuk badge listing, diturunkan dari `health_score` (0-100,
 * lihat `docs/data-contracts.md §5`). Ambang batas 75/50 adalah placeholder — belum
 * dikalibrasi tim, boleh disesuaikan.
 */
enum class HealthScoreTier {
    TINGGI,
    SEDANG,
    RENDAH
}

fun healthScoreTierOf(healthScore: Double): HealthScoreTier = when {
    healthScore >= 75.0 -> HealthScoreTier.TINGGI
    healthScore >= 50.0 -> HealthScoreTier.SEDANG
    else -> HealthScoreTier.RENDAH
}

/**
 * Label + warna presentasi untuk satu [HealthScoreTier] — sumber kebenaran tunggal dipakai
 * [HealthScoreBadge] (pill kecil di kartu listing) maupun kartu skor kesehatan yang lebih besar
 * di Detail Produk, agar label & warna tidak pernah drift antar layar.
 */
data class HealthScoreTierPresentation(
    val label: String,
    val containerColor: Color,
    val contentColor: Color
)

/**
 * Tidak pernah color-only (lihat design system Stitch "Health Score Badge"): Tinggi="Baik",
 * Sedang="Sedang", Rendah="Perlu Perhatian".
 */
@Composable
fun healthScoreTierPresentation(tier: HealthScoreTier): HealthScoreTierPresentation = when (tier) {
    HealthScoreTier.TINGGI -> HealthScoreTierPresentation(
        "Baik",
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer
    )
    HealthScoreTier.SEDANG -> HealthScoreTierPresentation(
        "Sedang",
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer
    )
    HealthScoreTier.RENDAH -> HealthScoreTierPresentation(
        "Perlu Perhatian",
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer
    )
}

/** Badge skor kesehatan — pill chip ikon daun + label, dipakai di kartu listing. */
@Composable
fun HealthScoreBadge(
    tier: HealthScoreTier,
    modifier: Modifier = Modifier
) {
    val presentation = healthScoreTierPresentation(tier)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = presentation.containerColor, shape = RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Eco,
            contentDescription = null,
            tint = presentation.contentColor,
            modifier = Modifier.size(14.dp).padding(end = 2.dp)
        )
        Text(
            text = presentation.label,
            style = MaterialTheme.typography.labelSmall,
            color = presentation.contentColor
        )
    }
}
