package com.teti2026.smartgreenhouse.ui.farmer

/**
 * Jenis notifikasi menentukan ikon & warna badge (lihat `NotificationFarmerScreen.kt`,
 * `notificationVisualsFor`) — padanan [com.teti2026.smartgreenhouse.ui.buyer.NotificationType],
 * enum sama persis (tipe generik lintas role, sesuai mockup Stitch "Notifikasi - AgriSmart").
 */
enum class NotificationFarmerType { AI_INSIGHT, ORDER, CHAT, SHIPPING, STOCK_ALERT }

data class NotificationFarmerItem(
    val id: String,
    val type: NotificationFarmerType,
    val message: String,
    val timeLabel: String,
    val isUnread: Boolean
)

data class NotificationFarmerGroup(
    val dateLabel: String,
    val items: List<NotificationFarmerItem>
)

// TODO: ganti dengan data notifikasi FCM/Firestore sungguhan begitu MOB-T24 dikerjakan — padanan
// TODO [com.teti2026.smartgreenhouse.ui.buyer.sampleNotificationGroups]. Konten ditulis ulang dari
// sudut pandang Petani (chat pembeli, status pesanan masuk, insight AI kesehatan lahan, jadwal
// panen/pengiriman, peringatan stok listing) sesuai MOB-FR-21 (`mobile/docs/SRS.md §3.3`), pola
// ikon+warna tiap tipe tetap mengikuti mockup asli.
val sampleFarmerNotificationGroups = listOf(
    NotificationFarmerGroup(
        dateLabel = "Hari Ini",
        items = listOf(
            NotificationFarmerItem(
                id = "farmer-notif-1",
                type = NotificationFarmerType.AI_INSIGHT,
                message = "Skor kesehatan lahan Cabai Rawit Merah naik menjadi 92 (AI)",
                timeLabel = "08:30",
                isUnread = true
            ),
            NotificationFarmerItem(
                id = "farmer-notif-2",
                type = NotificationFarmerType.ORDER,
                message = "Pesanan baru masuk untuk Tomat Merah Segar dari Dewi Anggraini",
                timeLabel = "10:15",
                isUnread = true
            ),
            NotificationFarmerItem(
                id = "farmer-notif-3",
                type = NotificationFarmerType.CHAT,
                message = "Pesan baru dari Rina Wijaya",
                timeLabel = "14:20",
                isUnread = true
            )
        )
    ),
    NotificationFarmerGroup(
        dateLabel = "Kemarin",
        items = listOf(
            NotificationFarmerItem(
                id = "farmer-notif-4",
                type = NotificationFarmerType.SHIPPING,
                message = "Pesanan Cabai Merah untuk Hendra Saputra siap dikirim",
                timeLabel = "Kemarin",
                isUnread = false
            ),
            NotificationFarmerItem(
                id = "farmer-notif-5",
                type = NotificationFarmerType.STOCK_ALERT,
                message = "Stok listing Bayam Cabut Organik tinggal sedikit, segera perbarui",
                timeLabel = "Kemarin",
                isUnread = false
            )
        )
    )
)
