package com.teti2026.smartgreenhouse.ui.farmer.history

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.ImageHistoryUiState
import com.teti2026.smartgreenhouse.viewmodel.ImageHistoryViewModel

/**
 * Route "Riwayat Citra - Petani": state filter & [selectedImageId] di-hoist di sini (pola sama
 * seperti [com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute]), screen tetap stateless.
 * Data ([CropImageHistoryItem]/[ImageAnalysisDetail]) berasal dari [ImageHistoryViewModel] —
 * dokumen `crop_images` sungguhan milik plot petani ini (`FirestoreRepository.getCropImages`,
 * ditulis lewat "Simpan ke Riwayat Citra" di `ScanPlantRoute`), BUKAN lagi [sampleCropImageHistoryItems]
 * statis (sebelumnya root cause bug "gambar tersimpan tapi tidak muncul di Riwayat Citra" — layar
 * ini tidak pernah membaca Firestore sama sekali).
 *
 * [selectedImageId] (bukan destination NavHost terpisah) yang mengontrol tampil/sembunyinya
 * [ImageAnalysisDetailRoute] sebagai `ModalBottomSheet` DI ATAS grid ini pada composable yang
 * SAMA — grid tetap ada di composition sebagai layar di belakang sheet, sehingga scrim
 * menampilkan konten Riwayat asli (bukan layar kosong seperti saat detail jadi destination
 * NavHost terpisah — pendekatan awal yang menyebabkan window Dialog kosong bertumpuk, karena
 * `ModalBottomSheet` M3 sudah membuat `Dialog`-nya sendiri).
 *
 * TODO: filter "Minggu Ini" idealnya menyaring berdasarkan timestamp asli 7 hari terakhir —
 * [CropImageHistoryItem] cuma punya [CropImageHistoryItem.timestampLabel] (String tampilan, sudah
 * diformat), bukan epoch — untuk sekarang tetap tampilkan semua (sama seperti perilaku sebelumnya).
 *
 * [ImageHistoryViewModel] hanya `load()` sekali di `init` — `onSaveToHistoryClick` di
 * `ScanPlantRoute` cuma `popBackStack()` (bukan destination baru), jadi instance ViewModel yang
 * SAMA dipakai lagi dengan `state` lama (list masih kosong walau `crop_images` baru saja berhasil
 * ditulis). `ON_RESUME` listener di bawah memanggil `viewModel.load()` ulang setiap kali layar ini
 * kembali terlihat, supaya foto yang baru disimpan langsung muncul tanpa perlu keluar-masuk app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageHistoryRoute(
    onCreateListingFromImage: (ImageAnalysisDetail) -> Unit,
    onCreateListingClick: () -> Unit,
    onBackClick: () -> Unit,
    onBottomNavigate: (String) -> Unit,
    viewModel: ImageHistoryViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf(ImageHistoryFilter.ALL) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel by rememberUpdatedState(viewModel)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentViewModel.load()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is ImageHistoryUiState.Loading -> ProfileLoadingIndicator()
        is ImageHistoryUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load
        )
        is ImageHistoryUiState.Success -> {
            val filteredItems = when (selectedFilter) {
                ImageHistoryFilter.ALL -> s.items
                ImageHistoryFilter.HEALTHY -> s.items.filter { it.category == ImageHealthCategory.GOOD }
                ImageHistoryFilter.SICK -> s.items.filter { it.category == ImageHealthCategory.SICK }
                ImageHistoryFilter.THIS_WEEK -> s.items
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

            val detail = selectedImageId?.let { s.details[it] }
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
    }
}
