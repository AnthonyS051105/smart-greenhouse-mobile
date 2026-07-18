package com.teti2026.smartgreenhouse.ui.buyer

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.BuyerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.ErrorRedAccent
import com.teti2026.smartgreenhouse.ui.theme.InfoBlue
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Riwayat Pesanan - Pembeli" dari Stitch. Stateless: seluruh data & event di-hoist ke
 * caller (nantinya OrderHistoryViewModel + FirestoreRepository.getOrders(buyerUid), lihat
 * `docs/SDD.md §4.2/§5`). Dijangkau dari tab "Pesanan" di [BuyerBottomNavBar].
 */
@Composable
fun OrderHistoryScreen(
    orders: List<OrderHistoryItem>,
    selectedTab: OrderHistoryTab,
    onTabSelected: (OrderHistoryTab) -> Unit,
    onOrderClick: (String) -> Unit,
    onBackClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { OrderHistoryTopBar(onBackClick = onBackClick) },
        bottomBar = {
            BuyerBottomNavBar(currentRoute = currentBottomNavRoute, onNavigate = onBottomNavigate)
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
            OrderHistoryTabBar(selectedTab = selectedTab, onTabSelected = onTabSelected)

            // Transisi khusus per tab: geser mengikuti arah perpindahan (kiri<->kanan sesuai
            // urutan tab) + fade, agar terasa berbeda dari transisi standar Navigation Compose.
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val distance = if (forward) 1 else -1
                    (slideInHorizontally(animationSpec = tween(220)) { fullWidth -> distance * fullWidth / 4 } +
                        fadeIn(animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(120)))
                },
                label = "order_history_tab_content"
            ) { tab ->
                val filtered = orders.filter { it.status.toHistoryTab() == tab }
                if (filtered.isEmpty()) {
                    OrderHistoryEmptyState(tab = tab)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filtered.forEach { order ->
                            OrderHistoryCard(order = order, onClick = { onOrderClick(order.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
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
                    contentDescription = stringResource(R.string.order_history_back_content_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.order_history_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Segmented control 3 opsi khusus layar ini (bukan reuse [com.teti2026.smartgreenhouse.ui.components.RoleSegmentedControl]
 * yang spesifik untuk 2 role) — tampilan pill mengikuti mockup Stitch, warna & posisi tab aktif
 * bertransisi halus lewat `animateColorAsState`.
 */
@Composable
private fun OrderHistoryTabBar(
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
            OrderHistoryTabSegment(
                label = orderHistoryTabLabel(tab),
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OrderHistoryTabSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "order_history_tab_bg"
    )
    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "order_history_tab_content_color"
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
private fun orderHistoryTabLabel(tab: OrderHistoryTab): String = when (tab) {
    OrderHistoryTab.BERLANGSUNG -> stringResource(R.string.order_history_tab_ongoing)
    OrderHistoryTab.SELESAI -> stringResource(R.string.order_history_tab_completed)
    OrderHistoryTab.DIBATALKAN -> stringResource(R.string.order_history_tab_cancelled)
}

private data class OrderStatusPresentation(
    val label: String,
    val containerColor: Color,
    val contentColor: Color
)

/**
 * Warna badge status TIDAK sama dengan palet health_score — mengikuti mockup Stitch persis:
 * info-blue untuk status berjalan, primary untuk selesai, error-red (aksen, bukan MD3 error)
 * untuk dibatalkan.
 */
@Composable
private fun orderStatusPresentation(status: OrderStatus): OrderStatusPresentation = when (status) {
    OrderStatus.PENDING -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_pending),
        InfoBlue.copy(alpha = 0.12f),
        InfoBlue
    )
    OrderStatus.CONFIRMED -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_confirmed),
        InfoBlue.copy(alpha = 0.12f),
        InfoBlue
    )
    OrderStatus.COMPLETED -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_completed),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        MaterialTheme.colorScheme.primary
    )
    OrderStatus.CANCELLED -> OrderStatusPresentation(
        stringResource(R.string.order_history_status_cancelled),
        ErrorRedAccent.copy(alpha = 0.12f),
        ErrorRedAccent
    )
}

@Composable
private fun OrderHistoryCard(
    order: OrderHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = orderStatusPresentation(order.status)
    // Tampilan dibedakan per status (bukan cuma warna badge), sesuai mockup Stitch: kartu
    // "Selesai" sedikit diredupkan, kartu "Dibatalkan" lebih redup lagi + gambar didesaturasi.
    val cardAlpha = when (order.status) {
        OrderStatus.COMPLETED -> 0.85f
        OrderStatus.CANCELLED -> 0.7f
        else -> 1f
    }
    val imageSaturation = if (order.status == OrderStatus.CANCELLED) 0.7f else 1f
    val priceColor = if (order.status == OrderStatus.CANCELLED) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        AsyncImage(
            model = order.imageUrl,
            contentDescription = order.imageContentDescription,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(imageSaturation) }),
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
                Surface(
                    color = presentation.containerColor,
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(
                        text = presentation.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = presentation.contentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = order.dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Text(
                    text = order.totalPriceLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = priceColor
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OrderHistoryEmptyState(tab: OrderHistoryTab, modifier: Modifier = Modifier) {
    val messageRes = when (tab) {
        OrderHistoryTab.BERLANGSUNG -> R.string.order_history_empty_ongoing
        OrderHistoryTab.SELESAI -> R.string.order_history_empty_completed
        OrderHistoryTab.DIBATALKAN -> R.string.order_history_empty_cancelled
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingBag,
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
private fun OrderHistoryScreenOngoingPreview() {
    SmartgreenhousemobileTheme {
        OrderHistoryScreen(
            orders = sampleOrderHistory,
            selectedTab = OrderHistoryTab.BERLANGSUNG,
            onTabSelected = {},
            onOrderClick = {},
            onBackClick = {},
            currentBottomNavRoute = Routes.BUYER_ORDERS,
            onBottomNavigate = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun OrderHistoryScreenCancelledPreview() {
    SmartgreenhousemobileTheme {
        OrderHistoryScreen(
            orders = sampleOrderHistory,
            selectedTab = OrderHistoryTab.DIBATALKAN,
            onTabSelected = {},
            onOrderClick = {},
            onBackClick = {},
            currentBottomNavRoute = Routes.BUYER_ORDERS,
            onBottomNavigate = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun OrderHistoryScreenEmptyPreview() {
    SmartgreenhousemobileTheme {
        OrderHistoryScreen(
            orders = emptyList(),
            selectedTab = OrderHistoryTab.SELESAI,
            onTabSelected = {},
            onOrderClick = {},
            onBackClick = {},
            currentBottomNavRoute = Routes.BUYER_ORDERS,
            onBottomNavigate = {}
        )
    }
}
