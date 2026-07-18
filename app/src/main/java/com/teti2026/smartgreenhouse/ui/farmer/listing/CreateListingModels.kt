package com.teti2026.smartgreenhouse.ui.farmer.listing

import android.net.Uri

/**
 * Nilai form layar "Buat Listing - Petani". [photoUris] dipilih dari galeri lokal (belum
 * di-upload ke Cloudinary — MOB-T26 belum dikerjakan, lihat TODO di [CreateListingRoute]).
 * [healthScore] & [productName] bersifat read-only, diisi otomatis dari hasil AI/plot aktif
 * (data sampel untuk sekarang, lihat TODO di [CreateListingRoute]).
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
