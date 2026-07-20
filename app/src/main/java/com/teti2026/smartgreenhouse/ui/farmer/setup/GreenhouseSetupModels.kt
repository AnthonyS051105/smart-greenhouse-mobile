package com.teti2026.smartgreenhouse.ui.farmer.setup

import org.maplibre.spatialk.geojson.Position

/** Opsi jenis tanaman pada dropdown "Data Utama", sesuai desain Stitch. `value` selaras `crop_type`. */
enum class CropTypeOption(val value: String) {
    CABAI_RAWIT("cabai"),
    TOMAT("tomat"),
    SELADA("selada"),
    PAPRIKA("paprika")
}

/**
 * Data form 3-langkah Setup Greenhouse (Data Utama → Lokasi Lahan → Pairing Perangkat),
 * di-hoist di [GreenhouseSetupRoute] lalu dipetakan ke [com.teti2026.smartgreenhouse.data.model.Farm]
 * dan [com.teti2026.smartgreenhouse.data.model.Plot] saat submit (lihat `shared/data-contracts.md §3.2/§3.3`).
 */
data class GreenhouseSetupFormState(
    val greenhouseName: String = "",
    val sizeM2: String = "",
    val cropType: CropTypeOption = CropTypeOption.CABAI_RAWIT,
    val location: Position? = null,
    val locationLabel: String = "",
    val pairingCode: String = ""
)
