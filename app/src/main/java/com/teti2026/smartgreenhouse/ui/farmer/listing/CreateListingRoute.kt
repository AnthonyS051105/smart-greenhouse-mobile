package com.teti2026.smartgreenhouse.ui.farmer.listing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.CloudinaryRepository
import kotlinx.coroutines.launch

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
 * (`docs/data-contracts.md §4.6`) begitu BackendRepository/plot aktif petani dikerjakan. URL hasil
 * upload Cloudinary di bawah ini juga belum disimpan kemana pun — begitu FirestoreRepository
 * .createListing dikerjakan, teruskan `uploadedImageUrls` sebagai field foto `Listing`.
 */
@Composable
fun CreateListingRoute(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    initialFormState: CreateListingFormState? = null,
    cloudinaryRepository: CloudinaryRepository = remember { CloudinaryRepository() }
) {
    var formState by remember {
        mutableStateOf(
            initialFormState ?: CreateListingFormState(
                productName = "Cabai Rawit Merah",
                healthScore = 87.0
            )
        )
    }
    var publishState by remember { mutableStateOf<ListingPublishState>(ListingPublishState.Idle) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val publishErrorMessage = (publishState as? ListingPublishState.Error)?.let {
        stringResource(it.messageResId)
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
        isPublishing = publishState is ListingPublishState.Uploading,
        publishErrorMessage = publishErrorMessage,
        onPublishClick = {
            if (formState.photoUris.isEmpty()) {
                publishState = ListingPublishState.Error(R.string.create_listing_error_no_photo)
                return@CreateListingScreen
            }
            publishState = ListingPublishState.Uploading
            coroutineScope.launch {
                val uploadedImageUrls = mutableListOf<String>()
                var uploadFailed = false
                // Upload berurutan (bukan paralel) — cukup untuk maks. 3 foto (MAX_PHOTOS di
                // CreateListingScreen.kt) & lebih sederhana untuk melaporkan kegagalan per-file.
                for (uri in formState.photoUris) {
                    cloudinaryRepository.uploadImage(context, uri)
                        .onSuccess { url -> uploadedImageUrls += url }
                        .onFailure { uploadFailed = true }
                    if (uploadFailed) break
                }
                if (uploadFailed) {
                    publishState = ListingPublishState.Error(R.string.create_listing_error_upload_failed)
                } else {
                    publishState = ListingPublishState.Idle
                    onPublishClick()
                }
            }
        }
    )
}
