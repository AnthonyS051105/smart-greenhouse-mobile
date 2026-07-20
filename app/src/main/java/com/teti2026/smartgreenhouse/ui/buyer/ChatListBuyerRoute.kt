package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.ChatListUiState
import com.teti2026.smartgreenhouse.viewmodel.ChatListViewModel

@Composable
fun ChatListBuyerRoute(
    onConversationClick: (BuyerChatConversation) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is ChatListUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is ChatListUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load,
            modifier = modifier
        )
        is ChatListUiState.Success -> {
            ChatListBuyerScreen(
                conversations = s.conversations,
                onConversationClick = onConversationClick,
                onNotificationsClick = onNotificationsClick,
                currentBottomNavRoute = Routes.BUYER_CHAT_LIST,
                onBottomNavigate = onBottomNavigate,
                modifier = modifier
            )
        }
    }
}
