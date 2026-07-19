package com.teti2026.smartgreenhouse.ui.buyer

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.BuyerBottomNavBar
import com.teti2026.smartgreenhouse.ui.theme.BorderOutline
import com.teti2026.smartgreenhouse.ui.theme.ErrorRedAccent
import com.teti2026.smartgreenhouse.ui.theme.InfoBlue
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Notifikasi - AgriSmart" dari Stitch. Stateless: seluruh data & event di-hoist ke caller
 * (nantinya NotificationViewModel + FCM/Firestore, lihat `docs/SDD.md §4.2/§5`). Dijangkau dari
 * tombol ikon lonceng di top bar Marketplace - Pembeli.
 *
 * Catatan navbar: mockup Stitch aslinya punya bottom nav 5 ikon khusus (dengan "Notifikasi"
 * sebagai item aktif) — sesuai permintaan user, diganti [BuyerBottomNavBar] standar (4 ikon) yang
 * sama dipakai layar Pembeli lain, agar konsisten satu app-shell. Karena Notifikasi bukan salah
 * satu dari 4 tab tersebut, tidak ada item yang tersorot aktif di sini.
 */
@Composable
fun NotificationScreen(
    groups: List<NotificationGroup>,
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { NotificationTopBar(onBackClick = onBackClick) },
        bottomBar = {
            BuyerBottomNavBar(currentRoute = currentBottomNavRoute, onNavigate = onBottomNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (groups.isEmpty()) {
            NotificationEmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                groups.forEach { group ->
                    NotificationSection(
                        group = group,
                        onNotificationClick = onNotificationClick
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.notifications_back_content_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.notifications_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun NotificationSection(
    group: NotificationGroup,
    onNotificationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = group.dateLabel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 2.dp
        ) {
            Column {
                group.items.forEachIndexed { index, item ->
                    NotificationRow(item = item, onClick = { onNotificationClick(item.id) })
                    if (index != group.items.lastIndex) {
                        HorizontalDivider(color = BorderOutline)
                    }
                }
            }
        }
    }
}

private data class NotificationVisuals(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun notificationVisualsFor(type: NotificationType): NotificationVisuals = when (type) {
    NotificationType.AI_INSIGHT -> NotificationVisuals(
        icon = Icons.Filled.Psychology,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    NotificationType.ORDER -> NotificationVisuals(
        icon = Icons.Filled.ShoppingBag,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    NotificationType.CHAT -> NotificationVisuals(
        icon = Icons.AutoMirrored.Filled.Chat,
        containerColor = InfoBlue,
        contentColor = Color.White
    )
    NotificationType.SHIPPING -> NotificationVisuals(
        icon = Icons.Filled.LocalShipping,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    NotificationType.STOCK_ALERT -> NotificationVisuals(
        icon = Icons.Filled.Warning,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visuals = notificationVisualsFor(item.type)
    val rowBackground = if (item.isUnread) visuals.containerColor.copy(alpha = 0.1f) else Color.Transparent

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(visuals.containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = visuals.icon, contentDescription = null, tint = visuals.contentColor)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (item.isUnread) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ErrorRedAccent)
            )
        }
    }
}

@Composable
private fun NotificationEmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 64.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.NotificationsNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.notifications_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NotificationScreenPreview() {
    SmartgreenhousemobileTheme {
        NotificationScreen(
            groups = sampleNotificationGroups,
            onBackClick = {},
            onNotificationClick = {},
            // "" sengaja: Notifikasi bukan salah satu dari 4 tab BuyerBottomNavBar, jadi tidak
            // ada item yang tersorot aktif (lihat catatan navbar di NotificationScreen di atas).
            currentBottomNavRoute = "",
            onBottomNavigate = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NotificationScreenEmptyPreview() {
    SmartgreenhousemobileTheme {
        NotificationScreen(
            groups = emptyList(),
            onBackClick = {},
            onNotificationClick = {},
            currentBottomNavRoute = "",
            onBottomNavigate = {}
        )
    }
}
