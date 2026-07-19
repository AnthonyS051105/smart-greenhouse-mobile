package com.teti2026.smartgreenhouse.ui.farmer.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan daftar percakapan ke ChatListViewModel (StateFlow<UiState<List<FarmerChatConversation>>>)
// yang membaca Firestore `chat_messages` (query per-farmer, group by listingId+buyerId) via
// listener realtime, lihat docs/SDD.md §4.2/§5 & MOB-T20 (Task-Breakdown.md) — padanan TODO
// [com.teti2026.smartgreenhouse.ui.buyer.ChatRoute]. Untuk sekarang pakai data contoh statis.
@Composable
fun ChatListRoute(
    onConversationClick: (FarmerChatConversation) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ChatListScreen(
        conversations = sampleFarmerChatConversations,
        onConversationClick = onConversationClick,
        onNotificationsClick = onNotificationsClick,
        currentBottomNavRoute = Routes.FARMER_CHAT,
        onBottomNavigate = onBottomNavigate,
        modifier = modifier
    )
}
