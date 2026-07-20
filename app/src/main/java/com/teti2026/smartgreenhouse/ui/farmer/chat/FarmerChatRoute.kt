package com.teti2026.smartgreenhouse.ui.farmer.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.viewmodel.FarmerChatUiState
import com.teti2026.smartgreenhouse.viewmodel.FarmerChatViewModel

@Composable
fun FarmerChatRoute(
    conversationId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FarmerChatViewModel = viewModel()
) {
    var draftText by remember(conversationId) { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(conversationId) {
        viewModel.load(conversationId)
    }

    when (val s = state) {
        is FarmerChatUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is FarmerChatUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(conversationId) },
            modifier = modifier
        )
        is FarmerChatUiState.Success -> {
            FarmerChatScreen(
                conversation = s.conversation,
                messages = s.messages,
                draftText = draftText,
                onDraftTextChange = { draftText = it },
                onSendClick = {
                    viewModel.sendMessage(draftText)
                    draftText = ""
                },
                onQuickReplyClick = { reply -> draftText = reply },
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
    }
}
