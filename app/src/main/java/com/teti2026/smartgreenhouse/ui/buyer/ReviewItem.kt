package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Data ringkas transaksi yang ditampilkan di kartu ringkasan layar "Beri Rating & Ulasan"
 * (Stitch) — gabungan `orders` (id) + `listings.product_name`/foto + `users.name` milik penjual
 * (lihat `docs/data-contracts.md §3.7/§3.8/§3.10`). Dibangun sungguhan oleh `ReviewViewModel`
 * (join `Order`→`Listing`→`Farm`→`User`, pola sama `OrderSuccessViewModel`) sejak MOB-T23.
 */
data class ReviewTargetItem(
    val orderId: String,
    val cropName: String,
    val imageUrl: String,
    val imageContentDescription: String,
    val sellerName: String
)

/** Bangun [ReviewTargetItem] dari data sampel — KHUSUS `@Preview` (pola sama `orderSuccessItemFrom`). */
fun reviewTargetFrom(order: OrderHistoryItem, listing: ListingDetailItem?): ReviewTargetItem =
    ReviewTargetItem(
        orderId = order.id,
        cropName = order.cropName,
        imageUrl = order.imageUrl,
        imageContentDescription = order.imageContentDescription,
        sellerName = listing?.sellerName.orEmpty()
    )
