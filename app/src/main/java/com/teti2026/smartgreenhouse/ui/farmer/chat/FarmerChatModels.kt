package com.teti2026.smartgreenhouse.ui.farmer.chat

/**
 * Model presentasi satu percakapan pada layar "Pesan - Petani" (daftar chat, tab bawah App
 * Petani). Padanan sisi-petani dari [com.teti2026.smartgreenhouse.ui.buyer.ChatScreen], tapi
 * berupa daftar (belum ada padanannya di App Pembeli — pembeli masuk chat langsung dari Detail
 * Produk, bukan lewat daftar). Selaras `chat_messages`/`listings`/`users` (`docs/data-contracts.md
 * §3.7/§3.9`) — [buyerName]/[buyerAvatarUrl] adalah lawan bicara (pembeli), [listingId] penentu
 * satu thread chat (sama seperti [Routes.BUYER_CHAT]).
 */
data class FarmerChatConversation(
    val id: String,
    val listingId: String,
    val buyerName: String,
    val buyerAvatarUrl: String?,
    val buyerInitials: String = buyerName.take(2).uppercase(),
    val cropName: String,
    val lastMessage: String,
    val lastMessageTimeLabel: String,
    val unreadCount: Int = 0
)

/**
 * Data contoh sementara (belum ada Firestore `chat_messages` query per-farmer, lihat TODO
 * [FarmerChatListRoute]) — id [listingId] merujuk [com.teti2026.smartgreenhouse.ui.buyer.sampleListingDetails]
 * supaya konteks produk konsisten saat dibuka dari kedua sisi.
 */
val sampleFarmerChatConversations: List<FarmerChatConversation> = listOf(
    FarmerChatConversation(
        id = "conv-1",
        listingId = "listing-cabai-rawit-1",
        buyerName = "Rina Wijaya",
        buyerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAraAKJe9POKRHVkiDsEJYjgDGHROcdvEjca7nN5b5BWnxeYeE-1Du01CtX57wJaLFYsFU0XNfrJ3y0U6v9PBvSB6GT1SgIhUY9XbxS-rnGV7_eht-eRQhxyCCEKN3_iSFq6ya7rRgEDxfWxbFic0eONiv8QNRqmmY2-7cdUsVe9Cod3HvGWL6A8va_bOfYA6fI_w-Xl3okGvR5Q1y2EDfogkvYl9qSIJhniWIs8hTDTzvsX45MS3Al8Q6JxA31iB8zT_numaPnHw",
        cropName = "Cabai Rawit Merah",
        lastMessage = "Baik, kalau saya ambil lebih banyak apakah ada harga khusus, Pak Budi?",
        lastMessageTimeLabel = "09:48",
        unreadCount = 2
    ),
    FarmerChatConversation(
        id = "conv-2",
        listingId = "listing-tomat-1",
        buyerName = "Dewi Anggraini",
        buyerAvatarUrl = null,
        buyerInitials = "DA",
        cropName = "Tomat Merah Segar",
        lastMessage = "Terima kasih, ditunggu kirimannya besok ya.",
        lastMessageTimeLabel = "Kemarin",
        unreadCount = 0
    ),
    FarmerChatConversation(
        id = "conv-3",
        listingId = "listing-bayam-1",
        buyerName = "Hendra Saputra",
        buyerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD92y9Lqn26mTU-vC6UP_06cdliodJ-5jy7XSoXhSzKnTbWV5NjLatRH6UyBHa9B8GFwEtjFgyy0c6TbponmlXqqi4rda4DAODeXqcQvVEfkHmatBv1ONKnT2ikT_KKXetGlFNMg10KzIjeOyHSgxAUI8LgOjHoPBWT6muKpVgCNCiwi8laIxH1A6mYwBsA1zPrjLuzgfUUCcdWF2G2KL9DokzqpXN3BDycQhkPgLHUuBXSm36oGqmOhYISP3-hbp3QTPswNn2fSQ",
        cropName = "Bayam Cabut Organik",
        lastMessage = "Apakah stoknya masih ada untuk minggu depan?",
        lastMessageTimeLabel = "2 hari lalu",
        unreadCount = 0
    )
).sortedByDescending { it.unreadCount }
