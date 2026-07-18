package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Data ringkas transaksi yang ditampilkan di kartu ringkasan layar "Beri Rating & Ulasan"
 * (Stitch) — gabungan `orders` (id) + `listings.crop_type`/foto + `users.name` milik penjual
 * (lihat `docs/data-contracts.md §3.7/§3.8/§3.10`). Dibangun di [ReviewRoute] dengan join
 * [OrderHistoryItem] (foto & nama produk pesanan berstatus "Selesai") + [ListingDetailItem]
 * (nama penjual) lewat `listingId` — pola sama seperti join di `CheckoutRoute`. Data statis
 * sementara; diganti hasil query gabungan ViewModel + FirestoreRepository begitu MOB-T23
 * dikerjakan (lihat `docs/SDD.md §4.2/§5`).
 */
data class ReviewTargetItem(
    val orderId: String,
    val cropName: String,
    val imageUrl: String,
    val imageContentDescription: String,
    val sellerName: String
)

/** Bangun [ReviewTargetItem] dari order riwayat + detail listing terkait (join by listingId). */
fun reviewTargetFrom(order: OrderHistoryItem, listing: ListingDetailItem?): ReviewTargetItem =
    ReviewTargetItem(
        orderId = order.id,
        cropName = order.cropName,
        imageUrl = order.imageUrl,
        imageContentDescription = order.imageContentDescription,
        sellerName = listing?.sellerName.orEmpty()
    )
