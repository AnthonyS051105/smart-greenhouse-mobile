# Task Breakdown — Mobile

> Repo: **mobile/**. Penanggung jawab: **Anggota 3 (App Petani + Backend)** & **Anggota 4 (App Pembeli + Marketplace)**.
> Timeline Mobile: Coaching 11, 15, 18 Juli; Final Presentation 23 Juli (paling akhir).
> **Stack: Kotlin + Jetpack Compose** (revisi dari XML setelah coaching).

---

## Legenda: 🔴 P0 wajib · 🟡 P1 penting · 🟢 P2 opsional

---

## Fase 0: Fondasi Bersama (Hari 1–2) — dikerjakan bersama

| ID | Task | PIC | Prioritas | Est. |
|----|------|-----|-----------|------|
| MOB-T01 | Setup project Android (Kotlin + Jetpack Compose + Material 3), struktur package, theme (Color/Type/Theme.kt) | A3 | 🔴 P0 | 3j |
| MOB-T01b | Setup Navigation Compose (NavGraph, Routes, bottom bar per role) | A3 | 🔴 P0 | 3j |
| MOB-T02 | Setup Firebase (Auth, Firestore, FCM) + google-services.json | A3 | 🔴 P0 | 2j |
| MOB-T03 | Setup Retrofit + base URL backend + interceptor token | A3 | 🔴 P0 | 2j |
| MOB-T04 | Setup Cloudinary SDK + Google Maps API key | A4 | 🔴 P0 | 2j |
| MOB-T05 | Definisikan domain models sesuai data-contracts | A3+A4 | 🔴 P0 | 2j |
| MOB-T06 | Implement Auth (register role, login, routing) | A3 | 🔴 P0 | 3j |

## Fase 1: App Petani (Hari 2–7) — PIC: Anggota 3

| ID | Task | Prioritas | Est. | Dependensi |
|----|------|-----------|------|------------|
| MOB-T07 | Setup Greenhouse (form + pairing device) | 🔴 P0 | 3j | MOB-T06 |
| MOB-T08 | Tandai lokasi lahan di peta (Maps + Places) | 🔴 P0 | 3j | MOB-T04 |
| MOB-T09 | Dashboard: grafik sensor real-time (Firestore) | 🔴 P0 | 4j | MOB-T05 |
| MOB-T10 | Kartu status aktuator | 🔴 P0 | 2j | MOB-T09 |
| MOB-T11 | Kontrol manual override (REST → backend) | 🔴 P0 | 3j | MOB-T03 |
| MOB-T12 | Riwayat citra + skor AI (Firestore + Cloudinary) | 🔴 P0 | 3j | MOB-T05 |
| MOB-T13 | Buat listing + auto-fill health_score (REST) | 🔴 P0 | 4j | MOB-T11 |
| MOB-T14 | Kelola listing (edit, tandai sold) | 🟡 P1 | 2j | MOB-T13 |
| MOB-T15 | Chat sisi petani (Firestore realtime) | 🔴 P0 | 3j | MOB-T05 |
| MOB-T16 | Notifikasi FCM (order, chat, rekomendasi AI) | 🔴 P0 | 3j | MOB-T02 |

## Fase 2: App Pembeli (Hari 2–7) — PIC: Anggota 4

| ID | Task | Prioritas | Est. | Dependensi |
|----|------|-----------|------|------------|
| MOB-T17 | Marketplace: daftar listing + filter | 🔴 P0 | 4j | MOB-T05 |
| MOB-T18 | Detail listing (foto, harga, health_score, grafik sensor) | 🔴 P0 | 4j | MOB-T17 |
| MOB-T19 | Peta marketplace: marker farm + bottom sheet komoditas | 🔴 P0 | 4j | MOB-T04 |
| MOB-T20 | Chat sisi pembeli (Firestore realtime) | 🔴 P0 | 3j | MOB-T05 |
| MOB-T21 | Checkout: pilih jumlah, alamat, konfirmasi order | 🔴 P0 | 3j | MOB-T18 |
| MOB-T22 | Riwayat transaksi | 🟡 P1 | 2j | MOB-T21 |
| MOB-T23 | Beri rating & ulasan | 🔴 P0 | 2j | MOB-T21 |
| MOB-T24 | Notifikasi FCM (status pesanan, chat) | 🔴 P0 | 2j | MOB-T02 |

## Fase 3: Integrasi & Polish (Hari 7–10)

| ID | Task | PIC | Prioritas | Est. |
|----|------|-----|-----------|------|
| MOB-T25 | Integrasi end-to-end petani↔pembeli (listing→beli) | A3+A4 | 🔴 P0 | 3j |
| MOB-T26 | Upload foto produk ke Cloudinary | A4 | 🔴 P0 | 2j |
| MOB-T27 | Offline-first cache (Room) app petani | A3 | 🟢 P2 | 3j |
| MOB-T28 | State handling (loading/empty/error) semua layar | A3+A4 | 🟡 P1 | 3j |
| MOB-T29 | Polish UI (warna, ikon, badge health_score) | A4 | 🟡 P1 | 2j |

## Fase 4: UML, Demo & Finalisasi (Hari 10–12)

| ID | Task | Prioritas | Est. |
|----|------|-----------|------|
| MOB-T30 | Gambar diagram UML (use case, class, sequence) | 🔴 P0 | 3j |
| MOB-T31 | Uji alur kritis end-to-end (dengan IoT & AI nyata) | 🔴 P0 | 3j |
| MOB-T32 | Rekam video demo aplikasi | 🔴 P0 | 2j |
| MOB-T33 | Build APK + rapikan repo + README | 🔴 P0 | 2j |
| MOB-T34 | Siapkan slide presentasi Mobile Dev | 🔴 P0 | 2j |

---

## Milestone

| Milestone | Target | Kriteria |
|-----------|--------|----------|
| M1 | Akhir Hari 2 | Auth + routing role + fondasi siap. |
| M2 | Akhir Hari 7 | Kedua app punya fitur inti (monitoring/kontrol; marketplace/detail). |
| M3 | Akhir Hari 10 | Integrasi petani↔pembeli + peta + chat berfungsi. |
| M4 | Akhir Hari 12 | UML + video + APK + slide siap. |

---

## Pembagian PIC

- **Anggota 3:** App Petani + koordinasi backend (karena banyak REST call ke backend).
- **Anggota 4:** App Pembeli + marketplace + peta + Cloudinary.
- Fondasi (Fase 0) & integrasi (Fase 3–4) dikerjakan berdua.

---

## Catatan

- Deadline Mobile **paling akhir (23 Juli)** → bisa manfaatkan hasil AI & IoT yang sudah jadi lebih dulu.
- Prioritaskan alur demo end-to-end yang "bercerita": sensor → AI → listing → beli.
- Checkout boleh disederhanakan (tanpa payment gateway) — fokus alur & data konsisten.
- **Compose:** utamakan composable stateless + state hoisting sejak awal agar mudah di-preview & diuji. Pakai `@Preview` untuk iterasi UI cepat tanpa menjalankan emulator.
