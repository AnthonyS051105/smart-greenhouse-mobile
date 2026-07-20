# SRS — Software Requirements Specification (Mobile)

> Repo: **mobile/** — Smart Greenhouse + Marketplace.
> Referensi induk: `shared/PRD.md`, `shared/data-contracts.md`, `shared/Architecture.md`.

---

## 1. Pendahuluan

### 1.1 Tujuan
Mendefinisikan kebutuhan aplikasi Android dua sisi: **App Petani** (monitoring, kontrol, listing) dan **App Pembeli** (marketplace, chat, checkout, rating, peta).

### 1.2 Ruang Lingkup
Satu codebase Android (Kotlin + Jetpack Compose) dengan navigasi berbeda berdasarkan `role` setelah login. Terhubung ke Firebase (Auth, Firestore, FCM), Cloudinary (foto), backend FastAPI (kontrol aktuator & AI), dan peta MapLibre + OpenFreeMap (bukan Google Maps, lihat `shared/Architecture.md` ADR-08).

### 1.3 Definisi
Lihat `shared/glossary.md`.

---

## 2. Deskripsi Umum

### 2.1 Perspektif Produk
Aplikasi adalah antarmuka utama pengguna. Petani memantau & menjual; pembeli mencari & membeli. Transparansi kualitas (health_score dari AI) jadi pembeda.

### 2.2 Karakteristik Pengguna
- **Petani:** mungkin non-teknis → UI sederhana, Bahasa Indonesia, teks besar.
- **Pembeli:** terbiasa aplikasi e-commerce → UI marketplace familiar.

### 2.3 Batasan
- **Kotlin + Jetpack Compose** (declarative UI). Dikonfirmasi diperbolehkan oleh mentor saat coaching (revisi dari rencana awal XML).
- Min. 2 tech stack yang diajarkan (Firestore SDK + REST API + FCM + Maps → melebihi).
- Checkout tanpa payment gateway nyata (status manual).

---

## 3. Kebutuhan Fungsional

### 3.1 Autentikasi & Onboarding
| ID | Kebutuhan | Prioritas |
|----|-----------|-----------|
| MOB-FR-01 | Registrasi dengan pilihan role (petani/pembeli) via Firebase Auth. | Wajib |
| MOB-FR-02 | Login & logout; simpan sesi. | Wajib |
| MOB-FR-03 | Navigasi diarahkan sesuai role setelah login. | Wajib |

### 3.2 App Petani
| ID | Kebutuhan | Prioritas |
|----|-----------|-----------|
| MOB-FR-04 | Setup greenhouse: nama, ukuran, jenis tanaman, pairing device. | Wajib |
| MOB-FR-05 | Tandai lokasi lahan di peta (MapLibre; pencarian alamat/Places Autocomplete belum diimplementasikan). | Wajib |
| MOB-FR-06 | Dashboard monitoring: grafik suhu/kelembapan udara/kelembapan tanah/intensitas cahaya real-time (dari Firestore). | Wajib |
| MOB-FR-07 | Tampilkan status aktuator terkini (ON/OFF, auto/manual). | Wajib |
| MOB-FR-08 | Kontrol manual override aktuator (via REST API ke backend). | Wajib |
| MOB-FR-09 | Riwayat citra + skor kematangan/kesehatan dari AI. | Wajib |
| MOB-FR-10 | Buat listing: form auto-fill health_score (via REST API), lengkapi harga/jumlah. | Wajib |
| MOB-FR-11 | Kelola listing (edit, tandai sold). | Sebaiknya |
| MOB-FR-12 | Terima & balas chat dari pembeli. | Wajib |
| MOB-FR-13 | Terima notifikasi (rekomendasi AI, order baru, chat). | Wajib |

### 3.3 App Pembeli
| ID | Kebutuhan | Prioritas |
|----|-----------|-----------|
| MOB-FR-14 | Halaman marketplace: daftar listing + filter (jenis, lokasi, harga, health_score). | Wajib |
| MOB-FR-15 | Detail listing: foto, harga, health_score, grafik sensor historis, profil petani. | Wajib |
| MOB-FR-16 | Peta marketplace: marker lokasi lahan; tap marker → daftar komoditas farm. | Wajib |
| MOB-FR-17 | Chat negosiasi dengan petani (Firestore realtime). | Wajib |
| MOB-FR-18 | Checkout: pilih jumlah, alamat/pengambilan, konfirmasi pesanan. | Wajib |
| MOB-FR-19 | Riwayat transaksi. | Sebaiknya |
| MOB-FR-20 | Beri rating & ulasan setelah transaksi. | Wajib |
| MOB-FR-21 | Terima notifikasi (status pesanan, balasan chat). | Wajib |

### 3.4 Umum
| ID | Kebutuhan | Prioritas |
|----|-----------|-----------|
| MOB-FR-22 | Upload foto produk ke Cloudinary. | Wajib |
| MOB-FR-23 | Mode offline-first (cache listing via Room) pada app petani. | Sebaiknya |
| MOB-FR-24 | Pemuatan gambar efisien (Coil for Compose). | Wajib |

---

## 4. Kebutuhan Non-Fungsional

| ID | Kebutuhan |
|----|-----------|
| MOB-NFR-01 | **Usability:** Bahasa Indonesia; UI petani sederhana. |
| MOB-NFR-02 | **Performa:** daftar listing & grafik dimuat < 3 detik pada koneksi normal. |
| MOB-NFR-03 | **Keamanan:** semua request ke backend menyertakan Firebase ID token. |
| MOB-NFR-04 | **Kompatibilitas:** Android API level sesuai materi bootcamp (mis. min SDK 24). |
| MOB-NFR-05 | **Maintainability:** arsitektur berlapis (UI–Repository–Data source). |
| MOB-NFR-06 | **Resiliensi:** tangani error jaringan dengan pesan yang jelas. |

---

## 5. Antarmuka Eksternal

| Sistem | Cara Akses | Untuk |
|--------|-----------|-------|
| Firebase Auth | Firebase SDK | Login/register |
| Firestore | Firebase SDK | Listing, order, chat, review, sensor readings, farms |
| FCM | Firebase SDK | Notifikasi |
| Cloudinary | SDK/upload API | Foto produk |
| Backend FastAPI | REST (Retrofit) | Kontrol aktuator, trigger AI, auto-fill health_score, export CSV |
| OpenFreeMap | MapLibre Compose (tanpa API key) | Peta & lokasi lahan (bukan Google Maps, lihat `shared/Architecture.md` ADR-08) |

> Pembagian jalur lengkap: `shared/Architecture.md §3`.

---

## 6. Kriteria Penerimaan

- [ ] Login berbasis role berfungsi; navigasi sesuai role.
- [ ] Petani: monitoring real-time, kontrol manual, buat listing dengan health_score otomatis.
- [ ] Pembeli: marketplace, peta marker, chat, checkout, rating.
- [ ] Min. 2 tech stack terbukti bekerja end-to-end.
- [ ] Diagram UML lengkap tersedia.

---

## 7. Ketertelusuran ke Rubrik Mobile Dev

| Kebutuhan | Kriteria Rubrik |
|-----------|-----------------|
| Alur e-commerce + monitoring (MOB-FR-04..21) | Fitur yang ditawarkan (20%) |
| Demo end-to-end | Cara kerja & demonstrasi (20%) |
| Multi tech stack (§5) | Teknologi/Tools (15%) |
| Peta + health_score transparan | Orisinalitas (15%) |
| UI dua sisi | Desain Karya (15%) |
