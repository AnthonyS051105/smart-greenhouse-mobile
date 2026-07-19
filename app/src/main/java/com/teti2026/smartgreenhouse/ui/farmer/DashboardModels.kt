package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Model presentasi kartu sensor Dashboard Petani — satu kartu per field `sensor_readings`
 * terbaru (lihat `docs/data-contracts.md §3.4`: temperature/humidity/soil_moisture/light_intensity).
 * Sementara data statis (lihat [sampleSensorItems]); akan diganti hasil query Firestore
 * oleh DashboardViewModel + FirestoreRepository.getSensorReadings (MOB-T09).
 */
data class DashboardSensorItem(
    val labelRes: Int,
    val valueText: String,
    val unitText: String,
    val icon: ImageVector,
    /** 0f..1f, posisi nilai pada rentang normal sensor — dipakai bar indikator kartu. */
    val levelFraction: Float
)

/** Satu titik pada strip tanggal "Jadwal Panen"; [isToday]/[isHarvestDay] mengatur aksen visual. */
data class HarvestDay(
    val dayNumber: Int,
    val monthLabel: String,
    val isToday: Boolean = false,
    val isHarvestDay: Boolean = false
)

/** Status satu aktuator (irigasi/ventilasi) — padanan `irrigation_state`/`ventilation_state`
 * pada topik MQTT `greenhouse/{device_id}/status` (`docs/data-contracts.md §1.3`). */
data class ActuatorStatusItem(
    val labelRes: Int,
    val isOn: Boolean,
    val statusDetailText: String,
    val icon: ImageVector
)
