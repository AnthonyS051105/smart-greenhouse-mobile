package com.teti2026.smartgreenhouse.ui.farmer.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryTab
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.viewmodel.FarmerOrdersUiState
import com.teti2026.smartgreenhouse.viewmodel.FarmerOrdersViewModel

/**
 * Wrapper stateful "Pesanan Masuk - Petani". Dijangkau dari menu Profil Petani (di-push, bukan
 * tab bottom-nav) — [currentBottomNavRoute] sengaja `""` (tidak ada tab tersorot), pola sama
 * seperti [com.teti2026.smartgreenhouse.ui.farmer.NotificationFarmerRoute].
 */
@Composable
fun FarmerOrdersRoute(
    onBackClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    viewModel: FarmerOrdersViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(OrderHistoryTab.BERLANGSUNG) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is FarmerOrdersUiState.Loading -> ProfileLoadingIndicator()
        is FarmerOrdersUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load
        )
        is FarmerOrdersUiState.Success -> {
            FarmerOrdersScreen(
                orders = s.orders,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onStatusChange = viewModel::updateStatus,
                onBackClick = onBackClick,
                currentBottomNavRoute = "",
                onBottomNavigate = onBottomNavigate
            )
        }
    }
}
