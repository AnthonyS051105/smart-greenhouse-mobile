package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `reviews` sesuai `shared/data-contracts.md §3.10`. Nama field mengikuti dokumen
 * tersebut persis (case-sensitive lintas bidang). TIDAK ada `buyer_uid`/`listing_id` di skema
 * ini — kepemilikan pembeli dibuktikan lewat `orders.buyer_uid` (nested `get()` di
 * `docs/firestore.rules reviews.create`), dan produk/petani terkait diturunkan lewat
 * `orderId` → `orders.listing_id` bila suatu saat dibutuhkan (belum ada kebutuhan agregasi
 * rating per listing/farm di app ini, lihat TODO `sellerRatingLabel`/`MapFarmItem.rating`).
 */
data class Review(
    val id: String,
    val orderId: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)
