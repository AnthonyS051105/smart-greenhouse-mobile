package com.teti2026.smartgreenhouse.ui.farmer.orders

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryTab
import com.teti2026.smartgreenhouse.ui.buyer.OrderStatus
import com.teti2026.smartgreenhouse.ui.buyer.toHistoryTab
import com.teti2026.smartgreenhouse.ui.navigation.FarmerBottomNavBar
import com.teti2026.smartgreenhouse.ui.theme.ErrorRedAccent
import com.teti2026.smartgreenhouse.ui.theme.InfoBlue
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Pesanan Masuk - Petani" — screen BARU (tidak ada mockup Stitch), dibangun setelah
 * disadari tidak ada UI sisi Petani manapun untuk melihat/mengonfirmasi pesanan pembeli (lihat
 * KDoc [FarmerOrderItem]). Struktur & gaya sengaja disamakan persis dengan
 * [com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryScreen] (tab 3-status yang sama, kartu
 * serupa) supaya "UI dua sisi" tetap terasa satu sistem desain — ditambah baris nama pembeli &
 * tombol aksi ubah status yang tidak ada padanannya di sisi Pembeli. Dijangkau dari menu
 * "Pesanan Masuk" di Profil Petani (di-push, bukan tab bottom-nav — sama seperti Notifikasi).
 */
@Composable
fun FarmerOrdersScreen(
    orders: List<FarmerOrderItem>,
    selectedTab: OrderHistoryTab,
    onTabSelected: (OrderHistoryTab) -> Unit,
    onStatusChange: (orderId: String, newStatus: OrderStatus) -> Unit,
    onBackClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { FarmerOrdersTopBar(onBackClick = onBackClick) },
        bottomBar = {
            FarmerBottomNavBar(currentRoute = currentBottomNavRoute, onNavigate = onBottomNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
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
            FarmerOrdersTabBar(selectedTab = selectedTab, onTabSelected = onTabSelected)

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val distance = if (forward) 1 else -1
                    (slideInHorizontally(animationSpec = tween(220)) { fullWidth -> distance * fullWidth / 4 } +
                        fadeIn(animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(120)))
                },
                label = "farmer_orders_tab_content"
            ) { tab ->
                val filtered = orders.filter { it.status.toHistoryTab() == tab }
                if (filtered.isEmpty()) {
                    FarmerOrdersEmptyState(tab = tab)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filtered.forEach { order ->
                            FarmerOrderCard(
                                order = order,
                                onConfirm = { onStatusChange(order.id, OrderStatus.CONFIRMED) },
                                onComplete = { onStatusChange(order.id, OrderStatus.COMPLETED) },
                                onReject = { onStatusChange(order.id, OrderStatus.CANCELLED) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmerOrdersTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
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
                    contentDescription = stringResource(R.string.farmer_orders_back_content_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.farmer_orders_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** Reuse [OrderHistoryTab] & label string yang sama dengan sisi Pembeli — semantik tab identik. */
@Composable
private fun FarmerOrdersTabBar(
    selectedTab: OrderHistoryTab,
    onTabSelected: (OrderHistoryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(percent = 50))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(percent = 50))
            .padding(4.dp)
    ) {
        OrderHistoryTab.entries.forEach { tab ->
            FarmerOrdersTabSegment(
                label = farmerOrdersTabLabel(tab),
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FarmerOrdersTabSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "farmer_orders_tab_bg"
    )
    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "farmer_orders_tab_content_color"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun farmerOrdersTabLabel(tab: OrderHistoryTab): String = when (tab) {
    OrderHistoryTab.BERLANGSUNG -> stringResource(R.string.order_history_tab_ongoing)
    OrderHistoryTab.SELESAI -> stringResource(R.string.order_history_tab_completed)
    OrderHistoryTab.DIBATALKAN -> stringResource(R.string.order_history_tab_cancelled)
}

private data class OrderStatusPresentation(val label: String, val containerColor: Color, val contentColor: Color)

/** Warna badge status SAMA PERSIS dengan [com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryScreen] — satu sistem visual status pesanan di kedua sisi. */
@Composable
private fun orderStatusPresentation(status: OrderStatus): OrderStatusPresentation = when (status) {
    OrderStatus.PENDING -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_pending), InfoBlue.copy(alpha = 0.12f), InfoBlue
    )
    OrderStatus.CONFIRMED -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_confirmed), InfoBlue.copy(alpha = 0.12f), InfoBlue
    )
    OrderStatus.COMPLETED -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_completed),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        MaterialTheme.colorScheme.primary
    )
    OrderStatus.CANCELLED -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_cancelled), ErrorRedAccent.copy(alpha = 0.12f), ErrorRedAccent
    )
}

@Composable
private fun FarmerOrderCard(
    order: FarmerOrderItem,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = orderStatusPresentation(order.status)

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        AsyncImage(
            model = order.imageUrl,
            contentDescription = order.imageContentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = order.cropName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                )
                Surface(color = presentation.containerColor, shape = RoundedCornerShape(percent = 50)) {
                    Text(
                        text = presentation.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = presentation.contentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.farmer_orders_buyer_prefix, order.buyerName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.farmer_orders_quantity_date_format, order.quantityLabel, order.dateLabel),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = order.totalPriceLabel,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
                FarmerOrderActions(
                    status = order.status,
                    onConfirm = onConfirm,
                    onComplete = onComplete,
                    onReject = onReject,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Aksi ubah status — TANPA dialog konfirmasi (di luar lingkup pertama; beda dari
 * `ProfileBuyerLogoutDialog` yang memang butuh konfirmasi eksplisit karena aksinya membersihkan
 * sesi login). [status] PENDING: "Konfirmasi"/"Tolak". CONFIRMED: "Tandai Selesai"/"Batalkan".
 */
@Composable
private fun FarmerOrderActions(
    status: OrderStatus,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onReject,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(
                    if (status == OrderStatus.PENDING) R.string.farmer_orders_action_reject else R.string.farmer_orders_action_cancel
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Button(
            onClick = if (status == OrderStatus.PENDING) onConfirm else onComplete,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(
                    if (status == OrderStatus.PENDING) R.string.farmer_orders_action_confirm else R.string.farmer_orders_action_complete
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FarmerOrdersEmptyState(tab: OrderHistoryTab, modifier: Modifier = Modifier) {
    val messageRes = when (tab) {
        OrderHistoryTab.BERLANGSUNG -> R.string.farmer_orders_empty_ongoing
        OrderHistoryTab.SELESAI -> R.string.farmer_orders_empty_completed
        OrderHistoryTab.DIBATALKAN -> R.string.farmer_orders_empty_cancelled
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 64.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FarmerOrdersScreenOngoingPreview() {
    SmartgreenhousemobileTheme {
        FarmerOrdersScreen(
            orders = sampleFarmerOrders,
            selectedTab = OrderHistoryTab.BERLANGSUNG,
            onTabSelected = {},
            onStatusChange = { _, _ -> },
            onBackClick = {},
            currentBottomNavRoute = "",
            onBottomNavigate = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FarmerOrdersScreenEmptyPreview() {
    SmartgreenhousemobileTheme {
        FarmerOrdersScreen(
            orders = emptyList(),
            selectedTab = OrderHistoryTab.DIBATALKAN,
            onTabSelected = {},
            onStatusChange = { _, _ -> },
            onBackClick = {},
            currentBottomNavRoute = "",
            onBottomNavigate = {}
        )
    }
}
