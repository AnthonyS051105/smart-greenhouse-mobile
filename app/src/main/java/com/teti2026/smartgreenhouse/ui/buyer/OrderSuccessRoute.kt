package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.viewmodel.OrderSuccessUiState
import com.teti2026.smartgreenhouse.viewmodel.OrderSuccessViewModel

/**
 * Wrapper stateful "Konfirmasi Pesanan - Berhasil". Dijangkau dari `onOrderConfirmed` di
 * Checkout (lihat `NavGraph.kt`) dengan [orderId] = id dokumen `orders` NYATA (hasil
 * `CheckoutViewModel.submitOrder`) — bukan lagi `listingId` seperti sebelum sesi ini.
 */
@Composable
fun OrderSuccessRoute(
    orderId: String,
    onViewHistoryClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderSuccessViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    when (val s = state) {
        is OrderSuccessUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is OrderSuccessUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(orderId) },
            modifier = modifier
        )
        is OrderSuccessUiState.Success -> {
            OrderSuccessScreen(
                item = s.item,
                onViewHistoryClick = onViewHistoryClick,
                onBackToHomeClick = onBackToHomeClick,
                modifier = modifier
            )
        }
    }
}
