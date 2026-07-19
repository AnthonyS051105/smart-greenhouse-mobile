package com.teti2026.smartgreenhouse.ui.buyer

import com.teti2026.smartgreenhouse.ui.components.SensorChartPoint

/**
 * Model presentasi "Detail Produk - Pembeli" dari Stitch — gabungan `listings` + `farms` + `users`
 * + `sensor_readings` (lihat `docs/data-contracts.md §3.2/§3.4/§3.7`). Data statis sementara
 * (lihat [sampleListingDetails]); akan diganti hasil query gabungan ViewModel + FirestoreRepository
 * begitu MOB-T18 dikerjakan (lihat `docs/SDD.md §4.2/§5`).
 *
 * Catatan: [imageUrls] (multi-foto), [description], [minOrderLabel] belum punya field resmi di
 * `data-contracts.md` — akan didefinisikan bersama tim saat MOB-T13 (Buat Listing, sisi Petani)
 * dikerjakan. Field ini bersifat presentasional sementara, bukan keputusan skema final.
 *
 * [pricePerKg]/[quantityAvailableKg]/[minOrderKg] adalah versi angka mentah dari `price_per_kg`/
 * `quantity_kg` (`data-contracts.md §3.7`) — dipakai untuk hitung subtotal di Checkout (MOB-T21),
 * berdampingan dengan versi *Label yang sudah diformat untuk tampilan.
 *
 * [farmId] — padanan `listings.farm_id` (`data-contracts.md §3.7`), dipakai [listingsForFarm]
 * untuk menampilkan seluruh produk satu kebun di screen "Produk Lahan - Peta" (lihat
 * `FarmProductsMapScreen`), dijangkau dari tap kebun di `MapScreen`.
 */
data class ListingDetailItem(
    val id: String,
    val farmId: String,
    val cropName: String,
    val locationLabel: String,
    val harvestLabel: String,
    val imageUrls: List<String>,
    val imageContentDescription: String,
    val healthScore: Double,
    val pricePerKg: Long,
    val pricePerKgLabel: String,
    val quantityAvailableKg: Double,
    val quantityAvailableLabel: String,
    val minOrderKg: Double,
    val minOrderLabel: String,
    val unitLabel: String,
    val description: String,
    val sensorHistoryStatusLabel: String,
    val sensorHistory: List<SensorChartPoint>,
    val sellerName: String,
    val sellerAvatarUrl: String?,
    val sellerInitials: String = sellerName.take(2).uppercase(),
    val sellerRatingLabel: String?,
    val sellerActivityLabel: String
)

/**
 * Sample data selaras id dengan [sampleMarketplaceListings] (Marketplace) & [sampleNearbyFarms]
 * (Peta) agar navigasi lintas screen konsisten. Konten "listing-cabai-rawit-1" mengikuti persis
 * mockup Stitch "Detail Produk - Pembeli"; 3 lainnya dilengkapi mengikuti struktur yang sama
 * (deskripsi/riwayat sensor invented, ditandai jelas sebagai data contoh).
 */
