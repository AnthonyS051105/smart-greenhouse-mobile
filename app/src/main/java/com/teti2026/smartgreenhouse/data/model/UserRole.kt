package com.teti2026.smartgreenhouse.data.model

/**
 * Role pengguna sesuai `shared/data-contracts.md §3.1` (koleksi `users`, field `role`).
 * [value] merepresentasikan nilai persis yang disimpan di Firestore — jangan diubah,
 * enum bersifat case-sensitive lintas bidang.
 */
enum class UserRole(val value: String) {
    FARMER("farmer"),
    BUYER("buyer")
}
