# UI/UX Flow — Mobile

> Repo: **mobile/** (khusus Mobile Dev — tidak relevan untuk IoT & AI).
> Referensi: `mobile/SRS.md`, `shared/data-contracts.md`.
> **Stack UI: Kotlin + Jetpack Compose + Material 3** (revisi dari XML setelah dikonfirmasi mentor).

---

## 1. Prinsip Desain

- **Bahasa Indonesia** di seluruh UI.
- **App Petani:** sederhana, teks jelas, aksi utama menonjol (untuk pengguna non-teknis).
- **App Pembeli:** pola e-commerce familiar (grid produk, filter, detail, checkout).
- **Konsistensi:** warna tema hijau (agriculture), ikon jelas, feedback aksi (loading/success/error).

---

## 2. Peta Navigasi Global

```
[Splash]
   ↓
[Login / Register]
   ├── role = Petani → [Farmer Home]
   │       BottomNav: Dashboard | Listing | Chat | Profil
   └── role = Pembeli → [Buyer Home]
           BottomNav: Marketplace | Peta | Pesanan | Profil
```

---

## 3. Flow Petani

### 3.1 Onboarding & Setup
```
Register (pilih "Petani")
   ↓
Verifikasi / Login berhasil
   ↓
[Setup Greenhouse]
   - Input: nama greenhouse, ukuran, jenis tanaman
   - [Tandai Lokasi di Peta]  ← MapLibre (geser peta, bukan Google Maps — lihat `shared/Architecture.md` ADR-08; pencarian alamat/Places Autocomplete belum diimplementasikan)
       · geser pin / cari alamat → simpan lat,lng
   - [Pairing Perangkat]  ← input device_id/pairing code dari OLED
   ↓
[Dashboard Petani]
```

### 3.2 Monitoring & Kontrol
```
[Dashboard]
   - Grafik real-time: suhu, kelembapan udara, kelembapan tanah, intensitas cahaya (dari Firestore sensor_readings)
   - Kartu status aktuator: Irigasi (ON/OFF, auto/manual), Ventilasi (...)
   - Notifikasi terbaru (mis. "Irigasi otomatis 3 mnt oleh AI")
   ↓ tap "Kontrol Manual"
[Kontrol Aktuator]
   - Pilih aktuator (Irigasi / Ventilasi)
   - [Aktifkan] / [Matikan]  → POST /irrigation/trigger (backend)
   - Status diperbarui real-time
```

### 3.3 Analisis Citra & Buat Listing
```
[Riwayat Citra]
   - Grid foto tanaman (Cloudinary) + skor kematangan/kesehatan (AI)
   ↓ pilih foto/periode → tap "Buat Listing dari Data Ini"
[Buat Listing]
   - health_score TERISI OTOMATIS  ← POST /listings/auto-fill-health-score
   - Input: harga/kg, jumlah (kg), foto tambahan (upload Cloudinary)
   - [Publikasikan]  → simpan ke Firestore listings
   ↓
[Listing muncul di Marketplace]
```

### 3.4 Chat & Transaksi (sisi Petani)
```
[Notifikasi: pesan baru / order baru]
   ↓
[Chat] balas negosiasi (Firestore realtime)
   ↓
[Order masuk] → [Konfirmasi Pesanan] → status listing "sold" bila habis
```

---

## 4. Flow Pembeli

### 4.1 Onboarding
```
Register (pilih "Pembeli") → Login → [Marketplace]
```

### 4.2 Jelajah & Cari
```
[Marketplace]
   - Daftar/grid listing (foto, harga, health_score badge)
   - Filter: jenis produk, lokasi, rentang harga, health_score minimum
   ↓ tap listing
[Detail Listing]
   - Foto, harga, jumlah, health_score
   - Grafik data sensor historis (transparansi kualitas)
   - Profil petani + lokasi
   - [Chat dengan Petani] | [Beli Sekarang]
```

### 4.3 Peta Marketplace (MapLibre + OpenFreeMap)
```
[Peta]
   - Marker di tiap lokasi lahan petani (dari Firestore farms)
   ↓ tap marker
[Bottom Sheet Farm]
   - Nama petani/lahan, jarak dari pembeli
   - Daftar komoditas aktif farm tersebut
   ↓ tap komoditas
[Detail Listing]  (sama seperti 4.2)
```

### 4.4 Negosiasi, Checkout, Rating
```
[Detail Listing] → [Chat dengan Petani] (nego harga/jumlah)
   ↓ sepakat → [Beli Sekarang]
[Checkout]
   - Jumlah pesanan, alamat/pengambilan
   - [Konfirmasi Pesanan]  → Firestore orders (status: pending)
   ↓ notifikasi status (dikonfirmasi/selesai)
[Riwayat Transaksi]
   ↓ setelah selesai
[Beri Rating & Ulasan]  → Firestore reviews
```

---

## 5. Daftar Layar (Screens)

| App Petani | App Pembeli |
|------------|-------------|
| Login / Register | Login / Register |
| Setup Greenhouse (+ Peta lokasi) | Marketplace (list + filter) |
| Dashboard Monitoring | Peta Marketplace (marker + bottom sheet) |
| Kontrol Aktuator | Detail Listing |
| Riwayat Citra & Skor AI | Chat Negosiasi |
| Buat / Kelola Listing | Checkout & Konfirmasi |
| Chat | Riwayat Transaksi |
| Notifikasi | Beri Rating |
| Profil & Pengaturan | Notifikasi · Profil |

---

## 6. State & Feedback UI

| State | Perlakuan |
|-------|-----------|
| Loading | ProgressBar / shimmer pada daftar. |
| Empty | Ilustrasi + teks ("Belum ada listing"). |
| Error | Snackbar + tombol "Coba lagi". |
| Success aksi | Snackbar konfirmasi ("Listing dipublikasikan"). |
| Realtime update | Chat & status pesanan diperbarui otomatis (listener). |

---

## 7. Komponen UI Kunci (Jetpack Compose + Material 3)

- `LazyColumn` / `LazyVerticalGrid` — daftar listing, chat, riwayat.
- **Vico** / **YCharts** (Compose-native) — grafik sensor.
- `MaplibreMap` dari **MapLibre Compose** (tile **OpenFreeMap**, bukan Google Maps) — peta marketplace.
- `ModalBottomSheet` (M3) — detail farm saat tap marker.
- `Card` (M3) — kartu listing & status aktuator.
- `NavigationBar` + `NavigationBarItem` — navigasi utama per role.
- `FilterChip` (M3) — filter marketplace.
- `AsyncImage` (**Coil**) — foto tanaman & produk.
- `Snackbar` via `SnackbarHostState` — feedback aksi/error.

---

## 8. Catatan untuk Desainer/Developer

- Health_score tampil sebagai **badge warna** (hijau tinggi, kuning sedang, merah rendah) untuk cepat dipahami.
- Grafik sensor di detail listing = pembeda utama ("transparansi") → tonjolkan saat demo.
- Peta = fitur orisinalitas → pastikan smooth saat presentasi (siapkan data dummy farm bila perlu).
