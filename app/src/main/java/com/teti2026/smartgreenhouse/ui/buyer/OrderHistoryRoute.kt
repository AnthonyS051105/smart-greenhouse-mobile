package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan state & data ke OrderHistoryViewModel (StateFlow<UiState<List<Order>>>) yang
// mengambil data dari FirestoreRepository.getOrders(buyerUid) begitu MOB-T22 dikerjakan.
@Composable
fun OrderHistoryRoute(
    onBackClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(OrderHistoryTab.BERLANGSUNG) }

    OrderHistoryScreen(
        orders = sampleOrderHistory,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onOrderClick = { /* TODO: navigasi ke Detail Pesanan saat screen tersebut dibuat */ },
        onBackClick = onBackClick,
        currentBottomNavRoute = Routes.BUYER_ORDERS,
        onBottomNavigate = onBottomNavigate
    )
}
