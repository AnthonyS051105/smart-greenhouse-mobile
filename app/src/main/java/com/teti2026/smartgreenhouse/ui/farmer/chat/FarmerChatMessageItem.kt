package com.teti2026.smartgreenhouse.ui.farmer.chat

/**
 * Model presentasi pesan chat sisi Petani — padanan [com.teti2026.smartgreenhouse.ui.buyer.ChatMessageItem],
 * selaras `chat_messages` (`docs/data-contracts.md §3.9`). [isSentByMe] di sini berarti petani yang
 * sedang login (kanan, `primary`); false = pembeli lawan bicara (kiri, netral) — kebalikan dari
 * makna [isSentByMe] pada sisi Pembeli.
 */
data class FarmerChatMessageItem(
    val id: String,
    val text: String,
    val timeLabel: String,
    val isSentByMe: Boolean,
    val isRead: Boolean = false
)

/**
 * Percakapan awal contoh untuk [conversation] — konten dibalik dari [com.teti2026.smartgreenhouse.ui.buyer.sampleChatMessagesFor]
 * (pesan pembeli jadi `isSentByMe = false`, balasan petani jadi `isSentByMe = true`) supaya kedua
 * sisi terlihat konsisten bila dibuka bersamaan saat demo.
 */
fun sampleFarmerChatMessagesFor(conversation: FarmerChatConversation): List<FarmerChatMessageItem> = listOf(
    FarmerChatMessageItem(
        id = "msg-seed-1",
        text = "Halo, untuk ${conversation.cropName} apakah bisa pesan dalam jumlah besar untuk " +
            "pengiriman besok pagi?",
        timeLabel = "09:41",
        isSentByMe = false
    ),
    FarmerChatMessageItem(
        id = "msg-seed-2",
        text = "Halo, stok ${conversation.cropName} masih tersedia. Bisa untuk besok pagi.",
        timeLabel = "09:45",
        isSentByMe = true,
        isRead = true
    ),
    FarmerChatMessageItem(
        id = "msg-seed-3",
        text = conversation.lastMessage,
        timeLabel = conversation.lastMessageTimeLabel,
        isSentByMe = false
    )
)
