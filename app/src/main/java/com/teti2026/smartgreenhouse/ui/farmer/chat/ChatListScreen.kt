package com.teti2026.smartgreenhouse.ui.farmer.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.FarmerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Pesan - Petani" (daftar percakapan), tujuan tab "Pesan" [FarmerBottomNavBar]. Stateless:
 * seluruh data & event di-hoist ke caller (nantinya ChatListViewModel + query Firestore
 * `chat_messages` per-farmer, padanan TODO [FarmerChatListRoute]). Baris chat mengikuti pola
 * daftar percakapan messaging umum (WhatsApp/marketplace) — baris rata penuh dipisah divider
 * tipis, BUKAN kartu dengan border/shadow terpisah seperti [com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryScreen].
 */
@Composable
fun ChatListScreen(
    conversations: List<FarmerChatConversation>,
    onConversationClick: (FarmerChatConversation) -> Unit,
    onNotificationsClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { ChatListTopBar(onNotificationsClick = onNotificationsClick) },
        bottomBar = {
            FarmerBottomNavBar(currentRoute = currentBottomNavRoute, onNavigate = onBottomNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (conversations.isEmpty()) {
            ChatListEmptyState(modifier = Modifier.padding(innerPadding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
            ) {
                items(conversations, key = { it.id }) { conversation ->
                    ChatListRow(conversation = conversation, onClick = { onConversationClick(conversation) })
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 84.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListTopBar(onNotificationsClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_list_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.chat_list_notifications_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Satu baris percakapan bergaya daftar chat messaging umum (avatar bulat + nama & preview pesan
 * di tengah + waktu & badge unread di kanan) — bukan kartu bersudut/berbayang terpisah, sesuai
 * permintaan supaya identik pola WhatsApp/marketplace, bukan pola card seperti Riwayat Pesanan.
 */
@Composable
private fun ChatListRow(
    conversation: FarmerChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnread = conversation.unreadCount > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (conversation.buyerAvatarUrl != null) {
            AsyncImage(
                model = conversation.buyerAvatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
            ) {
                Text(
                    text = conversation.buyerInitials,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.buyerName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnread) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = conversation.lastMessageTimeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnread) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (isUnread) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListEmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.chat_list_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ChatListScreenPreview() {
    SmartgreenhousemobileTheme {
        ChatListScreen(
            conversations = sampleFarmerChatConversations,
            onConversationClick = {},
            onNotificationsClick = {},
            currentBottomNavRoute = "farmer/chat",
            onBottomNavigate = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ChatListScreenEmptyPreview() {
    SmartgreenhousemobileTheme {
        ChatListScreen(
            conversations = emptyList(),
            onConversationClick = {},
            onNotificationsClick = {},
            currentBottomNavRoute = "farmer/chat",
            onBottomNavigate = {}
        )
    }
}
