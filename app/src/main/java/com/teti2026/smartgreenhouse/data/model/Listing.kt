package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `listings` sesuai `shared/data-contracts.md §3.7`. Nama field mengikuti dokumen
 * tersebut persis (case-sensitive lintas bidang). [status]: "available" | "sold".
 * [healthScore] (0-100) selalu berasal dari backend (POST /listings/auto-fill-health-score) —
 * TIDAK dihitung di app.
 */
data class Listing(
    val id: String,
    val farmId: String,
    val plotId: String,
    val cropType: String,
    val quantityKg: Double,
    val pricePerKg: Long,
    val healthScore: Double,
    val harvestDate: String,
    val status: String,
    val createdAt: String
)
