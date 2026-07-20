package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.viewmodel.ListingDetailUiState
import com.teti2026.smartgreenhouse.viewmodel.ListingDetailViewModel

// TODO: onChatClick/onBuyClick/onVisitStoreClick/onShareClick — Chat/Checkout sudah punya
// destination (lihat NavGraph.kt), onVisitStoreClick menunggu screen Profil Petani versi publik
// dibuat (belum ada di Stitch maupun kode).
@Composable
fun ListingDetailRoute(
    listingId: String,
    onBackClick: () -> Unit,
    onChatClick: (listingId: String) -> Unit = {},
    onBuyClick: (listingId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ListingDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isFavorite by remember(listingId) { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        viewModel.load(listingId)
    }

    when (val s = state) {
        is ListingDetailUiState.Loading -> ProfileLoadingIndicator(modifier = modifier)
        is ListingDetailUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(listingId) },
            modifier = modifier
        )
        is ListingDetailUiState.Success -> {
            ListingDetailScreen(
                listing = s.listing,
                isFavorite = isFavorite,
                onFavoriteClick = { isFavorite = !isFavorite },
                onBackClick = onBackClick,
                onShareClick = { /* TODO: share intent produk */ },
                onChatClick = { onChatClick(s.listing.id) },
                onBuyClick = { onBuyClick(s.listing.id) },
                onVisitStoreClick = { /* TODO: navigasi ke Profil Petani saat screen tersebut dibuat */ },
                modifier = modifier
            )
        }
    }
}
