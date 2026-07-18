package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Model presentasi kartu listing di Marketplace — gabungan `listings` + `farms` + `users`
 * (lihat `docs/data-contracts.md §3.7`). Sementara data statis (lihat [sampleMarketplaceListings]);
 * akan diganti hasil join Firestore oleh MarketplaceViewModel + FirestoreRepository (MOB-T17).
 */
data class MarketplaceListingItem(
    val id: String,
    val cropName: String,
    val priceLabel: String,
    val locationLabel: String,
    val imageUrl: String,
    val imageContentDescription: String,
    val healthScore: Double,
    val sellerName: String,
    val sellerAvatarUrl: String? = null,
    val sellerInitials: String = sellerName.take(2).uppercase()
)

val sampleMarketplaceListings = listOf(
    MarketplaceListingItem(
        id = "listing-cabai-rawit-1",
        cropName = "Cabai Rawit Merah",
        priceLabel = "Rp 25.000/kg",
        locationLabel = "Boyolali, 3.2 km",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCu_isAVJaetKcYT5roOfZADONL3PEE6-OPBshhA_7gyTmSIbP3kaYdD_5lf4Dw60aJOpHahd5MMfRZsApOxMu2Qra4DskF36xP_30qc9ARjzgRqAqsR8JVOGmjS6rU9UnrBlQABmx0TCAWnbfug8_lm3hugypKwA9QodFO49FK078JcQFshtCwNz8JTs8L89z5MDktnHey-XQvrtDVf2378Ft7ALdnbvmSgJyjoLBnZwmpVD5PP3_QVTDjhMJNG--yZ8IRRGPEVw",
        imageContentDescription = "Tumpukan cabai rawit merah segar",
        healthScore = 88.5,
        sellerName = "Pak Budi",
        sellerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDQxc16yUlcMQiQ0PMlJYJE7veoyAJgK98TrdjtPrpvVPiJ46CPDYWt_-O7VjryFbASwc5tXhusjF0XgXPrSOXWq6m6ePjp3WFFHb1Ws_3O5IZxpORbkKX6e7-yIabvJe59iBTWfu8sqAIEcYS-1nbzqiVnfdvBX5HhUEXeSvL9bhbLiJUc4KcxkTEmN4ps4gHssZz_qZoWQTm0uXOoEIKLy7yPMha4V03jYLWGzE1B0gaDw3lqRXo_uHiKCctQpC9sfSuJcbeu_g"
    ),
    MarketplaceListingItem(
        id = "listing-tomat-1",
        cropName = "Tomat Merah Segar",
        priceLabel = "Rp 12.000/kg",
        locationLabel = "Sleman, 5.1 km",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAbVIAAedwbMIpJNupI7Or6ePafoBgGzKF3CsxqXz-LMK9HOAQ_yMvGbN_14cnBd0Nd08bcwC05JKMohy4e-5A5Yq7_WLqPTyl326UJaq0icQD6lHyxcJ65ugTIrJiM_R6LEyT_NujHvHZDsl70q8V1aVioBivF26a6R-wHtHBYCZgStPqS2sUlsX5xvxy1NVUi3PB2p23hSO0rwIfyL9MCWyAxeLB9Yy0CMwF1I6IZtNt1gmLvV88hExnIAX3Qxobssy2vafNBQA",
        imageContentDescription = "Tomat merah segar dalam keranjang kayu",
        healthScore = 82.0,
        sellerName = "Bu Siti",
        sellerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAraAKJe9POKRHVkiDsEJYjgDGHROcdvEjca7nN5b5BWnxeYeE-1Du01CtX57wJaLFYsFU0XNfrJ3y0U6v9PBvSB6GT1SgIhUY9XbxS-rnGV7_eht-eRQhxyCCEKN3_iSFq6ya7rRgEDxfWxbFic0eONiv8QNRqmmY2-7cdUsVe9Cod3HvGWL6A8va_bOfYA6fI_w-Xl3okGvR5Q1y2EDfogkvYl9qSIJhniWIs8hTDTzvsX45MS3Al8Q6JxA31iB8zT_numaPnHw"
    ),
    MarketplaceListingItem(
        id = "listing-bayam-1",
        cropName = "Bayam Cabut Organik",
        priceLabel = "Rp 4.500/ikat",
        locationLabel = "Bantul, 8.0 km",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCftwJ4-ZP4f2pboXPlyK5JYSgezbmshkPrLrwOZedSpgl_VsDDYLTj3D3XfasBWDT8y4O5eybwSuVzh95tL71mWBQel3EtD-uRPzriKlwQbkNtLifH7HVQRHdAxGGqbPsr4mU2gwi_-IUTMUznEqk-07cDsxVDqp2tn2Ij7Wf0Hu4H2nDbHlCAuTrWS0SwdVVoNFyxg0WyYCHsuJXZaehxtdRFY6KXQJjZPLnX6v81LCIT-H9DrGB-adtVPjvPQ0JndTyqBhCxag",
        imageContentDescription = "Bayam cabut organik segar dalam ikatan",
        healthScore = 63.0,
        sellerName = "Agus T.",
        sellerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD92y9Lqn26mTU-vC6UP_06cdliodJ-5jy7XSoXhSzKnTbWV5NjLatRH6UyBHa9B8GFwEtjFgyy0c6TbponmlXqqi4rda4DAODeXqcQvVEfkHmatBv1ONKnT2ikT_KKXetGlFNMg10KzIjeOyHSgxAUI8LgOjHoPBWT6muKpVgCNCiwi8laIxH1A6mYwBsA1zPrjLuzgfUUCcdWF2G2KL9DokzqpXN3BDycQhkPgLHUuBXSm36oGqmOhYISP3-hbp3QTPswNn2fSQ"
    ),
    MarketplaceListingItem(
        id = "listing-paprika-1",
        cropName = "Paprika Hijau Besar",
        priceLabel = "Rp 35.000/kg",
        locationLabel = "Magelang, 12.4 km",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBC68Tdn5Jtvv4eX32pMkG0NWXeFa9UEkeR0AcqWLtS6j6UJXwbbXPdPCQ1P5FcJ6scHT8zfrM7MlLFta_uf9s8tNJ3v2rqgeNpw3eGw--NpaCvJwxj0WViRLLmsMxCl5dyKLxjKniQbUkccJiT_8l0OZCZOs7t4mAIOaMFwOGpQ0g-fFQHgU0sTflh7R5okUKGkHpna-SYe0nZ34MI5gZWnvUc63c9GdOYakOURpOCgG5J19oV1FW4elNIdNylqud78L_g37LV_Q",
        imageContentDescription = "Paprika hijau besar di atas permukaan gelap",
        healthScore = 91.0,
        sellerName = "Koperasi Tani",
        sellerAvatarUrl = null,
        sellerInitials = "KT"
    )
)
