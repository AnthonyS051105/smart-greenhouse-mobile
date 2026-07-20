package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.viewmodel.CheckoutSubmitState
import com.teti2026.smartgreenhouse.viewmodel.CheckoutUiState
import com.teti2026.smartgreenhouse.viewmodel.CheckoutViewModel
import kotlin.math.roundToInt

@Composable
fun CheckoutRoute(
    listingId: String,
    onBackClick: () -> Unit,
    onOrderConfirmed: (orderId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    LaunchedEffect(listingId) {
        viewModel.load(listingId)
    }

    when (val s = state) {
        is CheckoutUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is CheckoutUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(listingId) },
            modifier = modifier
        )
        is CheckoutUiState.Success -> {
            val listing = s.listing
            var quantity by remember(listingId) { mutableStateOf(1) }
            var deliveryMethod by remember(listingId) { mutableStateOf(DeliveryMethod.PICKUP) }
            var address by remember(listingId) { mutableStateOf("") }

            CheckoutScreen(
                listing = listing,
                quantity = quantity,
                onIncreaseQuantity = {
                    val max = listing.quantityAvailableKg.roundToInt()
                    quantity = (quantity + 1).coerceAtMost(max)
                },
                onDecreaseQuantity = {
                    quantity = (quantity - 1).coerceAtLeast(1)
                },
                deliveryMethod = deliveryMethod,
                onDeliveryMethodChange = { deliveryMethod = it },
                address = address,
                onAddressChange = { address = it },
                onBackClick = onBackClick,
                onConfirmClick = {
                    // Perhitungan total SAMA PERSIS dengan yang ditampilkan CheckoutSummarySection
                    // (CheckoutScreen.kt) — memakai konstanta biaya yang sama, bukan dihitung ulang
                    // terpisah dengan kemungkinan hasil berbeda.
                    val subtotal = listing.pricePerKg * quantity
                    val shippingCost = if (deliveryMethod == DeliveryMethod.DELIVERY) {
                        CHECKOUT_SHIPPING_COST_RUPIAH
                    } else {
                        0L
                    }
                    val total = subtotal + CHECKOUT_SERVICE_FEE_RUPIAH + shippingCost
                    viewModel.submitOrder(quantity.toDouble(), total, onOrderConfirmed)
                },
                isSubmitting = submitState is CheckoutSubmitState.Submitting,
                submitErrorMessage = (submitState as? CheckoutSubmitState.Error)?.let { stringResource(it.messageResId) },
                modifier = modifier
            )
        }
    }
}
