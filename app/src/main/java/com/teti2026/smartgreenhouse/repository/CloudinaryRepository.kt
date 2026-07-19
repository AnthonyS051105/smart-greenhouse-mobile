package com.teti2026.smartgreenhouse.repository

import android.content.Context
import android.net.Uri
import com.teti2026.smartgreenhouse.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Upload foto (produk listing, citra tanaman) ke Cloudinary lewat REST API "unsigned upload" —
 * sengaja TIDAK memakai Cloudinary Android SDK resmi (com.cloudinary:cloudinary-android), yang
 * membawa dependency MediaManager + WorkManager untuk queue upload background; kebutuhan di sini
 * hanya kirim satu file & terima URL secara langsung. "Unsigned" berarti app TIDAK menyimpan API
 * Secret Cloudinary sama sekali (lihat docs/Architecture.md ADR-02 & komentar di
 * local.properties) — keamanan diatur di sisi Cloudinary Console lewat upload preset
 * (folder/size/format yang diizinkan), bukan lewat kerahasiaan credential di app.
 */
class CloudinaryRepository(
    private val client: OkHttpClient = OkHttpClient()
) {
    private val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    /**
     * Upload [imageUri] (mis. hasil pemilihan galeri/kamera) ke Cloudinary dan mengembalikan
     * `secure_url` (HTTPS) hasil upload — nilai inilah yang disimpan sebagai string URL foto di
     * dokumen Firestore terkait (mis. field foto pada `listings`/`crop_images`).
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                check(cloudName.isNotBlank() && uploadPreset.isNotBlank()) {
                    "CLOUDINARY_CLOUD_NAME/CLOUDINARY_UPLOAD_PRESET belum diisi di local.properties"
                }
                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: error("Tidak bisa membaca isi file dari Uri: $imageUri")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", uploadPreset)
                    .addFormDataPart(
                        "file",
                        "upload.jpg",
                        bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        error("Upload Cloudinary gagal (HTTP ${response.code}): $bodyString")
                    }
                    JSONObject(bodyString).getString("secure_url")
                }
            }
        }
}
