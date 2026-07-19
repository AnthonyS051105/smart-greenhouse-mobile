package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan data ke ProfileViewModel (StateFlow<UiState>) yang mengambil data user & statistik
// pesanan dari AuthRepository/Firestore begitu MOB-T06 dikerjakan. Untuk sekarang nama, badge, avatar
// & statistik masih data sampel statis (sama seperti pola ProfileFarmerRoute).
@Composable
fun ProfileBuyerRoute(
    onOrdersClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutConfirmed: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
) {
    ProfileBuyerScreen(
        buyerName = "Sari Wulandari",
        buyerBadgeLabel = "Pembeli Setia",
        avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCD1B_NV9iV_BpMZRDPgemryrKcmsJNPCKF8SU9sjDo2uraOypc0ct7ouMqUmTvu3Lroc4a_BNbvzSwWvAE9tn0TjNfzgAefbl9E6_trlUglrxbF6EfNZIeyTjsDLGCFmagXEm4VvqO141LEcohECfshJTGe9YsEYwBLNob7qkszI0k_yJ6_WTXeGt7HDSWENUl6rnNoplo1bSFMlZ7XZrnIVD7igzm6dObZru_nLgWpF6KoqzRLBHzDbZSPQSolszBxmYnegXPWQ",
        totalOrders = 24,
        favoriteStores = 5,
        onEditProfileClick = { /* TODO: navigasi ke Edit Profil saat screen dibuat */ },
        onOrdersClick = onOrdersClick,
        onSavedAddressesClick = { /* TODO: navigasi ke Alamat Tersimpan saat screen dibuat */ },
        onPaymentMethodsClick = { /* TODO: navigasi ke Metode Pembayaran saat screen dibuat */ },
        onLanguageClick = { /* TODO: navigasi ke pengaturan Bahasa saat screen dibuat */ },
        onNotificationsClick = onNotificationsClick,
        onHelpClick = { /* TODO: navigasi ke Pusat Bantuan saat screen dibuat */ },
        onLogoutConfirmed = onLogoutConfirmed,
        currentBottomNavRoute = Routes.BUYER_PROFILE,
        onBottomNavigate = onBottomNavigate
    )
}
