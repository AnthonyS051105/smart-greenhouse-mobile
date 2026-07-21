package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain `sensor_readings` sesuai `docs/data-contracts.md §3.4`. Nama field mengikuti
 * dokumen tersebut persis. [timestampMillis] adalah epoch millis dari Firestore `Timestamp`
 * (backend menulis via `datetime.now(timezone.utc)`, BUKAN string ISO 8601 seperti koleksi lain).
 */
data class SensorReading(
    val id: String,
    val plotId: String,
    val timestampMillis: Long,
    val temperature: Double?,
    val humidity: Double?,
    val soilMoisture: Double?,
    val lightIntensity: Double?
)
