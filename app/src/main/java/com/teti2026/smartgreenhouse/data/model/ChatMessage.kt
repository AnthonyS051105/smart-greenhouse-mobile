package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `chat_messages` sesuai `shared/data-contracts.md §3.9`. Nama field mengikuti
 * dokumen tersebut persis (case-sensitive lintas bidang), KECUALI [threadId] — field TAMBAHAN
 * mobile-only (lihat data-contracts.md §3.9 & `util/ChatThreadId.kt`) yang menyimpan
 * `chatThreadId(listingId, buyerUid)` di tiap dokumen pesan. Alasan penambahan: Firestore
 * Security Rules `chat_messages` mensyaratkan query membuktikan filter eksplisit ke
 * `sender_uid`/`receiver_uid` milik pengguna yang login — query "seluruh pesan satu percakapan"
 * TIDAK bisa dibuktikan aman hanya dari `listing_id`+lawan bicara tanpa composite index; dengan
 * [threadId] tersimpan, query jadi satu filter equality sederhana (lihat
 * `FirestoreRepository.getMessagesFlow`).
 */
data class ChatMessage(
    val id: String,
    val threadId: String,
    val senderUid: String,
    val receiverUid: String,
    val listingId: String,
    val message: String,
    val sentAt: String
)
