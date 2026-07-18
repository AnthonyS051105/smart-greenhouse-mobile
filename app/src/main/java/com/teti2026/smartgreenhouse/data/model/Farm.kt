package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `farms` sesuai `shared/data-contracts.md §3.2`. Nama field mengikuti dokumen
 * tersebut persis (case-sensitive lintas bidang).
 */
data class Farm(
    val id: String,
    val ownerUid: String,
    val farmName: String,
    val locationLat: Double,
    val locationLng: Double,
    val farmSizeM2: Double
)
