package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wrapper stateful "Konfirmasi Pesanan - Berhasil". Dijangkau dari `onOrderConfirmed` di
 * Checkout (lihat `NavGraph.kt`). [listingId] dipakai untuk menampilkan nama petani tujuan
 * pesanan — sama seperti pola fallback di [CheckoutRoute]/[ListingDetailRoute].
 */
@Composable
fun OrderSuccessRoute(
    listingId: String,
    onViewHistoryClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listing = sampleListingDetails[listingId] ?: sampleListingDetails.values.first()

    OrderSuccessScreen(
        item = orderSuccessItemFrom(listing),
        onViewHistoryClick = onViewHistoryClick,
        onBackToHomeClick = onBackToHomeClick,
        modifier = modifier
    )
}
