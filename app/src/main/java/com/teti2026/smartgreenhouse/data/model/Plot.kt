package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `plots` sesuai `shared/data-contracts.md §3.3`. Nama field mengikuti dokumen
 * tersebut persis (case-sensitive lintas bidang). [plantingDate] format ISO 8601 (yyyy-MM-dd).
 */
data class Plot(
    val id: String,
    val farmId: String,
    val cropType: String,
    val plantingDate: String,
    val deviceId: String
)
