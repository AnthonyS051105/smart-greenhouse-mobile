package com.teti2026.smartgreenhouse.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/** Body `POST /irrigation/trigger` (`docs/data-contracts.md §4.4`). */
data class IrrigationTriggerRequest(
    val plot_id: String,
    val device_id: String,
    val actuator: String, // "irrigation" | "ventilation"
    val action: String,   // "open" | "close"
    val duration_seconds: Int? = null
)

data class IrrigationTriggerResponse(
    val status: String,
    val trigger_type: String
)

data class IrrigationRecommendation(
    val Fan_actuator_status: String?,
    val Water_pump_actuator_status: String?
)

data class IrrigationRecommendationResponse(
    val plot_id: String,
    val based_on_reading_id: String,
    val recommendation: IrrigationRecommendation,
    val generated_at: String
)

data class VisionAnalyzeResponse(
    val image_id: String?,
    val ripeness_class: String,
    val confidence_score: Double
)

/**
 * Retrofit ke backend FastAPI (Railway) — HANYA kontrol aktuator & inferensi AI, lihat
 * `network/NetworkModule.kt` & `mobile/CLAUDE.md`. Endpoint sesuai `docs/data-contracts.md §4`.
 */
interface ApiService {
    @POST("irrigation/trigger")
    suspend fun triggerIrrigation(@Body body: IrrigationTriggerRequest): IrrigationTriggerResponse

    @GET("plots/{plotId}/irrigation/recommendation")
    suspend fun getIrrigationRecommendation(@Path("plotId") plotId: String): IrrigationRecommendationResponse

    /** `image_id` sengaja tidak dikirim — hasil pindaian kamera HP TIDAK dipersist ke `crop_images`
     * (jalur itu khusus citra ESP32-CAM lewat `POST /images`, di luar lingkup fitur ini). */
    @Multipart
    @POST("vision/analyze")
    suspend fun analyzeVision(@Part file: MultipartBody.Part): VisionAnalyzeResponse
}
