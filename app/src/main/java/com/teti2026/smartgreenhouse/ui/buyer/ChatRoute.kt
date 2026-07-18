package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: pindahkan riwayat pesan ke ChatViewModel (StateFlow<UiState<List<ChatMessageItem>>>) yang
// membaca Firestore `chat_messages` via listener realtime (`getMessages(listingId): Flow<...>`)
// dan mengirim lewat `sendMessage(msg)`, lihat docs/SDD.md §4.2/§5 & MOB-T20 (Task-Breakdown.md).
// Untuk sekarang, riwayat berupa data contoh lokal ([sampleChatMessagesFor]) + state di memori
// (hilang saat keluar screen) supaya alur ketik-kirim tetap bisa dicoba end-to-end.
@Composable
fun ChatRoute(
    listingId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fallback ke listing pertama bila id tidak dikenal, pola sama seperti ListingDetailRoute/
    // CheckoutRoute — produk & penjual yang ditampilkan selalu berasal dari listing yang sama
    // dengan yang sedang dilihat pembeli di Detail Produk.
    val product = sampleListingDetails[listingId] ?: sampleListingDetails.values.first()
    var messages by remember(listingId) { mutableStateOf(sampleChatMessagesFor(product)) }
    var draftText by remember(listingId) { mutableStateOf("") }

    ChatScreen(
        product = product,
        messages = messages,
        draftText = draftText,
        onDraftTextChange = { draftText = it },
        onSendClick = {
            val trimmed = draftText.trim()
            if (trimmed.isNotEmpty()) {
                messages = messages + ChatMessageItem(
                    id = "msg-local-${System.currentTimeMillis()}",
                    text = trimmed,
                    timeLabel = currentTimeLabel(),
                    isSentByMe = true
                )
                draftText = ""
            }
        },
        onQuickReplyClick = { reply -> draftText = reply },
        onBackClick = onBackClick,
        modifier = modifier
    )
}

private fun currentTimeLabel(): String =
    SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date())
