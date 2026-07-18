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
 * Turunkan [OrderSuccessItem] dari [ListingDetailItem] yang sedang di-checkout.
 *
 * TODO: ganti [OrderSuccessItem.orderId] dengan id dokumen `orders` sungguhan dari
 * `FirestoreRepository.createOrder(order)` (`docs/data-contracts.md §3.8`) begitu MOB-T21
 * dikerjakan — Checkout saat ini belum menyimpan order ke Firestore, jadi id di sini murni
 * placeholder tampilan (bukan id dokumen nyata), diturunkan deterministik dari [listing.id]
 * agar tetap stabil di preview & selama satu sesi.
 */
fun orderSuccessItemFrom(listing: ListingDetailItem): OrderSuccessItem {
    val displayNumber = (abs(listing.id.hashCode()) % 100_000).toString().padStart(5, '0')
    return OrderSuccessItem(
        orderId = "AGR-$displayNumber",
        sellerName = listing.sellerName
    )
}
