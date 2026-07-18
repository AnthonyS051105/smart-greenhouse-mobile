package com.teti2026.smartgreenhouse.ui.navigation

/**
 * Route id NavHost, sesuai peta navigasi di `docs/SDD.md §6`.
 * LOGIN, BUYER_MARKETPLACE, BUYER_MAP, BUYER_DETAIL & FARMER_DASHBOARD yang punya destination —
 * sisanya menyusul per screen dibuat.
 */
object Routes {
    const val LOGIN = "login"

    // Petani
    const val FARMER_DASHBOARD = "farmer/dashboard"

    // TODO: belum ada destination — dituju saat petani login tanpa data lahan
    // (lihat FarmerHomeRoute.kt). Dibuat pada sesi berikutnya.
    const val FARMER_SETUP_GREENHOUSE = "farmer/setup-greenhouse"

    // Pembeli
    const val BUYER_MARKETPLACE = "buyer/marketplace"
    const val BUYER_MAP = "buyer/map"
    const val BUYER_DETAIL = "buyer/listing/{listingId}"

    /** Bangun route [BUYER_DETAIL] konkret untuk navigasi ke listing tertentu. */
    fun buyerDetail(listingId: String) = "buyer/listing/$listingId"
}
