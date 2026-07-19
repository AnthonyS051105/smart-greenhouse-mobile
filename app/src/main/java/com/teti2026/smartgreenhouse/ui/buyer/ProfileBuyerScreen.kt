package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.BuyerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.MintTint
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Profil & Pengaturan - Pembeli" dari Stitch. Stateless: seluruh data & event di-hoist ke
 * caller (nantinya ProfileViewModel + AuthRepository, lihat `docs/SDD.md §4.1/§5`). Dijangkau dari
 * tab "Profil" [BuyerBottomNavBar] — padanan [Routes.FARMER_PROFILE] sisi Petani.
 *
 * Mockup Stitch tidak punya TopAppBar khusus di mobile (header web disembunyikan di layar kecil,
 * diganti judul "Profil Saya" biasa) — beda dari [ProfileFarmerScreen] yang punya TopAppBar dengan
 * ikon Eco + lonceng notifikasi. Layout kartu (bento/glass) & tiga grup menu (Akun/Preferensi/
 * Bantuan) mengikuti mockup persis; hanya "Pesanan Saya" & "Notifikasi" yang punya destination
 * nyata sejauh ini — item lain (Edit Profil, Alamat Tersimpan, Metode Pembayaran, Bahasa, Pusat
 * Bantuan) belum punya screen di Stitch, jadi callback-nya TODO no-op (sama pola dengan
 * [ProfileFarmerRoute]).
 *
 * Konfirmasi "Keluar": state tampil/tidaknya dialog dipegang LOKAL di composable ini
 * ([showLogoutDialog]) — bukan data bisnis, sama seperti pola `showManualModeInfo` di
 * `IrrigationControlScreen`. [onLogoutConfirmed] hanya dipanggil setelah user menekan "Ya".
 */
@Composable
fun ProfileBuyerScreen(
    buyerName: String,
    buyerBadgeLabel: String,
    avatarUrl: String?,
    totalOrders: Int,
    favoriteStores: Int,
    onEditProfileClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onSavedAddressesClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BuyerBottomNavBar(currentRoute = currentBottomNavRoute, onNavigate = onBottomNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_buyer_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            ProfileBuyerHeaderCard(
                buyerName = buyerName,
                buyerBadgeLabel = buyerBadgeLabel,
                avatarUrl = avatarUrl,
                totalOrders = totalOrders,
                favoriteStores = favoriteStores,
                onEditAvatarClick = onEditProfileClick
            )

            ProfileBuyerGroup(
                title = stringResource(R.string.profile_buyer_group_account),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                ProfileBuyerMenuItem(
                    icon = Icons.Filled.Person,
                    label = stringResource(R.string.profile_buyer_menu_edit_profile),
                    onClick = onEditProfileClick
                )
                ProfileBuyerMenuItem(
                    icon = Icons.Filled.ShoppingBag,
                    label = stringResource(R.string.profile_buyer_menu_orders),
                    onClick = onOrdersClick
                )
                ProfileBuyerMenuItem(
                    icon = Icons.Filled.LocationOn,
                    label = stringResource(R.string.profile_buyer_menu_saved_addresses),
                    onClick = onSavedAddressesClick
                )
                ProfileBuyerMenuItem(
                    icon = Icons.Filled.Payments,
                    label = stringResource(R.string.profile_buyer_menu_payment_methods),
                    onClick = onPaymentMethodsClick,
                    showDivider = false
                )
            }

            ProfileBuyerGroup(
                title = stringResource(R.string.profile_buyer_group_preferences),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                ProfileBuyerMenuItem(
                    icon = Icons.Filled.Language,
                    label = stringResource(R.string.profile_buyer_menu_language),
                    onClick = onLanguageClick,
                    trailingText = stringResource(R.string.profile_buyer_menu_language_value)
                )
                ProfileBuyerMenuItem(
                    icon = Icons.Filled.Notifications,
                    label = stringResource(R.string.profile_buyer_menu_notifications),
                    onClick = onNotificationsClick,
                    showDivider = false
                )
            }

            ProfileBuyerGroup(
                title = stringResource(R.string.profile_buyer_group_help),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                ProfileBuyerMenuItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = stringResource(R.string.profile_buyer_menu_help_center),
                    onClick = onHelpClick,
                    showDivider = false
                )
            }

            ProfileBuyerLogoutButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            )
        }
    }

    if (showLogoutDialog) {
        ProfileBuyerLogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogoutConfirmed()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun ProfileBuyerHeaderCard(
    buyerName: String,
    buyerBadgeLabel: String,
    avatarUrl: String?,
    totalOrders: Int,
    favoriteStores: Int,
    onEditAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp)) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.profile_buyer_avatar_content_description),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = stringResource(R.string.profile_buyer_avatar_content_description),
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onEditAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.profile_buyer_edit_avatar_content_description),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = buyerName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MintTint,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = buyerBadgeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 20.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileBuyerStat(
                    value = totalOrders.toString(),
                    label = stringResource(R.string.profile_buyer_stat_orders_label),
                    modifier = Modifier.weight(1f)
                )
                ProfileBuyerStat(
                    value = favoriteStores.toString(),
                    label = stringResource(R.string.profile_buyer_stat_favorites_label),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileBuyerStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileBuyerGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ProfileBuyerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailingText: String? = null,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ProfileBuyerLogoutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(R.string.profile_buyer_menu_logout),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ProfileBuyerLogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(text = stringResource(R.string.profile_buyer_logout_dialog_title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.profile_buyer_logout_dialog_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_buyer_logout_dialog_dismiss))
            }
        }
    )
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ProfileBuyerScreenPreview() {
    SmartgreenhousemobileTheme {
        ProfileBuyerScreen(
            buyerName = "Sari Wulandari",
            buyerBadgeLabel = "Pembeli Setia",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCD1B_NV9iV_BpMZRDPgemryrKcmsJNPCKF8SU9sjDo2uraOypc0ct7ouMqUmTvu3Lroc4a_BNbvzSwWvAE9tn0TjNfzgAefbl9E6_trlUglrxbF6EfNZIeyTjsDLGCFmagXEm4VvqO141LEcohECfshJTGe9YsEYwBLNob7qkszI0k_yJ6_WTXeGt7HDSWENUl6rnNoplo1bSFMlZ7XZrnIVD7igzm6dObZru_nLgWpF6KoqzRLBHzDbZSPQSolszBxmYnegXPWQ",
            totalOrders = 24,
            favoriteStores = 5,
            onEditProfileClick = {},
            onOrdersClick = {},
            onSavedAddressesClick = {},
            onPaymentMethodsClick = {},
            onLanguageClick = {},
            onNotificationsClick = {},
            onHelpClick = {},
            onLogoutConfirmed = {},
            currentBottomNavRoute = Routes.BUYER_PROFILE,
            onBottomNavigate = {}
        )
    }
}
