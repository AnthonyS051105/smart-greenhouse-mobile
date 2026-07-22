package com.teti2026.smartgreenhouse.util

import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHealthCategory

/**
 * Pemetaan `ripeness_class` (nilai mentah `POST /vision/analyze`, `docs/data-contracts.md §4.5`:
 * "Ripe"/"Unripe"/"Overipe") ke representasi UI — dipakai DUA jalur yang harus identik hasilnya:
 * [com.teti2026.smartgreenhouse.ui.farmer.scan.ScanPlantRoute] (hasil analisis baru saja) dan
 * [com.teti2026.smartgreenhouse.viewmodel.ImageHistoryViewModel] (rekonstruksi dari `crop_images`
 * tersimpan di Firestore). Diekstrak ke satu tempat supaya kedua jalur tidak bisa berbeda formula.
 */

/** `Ripe`=100/`Unripe`=70/`Overipe`=40 — sama seperti `RIPENESS_HEALTH_SCORE` backend
 * (`app/routers/ai.py`, dipakai `POST /listings/auto-fill-health-score`). Base 50 untuk nilai
 * `ripeness_class` di luar 3 ini (mengikuti fallback backend). */
private val RIPENESS_BASE_SCORE = mapOf(
    "Ripe" to 100,
    "Unripe" to 70,
    "Overipe" to 40
)

private val RIPENESS_LABEL_ID = mapOf(
    "Ripe" to "Matang",
    "Unripe" to "Belum Matang",
    "Overipe" to "Terlalu Matang"
)

/**
 * TIDAK ada model penyakit tanaman yang berjalan (`disease_class` belum diimplementasikan di
 * manapun) — kategori kesehatan diturunkan dari [ripenessClass] sebagai proksi visual, bukan
 * diagnosis penyakit sungguhan.
 */
fun ripenessCategory(ripenessClass: String): ImageHealthCategory = when (ripenessClass) {
    "Ripe" -> ImageHealthCategory.GOOD
    "Unripe" -> ImageHealthCategory.MEDIUM
    else -> ImageHealthCategory.SICK
}

fun ripenessLabelId(ripenessClass: String): String = RIPENESS_LABEL_ID[ripenessClass] ?: ripenessClass

fun ripenessHealthScore(ripenessClass: String, confidenceScore: Double): Double =
    Math.round((RIPENESS_BASE_SCORE[ripenessClass] ?: 50) * confidenceScore).toDouble()
