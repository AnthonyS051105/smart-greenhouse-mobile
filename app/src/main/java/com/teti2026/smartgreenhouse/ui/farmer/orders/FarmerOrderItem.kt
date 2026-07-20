package com.teti2026.smartgreenhouse.ui.farmer.orders

import com.teti2026.smartgreenhouse.ui.buyer.OrderStatus

/**
 * Model presentasi kartu "Pesanan Masuk - Petani" — padanan sisi-petani dari
 * [com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryItem], gabungan `orders`+`listings`+`users`
 * (data-contracts.md §3.1/§3.7/§3.8). Screen ini BARU (tidak ada mockup Stitch) — dibangun
 * setelah disadari tidak ada satupun UI sisi Petani untuk melihat/mengonfirmasi pesanan masuk,
 * padahal alur nilai `docs/UIUX-Flow.md §3.4` ("Order masuk → Konfirmasi Pesanan") mensyaratkan
 * ini. [OrderStatus]/`OrderHistoryTab`/`toHistoryTab()` dipakai ULANG dari package `ui.buyer`
 * (bukan duplikat) — semantik pengelompokan tab identik di kedua sisi.
 */
data class FarmerOrderItem(
    val id: String,
    val listingId: String,
    val cropName: String,
    val imageUrl: String,
    val imageContentDescription: String,
    val buyerName: String,
    val quantityLabel: String,
    val totalPriceLabel: String,
    val dateLabel: String,
    val status: OrderStatus
)

private const val PREVIEW_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCJnBIt86DXkIHrKnbo7v6o1ANBqDWBKHaRV6yL6zoG0gvtCvkTJs5oYAqo9PHQXwNkzKDk91UBr55j9aeN2Ma-FxzG71PwkmuGLs1pyTHsWgktsT2awYcxpC1nx46AwqqlWwijtaPg2asFWZ8ywfDdWjsvO5p7obO83eyTZLEqeA0Ue82cjOgFMEfemCCRhgV6XpDlv64UQQkuWHYNG6zH05hp1Rl_ezdCzAOPJ_tjseOEDdWm33bW1cvVivsjkrInP6leJ1U0ig"

/** Data contoh — HANYA dipakai `@Preview` di `FarmerOrdersScreen.kt`, tidak pernah dipakai alur data nyata. */
val sampleFarmerOrders = listOf(
    FarmerOrderItem(
        id = "order-preview-1",
        listingId = "listing-cabai-rawit-1",
        cropName = "Cabai Rawit Merah",
        imageUrl = PREVIEW_IMAGE_URL,
        imageContentDescription = "Cabai rawit merah dalam keranjang anyaman",
        buyerName = "Rina Wijaya",
        quantityLabel = "5 Kg",
        totalPriceLabel = "Rp 137.000",
        dateLabel = "24 Okt 2023",
        status = OrderStatus.PENDING
    ),
    FarmerOrderItem(
        id = "order-preview-2",
        listingId = "listing-tomat-1",
        cropName = "Tomat Merah Segar",
        imageUrl = PREVIEW_IMAGE_URL,
        imageContentDescription = "Tomat merah segar dalam keranjang kayu",
        buyerName = "Dewi Anggraini",
        quantityLabel = "3 Kg",
        totalPriceLabel = "Rp 48.000",
        dateLabel = "22 Okt 2023",
        status = OrderStatus.CONFIRMED
    ),
    FarmerOrderItem(
        id = "order-preview-3",
        listingId = "listing-bayam-1",
        cropName = "Bayam Cabut Organik",
        imageUrl = PREVIEW_IMAGE_URL,
        imageContentDescription = "Bayam cabut organik segar dalam ikatan",
        buyerName = "Hendra Saputra",
        quantityLabel = "10 Ikat",
        totalPriceLabel = "Rp 47.000",
        dateLabel = "18 Okt 2023",
        status = OrderStatus.COMPLETED
    )
)
