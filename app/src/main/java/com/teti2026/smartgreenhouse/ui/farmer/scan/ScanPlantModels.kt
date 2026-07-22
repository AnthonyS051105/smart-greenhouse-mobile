package com.teti2026.smartgreenhouse.ui.farmer.scan

import android.net.Uri
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHealthCategory

/** Tahap alur "Pindai Tanaman" — dikontrol oleh [ScanPlantRoute] sebagai state machine linear. */
enum class ScanPlantStage {
    VIEWFINDER,
    ANALYZING,
    RESULT
}

/**
 * Hasil analisis AI untuk satu foto pindaian on-demand (bukan dari siklus ESP32-CAM).
 * Field selaras `docs/data-contracts.md §3.6` (`crop_images`: [ripenessLabel] <- `ripeness_class`,
 * [category]/[healthLabel] <- `disease_class`, [confidenceScore] <- `confidence_score`), plus
 * [healthScore] gabungan (`health_score`, §5) — TODO: nilai sungguhan datang dari
 * `POST /vision/analyze` (`data-contracts.md §4.5`) begitu MOB-T09/T10 dikerjakan; untuk sekarang
 * disimulasikan di [ScanPlantRoute] (delay + data sampel), konsisten pola "screen dulu, wiring
 * data nanti" yang dipakai seluruh screen lain sejauh ini.
 */
data class ScanAnalysisResult(
    val imageUri: Uri,
    val category: ImageHealthCategory,
    val healthScore: Double,
    /** Nilai mentah `ripeness_class` ("Ripe"/"Unripe"/"Overipe") — dipakai saat "Simpan ke
     * Riwayat Citra" menulis dokumen `crop_images` (data-contracts.md §3.6 mensyaratkan field
     * mentah, BUKAN [ripenessLabel] yang sudah diterjemahkan ke Bahasa Indonesia untuk tampilan). */
    val ripenessClass: String,
    val ripenessLabel: String,
    val healthLabel: String,
    val confidenceScore: Double,
    /** Untuk transfer data ke "Buat Listing dari Hasil Ini" — padanan `crop_type` plot aktif. */
    val productName: String
)
