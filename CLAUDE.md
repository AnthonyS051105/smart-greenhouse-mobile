# CLAUDE.md — Panduan untuk Claude Code (Repo Mobile)

> Konteks untuk Claude Code saat mengerjakan repo **mobile/**.
> Baca juga: `docs/SRS.md`, `docs/SDD.md`, `docs/UIUX-Flow.md`, `docs/Architecture.md`, `docs/data-contracts.md`.

---

## Tentang Proyek

**Smart Greenhouse + Marketplace** — Final Project Bootcamp TETI 2026 (AI × IoT × Mobile).
Repo ini = **aplikasi Android** dua sisi: **App Petani** (monitoring, kontrol, listing) & **App Pembeli** (marketplace, chat, checkout, rating, peta). Satu codebase, navigasi berbeda per `role`.

**Tanaman fokus:** cabai (dapat berubah).

---

## Peran Repo Ini

Antarmuka pengguna. Terhubung ke:
- **Firebase** (Auth, Firestore, FCM) — langsung via SDK.
- **Cloudinary** — upload foto produk.
- **Backend FastAPI** (Railway) — hanya untuk kontrol aktuator, trigger AI, auto-fill health_score, export CSV.
- **MapLibre + OpenFreeMap** — peta lokasi lahan (BUKAN Google Maps, lihat ADR-08 `docs/Architecture.md`).

**IoT, AI, backend ada di repo terpisah.**

---

## Stack Teknologi

- **Bahasa:** Kotlin.
- **UI:** **Jetpack Compose** (declarative) + **Material 3**. — **BUKAN XML View system.**
- **Arsitektur:** MVVM + Unidirectional Data Flow (state hoisting, `StateFlow<UiState>`).
- **Navigasi:** **Navigation Compose** (bukan Fragment).
- **Library:**
  - Retrofit + OkHttp (REST ke backend)
  - Firebase SDK (Auth / Firestore / Messaging)
  - **MapLibre Compose** (`org.maplibre.compose:maplibre-compose`) + tile OpenFreeMap — **BUKAN
    Google Maps** (`maps-compose`/`play-services-maps`), lihat ADR-08 `docs/Architecture.md`
  - Cloudinary SDK
  - **Coil** (`AsyncImage`) — untuk gambar
  - **Vico** atau **YCharts** — grafik sensor (Compose-native)
  - Room (opsional cache)
  - Hilt (opsional, untuk DI)

---

## Konvensi Kode

- **Arsitektur berlapis** (lihat `docs/SDD.md §1`): jangan panggil Firestore/Retrofit langsung dari composable — lewat ViewModel → Repository.
- **State hoisting:** composable UI **stateless**; state dipegang ViewModel dan diturunkan sebagai parameter. Event naik lewat lambda.
- **Collect state:** gunakan `collectAsStateWithLifecycle()`, bukan `collectAsState()`.
- **Model domain** harus selaras `docs/data-contracts.md §3` (nama field konsisten).
- **Penamaan:** PascalCase untuk composable (`MarketplaceScreen`) & class; camelCase untuk fungsi/variabel.
- **String** di `strings.xml` (Bahasa Indonesia), akses via `stringResource(R.string.x)` — jangan hardcode di composable.
- **Async:** coroutines (`viewModelScope`) di ViewModel.
- **Theme:** definisikan di `ui/theme/` (Color.kt, Type.kt, Theme.kt) mengikuti Material 3.

---

## Aturan Penting (JANGAN dilanggar)

1. **Gunakan Jetpack Compose, JANGAN XML layout / Fragment / findViewById.**
   (Keputusan direvisi dari XML → Compose setelah dikonfirmasi mentor saat coaching. Lihat ADR-06 di `docs/Architecture.md`.)
2. **Pembagian jalur data** (lihat `docs/Architecture.md §3`):
   - CRUD listing/order/chat/review → **Firestore SDK langsung**.
   - Kontrol aktuator, trigger AI, auto-fill health_score, export CSV → **REST ke backend**.
   - JANGAN buat endpoint backend untuk hal yang bisa langsung ke Firestore.
3. **Format data** ikut `docs/data-contracts.md` — jangan ubah nama field/enum.
4. **Semua request backend** wajib header `Authorization: Bearer <firebase_id_token>`.
5. **health_score** diterima sebagai angka jadi dari backend — JANGAN hitung sendiri di app.
6. **UI Bahasa Indonesia**, sederhana untuk petani.
7. **Jangan pakai library View-based** (MPAndroidChart, Glide, SupportMapFragment) — pakai padanan Compose: Vico/YCharts, Coil, MapLibre Compose.
8. **Jangan pakai Google Maps SDK/Places/Geocoding** (`com.google.maps.android:maps-compose`, `com.google.android.gms:play-services-maps`) — diganti **MapLibre Compose** + tile **OpenFreeMap** (gratis, tanpa API key) sejak 2026-07-20. Lihat ADR-08 `docs/Architecture.md` untuk alasan & `ui/buyer/MapScreen.kt` (`MapMarkerOverlay`, `MAP_STYLE_URL`) untuk pola implementasi (marker custom via overlay proyeksi kamera — MapLibre Compose TIDAK punya `MarkerComposable` bawaan seperti maps-compose).

---

## Cara Menjalankan

```bash
# 1. Letakkan google-services.json di app/
# 2. Isi API key (Cloudinary, base URL backend) di local.properties / BuildConfig
#    (peta TIDAK butuh API key lagi sejak migrasi ke MapLibre + OpenFreeMap)
# 3. Build & run via Android Studio, atau:
./gradlew assembleDebug
```

**Dependency Compose (garis besar `app/build.gradle.kts`):**
```kotlin
implementation(platform("androidx.compose:compose-bom:<versi>"))
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.navigation:navigation-compose:<versi>")
implementation("androidx.lifecycle:lifecycle-runtime-compose:<versi>")   // collectAsStateWithLifecycle
implementation("org.maplibre.compose:maplibre-compose:<versi>")          // peta — tanpa API key
implementation("io.coil-kt:coil-compose:<versi>")
// + Firebase BoM, Retrofit, Cloudinary, Vico/YCharts
```

---

## Prioritas Saat Ini

Lihat `docs/Task-Breakdown.md`. Fondasi dulu (theme + navigation + Auth), lalu fitur inti kedua app secara paralel:
- Petani P0: dashboard (grafik), kontrol aktuator, buat listing.
- Pembeli P0: marketplace, detail, peta, chat, checkout, rating.

---

## Alur Demo yang Harus "Bercerita"

Sensor baca kondisi → AI nilai → petani buat listing (health_score otomatis) → pembeli lihat di marketplace/peta → chat → beli → rating. Pastikan alur ini mulus saat demo.

---

## Yang TIDAK Dikerjakan di Repo Ini

- Firmware/hardware (repo `iot/`).
- Training model AI (repo `ai/`).
- Logika MQTT & inferensi AI (backend) — app hanya memanggil endpoint yang sudah disediakan.
