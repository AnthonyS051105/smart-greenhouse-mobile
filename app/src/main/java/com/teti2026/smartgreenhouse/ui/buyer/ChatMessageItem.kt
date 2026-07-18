package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Model presentasi pesan chat pada layar "Chat Negosiasi - AgriSmart" — selaras `chat_messages`
 * (docs/data-contracts.md §3.9). [isSentByMe] menentukan sisi bubble: true = pembeli yang sedang
 * login (kanan, `primary`), false = penjual/petani lawan bicara (kiri, netral).
 */
data class ChatMessageItem(
    val id: String,
    val text: String,
    val timeLabel: String,
    val isSentByMe: Boolean,
    val isRead: Boolean = false
)

/**
 * Percakapan awal contoh untuk [product] — data statis sementara (belum ada Firestore
 * `chat_messages` nyata/listener realtime, lihat TODO MOB-T20 di [ChatRoute]). Isi pesan
 * mengikuti data produk yang sedang dinegosiasikan agar konteks tetap relevan per listing.
 */
fun sampleChatMessagesFor(product: ListingDetailItem): List<ChatMessageItem> = listOf(
    ChatMessageItem(
        id = "msg-seed-1",
        text = "Halo, untuk ${product.cropName} apakah bisa pesan ${product.minOrderLabel} untuk " +
            "pengiriman besok pagi?",
        timeLabel = "09:41",
        isSentByMe = true,
        isRead = true
    ),
    ChatMessageItem(
        id = "msg-seed-2",
        text = "Halo, stok ${product.cropName} masih tersedia ${product.quantityAvailableLabel}. " +
            "Bisa untuk besok pagi.",
        timeLabel = "09:45",
        isSentByMe = false
    ),
    ChatMessageItem(
        id = "msg-seed-3",
        text = "Baik, kalau saya ambil lebih banyak apakah ada harga khusus, ${product.sellerName}?",
        timeLabel = "09:48",
        isSentByMe = true,
        isRead = true
    )
)
