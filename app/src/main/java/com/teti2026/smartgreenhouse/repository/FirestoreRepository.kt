package com.teti2026.smartgreenhouse.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.teti2026.smartgreenhouse.data.model.Farm
import com.teti2026.smartgreenhouse.data.model.Plot
import com.teti2026.smartgreenhouse.data.model.User
import com.teti2026.smartgreenhouse.data.model.UserRole
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Jembatan ke koleksi Firestore lain di luar `users` (yang sudah dipegang [AuthRepository]) —
 * `farms`, `plots`, dan seterusnya menyusul per fitur disambungkan (lihat `docs/data-contracts.md §3`).
 * ViewModel memanggil lewat sini, TIDAK memanggil FirebaseFirestore langsung
 * (`mobile/CLAUDE.md` "Konvensi Kode").
 */
class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")
    private val farmsCollection = firestore.collection("farms")
    private val plotsCollection = firestore.collection("plots")

    /** Baca dokumen profil `users/{uid}` (data-contracts.md §3.1) — dipakai layar Profil kedua sisi. */
    suspend fun getUser(uid: String): Result<User> = runCatching {
        val snapshot = usersCollection.document(uid).get().await()
        val roleValue = snapshot.getString("role") ?: error("Dokumen users/$uid tidak punya field role")
        User(
            uid = uid,
            name = snapshot.getString("name").orEmpty(),
            email = snapshot.getString("email").orEmpty(),
            role = UserRole.entries.first { it.value == roleValue },
            createdAt = snapshot.getString("created_at").orEmpty()
        )
    }

    /**
     * Nama kebun (`farms.farm_name`) pertama milik [ownerUid] — dipakai sebagai subjudul layar
     * Profil Petani ("Pemilik <nama kebun>"). `null` bila petani belum setup kebun sama sekali
     * (seharusnya tidak terjadi di layar Profil karena Dashboard sudah mensyaratkan farm ada,
     * tapi tetap ditangani, bukan diasumsikan).
     */
    suspend fun getFarmNameForOwner(ownerUid: String): Result<String?> = runCatching {
        val snapshot = farmsCollection.whereEqualTo("owner_uid", ownerUid).limit(1).get().await()
        snapshot.documents.firstOrNull()?.getString("farm_name")
    }

    /**
     * Dipakai setelah login untuk menentukan tujuan navigasi Petani (Dashboard vs Setup
     * Greenhouse) — true bila petani ini sudah punya minimal satu dokumen `farms`.
     */
    suspend fun hasFarmSetup(ownerUid: String): Result<Boolean> = runCatching {
        val snapshot = farmsCollection.whereEqualTo("owner_uid", ownerUid).limit(1).get().await()
        !snapshot.isEmpty
    }

    /**
     * Simpan hasil form Setup Greenhouse 3-langkah sebagai satu dokumen `farms` + satu dokumen
     * `plots` (data-contracts.md §3.2/§3.3). [deviceId] berasal dari kode pairing 6-digit yang
     * diketik petani — TIDAK diverifikasi ke ESP32 sungguhan di sini (di luar lingkup Mobile,
     * lihat `docs/Architecture.md §3`: pairing device fisik ada di firmware/backend).
     */
    suspend fun createFarmWithPlot(
        ownerUid: String,
        farmName: String,
        farmSizeM2: Double,
        locationLat: Double,
        locationLng: Double,
        cropType: String,
        deviceId: String
    ): Result<Pair<Farm, Plot>> = runCatching {
        val farmRef = farmsCollection.document()
        val farm = Farm(
            id = farmRef.id,
            ownerUid = ownerUid,
            farmName = farmName,
            locationLat = locationLat,
            locationLng = locationLng,
            farmSizeM2 = farmSizeM2
        )
        farmRef.set(
            mapOf(
                "id" to farm.id,
                "owner_uid" to farm.ownerUid,
                "farm_name" to farm.farmName,
                "location_lat" to farm.locationLat,
                "location_lng" to farm.locationLng,
                "farm_size_m2" to farm.farmSizeM2
            )
        ).await()

        val plotRef = plotsCollection.document()
        val plot = Plot(
            id = plotRef.id,
            farmId = farm.id,
            cropType = cropType,
            plantingDate = LocalDate.now().toString(),
            deviceId = deviceId
        )
        plotRef.set(
            mapOf(
                "id" to plot.id,
                "farm_id" to plot.farmId,
                "crop_type" to plot.cropType,
                "planting_date" to plot.plantingDate,
                "device_id" to plot.deviceId
            )
        ).await()

        farm to plot
    }
}
