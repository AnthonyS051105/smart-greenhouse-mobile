package com.teti2026.smartgreenhouse.repository

import com.teti2026.smartgreenhouse.network.ApiService
import com.teti2026.smartgreenhouse.network.IrrigationTriggerRequest
import com.teti2026.smartgreenhouse.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Hasil rekomendasi AI, dipetakan ke boolean per aktuator — `null` bila model tidak mengembalikan status. */
data class ActuatorRecommendation(
    val ventilationOn: Boolean?,
    val irrigationOn: Boolean?
)

data class VisionAnalysisResult(
    val ripenessClass: String,
    val confidenceScore: Double
)

/**
 * Jembatan ke backend FastAPI (Railway) — kontrol aktuator (servo) & inferensi AI (`docs/
 * Architecture.md §3`). ViewModel/Route memanggil lewat sini, TIDAK memanggil Retrofit langsung
 * (konsisten dengan pola [FirestoreRepository] untuk Firestore).
 */
class BackendRepository(
    private val api: ApiService = NetworkModule.retrofit.create(ApiService::class.java)
) {
    /**
     * Kirim perintah manual/otomatis ke servo. [actuator] = "irrigation" | "ventilation",
     * [action] = "open" | "close" (`docs/data-contracts.md §1.2`/`§4.4`). Hardware fisik saat ini
     * hanya punya 1 servo generik (lihat `iot/CLAUDE.md`) — kedua actuator sama-sama menggerakkan
     * motor yang sama sampai servo ke-2 tersedia.
     */
    suspend fun triggerActuator(
        plotId: String,
        deviceId: String,
        actuator: String,
        action: String,
        durationSeconds: Int? = null
    ): Result<Unit> = runCatching {
        api.triggerIrrigation(
            IrrigationTriggerRequest(
                plot_id = plotId,
                device_id = deviceId,
                actuator = actuator,
                action = action,
                duration_seconds = durationSeconds
            )
        )
        Unit
    }

    /**
     * Rekomendasi ON/OFF dari model AI irigasi (`GET /plots/{id}/irrigation/recommendation`) —
     * dipakai mode OTOMATIS untuk memutuskan sendiri kapan trigger servo tanpa konfirmasi
     * petani. Backend hanya melatih `Fan_actuator_status` (→ ventilasi) & `Water_pump_actuator_status`
     * (→ irigasi); `Watering_plant_pump_status` (aktuator ke-3 di rencana awal) belum punya model.
     */
    suspend fun getIrrigationRecommendation(plotId: String): Result<ActuatorRecommendation> = runCatching {
        val response = api.getIrrigationRecommendation(plotId)
        ActuatorRecommendation(
            ventilationOn = response.recommendation.Fan_actuator_status?.equals("ON", ignoreCase = true),
            irrigationOn = response.recommendation.Water_pump_actuator_status?.equals("ON", ignoreCase = true)
        )
    }

    /**
     * Analisis kematangan buah dari foto kamera HP langsung (`POST /vision/analyze` dengan `file`,
     * TANPA `image_id`) — dipakai "Pindai Tanaman". Hasil TIDAK dipersist ke Firestore `crop_images`
     * oleh backend (jalur itu khusus citra ESP32-CAM via `POST /images`, hardware rusak & di luar
     * lingkup fitur ini) — pemanggil yang memutuskan mau disimpan ke mana (mis. riwayat lokal).
     */
    suspend fun analyzeVisionFile(imageBytes: ByteArray, mimeType: String): Result<VisionAnalysisResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val part = MultipartBody.Part.createFormData(
                    "file",
                    "scan.jpg",
                    imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                val response = api.analyzeVision(part)
                VisionAnalysisResult(
                    ripenessClass = response.ripeness_class,
                    confidenceScore = response.confidence_score
                )
            }
        }
}
