package com.teti2026.smartgreenhouse.ui.navigation

/**
 * Route id NavHost, sesuai peta navigasi di `docs/SDD.md §6`.
 * LOGIN, BUYER_MARKETPLACE, BUYER_MAP, BUYER_DETAIL, BUYER_CHAT, BUYER_CHECKOUT,
 * BUYER_ORDER_SUCCESS, BUYER_ORDERS, BUYER_REVIEW & FARMER_DASHBOARD yang punya destination —
 * sisanya menyusul per screen dibuat.
 */
object Routes {
    const val LOGIN = "login"

    // Petani
    const val FARMER_DASHBOARD = "farmer/dashboard"
    const val FARMER_PROFILE = "farmer/profile"

    // Setup Greenhouse — flow 3 langkah (Data Utama -> Lokasi Lahan -> Pairing Perangkat).
    // Dituju saat petani login tanpa data lahan (lihat NavGraph.kt) dan saat menekan
    // "Greenhouse Saya" di Profil untuk menambah lahan baru.
    const val FARMER_SETUP_GREENHOUSE_DATA = "farmer/setup-greenhouse/data"
    const val FARMER_SETUP_GREENHOUSE_LOCATION = "farmer/setup-greenhouse/location"
    const val FARMER_SETUP_GREENHOUSE_PAIRING = "farmer/setup-greenhouse/pairing"

    // Pembeli
    const val BUYER_MARKETPLACE = "buyer/marketplace"
    const val BUYER_MAP = "buyer/map"
    const val BUYER_DETAIL = "buyer/listing/{listingId}"
    const val BUYER_CHAT = "buyer/chat/{listingId}"
    const val BUYER_CHECKOUT = "buyer/checkout/{listingId}"
    const val BUYER_ORDER_SUCCESS = "buyer/order-success/{listingId}"
    const val BUYER_ORDERS = "buyer/orders"
    const val BUYER_REVIEW = "buyer/review/{orderId}"

    /** Bangun route [BUYER_DETAIL] konkret untuk navigasi ke listing tertentu. */
    fun buyerDetail(listingId: String) = "buyer/listing/$listingId"

    /** Bangun route [BUYER_CHAT] konkret untuk chat negosiasi listing tertentu. */
    fun buyerChat(listingId: String) = "buyer/chat/$listingId"

    /** Bangun route [BUYER_CHECKOUT] konkret untuk navigasi checkout listing tertentu. */
    fun buyerCheckout(listingId: String) = "buyer/checkout/$listingId"

    /** Bangun route [BUYER_ORDER_SUCCESS] konkret setelah pesanan listing tertentu terkonfirmasi. */
    fun buyerOrderSuccess(listingId: String) = "buyer/order-success/$listingId"

    /** Bangun route [BUYER_REVIEW] konkret untuk memberi ulasan pesanan tertentu. */
    fun buyerReview(orderId: String) = "buyer/review/$orderId"
}
