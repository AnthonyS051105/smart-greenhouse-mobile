package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Model presentasi satu percakapan pada layar "Pesan - Pembeli" (daftar chat, tab bawah App
 * Pembeli). Padanan sisi-pembeli dari
 * [com.teti2026.smartgreenhouse.ui.farmer.chat.FarmerChatConversation]. Selaras
 * `chat_messages`/`listings`/`users` (`docs/data-contracts.md §3.7/§3.9`) — [sellerName]/
 * [sellerAvatarUrl] adalah lawan bicara (petani/penjual), [listingId] penentu satu thread chat
 * (sama seperti [com.teti2026.smartgreenhouse.ui.navigation.Routes.BUYER_CHAT]).
 */
data class BuyerChatConversation(
    val id: String,
    val listingId: String,
    val sellerName: String,
    val sellerAvatarUrl: String?,
    val sellerInitials: String = sellerName.take(2).uppercase(),
    val cropName: String,
    val lastMessage: String,
    val lastMessageTimeLabel: String,
    val unreadCount: Int = 0
)

/**
 * Data contoh sementara (belum ada Firestore `chat_messages` query per-buyer, lihat TODO
 * [ChatListBuyerRoute]) — [listingId] & info penjual merujuk [sampleListingDetails] supaya
 * konteks produk & penjual tetap konsisten saat dibuka menuju [ChatRoute].
 */
val sampleBuyerChatConversations: List<BuyerChatConversation> = listOf(
    BuyerChatConversation(
        id = "buyer-conv-1",
        listingId = "listing-cabai-rawit-1",
        sellerName = sampleListingDetails.getValue("listing-cabai-rawit-1").sellerName,
        sellerAvatarUrl = sampleListingDetails.getValue("listing-cabai-rawit-1").sellerAvatarUrl,
        cropName = sampleListingDetails.getValue("listing-cabai-rawit-1").cropName,
        lastMessage = "Halo, stok Cabai Rawit Merah masih tersedia 500 Kg. Bisa untuk besok pagi.",
        lastMessageTimeLabel = "09:45",
        unreadCount = 1
    ),
    BuyerChatConversation(
        id = "buyer-conv-2",
        listingId = "listing-tomat-1",
        sellerName = sampleListingDetails.getValue("listing-tomat-1").sellerName,
        sellerAvatarUrl = sampleListingDetails.getValue("listing-tomat-1").sellerAvatarUrl,
        cropName = sampleListingDetails.getValue("listing-tomat-1").cropName,
        lastMessage = "Terima kasih, ditunggu kirimannya besok ya.",
        lastMessageTimeLabel = "Kemarin",
        unreadCount = 0
    ),
    BuyerChatConversation(
        id = "buyer-conv-3",
        listingId = "listing-bayam-1",
        sellerName = sampleListingDetails.getValue("listing-bayam-1").sellerName,
        sellerAvatarUrl = sampleListingDetails.getValue("listing-bayam-1").sellerAvatarUrl,
        cropName = sampleListingDetails.getValue("listing-bayam-1").cropName,
        lastMessage = "Apakah stoknya masih ada untuk minggu depan?",
        lastMessageTimeLabel = "2 hari lalu",
        unreadCount = 0
    )
).sortedByDescending { it.unreadCount }
