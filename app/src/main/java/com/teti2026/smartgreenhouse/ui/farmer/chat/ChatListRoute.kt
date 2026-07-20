package com.teti2026.smartgreenhouse.ui.farmer.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.FarmerChatListUiState
import com.teti2026.smartgreenhouse.viewmodel.FarmerChatListViewModel

@Composable
fun ChatListRoute(
    onConversationClick: (FarmerChatConversation) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FarmerChatListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is FarmerChatListUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is FarmerChatListUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load,
            modifier = modifier
        )
        is FarmerChatListUiState.Success -> {
            ChatListScreen(
                conversations = s.conversations,
                onConversationClick = onConversationClick,
                onNotificationsClick = onNotificationsClick,
                currentBottomNavRoute = Routes.FARMER_CHAT,
                onBottomNavigate = onBottomNavigate,
                modifier = modifier
            )
        }
    }
}
