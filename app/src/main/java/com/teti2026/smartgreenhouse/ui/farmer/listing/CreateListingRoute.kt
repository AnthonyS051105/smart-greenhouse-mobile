package com.teti2026.smartgreenhouse.ui.farmer.listing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Route "Buat Listing - Petani": state di-hoist di sini (pola sama seperti
 * [com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute]), screen tetap stateless.
 *
 * [initialFormState] memungkinkan caller mengisi form dari data lain — dipakai saat masuk dari
 * tombol "Buat Listing dari Data Ini" di Detail Analisis Citra (lihat `NavGraph.kt`), yang
 * membawa foto, deskripsi (catatan AI), & skor kesehatan citra terpilih. Default `null` berarti
 * masuk dari tombol "+" navbar biasa (pola lama, data sampel statis).
 *
 * TODO (MOB-T13): [productName] & [CreateListingFormState.healthScore] saat ini data sampel
 * dari plot aktif — ganti dengan hasil `POST /listings/auto-fill-health-score`
 * (`docs/data-contracts.md §4.6`) begitu BackendRepository/plot aktif petani dikerjakan.
 * TODO (MOB-T26): [photoUris] hanya preview lokal (galeri perangkat) — belum di-upload ke
 * Cloudinary. Saat MOB-T26 dikerjakan, upload tiap [Uri] via CloudinaryRepository.uploadImage
 * sebelum [onPublishClick] menyimpan `Listing` ke Firestore (FirestoreRepository.createListing).
 */
@Composable
fun CreateListingRoute(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    initialFormState: CreateListingFormState? = null
) {
    var formState by remember {
        mutableStateOf(
            initialFormState ?: CreateListingFormState(
                productName = "Cabai Rawit Merah",
                healthScore = 87.0
            )
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3 - formState.photoUris.size),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                formState = formState.copy(photoUris = (formState.photoUris + uris).take(3))
            }
        }
    )

    CreateListingScreen(
        productName = formState.productName,
        healthScore = formState.healthScore,
        photoUris = formState.photoUris,
        onAddPhotoClick = {
            photoPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
        onRemovePhoto = { uri ->
            formState = formState.copy(photoUris = formState.photoUris - uri)
        },
        pricePerKg = formState.pricePerKg,
        onPricePerKgChange = { formState = formState.copy(pricePerKg = it) },
        quantityKg = formState.quantityKg,
        onQuantityKgChange = { formState = formState.copy(quantityKg = it) },
        description = formState.description,
        onDescriptionChange = { formState = formState.copy(description = it) },
        preOrderEnabled = formState.preOrderEnabled,
        onPreOrderToggle = { formState = formState.copy(preOrderEnabled = it) },
        onBackClick = onBackClick,
        onPublishClick = onPublishClick
    )
}
