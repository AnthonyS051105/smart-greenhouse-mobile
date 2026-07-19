package com.teti2026.smartgreenhouse.ui.farmer.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: pindahkan riwayat pesan ke ChatViewModel (StateFlow<UiState<List<FarmerChatMessageItem>>>)
// yang membaca Firestore `chat_messages` (listingId+buyerId) via listener realtime dan mengirim
// lewat `sendMessage(msg)`, lihat docs/SDD.md §4.2/§5 & MOB-T20 — padanan TODO
// [com.teti2026.smartgreenhouse.ui.buyer.ChatRoute]. Untuk sekarang data contoh lokal
// ([sampleFarmerChatMessagesFor]) + state di memori.
@Composable
fun FarmerChatRoute(
    conversationId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversation = sampleFarmerChatConversations.firstOrNull { it.id == conversationId }
        ?: sampleFarmerChatConversations.first()
    var messages by remember(conversationId) { mutableStateOf(sampleFarmerChatMessagesFor(conversation)) }
    var draftText by remember(conversationId) { mutableStateOf("") }

    FarmerChatScreen(
        conversation = conversation,
        messages = messages,
        draftText = draftText,
        onDraftTextChange = { draftText = it },
        onSendClick = {
            val trimmed = draftText.trim()
            if (trimmed.isNotEmpty()) {
                messages = messages + FarmerChatMessageItem(
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
