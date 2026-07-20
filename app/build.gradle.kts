import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Membaca app/google-services.json (per-developer, lihat app/.gitignore) dan men-generate
    // resource yang dipakai Firebase SDK saat runtime (project id, API key, dst).
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

// Cloudinary cloud name & upload preset (unsigned) — dibaca dari local.properties (tidak
// di-commit) lalu diekspos sebagai BuildConfig field. Lihat CloudinaryRepository.kt untuk cara pakainya.
val cloudinaryCloudName: String = localProperties.getProperty("CLOUDINARY_CLOUD_NAME", "")
val cloudinaryUploadPreset: String = localProperties.getProperty("CLOUDINARY_UPLOAD_PRESET", "")

// Base URL backend FastAPI (Railway) — dibaca dari local.properties (tidak di-commit), pola
// sama seperti mapsApiKey/cloudinaryCloudName di atas. Isi BACKEND_BASE_URL=<url>/ (wajib
// trailing slash, syarat Retrofit) di local.properties. Lihat NetworkModule.kt untuk cara pakainya.
val backendBaseUrl: String = localProperties.getProperty("BACKEND_BASE_URL", "")

android {
    namespace = "com.teti2026.smartgreenhouse"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.teti2026.smartgreenhouse"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    // Peta (Peta Marketplace, Produk Lahan, Setup Greenhouse) — MapLibre Compose + tile
    // OpenFreeMap (gratis, tanpa API key). Renderer OpenGL (bukan Vulkan default) dipakai untuk
    // kompatibilitas emulator, lihat komentar libs.versions.toml.
    implementation(libs.maplibre.compose) {
        exclude(group = "org.maplibre.gl", module = "android-sdk")
    }
    implementation(libs.maplibre.android.opengl)
    implementation(libs.vico.compose.m3)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // Upload foto ke Cloudinary via unsigned upload REST API (multipart), lihat CloudinaryRepository.kt.
    implementation(libs.okhttp)
    // REST ke backend FastAPI (kontrol aktuator, trigger AI, export CSV) — lihat NetworkModule.kt.
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    // Firebase BoM: satu sumber versi untuk seluruh artifact Firebase di bawahnya, jangan
    // tambahkan version.ref terpisah per-artifact (lihat komentar di libs.versions.toml).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}