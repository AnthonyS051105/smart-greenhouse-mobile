package com.teti2026.smartgreenhouse.ui.buyer

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
import com.teti2026.smartgreenhouse.viewmodel.ChatUiState
import com.teti2026.smartgreenhouse.viewmodel.ChatViewModel

@Composable
fun ChatRoute(
    listingId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    var draftText by remember(listingId) { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(listingId) {
        viewModel.load(listingId)
    }

    when (val s = state) {
        is ChatUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is ChatUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(listingId) },
            modifier = modifier
        )
        is ChatUiState.Success -> {
            ChatScreen(
                product = s.product,
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
