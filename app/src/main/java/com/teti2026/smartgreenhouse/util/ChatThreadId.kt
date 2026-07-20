package com.teti2026.smartgreenhouse.util

/**
 * Identitas satu thread chat = kombinasi `listingId` + `buyerUid` — BUKAN pasangan simetris
 * sender/receiver, karena penjual satu listing selalu tunggal & bisa diturunkan dari `listingId`
 * (satu listing = satu farm = satu owner, lihat `FirestoreRepository.getFarmById`), jadi tidak
 * perlu disertakan terpisah di identitas thread. Dipakai KEDUA sisi (Petani & Pembeli) supaya
 * sisi manapun menghasilkan id yang identik untuk percakapan yang sama: sisi Pembeli menghitung
 * `chatThreadId(listingId, myUid)` (dirinya sendiri = buyerUid); sisi Petani membaca `buyerUid`
 * balik dari [parseChatThreadId] saat membangun daftar percakapan (`FarmerChatListViewModel`).
 * ':' dipakai sebagai pemisah — aman karena baik id dokumen Firestore (`listings`/farm auto-id)
 * maupun UID Firebase Auth tidak pernah mengandung karakter ':'.
 */
fun chatThreadId(listingId: String, buyerUid: String): String = "$listingId:$buyerUid"

/** Kebalikan [chatThreadId] → `(listingId, buyerUid)`, null bila [threadId] tidak berformat valid. */
fun parseChatThreadId(threadId: String): Pair<String, String>? {
    val separatorIndex = threadId.indexOf(':')
    if (separatorIndex <= 0 || separatorIndex == threadId.lastIndex) return null
    return threadId.substring(0, separatorIndex) to threadId.substring(separatorIndex + 1)
}
