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
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.CloudinaryRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
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
 * TODO (MOB-T13): [CreateListingFormState.healthScore]/`productName` saat masuk dari tombol "+"
 * navbar biasa masih data sampel ("Cabai Rawit Merah", 87.0) — ganti dengan hasil
 * `POST /listings/auto-fill-health-score` (`docs/data-contracts.md §4.6`) begitu
 * BackendRepository/plot aktif petani dikerjakan (di luar lingkup Firestore, backend REST
 * terpisah). `farm_id`/`plot_id`/`crop_type` SUDAH sungguhan (lihat [FirestoreRepository
 * .getFarmAndPlotForOwner] di bawah).
 */
@Composable
fun CreateListingRoute(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    initialFormState: CreateListingFormState? = null,
    cloudinaryRepository: CloudinaryRepository = remember { CloudinaryRepository() },
    authRepository: AuthRepository = remember { AuthRepository() },
    firestoreRepository: FirestoreRepository = remember { FirestoreRepository() }
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
            val quantityKg = formState.quantityKg.toDoubleOrNull()
            val pricePerKg = formState.pricePerKg.toLongOrNull()
            if (quantityKg == null || pricePerKg == null) {
                publishState = ListingPublishState.Error(R.string.create_listing_error_invalid_form)
                return@CreateListingScreen
            }
            val uid = authRepository.currentUser()?.uid
            if (uid == null) {
                publishState = ListingPublishState.Error(R.string.auth_error_generic)
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
                    return@launch
                }

                firestoreRepository.getFarmAndPlotForOwner(uid)
                    .mapCatching { (farm, plot) ->
                        firestoreRepository.createListing(
                            farmId = farm.id,
                            plotId = plot.id,
                            cropType = plot.cropType,
                            productName = formState.productName,
                            quantityKg = quantityKg,
                            pricePerKg = pricePerKg,
                            healthScore = formState.healthScore,
                            description = formState.description,
                            preOrderEnabled = formState.preOrderEnabled,
                            imageUrls = uploadedImageUrls
                        ).getOrThrow()
                    }
                    .onSuccess {
                        publishState = ListingPublishState.Idle
                        onPublishClick()
                    }
                    .onFailure {
                        publishState = ListingPublishState.Error(R.string.create_listing_error_save_failed)
                    }
            }
        }
    )
}
