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
 * Badge skor kesehatan — pill chip ikon daun + label, tidak pernah color-only
 * (lihat design system Stitch "Health Score Badge"): Tinggi="Baik", Sedang="Sedang",
 * Rendah="Perlu Perhatian".
 */
@Composable
fun HealthScoreBadge(
    tier: HealthScoreTier,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (tier) {
        HealthScoreTier.TINGGI -> Triple(
            "Baik",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        HealthScoreTier.SEDANG -> Triple(
            "Sedang",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        HealthScoreTier.RENDAH -> Triple(
            "Perlu Perhatian",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = containerColor, shape = RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Eco,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp).padding(end = 2.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
