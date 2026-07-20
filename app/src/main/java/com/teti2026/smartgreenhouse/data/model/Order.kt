package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `orders` sesuai `shared/data-contracts.md §3.8`. Nama field mengikuti dokumen
 * tersebut persis (case-sensitive lintas bidang). [status]: "pending" | "confirmed" | "completed" |
 * "cancelled" — SELALU "pending" saat dibuat ([FirestoreRepository.createOrder]), diubah lewat
 * `FirestoreRepository.updateOrderStatus` oleh Petani di screen "Pesanan Masuk"
 * ([com.teti2026.smartgreenhouse.ui.farmer.orders.FarmerOrdersRoute]). [sellerUid] field
 * mobile-only tambahan (data-contracts.md §3.8) — salinan `farms.owner_uid` pemilik listing,
 * dipakai supaya query "pesanan masuk milik petani ini" bisa satu filter equality (rule-safe
 * tanpa `get()` bersarang, sama alasan seperti `ChatMessage.threadId`).
 */
data class Order(
    val id: String,
    val buyerUid: String,
    val sellerUid: String,
    val listingId: String,
    val quantityKg: Double,
    val totalPrice: Long,
    val status: String,
    val createdAt: String
)
