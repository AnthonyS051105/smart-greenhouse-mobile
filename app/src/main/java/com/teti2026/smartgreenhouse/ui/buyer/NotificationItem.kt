package com.teti2026.smartgreenhouse.ui.buyer

/**
 * Jenis notifikasi menentukan ikon & warna badge (lihat `NotificationScreen.kt`,
 * `notificationVisualsFor`). Mockup Stitch "Notifikasi - AgriSmart" bersifat generik lintas role
 * (contoh konten aslinya bicara soal irigasi/sensor greenhouse) — karena screen ini dijangkau dari
 * Marketplace sisi Pembeli, konten sampel di [sampleNotificationGroups] ditulis ulang dari sudut
 * pandang Pembeli (health_score produk yang dipantau, status pesanan, chat, pengiriman, stok)
 * sesuai MOB-FR-21 (`mobile/docs/SRS.md §3.3`), sementara pola ikon+warna tiap tipe tetap
 * mengikuti mockup asli.
 */
enum class NotificationType { AI_INSIGHT, ORDER, CHAT, SHIPPING, STOCK_ALERT }

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val message: String,
    val timeLabel: String,
    val isUnread: Boolean
)

data class NotificationGroup(
    val dateLabel: String,
    val items: List<NotificationItem>
)

// TODO: ganti dengan data notifikasi FCM/Firestore sungguhan begitu MOB-T24 dikerjakan.
val sampleNotificationGroups = listOf(
    NotificationGroup(
        dateLabel = "Hari Ini",
        items = listOf(
            NotificationItem(
                id = "notif-1",
                type = NotificationType.AI_INSIGHT,
                message = "Skor kesehatan Cabai Rawit Merah yang Anda pantau naik menjadi 92 (AI)",
                timeLabel = "08:30",
                isUnread = true
            ),
            NotificationItem(
                id = "notif-2",
                type = NotificationType.ORDER,
                message = "Pesanan Tomat Merah Segar Anda telah dikonfirmasi petani",
                timeLabel = "10:15",
                isUnread = false
            ),
            NotificationItem(
                id = "notif-3",
                type = NotificationType.CHAT,
                message = "Pesan baru dari Pak Budi",
                timeLabel = "14:20",
                isUnread = true
            )
        )
    ),
    NotificationGroup(
        dateLabel = "Kemarin",
        items = listOf(
            NotificationItem(
                id = "notif-4",
                type = NotificationType.SHIPPING,
                message = "Pesanan Cabai Merah Anda sedang dalam pengiriman",
                timeLabel = "Kemarin",
                isUnread = false
            ),
            NotificationItem(
                id = "notif-5",
                type = NotificationType.STOCK_ALERT,
                message = "Stok Bayam Cabut Organik favorit Anda tinggal sedikit",
                timeLabel = "Kemarin",
                isUnread = false
            )
        )
    )
)
