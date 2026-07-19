package com.teti2026.smartgreenhouse.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.teti2026.smartgreenhouse.data.model.User
import com.teti2026.smartgreenhouse.data.model.UserRole
import kotlinx.coroutines.tasks.await
import java.time.Instant

/**
 * Jembatan ke Firebase Auth (login/register/logout/sesi) + dokumen profil koleksi `users` di
 * Firestore (`shared/data-contracts.md §3.1`). Satu-satunya sumber kebenaran identitas & role
 * pengguna — ViewModel TIDAK boleh memanggil FirebaseAuth/FirebaseFirestore langsung
 * (`mobile/CLAUDE.md` "Konvensi Kode": UI/ViewModel lewat Repository, bukan SDK langsung).
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    /** Akun Firebase Auth yang sedang login di device ini, null bila belum/sudah logout. */
    fun currentUser(): FirebaseUser? = auth.currentUser

    /**
     * Buat akun baru (Firebase Auth) + dokumen profil `users/{uid}` dengan [role] yang dipilih
     * user di layar Register. Kedua operasi tidak atomik (Firebase Auth & Firestore adalah dua
     * produk terpisah, tidak ada transaksi lintas keduanya) — bila penulisan Firestore gagal
     * setelah akun Auth berhasil dibuat, akun tetap ada tapi tanpa dokumen profil; [fetchRole]
     * pada login berikutnya akan gagal sampai dokumen ini dibuat ulang secara manual. Diterima
     * untuk skala demo bootcamp (lihat juga `docs/Architecture.md` ADR-01 soal tanpa transaksi
     * lintas Firestore/produk lain).
     */
    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User> =
        runCatching {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: error("Firebase tidak mengembalikan UID setelah registrasi")
            val createdAt = Instant.now().toString()
            val user = User(uid = uid, name = name, email = email, role = role, createdAt = createdAt)
            usersCollection.document(uid).set(
                mapOf(
                    "uid" to user.uid,
                    "name" to user.name,
                    "email" to user.email,
                    "role" to user.role.value,
                    "created_at" to user.createdAt
                )
            ).await()
            user
        }

    /** Login akun yang sudah ada. Role TIDAK ditentukan di sini — panggil [fetchRole] setelahnya. */
    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        authResult.user ?: error("Firebase tidak mengembalikan user setelah login")
    }

    /**
     * Ambil [UserRole] tersimpan dari dokumen `users/{uid}` — dipanggil setelah [login] berhasil
     * untuk menentukan tujuan navigasi (App Petani vs App Pembeli). Role akun ditetapkan SEKALI
     * saat [register] dan tidak bisa diganti sendiri oleh pengguna dari layar Login manapun.
     */
    suspend fun fetchRole(uid: String): Result<UserRole> = runCatching {
        val snapshot = usersCollection.document(uid).get().await()
        val roleValue = snapshot.getString("role")
            ?: error("Dokumen users/$uid tidak punya field role")
        UserRole.entries.first { it.value == roleValue }
    }

    fun logout() {
        auth.signOut()
    }
}
