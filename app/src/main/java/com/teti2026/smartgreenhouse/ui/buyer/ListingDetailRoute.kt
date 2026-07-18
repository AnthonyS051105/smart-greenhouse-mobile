package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// TODO: pindahkan lookup listing ke ListingDetailViewModel (StateFlow<UiState<ListingDetailItem>>)
// yang mengambil data gabungan Firestore listings+farms+users+sensor_readings begitu MOB-T18
// dikerjakan (lihat docs/SDD.md §4.2/§5). onChatClick/onBuyClick/onVisitStoreClick/onShareClick
// menunggu screen Chat/Checkout/Profil Petani dibuat (lihat TODO di NavGraph.kt).
@Composable
fun ListingDetailRoute(
    listingId: String,
    onBackClick: () -> Unit,
    onChatClick: (listingId: String) -> Unit = {},
    onBuyClick: (listingId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Fallback ke listing pertama bila id tidak dikenal (belum ada data nyata) — sekadar
    // menghindari crash; implementasi nyata memakai UiState.Empty/Error, bukan fallback diam-diam.
    val listing = sampleListingDetails[listingId] ?: sampleListingDetails.values.first()
    var isFavorite by remember(listingId) { mutableStateOf(false) }

    ListingDetailScreen(
        listing = listing,
        isFavorite = isFavorite,
        onFavoriteClick = { isFavorite = !isFavorite },
        onBackClick = onBackClick,
        onShareClick = { /* TODO: share intent produk */ },
        onChatClick = { onChatClick(listing.id) },
        onBuyClick = { onBuyClick(listing.id) },
        onVisitStoreClick = { /* TODO: navigasi ke Profil Petani saat screen tersebut dibuat */ },
        modifier = modifier
    )
}
