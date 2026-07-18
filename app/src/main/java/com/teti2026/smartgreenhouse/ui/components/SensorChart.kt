package com.teti2026.smartgreenhouse.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/** Satu titik grafik sensor: label sumbu-X (mis. hari/jam) + nilai numerik (suhu/kelembapan/dst). */
data class SensorChartPoint(val label: String, val value: Float)

/**
 * Grafik garis sensor Compose-native — padanan "Grafik" di design system Stitch (§6/§7 SDD.md):
 * `Vico` (bukan MPAndroidChart). Dipakai Dashboard Petani (MOB-T09) & Detail Listing (MOB-T18)
 * untuk menampilkan riwayat sensor (`docs/data-contracts.md §3.4`).
 */
@Composable
fun SensorLineChart(points: List<SensorChartPoint>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries { series(points.map { it.value }) }
        }
    }

    val valueFormatter = remember(points) {
        CartesianValueFormatter { _, value, _ -> points.getOrNull(value.toInt())?.label.orEmpty() }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = valueFormatter)
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    )
}
