package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.viewmodel.ProfileUiState
import com.teti2026.smartgreenhouse.viewmodel.ProfileViewModel

private const val FARMER_AVATAR_PLACEHOLDER_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBuPbYtc5CLdU1ViXIE9bHl7xPH9O0lxirPo6u9EtU4y5G-0uylLRH_4FIHiAVqmXi8i2mT8JYeu1CjeYJzX0LHv8guL3-1c_Hd7uyQ0x5Zg82pbUrU2DIMNbKhwbGwLu6I-lUeexsXsQyd5e9IJ6dVmIBZ3_QcGYI1tzij5_V3PvqPbMy9_mHWoiZq5WSzkn9Y1vFZig8DDR2qGIBb2XtoH49NMh0uEtSFHHeCKbztlzteyJRJjR_B1Z_0orR2PkKv4OjQ-AKtfw"

/**
 * Wrapper stateful — data user (`nama`, subjudul "Pemilik <kebun>") diambil sungguhan dari
 * [ProfileViewModel] (Firestore `users`+`farms`). [avatarUrl] TETAP placeholder statis — belum
 * ada field foto profil di `data-contracts.md §3.1`/upload avatar (di luar lingkup sesi ini).
 */
@Composable
fun ProfileFarmerRoute(
    onMyGreenhousesClick: () -> Unit = {},
    onIncomingOrdersClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is ProfileUiState.Loading -> ProfileLoadingIndicator()
        is ProfileUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = viewModel::load
        )
        is ProfileUiState.Success -> {
            val roleLabel = s.farmName?.let { stringResource(R.string.profile_farmer_role_label_format, it) }
                ?: stringResource(R.string.profile_farmer_role_label_fallback)
            ProfileFarmerScreen(
                farmerName = s.user.name,
                farmerRoleLabel = roleLabel,
                avatarUrl = FARMER_AVATAR_PLACEHOLDER_URL,
                onEditProfileClick = { /* TODO: navigasi ke Edit Profil saat screen dibuat */ },
                onMyGreenhousesClick = onMyGreenhousesClick,
                onIncomingOrdersClick = onIncomingOrdersClick,
                onLanguageClick = { /* TODO: navigasi ke pengaturan Bahasa saat screen dibuat */ },
                onNotificationsClick = onNotificationsClick,
                onHelpClick = { /* TODO: navigasi ke Bantuan saat screen dibuat */ },
                onLogoutClick = onLogoutClick,
                currentBottomNavRoute = Routes.FARMER_PROFILE,
                onBottomNavigate = onBottomNavigate
            )
        }
    }
}
