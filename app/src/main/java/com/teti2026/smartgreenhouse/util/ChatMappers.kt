package com.teti2026.smartgreenhouse.util

import com.teti2026.smartgreenhouse.data.model.ChatMessage
import com.teti2026.smartgreenhouse.ui.buyer.ChatMessageItem
import com.teti2026.smartgreenhouse.ui.farmer.chat.FarmerChatMessageItem
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

private fun ChatMessage.timeLabel(): String {
    val instant = runCatching { Instant.parse(sentAt) }.getOrNull() ?: return ""
    return SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date.from(instant))
}

/**
 * [ChatMessage] → [ChatMessageItem] (bubble sisi Pembeli). [isSentByMe]: true bila [myUid] adalah
 * pengirim — [isRead] SELALU false (belum ada sistem status baca/read-receipt, konsisten dengan
 * gap lain yang belum diimplementasikan seperti `sellerRatingLabel`).
 */
fun ChatMessage.toChatMessageItem(myUid: String): ChatMessageItem = ChatMessageItem(
    id = id,
    text = message,
    timeLabel = timeLabel(),
    isSentByMe = senderUid == myUid
)

/** [ChatMessage] → [FarmerChatMessageItem] (bubble sisi Petani) — padanan [toChatMessageItem]. */
fun ChatMessage.toFarmerChatMessageItem(myUid: String): FarmerChatMessageItem = FarmerChatMessageItem(
    id = id,
    text = message,
    timeLabel = timeLabel(),
    isSentByMe = senderUid == myUid
)

/**
 * Label waktu relatif untuk baris daftar percakapan (`ChatListViewModel`/`FarmerChatListViewModel`)
 * — "09:45" (hari ini), "Kemarin", atau "N hari lalu", meniru format yang sudah ada di data contoh
 * lama ([com.teti2026.smartgreenhouse.ui.buyer.sampleBuyerChatConversations]). Teks Indonesia
 * ditulis langsung (bukan `stringResource`) karena dipanggil dari layer ViewModel yang tidak
 * punya akses `Context`/composable — pola sama seperti string hardcode lain di
 * `ListingDetailViewModel`/`util/BuyerDataMappers.kt`.
 */
fun relativeDayOrTimeLabel(sentAt: String): String {
    val instant = runCatching { Instant.parse(sentAt) }.getOrNull() ?: return sentAt
    val zone = ZoneId.systemDefault()
    val messageDate = instant.atZone(zone).toLocalDate()
    val daysBetween = ChronoUnit.DAYS.between(messageDate, LocalDate.now(zone))
    return when {
        daysBetween <= 0 -> SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date.from(instant))
        daysBetween == 1L -> "Kemarin"
        else -> "$daysBetween hari lalu"
    }
}
