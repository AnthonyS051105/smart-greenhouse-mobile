package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// TODO: pindahkan state & submit ke ReviewViewModel (StateFlow<UiState<...>>) yang memanggil
// FirestoreRepository.createReview(review) mengikuti skema `reviews` (docs/data-contracts.md
// §3.10) begitu MOB-T23 dikerjakan (lihat docs/SDD.md §4.2/§5). rating/comment saat ini state
// lokal murni — submit belum benar-benar menyimpan review ke Firestore.
@Composable
fun ReviewRoute(
    orderId: String,
    onBackClick: () -> Unit = {},
    onReviewSubmitted: (orderId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Fallback ke order pertama bila id tidak dikenal (belum ada data nyata) — sama seperti
    // pola di CheckoutRoute.
    val order = sampleOrderHistory.firstOrNull { it.id == orderId } ?: sampleOrderHistory.first()
    val listing = sampleListingDetails[order.listingId]
    val target = reviewTargetFrom(order, listing)

    var rating by remember(orderId) { mutableIntStateOf(0) }
    var comment by remember(orderId) { mutableStateOf("") }

    ReviewScreen(
        target = target,
        rating = rating,
        onRatingChange = { rating = it },
        comment = comment,
        onCommentChange = { comment = it },
        onBackClick = onBackClick,
        onSubmitClick = { onReviewSubmitted(order.id) },
        modifier = modifier
    )
}