val sampleListingDetails: Map<String, ListingDetailItem> = listOf(
    ListingDetailItem(
        id = "listing-cabai-rawit-1",
        farmId = "farm-pak-budi",
        cropName = "Cabai Rawit Merah",
        locationLabel = "Boyolali, Jawa Tengah",
        harvestLabel = "Panen Hari Ini",
        imageUrls = listOf(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCu_isAVJaetKcYT5roOfZADONL3PEE6-OPBshhA_7gyTmSIbP3kaYdD_5lf4Dw60aJOpHahd5MMfRZsApOxMu2Qra4DskF36xP_30qc9ARjzgRqAqsR8JVOGmjS6rU9UnrBlQABmx0TCAWnbfug8_lm3hugypKwA9QodFO49FK078JcQFshtCwNz8JTs8L89z5MDktnHey-XQvrtDVf2378Ft7ALdnbvmSgJyjoLBnZwmpVD5PP3_QVTDjhMJNG--yZ8IRRGPEVw",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCMp8NIYHbcVWBei9BUDFnbluExNT-C-b9MteTJNDGGfZMXzhkQbfpJRE1ajM2w3aA0wBHT8d9cc8W26WMVTJkK6FXPkiiEj5ohSgsOXJw0GC8BAtRXx2jWfdDfPQDHsf60j1ZcOgRz2tuCIeqHKsy5z4e_UaCChH42_Q7bEwud_5QF64iD5ZGV5qI-VJ0s2gonr5WkY8XvDWMJ6MW_Ji8BiKIbQL3ifqEmiOxysEmdGNBS52BEWqchuDjwKtvOue6OY6NcT5byUA"
        ),
        imageContentDescription = "Tumpukan cabai rawit merah segar",
        healthScore = 88.5,
        pricePerKg = 25_000L,
        pricePerKgLabel = "Rp 25.000",
        quantityAvailableKg = 500.0,
        quantityAvailableLabel = "500 Kg",
        minOrderKg = 10.0,
        minOrderLabel = "10 Kg",
        unitLabel = "Kg",
        description = "Cabai rawit merah kualitas super, ditanam dengan metode pertanian presisi " +
            "berbasis monitoring IoT. Tingkat kepedasan tinggi dan tahan lama untuk pengiriman " +
            "jarak jauh. Dipanen tepat waktu untuk memastikan kesegaran maksimal.",
        sensorHistoryStatusLabel = "Kondisi Stabil",
        sensorHistory = listOf(
            SensorChartPoint("Sen", 68f),
            SensorChartPoint("Rab", 74f),
            SensorChartPoint("Jum", 65f),
            SensorChartPoint("Min", 72f)
        ),
        sellerName = "Pak Budi",
        sellerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDQxc16yUlcMQiQ0PMlJYJE7veoyAJgK98TrdjtPrpvVPiJ46CPDYWt_-O7VjryFbASwc5tXhusjF0XgXPrSOXWq6m6ePjp3WFFHb1Ws_3O5IZxpORbkKX6e7-yIabvJe59iBTWfu8sqAIEcYS-1nbzqiVnfdvBX5HhUEXeSvL9bhbLiJUc4KcxkTEmN4ps4gHssZz_qZoWQTm0uXOoEIKLy7yPMha4V03jYLWGzE1B0gaDw3lqRXo_uHiKCctQpC9sfSuJcbeu_g",
        sellerRatingLabel = "4.9",
        sellerActivityLabel = "Aktif 2 jam lalu"
    ),
    ListingDetailItem(
        id = "listing-tomat-1",
        farmId = "farm-agro-sejahtera",
        cropName = "Tomat Merah Segar",
        locationLabel = "Sleman, Yogyakarta",
        harvestLabel = "Panen 1 Hari Lalu",
        imageUrls = listOf(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuAbVIAAedwbMIpJNupI7Or6ePafoBgGzKF3CsxqXz-LMK9HOAQ_yMvGbN_14cnBd0Nd08bcwC05JKMohy4e-5A5Yq7_WLqPTyl326UJaq0icQD6lHyxcJ65ugTIrJiM_R6LEyT_NujHvHZDsl70q8V1aVioBivF26a6R-wHtHBYCZgStPqS2sUlsX5xvxy1NVUi3PB2p23hSO0rwIfyL9MCWyAxeLB9Yy0CMwF1I6IZtNt1gmLvV88hExnIAX3Qxobssy2vafNBQA"
        ),
        imageContentDescription = "Tomat merah segar dalam keranjang kayu",
        healthScore = 82.0,
        pricePerKg = 12_000L,
        pricePerKgLabel = "Rp 12.000",
        quantityAvailableKg = 300.0,
        quantityAvailableLabel = "300 Kg",
        minOrderKg = 5.0,
        minOrderLabel = "5 Kg",
        unitLabel = "Kg",
        description = "Tomat merah segar hasil greenhouse dengan kontrol suhu & kelembapan otomatis. " +
            "Rasa asam-manis seimbang, cocok untuk kebutuhan dapur maupun industri olahan.",
        sensorHistoryStatusLabel = "Kondisi Stabil",
        sensorHistory = listOf(
            SensorChartPoint("Sen", 70f),
            SensorChartPoint("Rab", 66f),
            SensorChartPoint("Jum", 71f),
            SensorChartPoint("Min", 69f)
        ),
        sellerName = "Bu Siti",
        sellerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAraAKJe9POKRHVkiDsEJYjgDGHROcdvEjca7nN5b5BWnxeYeE-1Du01CtX57wJaLFYsFU0XNfrJ3y0U6v9PBvSB6GT1SgIhUY9XbxS-rnGV7_eht-eRQhxyCCEKN3_iSFq6ya7rRgEDxfWxbFic0eONiv8QNRqmmY2-7cdUsVe9Cod3HvGWL6A8va_bOfYA6fI_w-Xl3okGvR5Q1y2EDfogkvYl9qSIJhniWIs8hTDTzvsX45MS3Al8Q6JxA31iB8zT_numaPnHw",
        sellerRatingLabel = "4.7",
        sellerActivityLabel = "Aktif 1 hari lalu"
    ),
    ListingDetailItem(
        id = "listing-bayam-1",
        farmId = "farm-hidroponik-lestari",
        cropName = "Bayam Cabut Organik",
        locationLabel = "Bantul, Yogyakarta",
        harvestLabel = "Panen 2 Hari Lalu",
        imageUrls = listOf(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCftwJ4-ZP4f2pboXPlyK5JYSgezbmshkPrLrwOZedSpgl_VsDDYLTj3D3XfasBWDT8y4O5eybwSuVzh95tL71mWBQel3EtD-uRPzriKlwQbkNtLifH7HVQRHdAxGGqbPsr4mU2gwi_-IUTMUznEqk-07cDsxVDqp2tn2Ij7Wf0Hu4H2nDbHlCAuTrWS0SwdVVoNFyxg0WyYCHsuJXZaehxtdRFY6KXQJjZPLnX6v81LCIT-H9DrGB-adtVPjvPQ0JndTyqBhCxag"
        ),
        imageContentDescription = "Bayam cabut organik segar dalam ikatan",
        healthScore = 63.0,
        pricePerKg = 4_500L,
        pricePerKgLabel = "Rp 4.500",
        quantityAvailableKg = 120.0,
        quantityAvailableLabel = "120 Ikat",
        minOrderKg = 5.0,
        minOrderLabel = "5 Ikat",
        unitLabel = "Ikat",
        description = "Bayam cabut organik tanpa pestisida kimia, ditanam di greenhouse dengan " +
            "monitoring kelembapan tanah berkala. Cocok untuk pelanggan yang mengutamakan produk sehat.",
        sensorHistoryStatusLabel = "Perlu Perhatian",
        sensorHistory = listOf(
            SensorChartPoint("Sen", 55f),
            SensorChartPoint("Rab", 50f),
            SensorChartPoint("Jum", 58f),
            SensorChartPoint("Min", 52f)
        ),
        sellerName = "Agus T.",
        sellerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD92y9Lqn26mTU-vC6UP_06cdliodJ-5jy7XSoXhSzKnTbWV5NjLatRH6UyBHa9B8GFwEtjFgyy0c6TbponmlXqqi4rda4DAODeXqcQvVEfkHmatBv1ONKnT2ikT_KKXetGlFNMg10KzIjeOyHSgxAUI8LgOjHoPBWT6muKpVgCNCiwi8laIxH1A6mYwBsA1zPrjLuzgfUUCcdWF2G2KL9DokzqpXN3BDycQhkPgLHUuBXSm36oGqmOhYISP3-hbp3QTPswNn2fSQ",
        sellerRatingLabel = "4.5",
        sellerActivityLabel = "Aktif 3 hari lalu"
    ),
    ListingDetailItem(
        // Sengaja farmId = "farm-pak-budi" (sama dengan listing-cabai-rawit-1) — demo kebun dengan
        // >1 produk untuk screen "Produk Lahan - Peta" (lihat listingsForFarm di bawah).
        id = "listing-paprika-1",
        farmId = "farm-pak-budi",
        cropName = "Paprika Hijau Besar",
        locationLabel = "Magelang, Jawa Tengah",
        harvestLabel = "Panen Hari Ini",
        imageUrls = listOf(
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBC68Tdn5Jtvv4eX32pMkG0NWXeFa9UEkeR0AcqWLtS6j6UJXwbbXPdPCQ1P5FcJ6scHT8zfrM7MlLFta_uf9s8tNJ3v2rqgeNpw3eGw--NpaCvJwxj0WViRLLmsMxCl5dyKLxjKniQbUkccJiT_8l0OZCZOs7t4mAIOaMFwOGpQ0g-fFQHgU0sTflh7R5okUKGkHpna-SYe0nZ34MI5gZWnvUc63c9GdOYakOURpOCgG5J19oV1FW4elNIdNylqud78L_g37LV_Q"
        ),
        imageContentDescription = "Paprika hijau besar di atas permukaan gelap",
        healthScore = 91.0,
        pricePerKg = 35_000L,
        pricePerKgLabel = "Rp 35.000",
        quantityAvailableKg = 150.0,
        quantityAvailableLabel = "150 Kg",
        minOrderKg = 10.0,
        minOrderLabel = "10 Kg",
        unitLabel = "Kg",
        description = "Paprika hijau besar hasil budidaya greenhouse Koperasi Tani, dipanen segar " +
            "dengan skor kesehatan tanaman tinggi. Tekstur renyah dan tebal, ideal untuk restoran " +
            "maupun ritel.",
        sensorHistoryStatusLabel = "Kondisi Stabil",
        sensorHistory = listOf(
            SensorChartPoint("Sen", 75f),
            SensorChartPoint("Rab", 78f),
            SensorChartPoint("Jum", 73f),
            SensorChartPoint("Min", 77f)
        ),
        sellerName = "Koperasi Tani",
        sellerAvatarUrl = null,
        sellerInitials = "KT",
        sellerRatingLabel = "4.8",
        sellerActivityLabel = "Aktif 5 jam lalu"
    )
).associateBy { it.id }

/**
 * Seluruh listing milik satu farm, dipakai screen "Produk Lahan - Peta" (`FarmProductsMapScreen`).
 * Padanan sementara query `FirestoreRepository.getListings(filter = farmId)` — akan diganti begitu
 * MOB-T17/T19 dikerjakan.
 */
fun listingsForFarm(farmId: String): List<ListingDetailItem> =
    sampleListingDetails.values.filter { it.farmId == farmId }
