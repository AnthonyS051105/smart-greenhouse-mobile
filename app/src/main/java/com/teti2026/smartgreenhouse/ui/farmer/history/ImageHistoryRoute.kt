package com.teti2026.smartgreenhouse.ui.farmer.history

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.teti2026.smartgreenhouse.ui.navigation.Routes

/**
 * Route "Riwayat Citra - Petani": state filter & [selectedImageId] di-hoist di sini (pola sama
 * seperti [com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute]), screen tetap stateless.
 *
 * [selectedImageId] (bukan destination NavHost terpisah) yang mengontrol tampil/sembunyinya
 * [ImageAnalysisDetailRoute] sebagai `ModalBottomSheet` DI ATAS grid ini pada composable yang
 * SAMA — grid tetap ada di composition sebagai layar di belakang sheet, sehingga scrim
 * menampilkan konten Riwayat asli (bukan layar kosong seperti saat detail jadi destination
 * NavHost terpisah — pendekatan awal yang menyebabkan window Dialog kosong bertumpuk, karena
 * `ModalBottomSheet` M3 sudah membuat `Dialog`-nya sendiri).
 *
 * TODO (MOB-T09/T10): [sampleCropImageHistoryItems] statis — ganti dengan hasil
 * FirestoreRepository.getCropImages(plotId) via ImageHistoryViewModel begitu dikerjakan.
 * Filter "Sehat"/"Sakit"/"Minggu Ini" sudah difungsikan di client (data sampel kecil), nanti
 * bisa dipindah jadi query Firestore langsung bila data besar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageHistoryRoute(
    onCreateListingFromImage: (ImageAnalysisDetail) -> Unit,
    onCreateListingClick: () -> Unit,
    onBackClick: () -> Unit,
    onBottomNavigate: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ImageHistoryFilter.ALL) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }

    val filteredItems = when (selectedFilter) {
        ImageHistoryFilter.ALL -> sampleCropImageHistoryItems
        ImageHistoryFilter.HEALTHY -> sampleCropImageHistoryItems.filter {
            it.category == ImageHealthCategory.GOOD
        }
        ImageHistoryFilter.SICK -> sampleCropImageHistoryItems.filter {
            it.category == ImageHealthCategory.SICK
        }
        // TODO: "Minggu Ini" idealnya menyaring berdasarkan timestamp asli 7 hari terakhir —
        // data sampel belum punya Instant/Long, jadi untuk sekarang tampilkan semua.
        ImageHistoryFilter.THIS_WEEK -> sampleCropImageHistoryItems
    }

    ImageHistoryScreen(
        items = filteredItems,
        selectedFilter = selectedFilter,
        onFilterSelected = { selectedFilter = it },
        onItemClick = { imageId -> selectedImageId = imageId },
        onCreateListingClick = onCreateListingClick,
        onBackClick = onBackClick,
        currentBottomNavRoute = Routes.FARMER_IMAGE_HISTORY,
        onBottomNavigate = onBottomNavigate
    )

    val detail = selectedImageId?.let { sampleImageAnalysisDetails[it] }
    if (detail != null) {
        ImageAnalysisDetailRoute(
            detail = detail,
            onCreateListingClick = { selected ->
                selectedImageId = null
                onCreateListingFromImage(selected)
            },
            onCloseClick = { selectedImageId = null }
        )
    }
}
