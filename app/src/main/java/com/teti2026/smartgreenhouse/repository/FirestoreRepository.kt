package com.teti2026.smartgreenhouse.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.teti2026.smartgreenhouse.data.model.ChatMessage
import com.teti2026.smartgreenhouse.data.model.Farm
import com.teti2026.smartgreenhouse.data.model.Listing
import com.teti2026.smartgreenhouse.data.model.Order
import com.teti2026.smartgreenhouse.data.model.Plot
import com.teti2026.smartgreenhouse.data.model.Review
import com.teti2026.smartgreenhouse.data.model.SensorReading
import com.teti2026.smartgreenhouse.data.model.User
import com.teti2026.smartgreenhouse.data.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    private val chatMessagesCollection = firestore.collection("chat_messages")
    private val ordersCollection = firestore.collection("orders")
    private val reviewsCollection = firestore.collection("reviews")
    private val sensorReadingsCollection = firestore.collection("sensor_readings")

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
     * `plots` (data-contracts.md §3.2/§3.3). [deviceId] adalah `device_id` firmware IoT (string
     * bebas, mis. "gh-esp32-01", TIDAK diverifikasi ke ESP32 sungguhan di sini — lihat
     * `docs/Architecture.md §3`: pairing device fisik ada di firmware/backend).
     *
     * [plotId] dipakai LANGSUNG sebagai Firestore document ID plot (`plotsCollection.document(plotId)`,
     * BUKAN `.document()` auto-generate) — firmware IoT mengirim `plot_id` TETAP (hardcoded di
     * `config.h` saat flash), jadi plot harus dibuat dengan ID yang SAMA persis supaya
     * `sensor_readings` yang ditulis backend (dari MQTT) langsung terhubung tanpa langkah manual
     * di Firestore Console. Auto-generate ID sebelumnya membuat plot baru MUSTAHIL cocok dengan
     * `plot_id` firmware manapun — ditemukan saat testing manual end-to-end.
     */
    suspend fun createFarmWithPlot(
        ownerUid: String,
        farmName: String,
        farmSizeM2: Double,
        locationLat: Double,
        locationLng: Double,
        cropType: String,
        deviceId: String,
        plotId: String
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

        val plotRef = plotsCollection.document(plotId)
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

    /**
     * Listing berstatus "available" milik satu [farmId] — dipakai screen "Produk Lahan - Peta"
     * (`FarmProductsMapViewModel`). Dua `whereEqualTo` (farm_id + status) TIDAK butuh composite
     * index tambahan di Firestore Console (kombinasi filter equality-only otomatis didukung index
     * bawaan, beda dari kombinasi equality+range/orderBy yang perlu index komposit manual).
     */
    suspend fun getListingsForFarm(farmId: String): Result<List<Listing>> = runCatching {
        listingsCollection
            .whereEqualTo("farm_id", farmId)
            .whereEqualTo("status", "available")
            .get().await()
            .documents.map { mapListingDocument(it) }
    }

    /**
     * Listener realtime seluruh pesan satu thread ([threadId] = `chatThreadId(listingId, buyerUid)`,
     * lihat `util/ChatThreadId.kt`). Filter ganda ke [myUid] via [Filter.or] SECARA LOGIKA tidak
     * mengubah hasil (tiap pesan di thread ini pasti melibatkan [myUid] sebagai sender ATAU
     * receiver, by construction) — ditambahkan supaya Firestore Security Rules `chat_messages`
     * bisa MEMBUKTIKAN query ini aman tanpa composite index: rules mensyaratkan filter eksplisit
     * ke `sender_uid`/`receiver_uid`, filter `thread_id` saja tidak cukup dibuktikan aman oleh
     * Firestore meski secara semantik sudah pasti benar (query ditolak permission-denied kalau
     * filter ini dihapus). Diurutkan CLIENT-SIDE by [ChatMessage.sentAt] (string ISO 8601 UTC,
     * urut leksikografis = urut waktu) — BUKAN `orderBy` Firestore, supaya kombinasi filter di
     * atas tetap equality-only (tidak butuh composite index tambahan yang harus dibuat manual di
     * Firebase Console).
     */
    fun getMessagesFlow(threadId: String, myUid: String): Flow<List<ChatMessage>> =
        chatMessagesQueryFlow(
            chatMessagesCollection
                .whereEqualTo("thread_id", threadId)
                .where(Filter.or(Filter.equalTo("sender_uid", myUid), Filter.equalTo("receiver_uid", myUid)))
        ).map { messages -> messages.sortedBy { it.sentAt } }

    /** Kirim satu pesan baru ke thread [threadId] (lihat `chatThreadId` di `util/ChatThreadId.kt`). */
    suspend fun sendMessage(
        threadId: String,
        listingId: String,
        senderUid: String,
        receiverUid: String,
        message: String
    ): Result<Unit> = runCatching {
        val ref = chatMessagesCollection.document()
        ref.set(
            mapOf(
                "id" to ref.id,
                "thread_id" to threadId,
                "sender_uid" to senderUid,
                "receiver_uid" to receiverUid,
                "listing_id" to listingId,
                "message" to message,
                "sent_at" to Instant.now().toString()
            )
        ).await()
    }

    /**
     * Realtime seluruh `chat_messages` yang melibatkan [uid] (sebagai sender ATAU receiver)
     * lintas thread — dipakai daftar percakapan (`ChatListViewModel`/`FarmerChatListViewModel`,
     * dikelompokkan client-side per `thread_id`, ambil pesan terbaru per grup) supaya baris
     * "pesan terakhir" ikut ter-update live saat lawan bicara membalas, PERSIS seperti thread
     * individual ([getMessagesFlow]) — sebelumnya (`getMessagesForUser`, sekali baca) ini
     * dilaporkan user TIDAK ikut ter-update sampai layar dibuka ulang, root cause dari gap
     * tersebut. Dua listener terpisah (equality TUNGGAL per query, otomatis rule-safe tanpa perlu
     * [Filter.or]) digabung [combine] + dedup client-side — beda dari [getMessagesFlow] yang bisa
     * satu listener [Filter.or] karena ada `thread_id` sebagai pembatas bersama; di sini tidak ada
     * pembatas semacam itu (mencakup SELURUH thread milik [uid]).
     */
    fun getMessagesForUserFlow(uid: String): Flow<List<ChatMessage>> {
        val sentFlow = chatMessagesQueryFlow(chatMessagesCollection.whereEqualTo("sender_uid", uid))
        val receivedFlow = chatMessagesQueryFlow(chatMessagesCollection.whereEqualTo("receiver_uid", uid))
        return combine(sentFlow, receivedFlow) { sent, received -> (sent + received).distinctBy { it.id } }
    }

    /** Listener realtime generik satu [query] `chat_messages` → daftar [ChatMessage], dipakai [getMessagesFlow]/[getMessagesForUserFlow]. */
    private fun chatMessagesQueryFlow(query: Query): Flow<List<ChatMessage>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents.orEmpty().map { mapChatMessageDocument(it) })
        }
        awaitClose { registration.remove() }
    }

    /**
     * Listener realtime `sensor_readings` milik [plotId], [limit] dokumen terbaru diurutkan
     * `timestamp` descending (index bawaan cukup: satu `whereEqualTo` + satu `orderBy` field yang
     * sama-sama dipakai filter equality tunggal — TIDAK butuh composite index manual, beda dari
     * kasus `getMessagesFlow` yang memang butuh `Filter.or`). Dashboard menampilkan data IoT
     * TERBARU secara otomatis begitu backend menulis dokumen baru (dari MQTT `sensor_service.
     * save_sensor_reading`) — TANPA polling REST, sesuai `docs/Architecture.md §3` (backend
     * bukan sumber baca data terstruktur, mobile baca Firestore langsung).
     */
    fun observeSensorReadings(plotId: String, limit: Long = 20): Flow<List<SensorReading>> = callbackFlow {
        val registration = sensorReadingsCollection
            .whereEqualTo("plot_id", plotId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().map { mapSensorReadingDocument(it) })
            }
        awaitClose { registration.remove() }
    }

    /**
     * Simpan pesanan baru (data-contracts.md §3.8). [status] awal SELALU "pending" — diubah
     * petani lewat [updateOrderStatus] di screen "Pesanan Masuk". [sellerUid] (pemilik farm dari
     * `listingId`, di-resolve `CheckoutViewModel` sebelum memanggil ini) disimpan sebagai field
     * mobile-only supaya [getOrdersForSeller] bisa query rule-safe tanpa `get()` bersarang. [totalPrice]
     * adalah nominal LENGKAP yang ditagih ke pembeli (subtotal + biaya layanan + ongkir bila ada) —
     * dihitung di `CheckoutViewModel`, bukan di sini, supaya repository tidak perlu tahu aturan
     * biaya UI. TIDAK mengurangi `listings.quantity_kg` (stok) setelah order dibuat — di luar
     * lingkup versi bootcamp (checkout memang disederhanakan tanpa payment gateway, lihat
     * `docs/PRD.md §5.2`; mencegah overselling butuh Firestore transaction, ditunda sampai
     * benar-benar dibutuhkan).
     */
    suspend fun createOrder(
        buyerUid: String,
        sellerUid: String,
        listingId: String,
        quantityKg: Double,
        totalPrice: Long
    ): Result<Order> = runCatching {
        val ref = ordersCollection.document()
        val order = Order(
            id = ref.id,
            buyerUid = buyerUid,
            sellerUid = sellerUid,
            listingId = listingId,
            quantityKg = quantityKg,
            totalPrice = totalPrice,
            status = "pending",
            createdAt = Instant.now().toString()
        )
        ref.set(
            mapOf(
                "id" to order.id,
                "buyer_uid" to order.buyerUid,
                "seller_uid" to order.sellerUid,
                "listing_id" to order.listingId,
                "quantity_kg" to order.quantityKg,
                "total_price" to order.totalPrice,
                "status" to order.status,
                "created_at" to order.createdAt
            )
        ).await()
        order
    }

    /** Seluruh pesanan milik [buyerUid] — dipakai Riwayat Pesanan & statistik Profil Pembeli. */
    suspend fun getOrdersForBuyer(buyerUid: String): Result<List<Order>> = runCatching {
        ordersCollection.whereEqualTo("buyer_uid", buyerUid).get().await()
            .documents.map { mapOrderDocument(it) }
    }

    /** Seluruh pesanan MASUK milik [sellerUid] (petani) — dipakai screen "Pesanan Masuk". */
    suspend fun getOrdersForSeller(sellerUid: String): Result<List<Order>> = runCatching {
        ordersCollection.whereEqualTo("seller_uid", sellerUid).get().await()
            .documents.map { mapOrderDocument(it) }
    }

    /** Satu order by-id — dipakai "Konfirmasi Pesanan - Berhasil" & Beri Rating. */
    suspend fun getOrderById(orderId: String): Result<Order> = runCatching {
        mapOrderDocument(ordersCollection.document(orderId).get().await())
    }

    /**
     * Ubah `status` satu order (Petani konfirmasi/tandai-selesai/tolak dari "Pesanan Masuk").
     * Firestore Security Rules `orders` HANYA mengizinkan field `status` yang berubah lewat
     * update (lihat `docs/firestore.rules`) — cocok dengan `.update()` (bukan `.set()`) yang
     * hanya menyentuh satu field ini, bukan menulis ulang dokumen penuh.
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = runCatching {
        ordersCollection.document(orderId).update("status", status).await()
    }

    /**
     * Simpan ulasan baru (data-contracts.md §3.10) untuk pesanan [orderId] yang sudah "completed".
     * Kepemilikan pembeli dibuktikan Firestore Security Rules lewat `get()` bersarang ke
     * `orders/{orderId}.buyer_uid` (`docs/firestore.rules reviews.create`) — TIDAK ada
     * `buyer_uid`/`listing_id` di skema `reviews` itu sendiri, jadi tidak perlu dikirim di sini.
     * Tidak ada pengecekan sisi klien apakah [orderId] sudah pernah direview sebelumnya (rules
     * juga tidak mencegah ulasan ganda) — di luar lingkup MOB-T23 saat ini.
     */
    suspend fun createReview(orderId: String, rating: Int, comment: String): Result<Review> = runCatching {
        val ref = reviewsCollection.document()
        val review = Review(
            id = ref.id,
            orderId = orderId,
            rating = rating,
            comment = comment,
            createdAt = Instant.now().toString()
        )
        ref.set(
            mapOf(
                "id" to review.id,
                "order_id" to review.orderId,
                "rating" to review.rating,
                "comment" to review.comment,
                "created_at" to review.createdAt
            )
        ).await()
        review
    }

    private fun mapOrderDocument(doc: DocumentSnapshot): Order = Order(
        id = doc.id,
        buyerUid = doc.getString("buyer_uid").orEmpty(),
        sellerUid = doc.getString("seller_uid").orEmpty(),
        listingId = doc.getString("listing_id").orEmpty(),
        quantityKg = doc.getDouble("quantity_kg") ?: 0.0,
        totalPrice = doc.getLong("total_price") ?: 0L,
        status = doc.getString("status").orEmpty(),
        createdAt = doc.getString("created_at").orEmpty()
    )

    private fun mapSensorReadingDocument(doc: DocumentSnapshot): SensorReading = SensorReading(
        id = doc.id,
        plotId = doc.getString("plot_id").orEmpty(),
        timestampMillis = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
        temperature = doc.getDouble("temperature"),
        humidity = doc.getDouble("humidity"),
        soilMoisture = doc.getDouble("soil_moisture"),
        lightIntensity = doc.getDouble("light_intensity")
    )

    private fun mapChatMessageDocument(doc: DocumentSnapshot): ChatMessage = ChatMessage(
        id = doc.id,
        threadId = doc.getString("thread_id").orEmpty(),
        senderUid = doc.getString("sender_uid").orEmpty(),
        receiverUid = doc.getString("receiver_uid").orEmpty(),
        listingId = doc.getString("listing_id").orEmpty(),
        message = doc.getString("message").orEmpty(),
        sentAt = doc.getString("sent_at").orEmpty()
    )

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
