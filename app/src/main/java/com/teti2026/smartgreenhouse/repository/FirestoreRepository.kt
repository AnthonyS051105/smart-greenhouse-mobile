package com.teti2026.smartgreenhouse.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.teti2026.smartgreenhouse.data.model.Farm
import com.teti2026.smartgreenhouse.data.model.Listing
import com.teti2026.smartgreenhouse.data.model.Plot
import com.teti2026.smartgreenhouse.data.model.User
import com.teti2026.smartgreenhouse.data.model.UserRole
import kotlinx.coroutines.tasks.await
import java.time.Instant
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
    private val listingsCollection = firestore.collection("listings")

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

    /**
     * Ambil kebun+petak tanam milik [ownerUid] — dipakai Buat Listing untuk mengisi `farm_id`/
     * `plot_id`/`crop_type` otomatis (petani TIDAK memilih ini sendiri di form, lihat
     * `CreateListingFormState`). Asumsi MVP: satu petani = satu farm = satu plot (sesuai flow
     * Setup Greenhouse saat ini) — bila nanti multi-plot didukung, ganti jadi daftar & biarkan
     * petani pilih.
     */
    suspend fun getFarmAndPlotForOwner(ownerUid: String): Result<Pair<Farm, Plot>> = runCatching {
        val farmDoc = farmsCollection.whereEqualTo("owner_uid", ownerUid).limit(1).get().await()
            .documents.firstOrNull() ?: error("Petani belum memiliki data kebun (farms)")
        val farm = Farm(
            id = farmDoc.id,
            ownerUid = ownerUid,
            farmName = farmDoc.getString("farm_name").orEmpty(),
            locationLat = farmDoc.getDouble("location_lat") ?: 0.0,
            locationLng = farmDoc.getDouble("location_lng") ?: 0.0,
            farmSizeM2 = farmDoc.getDouble("farm_size_m2") ?: 0.0
        )
        val plotDoc = plotsCollection.whereEqualTo("farm_id", farm.id).limit(1).get().await()
            .documents.firstOrNull() ?: error("Kebun belum memiliki data petak tanam (plots)")
        val plot = Plot(
            id = plotDoc.id,
            farmId = farm.id,
            cropType = plotDoc.getString("crop_type").orEmpty(),
            plantingDate = plotDoc.getString("planting_date").orEmpty(),
            deviceId = plotDoc.getString("device_id").orEmpty()
        )
        farm to plot
    }

    /**
     * Simpan listing baru (data-contracts.md §3.7). [farmId]/[plotId]/[cropType] berasal dari
     * [getFarmAndPlotForOwner] (bukan input bebas petani). [harvestDate] tidak ada di form Buat
     * Listing sama sekali — didefaultkan ke tanggal hari ini (simplifikasi, petani biasanya buat
     * listing tepat saat/sesaat setelah panen).
     */
    suspend fun createListing(
        farmId: String,
        plotId: String,
        cropType: String,
        productName: String,
        quantityKg: Double,
        pricePerKg: Long,
        healthScore: Double,
        description: String,
        preOrderEnabled: Boolean,
        imageUrls: List<String>
    ): Result<Listing> = runCatching {
        val listingRef = listingsCollection.document()
        val listing = Listing(
            id = listingRef.id,
            farmId = farmId,
            plotId = plotId,
            cropType = cropType,
            quantityKg = quantityKg,
            pricePerKg = pricePerKg,
            healthScore = healthScore,
            harvestDate = LocalDate.now().toString(),
            status = "available",
            createdAt = Instant.now().toString(),
            description = description,
            preOrderEnabled = preOrderEnabled,
            productName = productName,
            imageUrls = imageUrls
        )
        listingRef.set(
            mapOf(
                "id" to listing.id,
                "farm_id" to listing.farmId,
                "plot_id" to listing.plotId,
                "crop_type" to listing.cropType,
                "quantity_kg" to listing.quantityKg,
                "price_per_kg" to listing.pricePerKg,
                "health_score" to listing.healthScore,
                "harvest_date" to listing.harvestDate,
                "status" to listing.status,
                "created_at" to listing.createdAt,
                "description" to listing.description,
                "pre_order_enabled" to listing.preOrderEnabled,
                "product_name" to listing.productName,
                "image_urls" to listing.imageUrls
            )
        ).await()
        listing
    }

    /** Baca satu dokumen `farms/{farmId}` langsung by-id (beda dari [getFarmAndPlotForOwner] yang query by owner_uid). */
    suspend fun getFarmById(farmId: String): Result<Farm> = runCatching {
        val doc = farmsCollection.document(farmId).get().await()
        Farm(
            id = doc.id,
            ownerUid = doc.getString("owner_uid").orEmpty(),
            farmName = doc.getString("farm_name").orEmpty(),
            locationLat = doc.getDouble("location_lat") ?: 0.0,
            locationLng = doc.getDouble("location_lng") ?: 0.0,
            farmSizeM2 = doc.getDouble("farm_size_m2") ?: 0.0
        )
    }

    /** Seluruh listing berstatus "available" — dipakai Marketplace. Tidak difilter/di-query lebih lanjut di sini (filter Marketplace masih UI-only, lihat TODO di MarketplaceRoute). */
    suspend fun getAvailableListings(): Result<List<Listing>> = runCatching {
        listingsCollection.whereEqualTo("status", "available").get().await()
            .documents.map { mapListingDocument(it) }
    }

    /** Satu listing by-id — dipakai Detail Produk. */
    suspend fun getListingById(listingId: String): Result<Listing> = runCatching {
        mapListingDocument(listingsCollection.document(listingId).get().await())
    }

    private fun mapListingDocument(doc: DocumentSnapshot): Listing = Listing(
        id = doc.id,
        farmId = doc.getString("farm_id").orEmpty(),
        plotId = doc.getString("plot_id").orEmpty(),
        cropType = doc.getString("crop_type").orEmpty(),
        quantityKg = doc.getDouble("quantity_kg") ?: 0.0,
        pricePerKg = doc.getLong("price_per_kg") ?: 0L,
        healthScore = doc.getDouble("health_score") ?: 0.0,
        harvestDate = doc.getString("harvest_date").orEmpty(),
        status = doc.getString("status").orEmpty(),
        createdAt = doc.getString("created_at").orEmpty(),
        description = doc.getString("description").orEmpty(),
        preOrderEnabled = doc.getBoolean("pre_order_enabled") ?: false,
        productName = doc.getString("product_name").orEmpty(),
        imageUrls = (doc.get("image_urls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}
