package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan daftar percakapan ke ChatListViewModel (StateFlow<UiState<List<BuyerChatConversation>>>)
// yang membaca Firestore `chat_messages` (query per-buyer, group by listingId+sellerId) via
// listener realtime, lihat docs/SDD.md §4.2/§5 & MOB-T20 (Task-Breakdown.md) — padanan TODO
// [com.teti2026.smartgreenhouse.ui.farmer.chat.ChatListRoute]. Untuk sekarang pakai data contoh statis.
@Composable
fun ChatListBuyerRoute(
    onConversationClick: (BuyerChatConversation) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ChatListBuyerScreen(
        conversations = sampleBuyerChatConversations,
        onConversationClick = onConversationClick,
        onNotificationsClick = onNotificationsClick,
        currentBottomNavRoute = Routes.BUYER_CHAT_LIST,
        onBottomNavigate = onBottomNavigate,
        modifier = modifier
    )
}
