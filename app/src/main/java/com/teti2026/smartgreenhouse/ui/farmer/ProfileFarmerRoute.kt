package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.runtime.Composable
import com.teti2026.smartgreenhouse.ui.navigation.Routes

// TODO: pindahkan data ke ProfileViewModel (StateFlow<UiState>) yang mengambil data user dari
// AuthRepository/Firestore `users` begitu MOB-T06 dikerjakan. Untuk sekarang nama & role masih
// data sampel statis (sama seperti pola DashboardFarmerRoute).
@Composable
fun ProfileFarmerRoute(
    onMyGreenhousesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
) {
    ProfileFarmerScreen(
        farmerName = "Pak Budi",
        farmerRoleLabel = "Pemilik Greenhouse Cabai",
        avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBuPbYtc5CLdU1ViXIE9bHl7xPH9O0lxirPo6u9EtU4y5G-0uylLRH_4FIHiAVqmXi8i2mT8JYeu1CjeYJzX0LHv8guL3-1c_Hd7uyQ0x5Zg82pbUrU2DIMNbKhwbGwLu6I-lUeexsXsQyd5e9IJ6dVmIBZ3_QcGYI1tzij5_V3PvqPbMy9_mHWoiZq5WSzkn9Y1vFZig8DDR2qGIBb2XtoH49NMh0uEtSFHHeCKbztlzteyJRJjR_B1Z_0orR2PkKv4OjQ-AKtfw",
        onEditProfileClick = { /* TODO: navigasi ke Edit Profil saat screen dibuat */ },
        onMyGreenhousesClick = onMyGreenhousesClick,
        onLanguageClick = { /* TODO: navigasi ke pengaturan Bahasa saat screen dibuat */ },
        onNotificationsClick = onNotificationsClick,
        onHelpClick = { /* TODO: navigasi ke Bantuan saat screen dibuat */ },
        onLogoutClick = onLogoutClick,
        currentBottomNavRoute = Routes.FARMER_PROFILE,
        onBottomNavigate = onBottomNavigate
    )
}
