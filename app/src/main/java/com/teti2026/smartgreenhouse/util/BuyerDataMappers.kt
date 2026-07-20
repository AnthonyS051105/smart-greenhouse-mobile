package com.teti2026.smartgreenhouse.util

import com.teti2026.smartgreenhouse.data.model.Farm
import com.teti2026.smartgreenhouse.data.model.Listing
import com.teti2026.smartgreenhouse.data.model.User
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailItem
import com.teti2026.smartgreenhouse.ui.buyer.MapFarmItem
import org.maplibre.spatialk.geojson.Position

/**
 * Gabungan [Listing] + [Farm] (pemilik) + [User] (penjual) menjadi [ListingDetailItem] — dipakai
 * bersama oleh `ListingDetailViewModel` (Detail Produk) dan `FarmProductsMapViewModel` (Produk
 * Lahan - Peta) supaya logika mapping tidak diduplikasi. [ListingDetailItem.sensorHistory] SELALU
 * kosong & [ListingDetailItem.sellerRatingLabel] SELALU null — belum ada `sensor_readings`/
 * agregasi `reviews` sungguhan (keputusan sama seperti `ListingDetailViewModel` sebelumnya).
 * [ListingDetailItem.minOrderKg] didefaultkan 1 kg — form Buat Listing tidak punya input untuk ini.
 */
fun Listing.toListingDetailItem(farm: Farm?, seller: User?): ListingDetailItem = ListingDetailItem(
    id = id,
    farmId = farmId,
    cropName = productName.ifBlank { cropType },
    locationLabel = farm?.farmName.orEmpty(),
    harvestLabel = harvestDate,
    imageUrls = imageUrls,
    imageContentDescription = productName,
    healthScore = healthScore,
    pricePerKg = pricePerKg,
    pricePerKgLabel = formatRupiah(pricePerKg),
    quantityAvailableKg = quantityKg,
    quantityAvailableLabel = "${quantityKg.toInt()} Kg",
    minOrderKg = 1.0,
    minOrderLabel = "1 Kg",
    unitLabel = "Kg",
    description = description,
    sensorHistoryStatusLabel = "Belum ada data sensor",
    sensorHistory = emptyList(),
    sellerName = seller?.name.orEmpty(),
    sellerAvatarUrl = null,
    sellerRatingLabel = null,
    sellerActivityLabel = ""
)

/**
 * Gabungan [Farm] + listing-listing aktif miliknya menjadi [MapFarmItem] — dipakai bersama oleh
 * `MapViewModel` (Peta Marketplace) & `FarmProductsMapViewModel` (Produk Lahan) supaya logika
 * mapping tidak diduplikasi. [MapFarmItem.rating] SELALU null (belum ada agregasi `reviews`
 * sungguhan). [MapFarmItem.distanceLabel] SELALU null di sini — jarak ke pembeli bergantung
 * lokasi PERANGKAT saat ini, bukan data Firestore, jadi dihitung & disisipkan terpisah di layer UI
 * (lihat `distanceLabelFrom` di `MapScreen.kt`). [MapFarmItem.locationLabel] memakai koordinat
 * yang dibulatkan sebagai placeholder — reverse geocoding belum diimplementasikan di app ini
 * manapun (TODO sama persis di `GreenhouseSetupRoutes.onLocationChanged`, MOB-T04).
 */
fun Farm.toMapFarmItem(listings: List<Listing>): MapFarmItem = MapFarmItem(
    id = id,
    farmName = farmName,
    locationLabel = "%.4f, %.4f".format(locationLat, locationLng),
    position = Position(latitude = locationLat, longitude = locationLng),
    distanceLabel = null,
    rating = null,
    imageUrl = listings.firstNotNullOfOrNull { it.imageUrls.firstOrNull() },
    imageContentDescription = farmName,
    cropTags = buildFarmCropTags(listings)
)

/** Maks. 2 nama produk ditampilkan sebagai tag, sisanya diringkas "+N" (mis. ["Selada","Tomat","+2"]). */
private fun buildFarmCropTags(listings: List<Listing>): List<String> {
    val cropNames = listings.map { it.productName.ifBlank { it.cropType } }.distinct()
    val visible = cropNames.take(2)
    val remaining = cropNames.size - visible.size
    return if (remaining > 0) visible + "+$remaining" else visible
}
