package com.teti2026.smartgreenhouse.ui.farmer.listing

import android.net.Uri
import androidx.annotation.StringRes

/**
 * Nilai form layar "Buat Listing - Petani". [photoUris] dipilih dari galeri lokal, di-upload ke
 * Cloudinary saat "Publikasikan" ditekan (lihat [CreateListingRoute]). [healthScore] &
 * [productName] bersifat read-only, diisi otomatis dari hasil AI/plot aktif (data sampel untuk
 * sekarang — TODO MOB-T13, lihat TODO di [CreateListingRoute]).
 */
data class CreateListingFormState(
    val productName: String,
    val healthScore: Double,
    val photoUris: List<Uri> = emptyList(),
    val pricePerKg: String = "",
    val quantityKg: String = "",
    val description: String = "",
    val preOrderEnabled: Boolean = false
)

/**
 * Status upload foto ke Cloudinary saat tombol "Publikasikan" ditekan. Sesuai pola `UiState` di
 * `docs/SDD.md §5` ([com.teti2026.smartgreenhouse.viewmodel.AuthUiState] jadi acuan penamaan).
 */
sealed interface ListingPublishState {
    data object Idle : ListingPublishState
    data object Uploading : ListingPublishState
    data class Error(@param:StringRes val messageResId: Int) : ListingPublishState
}
