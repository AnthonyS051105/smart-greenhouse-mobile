package com.teti2026.smartgreenhouse.ui.buyer

import kotlin.math.abs

/**
 * Model presentasi kartu ringkasan layar "Konfirmasi Pesanan - Berhasil" (Stitch) — hanya
 * butuh [orderId] tampilan & [sellerName] petani tujuan pesanan.
 */
data class OrderSuccessItem(
    val orderId: String,
    val sellerName: String
)

/**
 * Turunkan [OrderSuccessItem] dari [ListingDetailItem] — dipakai HANYA untuk `@Preview` di
 * `OrderSuccessScreen.kt`. Alur nyata (`OrderSuccessRoute`/`OrderSuccessViewModel`) membangun
 * [OrderSuccessItem] dari dokumen `orders` sungguhan (data-contracts.md §3.8), bukan lewat fungsi
 * ini — [orderId] di sini murni placeholder tampilan (diturunkan deterministik dari [listing.id]
 * agar tetap stabil di preview), BUKAN id dokumen nyata.
 */
fun orderSuccessItemFrom(listing: ListingDetailItem): OrderSuccessItem {
    val displayNumber = (abs(listing.id.hashCode()) % 100_000).toString().padStart(5, '0')
    return OrderSuccessItem(
        orderId = "AGR-$displayNumber",
        sellerName = listing.sellerName
    )
}
