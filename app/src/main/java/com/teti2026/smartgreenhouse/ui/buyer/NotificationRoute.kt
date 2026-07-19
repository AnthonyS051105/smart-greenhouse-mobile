package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// TODO: pindahkan state & data ke NotificationViewModel yang mengambil dari FCM/Firestore
// begitu MOB-T24 dikerjakan. [currentBottomNavRoute] dioper dari NavGraph sebagai "" (lihat
// Routes.BUYER_NOTIFICATIONS) karena Notifikasi bukan salah satu dari 4 tab BuyerBottomNavBar.
@Composable
fun NotificationRoute(
    onBackClick: () -> Unit = {},
    currentBottomNavRoute: String = "",
    onBottomNavigate: (String) -> Unit = {}
) {
    var groups by remember { mutableStateOf(sampleNotificationGroups) }

    NotificationScreen(
        groups = groups,
        onBackClick = onBackClick,
        // Menandai notifikasi sebagai sudah dibaca saat di-tap (hilangkan tint & titik merah) —
        // interaksi minimal yang masuk akal tanpa perlu destination baru per jenis notifikasi.
        onNotificationClick = { id ->
            groups = groups.map { group ->
                group.copy(items = group.items.map { item ->
                    if (item.id == id) item.copy(isUnread = false) else item
                })
            }
        },
        currentBottomNavRoute = currentBottomNavRoute,
        onBottomNavigate = onBottomNavigate
    )
}
