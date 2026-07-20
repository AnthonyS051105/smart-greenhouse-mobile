package com.teti2026.smartgreenhouse.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.teti2026.smartgreenhouse.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Interceptor yang menyisipkan header `Authorization: Bearer <firebase_id_token>` ke setiap
 * request backend, sesuai aturan wajib di `mobile/CLAUDE.md`. `Interceptor.intercept()` berjalan
 * sinkron di background thread milik OkHttp (bukan main thread) — pakai `Tasks.await()` (blocking,
 * bukan `await()` coroutine) karena tidak ada scope suspend di titik ini.
 */
private class FirebaseAuthInterceptor(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val idToken = auth.currentUser?.let { user ->
            runCatching { Tasks.await(user.getIdToken(false)).token }.getOrNull()
        }
        val request = chain.request().let { original ->
            if (idToken != null) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $idToken")
                    .build()
            } else {
                original
            }
        }
        return chain.proceed(request)
    }
}

/**
 * Retrofit ke backend FastAPI (Railway) — HANYA untuk kontrol aktuator, trigger AI, auto-fill
 * health_score, export CSV (`docs/Architecture.md §3`). CRUD listing/order/chat/review tetap
 * lewat Firestore SDK langsung, JANGAN dipanggil lewat sini.
 */
object NetworkModule {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(FirebaseAuthInterceptor())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        check(BuildConfig.BACKEND_BASE_URL.isNotBlank()) {
            "BACKEND_BASE_URL belum diisi di local.properties"
        }
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
