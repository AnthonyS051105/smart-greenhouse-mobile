package com.teti2026.smartgreenhouse.ui.buyer

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

private const val BUYER_AVATAR_PLACEHOLDER_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCD1B_NV9iV_BpMZRDPgemryrKcmsJNPCKF8SU9sjDo2uraOypc0ct7ouMqUmTvu3Lroc4a_BNbvzSwWvAE9tn0TjNfzgAefbl9E6_trlUglrxbF6EfNZIeyTjsDLGCFmagXEm4VvqO141LEcohECfshJTGe9YsEYwBLNob7qkszI0k_yJ6_WTXeGt7HDSWENUl6rnNoplo1bSFMlZ7XZrnIVD7igzm6dObZru_nLgWpF6KoqzRLBHzDbZSPQSolszBxmYnegXPWQ"

/**
 * Wrapper stateful — nama pembeli sungguhan dari [ProfileViewModel] (Firestore `users`),
 * [totalOrders]/[favoriteStores] sungguhan dari `orders` ([ProfileUiState.Success.totalOrders]/
 * [ProfileUiState.Success.favoriteStores]). [buyerBadgeLabel] tetap label generik statis
 * ("Pembeli") — belum ada sistem badge/tier. [avatarUrl] juga tetap placeholder statis, sama
 * seperti [com.teti2026.smartgreenhouse.ui.farmer.ProfileFarmerRoute].
 */
@Composable
fun ProfileBuyerRoute(
    onOrdersClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLogoutConfirmed: () -> Unit = {},
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
            ProfileBuyerScreen(
                buyerName = s.user.name,
                buyerBadgeLabel = stringResource(R.string.profile_buyer_badge_default),
                avatarUrl = BUYER_AVATAR_PLACEHOLDER_URL,
                totalOrders = s.totalOrders ?: 0,
                favoriteStores = s.favoriteStores ?: 0,
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
    }
}
