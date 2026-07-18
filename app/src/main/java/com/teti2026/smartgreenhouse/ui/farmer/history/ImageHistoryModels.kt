package com.teti2026.smartgreenhouse.ui.farmer.history

/**
 * Kategori kesehatan tampilan (badge) satu [CropImageHistoryItem], diturunkan dari
 * `disease_class` (`docs/data-contracts.md §3.6`): "healthy" -> [GOOD], nama penyakit -> [SICK].
 * [MEDIUM] dipakai untuk kasus butuh perhatian tapi belum terklasifikasi sakit — sesuai contoh
 * "Sedang" pada desain Stitch "Riwayat Citra - Petani".
 */
enum class ImageHealthCategory {
    GOOD, MEDIUM, SICK
}

/** Filter chip di atas grid — "Semua" tidak menyaring apa pun. */
enum class ImageHistoryFilter {
    ALL, HEALTHY, SICK, THIS_WEEK
}

/**
 * Model presentasi satu kartu grid Riwayat Citra. [id] dipakai untuk navigasi ke Detail Analisis
 * (`Routes.imageAnalysisDetail(id)`) dan dipetakan balik ke [CropImage] sumbernya.
 */
data class CropImageHistoryItem(
    val id: String,
    val imageUrl: String,
    val category: ImageHealthCategory,
    val timestampLabel: String,
    val plotLabel: String
)

/**
 * Model presentasi layar Detail Analisis Citra — gabungan [CropImage] + metadata AI yang belum
 * ada field-nya di `data-contracts.md §3.6` ([aiNote], [detectionLocationLabel], [deviceLabel]),
 * jadi untuk sekarang data sampel statis (lihat TODO di [ImageAnalysisDetailRoute]).
 */
data class ImageAnalysisDetail(
    val id: String,
    val imageUrl: String,
    val category: ImageHealthCategory,
    val healthScore: Double,
    val timestampLabel: String,
    val aiNote: String,
    val detectionLocationLabel: String,
    val deviceLabel: String,
    /** Untuk transfer data ke "Buat Listing dari Data Ini" — padanan `crop_type` plot ini. */
    val productName: String
)
