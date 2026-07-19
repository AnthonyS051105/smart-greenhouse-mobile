package com.teti2026.smartgreenhouse.data.model

/**
 * Model domain koleksi `users` sesuai `shared/data-contracts.md §3.1`. Nama field mengikuti
 * dokumen tersebut persis (case-sensitive lintas bidang). [uid] = Firebase Auth UID, juga
 * dipakai sebagai id dokumen Firestore.
 */
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val createdAt: String
)
