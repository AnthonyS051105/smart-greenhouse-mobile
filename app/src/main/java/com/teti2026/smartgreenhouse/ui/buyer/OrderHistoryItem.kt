package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Status pesanan — nilai persis sesuai `docs/data-contracts.md §3.8` (`orders.status`,
 * case-sensitive). [wireValue] adalah representasi string yang dikirim/diterima Firestore.
 */
enum class OrderStatus(val wireValue: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    COMPLETED("completed"),
    CANCELLED("cancelled")
}

/** Tab layar "Riwayat Pesanan - Pembeli" (Stitch) — mengelompokkan [OrderStatus] untuk ditampilkan. */
enum class OrderHistoryTab {
    BERLANGSUNG,
    SELESAI,
    DIBATALKAN
}

/** [OrderStatus.PENDING] & [OrderStatus.CONFIRMED] sama-sama "masih berjalan" di sisi pembeli. */
fun OrderStatus.toHistoryTab(): OrderHistoryTab = when (this) {
    OrderStatus.PENDING, OrderStatus.CONFIRMED -> OrderHistoryTab.BERLANGSUNG
    OrderStatus.COMPLETED -> OrderHistoryTab.SELESAI
    OrderStatus.CANCELLED -> OrderHistoryTab.DIBATALKAN
}

/**
 * Model presentasi kartu riwayat pesanan — gabungan `orders` + `listings` (nama produk & foto,
 * lihat `docs/data-contracts.md §3.7/§3.8`). Sementara data statis (lihat [sampleOrderHistory]);
 * akan diganti hasil join Firestore oleh OrderHistoryViewModel + FirestoreRepository.getOrders(buyerUid)
 * (MOB-T22).
 */
data class OrderHistoryItem(
    val id: String,
    val listingId: String,
    val cropName: String,
    val imageUrl: String,
    val imageContentDescription: String,
    val totalPriceLabel: String,
    val dateLabel: String,
    val status: OrderStatus
)

val sampleOrderHistory = listOf(
    OrderHistoryItem(
        id = "order-cabai-1",
        listingId = "listing-cabai-rawit-1",
        cropName = "Cabai Rawit Merah",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCJnBIt86DXkIHrKnbo7v6o1ANBqDWBKHaRV6yL6zoG0gvtCvkTJs5oYAqo9PHQXwNkzKDk91UBr55j9aeN2Ma-FxzG71PwkmuGLs1pyTHsWgktsT2awYcxpC1nx46AwqqlWwijtaPg2asFWZ8ywfDdWjsvO5p7obO83eyTZLEqeA0Ue82cjOgFMEfemCCRhgV6XpDlv64UQQkuWHYNG6zH05hp1Rl_ezdCzAOPJ_tjseOEDdWm33bW1cvVivsjkrInP6leJ1U0ig",
        imageContentDescription = "Cabai rawit merah dalam keranjang anyaman",
        totalPriceLabel = "Rp 125.000",
        dateLabel = "24 Okt 2023",
        status = OrderStatus.CONFIRMED
    ),
    OrderHistoryItem(
        id = "order-bayam-1",
        listingId = "listing-bayam-1",
        cropName = "Bayam Cabut Organik",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCftwJ4-ZP4f2pboXPlyK5JYSgezbmshkPrLrwOZedSpgl_VsDDYLTj3D3XfasBWDT8y4O5eybwSuVzh95tL71mWBQel3EtD-uRPzriKlwQbkNtLifH7HVQRHdAxGGqbPsr4mU2gwi_-IUTMUznEqk-07cDsxVDqp2tn2Ij7Wf0Hu4H2nDbHlCAuTrWS0SwdVVoNFyxg0WyYCHsuJXZaehxtdRFY6KXQJjZPLnX6v81LCIT-H9DrGB-adtVPjvPQ0JndTyqBhCxag",
        imageContentDescription = "Bayam cabut organik segar dalam ikatan",
        totalPriceLabel = "Rp 22.500",
        dateLabel = "27 Okt 2023",
        status = OrderStatus.PENDING
    ),
    OrderHistoryItem(
        id = "order-tomat-1",
        listingId = "listing-tomat-1",
        cropName = "Tomat Merah Segar",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAbVIAAedwbMIpJNupI7Or6ePafoBgGzKF3CsxqXz-LMK9HOAQ_yMvGbN_14cnBd0Nd08bcwC05JKMohy4e-5A5Yq7_WLqPTyl326UJaq0icQD6lHyxcJ65ugTIrJiM_R6LEyT_NujHvHZDsl70q8V1aVioBivF26a6R-wHtHBYCZgStPqS2sUlsX5xvxy1NVUi3PB2p23hSO0rwIfyL9MCWyAxeLB9Yy0CMwF1I6IZtNt1gmLvV88hExnIAX3Qxobssy2vafNBQA",
        imageContentDescription = "Tomat merah segar dalam keranjang kayu",
        totalPriceLabel = "Rp 60.000",
        dateLabel = "20 Okt 2023",
        status = OrderStatus.COMPLETED
    ),
    OrderHistoryItem(
        id = "order-paprika-1",
        listingId = "listing-paprika-1",
        cropName = "Paprika Hijau Besar",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBC68Tdn5Jtvv4eX32pMkG0NWXeFa9UEkeR0AcqWLtS6j6UJXwbbXPdPCQ1P5FcJ6scHT8zfrM7MlLFta_uf9s8tNJ3v2rqgeNpw3eGw--NpaCvJwxj0WViRLLmsMxCl5dyKLxjKniQbUkccJiT_8l0OZCZOs7t4mAIOaMFwOGpQ0g-fFQHgU0sTflh7R5okUKGkHpna-SYe0nZ34MI5gZWnvUc63c9GdOYakOURpOCgG5J19oV1FW4elNIdNylqud78L_g37LV_Q",
        imageContentDescription = "Paprika hijau besar di atas permukaan gelap",
        totalPriceLabel = "Rp 35.000",
        dateLabel = "18 Okt 2023",
        status = OrderStatus.CANCELLED
    )
)
