package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

// TODO: pindahkan lookup listing & state ke CheckoutViewModel (StateFlow<UiState<...>>) yang
// memanggil FirestoreRepository.createOrder(order) mengikuti skema `orders` (docs/data-contracts.md
// §3.8) begitu MOB-T21 dikerjakan (lihat docs/SDD.md §4.2/§5). Saat ini quantity/deliveryMethod/
// address murni state lokal, konfirmasi belum benar-benar membuat order di Firestore.
@Composable
fun CheckoutRoute(
    listingId: String,
    onBackClick: () -> Unit,
    onOrderConfirmed: (listingId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Fallback ke listing pertama bila id tidak dikenal (belum ada data nyata) — sama seperti
    // pola di ListingDetailRoute.
    val listing = sampleListingDetails[listingId] ?: sampleListingDetails.values.first()

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
        onConfirmClick = { onOrderConfirmed(listing.id) },
        modifier = modifier
    )
}
