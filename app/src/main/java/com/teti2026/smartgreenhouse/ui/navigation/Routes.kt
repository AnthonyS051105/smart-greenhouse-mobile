package com.teti2026.smartgreenhouse.ui.navigation

/**
 * Route id NavHost, sesuai peta navigasi di `docs/SDD.md §6`.
 * LOGIN, BUYER_MARKETPLACE, BUYER_MAP, BUYER_DETAIL, BUYER_CHAT, BUYER_CHECKOUT,
 * BUYER_ORDER_SUCCESS, BUYER_ORDERS, BUYER_REVIEW, BUYER_NOTIFICATIONS, BUYER_PROFILE,
 * FARMER_DASHBOARD, FARMER_PROFILE, FARMER_SETUP_GREENHOUSE_*, FARMER_CREATE_LISTING &
 * FARMER_IMAGE_HISTORY yang punya destination — sisanya menyusul per screen dibuat. Detail
 * Analisis Citra BUKAN destination NavHost (lihat
 * `ImageHistoryRoute` — dirender sebagai overlay `ModalBottomSheet` di atas Riwayat Citra).
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

    // Tujuan tombol "+" navbar Petani — form buat listing hasil panen. [prefillImageId] opsional
    // (query param, default "none") dipakai saat masuk dari tombol "Buat Listing dari Data Ini"
    // di Detail Analisis Citra, membawa id citra terpilih supaya form bisa di-prefill.
    const val FARMER_CREATE_LISTING = "farmer/listing/create?prefillImageId={prefillImageId}"
    const val FARMER_CREATE_LISTING_BASE = "farmer/listing/create"

    // Tujuan tab "Riwayat" navbar Petani — grid citra & skor AI. Detail satu citra ditampilkan
    // sebagai ModalBottomSheet overlay di composable yang sama (bukan route terpisah).
    const val FARMER_IMAGE_HISTORY = "farmer/image-history"

    // Tujuan tombol "Buat Listing" (FAB) di Riwayat Citra — pindaian tanaman on-demand lewat
    // kamera HP (bukan siklus ESP32-CAM), untuk petani yang ingin cek health_score seketika saat
    // melihat gejala penyakit. Flow 3 tahap (Viewfinder -> Menganalisis -> Hasil Analisis)
    // dikelola sebagai satu state machine di ScanPlantRoute, BUKAN 3 destination NavHost terpisah
    // (pola sama seperti alasan Detail Analisis Citra jadi ModalBottomSheet: transisi antar tahap
    // di sini bersifat otomatis/berurutan, bukan navigasi bebas yang butuh back stack sendiri).
    const val FARMER_SCAN_PLANT = "farmer/scan-plant"

    // Tujuan tab "Pesan" navbar Petani — daftar percakapan dengan pembeli. Chat negosiasi satu
    // percakapan adalah destination terpisah (bukan tab persisten), padanan [BUYER_CHAT].
    const val FARMER_CHAT = "farmer/chat"
    const val FARMER_CHAT_CONVERSATION = "farmer/chat/{conversationId}"

    /** Bangun route [FARMER_CHAT_CONVERSATION] konkret untuk satu percakapan. */
    fun farmerChatConversation(conversationId: String) = "farmer/chat/$conversationId"

    // Pembeli
    const val BUYER_MARKETPLACE = "buyer/marketplace"
    const val BUYER_MAP = "buyer/map"
    const val BUYER_DETAIL = "buyer/listing/{listingId}"
    const val BUYER_CHAT = "buyer/chat/{listingId}"
    const val BUYER_CHECKOUT = "buyer/checkout/{listingId}"
    const val BUYER_ORDER_SUCCESS = "buyer/order-success/{listingId}"
    const val BUYER_ORDERS = "buyer/orders"
    const val BUYER_REVIEW = "buyer/review/{orderId}"

    // Tujuan tab "Profil" navbar Pembeli — padanan [FARMER_PROFILE] sisi Petani.
    const val BUYER_PROFILE = "buyer/profile"

    // Tujuan tombol ikon lonceng (top bar Marketplace - Pembeli). Destination terpisah yang
    // di-push biasa (bukan tab bottom-nav persisten, padanan [BUYER_CHAT]/[BUYER_CHECKOUT]) —
    // BuyerBottomNavBar tetap ditampilkan di layar ini (permintaan user) tapi tidak ada item
    // yang aktif karena Notifikasi bukan salah satu dari 4 tab tersebut.
    const val BUYER_NOTIFICATIONS = "buyer/notifications"

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

    /** Bangun route [FARMER_CREATE_LISTING] dengan citra terpilih untuk prefill form. */
    fun farmerCreateListingFromImage(imageId: String) = "$FARMER_CREATE_LISTING_BASE?prefillImageId=$imageId"
}
