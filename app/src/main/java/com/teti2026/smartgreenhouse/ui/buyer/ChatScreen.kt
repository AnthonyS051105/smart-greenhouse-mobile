package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.BorderOutline
import com.teti2026.smartgreenhouse.ui.theme.DisabledBg
import com.teti2026.smartgreenhouse.ui.theme.DisabledText
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Chat Negosiasi - AgriSmart" dari Stitch. Stateless: seluruh data & event di-hoist ke
 * caller (nantinya ChatViewModel + FirestoreRepository.getMessages/sendMessage realtime listener,
 * lihat `docs/SDD.md §4.2/§5`). Dijangkau dari tombol ikon chat di "Detail Produk - Pembeli".
 * [product] jadi sumber tunggal info penjual & produk yang dinegosiasikan — sama persis dengan
 * objek yang dipakai Detail Produk/Checkout untuk [product.id] yang sama, agar keduanya selalu
 * konsisten.
 */
@Composable
fun ChatScreen(
    product: ListingDetailItem,
    messages: List<ChatMessageItem>,
    draftText: String,
    onDraftTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onQuickReplyClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        ChatTopBar(
            sellerName = product.sellerName,
            sellerAvatarUrl = product.sellerAvatarUrl,
            sellerInitials = product.sellerInitials,
            onBackClick = onBackClick
        )
        ChatProductContextCard(product = product)
        ChatMessageList(messages = messages, modifier = Modifier.weight(1f))
        ChatInputBar(
            draftText = draftText,
            onDraftTextChange = onDraftTextChange,
            onSendClick = onSendClick,
            onQuickReplyClick = onQuickReplyClick
        )
    }
}

@Composable
private fun ChatTopBar(
    sellerName: String,
    sellerAvatarUrl: String?,
    sellerInitials: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.chat_back_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sellerAvatarUrl != null) {
                    AsyncImage(
                        model = sellerAvatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, BorderOutline, CircleShape)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                    ) {
                        Text(
                            text = sellerInitials,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = sellerName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            text = stringResource(R.string.chat_online_status),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = { /* TODO: menu opsi (blokir/laporkan) menyusul saat dibutuhkan */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.chat_more_options_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatProductContextCard(product: ListingDetailItem, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(12.dp))
            .border(1.dp, BorderOutline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        AsyncImage(
            model = product.imageUrls.firstOrNull(),
            contentDescription = product.imageContentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.cropName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.chat_product_price_per_kg, product.pricePerKgLabel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.chat_product_stock, product.quantityAvailableLabel),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatMessageList(messages: List<ChatMessageItem>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    // Selalu tampilkan pesan terbaru begitu daftar berubah (pesan baru dikirim) maupun saat
    // pertama kali dibuka (percakapan awal langsung terlihat dari bawah, bukan dari atas).
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item {
            Text(
                text = stringResource(R.string.chat_today_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(messages, key = { it.id }) { message ->
            ChatMessageBubble(message = message)
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessageItem, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = if (message.isSentByMe) Alignment.End else Alignment.Start,
            modifier = Modifier
                .align(if (message.isSentByMe) Alignment.CenterEnd else Alignment.CenterStart)
                .widthIn(max = 280.dp)
        ) {
            Surface(
                color = if (message.isSentByMe) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                shape = RoundedCornerShape(
                    topStart = if (message.isSentByMe) 20.dp else 4.dp,
                    topEnd = if (message.isSentByMe) 4.dp else 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isSentByMe) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = message.timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (message.isSentByMe && message.isRead) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = stringResource(R.string.chat_message_read_content_description),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    draftText: String,
    onDraftTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onQuickReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                val agreeLabel = stringResource(R.string.chat_quick_reply_agree)
                val negotiateLabel = stringResource(R.string.chat_quick_reply_negotiate)
                ChatQuickReplyChip(label = agreeLabel, onClick = { onQuickReplyClick(agreeLabel) })
                ChatQuickReplyChip(label = negotiateLabel, onClick = { onQuickReplyClick(negotiateLabel) })
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { /* TODO: lampiran foto (Cloudinary) menyusul saat dibutuhkan */ },
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = stringResource(R.string.chat_attachment_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 100.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(20.dp))
                        .border(1.dp, BorderOutline, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (draftText.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_message_input_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = draftText,
                        onValueChange = onDraftTextChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                val canSend = draftText.isNotBlank()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (canSend) MaterialTheme.colorScheme.primary else DisabledBg)
                        .clickable(enabled = canSend, onClick = onSendClick)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send_content_description),
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary else DisabledText
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatQuickReplyChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ChatScreenPreview() {
    SmartgreenhousemobileTheme {
        val product = sampleListingDetails.getValue("listing-cabai-rawit-1")
        ChatScreen(
            product = product,
            messages = sampleChatMessagesFor(product),
            draftText = "",
            onDraftTextChange = {},
            onSendClick = {},
            onQuickReplyClick = {},
            onBackClick = {}
        )
    }
}
