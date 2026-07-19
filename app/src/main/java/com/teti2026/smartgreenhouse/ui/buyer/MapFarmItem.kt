package com.teti2026.smartgreenhouse.ui.buyer

import com.google.android.gms.maps.model.LatLng

/**
 * Model presentasi marker + kartu kebun di Peta Marketplace — gabungan `farms` + `listings`
 * (lihat `docs/data-contracts.md §3.2/§3.7`). Data statis sementara (lihat [sampleNearbyFarms]);
 * akan diganti hasil query `FirestoreRepository.getFarmsForMap()` begitu MOB-T19 dikerjakan
 * (lihat `docs/SDD.md §4.2`).
 *
 * [locationLabel] — nama kota/kecamatan untuk header screen "Produk Lahan" (mis. "Boyolali"),
 * dipasangkan dengan [distanceLabel] (format "Boyolali • 1.2 km" sesuai mockup Stitch).
 *
 * Tap kartu/marker kebun sekarang menuju screen "Produk Lahan" (lihat `FarmProductsMapScreen`)
 * yang menampilkan SELURUH listing farm ini (via `listingsForFarm(farm.id)` di
 * `ListingDetailItem.kt`, filter `ListingDetailItem.farmId`) — bukan lagi satu listing utama
 * (`primaryListingId` lama dihapus, superseded).
 */
data class MapFarmItem(
    val id: String,
    val farmName: String,
    val locationLabel: String,
    val position: LatLng,
    val distanceLabel: String,
    val rating: Double?,
    val imageUrl: String?,
    val imageContentDescription: String?,
    val cropTags: List<String>
)

val sampleNearbyFarms = listOf(
    MapFarmItem(
        id = "farm-pak-budi",
        farmName = "Kebun Pak Budi",
        locationLabel = "Boyolali",
        position = LatLng(-7.7956, 110.3695),
        distanceLabel = "1.2 km",
        rating = 4.8,
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC5WMa_uwzeuUgxZbGmjwzEVdQdAY4VK65eZinSSN7mBvUal7eLGOdFCICBWxDmJy6SfKCuSNU58B6A6FPEmSj8UZHuhon2HunRmb6eSy2ma-pmoVAs5orI3T3wh2pzeZtksT9hPeGDEGXG47w7MR0KEOR2notg4_akJY3MImH1sCSlVCFLHJruKBGJpf10LALrjCSUBYGJt8mQtQhwGE5NSS3giD-h_gz411a-tpwl_CQ3wvsknJyajbHZOvCQyhrugba8Iwgh9w",
        imageContentDescription = "Kebun sayur hidroponik dengan selada dan bayam segar di dalam greenhouse",
        cropTags = listOf("Selada", "Tomat", "+2")
    ),
    MapFarmItem(
        id = "farm-agro-sejahtera",
        farmName = "Agro Farm Sejahtera",
        locationLabel = "Sleman",
        position = LatLng(-7.8014, 110.3644),
        distanceLabel = "2.5 km",
        rating = 4.5,
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBE-ApToVeyiW4PJfO8c2v0Qes3wHbIkCbS6h67SPb4P3v8i8SGZHakcjXfd3t00pVphShiYA7jRZOoFkUsSq_XJ1gUoMPfp-8uKEVrPlx1-zRPzng3gRhFh3i-8hw2HTgzbLfx6MbHtt6KMLHOztS6HNi3wC8HwpPm-AZGpMKYcdESGhCM_7CSXbAjsWKd76xC5z02Pb1UMKe_F3-nF1PusONbuQwhLdsIwDm6i7uYvsGKNUKgluqhMLL_vEICkDTptrgiEW1UGw",
        imageContentDescription = "Barisan tanaman tomat matang siap panen",
        cropTags = listOf("Cabai", "Bawang")
    ),
    MapFarmItem(
        id = "farm-hidroponik-lestari",
        farmName = "Kebun Hidroponik Lestari",
        locationLabel = "Bantul",
        position = LatLng(-7.8100, 110.3550),
        distanceLabel = "3.1 km",
        rating = null,
        imageUrl = null,
        imageContentDescription = null,
        cropTags = listOf("Pakcoy", "Kangkung")
    )
)
