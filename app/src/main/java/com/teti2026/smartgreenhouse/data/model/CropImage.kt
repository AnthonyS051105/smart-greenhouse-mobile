package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `crop_images` sesuai `docs/data-contracts.md §3.6`. Nama field mengikuti dokumen
 * tersebut persis. [ripenessClass]: "unripe" | "ripe" | "overripe". [diseaseClass]: "healthy" |
 * nama penyakit | null.
 */
data class CropImage(
    val id: String,
    val plotId: String,
    val timestamp: String,
    val imageUrl: String,
    val ripenessClass: String,
    val diseaseClass: String?,
    val confidenceScore: Double
)
