package com.teti2026.smartgreenhouse.util

import com.teti2026.smartgreenhouse.data.model.CropImage
import com.teti2026.smartgreenhouse.data.model.Farm
import com.teti2026.smartgreenhouse.data.model.Plot
import com.teti2026.smartgreenhouse.ui.farmer.history.CropImageHistoryItem
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageAnalysisDetail
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHealthCategory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CROP_IMAGE_TIMESTAMP_FORMATTER = DateTimeFormatter
    .ofPattern("d MMM yyyy, HH:mm 'WIB'", Locale.forLanguageTag("id-ID"))
    .withZone(ZoneId.of("Asia/Jakarta"))

/** [CropImage.timestamp] (ISO 8601 UTC) -> label tampilan WIB. Fallback ke string mentah bila
 * gagal parse (seharusnya tidak terjadi untuk dokumen yang ditulis app ini sendiri). */
private fun CropImage.timestampLabel(): String =
    runCatching { CROP_IMAGE_TIMESTAMP_FORMATTER.format(Instant.parse(timestamp)) }.getOrDefault(timestamp)

/** [CropImage.diseaseClass] (kalau ada, dari siklus ESP32-CAM) diprioritaskan di atas
 * [ripenessCategory] (dipakai jalur "Pindai Tanaman" on-demand yang tidak punya deteksi penyakit). */
private fun CropImage.category(): ImageHealthCategory = when (val disease = diseaseClass) {
    null -> ripenessCategory(ripenessClass)
    "healthy" -> ImageHealthCategory.GOOD
    else -> ImageHealthCategory.SICK
}

/** Kartu grid "Riwayat Citra - Petani" dari satu dokumen `crop_images` + kebun/plot pemiliknya. */
fun CropImage.toCropImageHistoryItem(farm: Farm, plot: Plot): CropImageHistoryItem = CropImageHistoryItem(
    id = id,
    imageUrl = imageUrl,
    category = category(),
    timestampLabel = timestampLabel(),
    plotLabel = "${plot.cropType.replaceFirstChar { it.uppercase() }} - ${farm.farmName}"
)

/** Detail Analisis Citra dari satu dokumen `crop_images`. [aiNote]/[detectionLocationLabel]/
 * [deviceLabel] tidak ada padanan field-nya di `crop_images` (lihat KDoc [ImageAnalysisDetail]) —
 * diturunkan dari data yang ada (ripeness/confidence/farm) untuk citra hasil "Pindai Tanaman". */
fun CropImage.toImageAnalysisDetail(farm: Farm, plot: Plot): ImageAnalysisDetail {
    val category = category()
    val confidencePercent = (confidenceScore * 100).toInt()
    val aiNote = when (category) {
        ImageHealthCategory.GOOD -> "Tanaman terdeteksi dalam kondisi baik"
        ImageHealthCategory.MEDIUM -> "Tanaman perlu dipantau lebih lanjut"
        ImageHealthCategory.SICK -> "Tanaman terindikasi kurang sehat"
    } + " (tingkat kematangan: ${ripenessLabelId(ripenessClass)}, keyakinan AI $confidencePercent%)."
    return ImageAnalysisDetail(
        id = id,
        imageUrl = imageUrl,
        category = category,
        healthScore = ripenessHealthScore(ripenessClass, confidenceScore),
        timestampLabel = timestampLabel(),
        aiNote = aiNote,
        detectionLocationLabel = farm.farmName,
        deviceLabel = "Kamera Ponsel (Pindai Tanaman)",
        productName = plot.cropType
    )
}
