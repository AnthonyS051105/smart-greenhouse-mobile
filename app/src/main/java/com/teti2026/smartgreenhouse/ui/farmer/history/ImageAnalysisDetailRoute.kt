package com.teti2026.smartgreenhouse.ui.farmer.history

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Route "Detail Analisis Citra - AgriSmart". Tidak ada state form (layar murni menampilkan
 * [detail] yang di-resolve dari [imageId] oleh caller, lihat `NavGraph.kt`) — pola sama seperti
 * [com.teti2026.smartgreenhouse.ui.buyer.ListingDetailRoute] yang resolve id via sample data.
 * [sheetState] & [scope] mengelola animasi collapse [ModalBottomSheet] SEBELUM
 * `NavController.popBackStack()` dipanggil di [onCloseClick]/[onDismiss] — tanpa ini, sheet akan
 * hilang instan tanpa animasi turun saat back stack di-pop lebih dulu.
 *
 * TODO (MOB-T09/T10): [detail] statis dari [sampleImageAnalysisDetails] — ganti dengan hasil
 * FirestoreRepository.getCropImage(imageId) via ViewModel begitu dikerjakan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageAnalysisDetailRoute(
    detail: ImageAnalysisDetail,
    onCreateListingClick: (ImageAnalysisDetail) -> Unit,
    onCloseClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    fun dismissThenNavigate(action: () -> Unit) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
            }
        }
    }

    ImageAnalysisDetailScreen(
        detail = detail,
        sheetState = sheetState,
        onCreateListingClick = { dismissThenNavigate { onCreateListingClick(detail) } },
        onDismiss = { dismissThenNavigate(onCloseClick) }
    )
}
