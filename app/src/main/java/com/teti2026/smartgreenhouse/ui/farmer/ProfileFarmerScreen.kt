package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.FarmerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Screen "Profil & Pengaturan - Petani" dari Stitch. Stateless: seluruh data & event
 * di-hoist ke caller (nantinya ProfileViewModel + AuthRepository, lihat `docs/SDD.md §4.1/§5`).
 *
 * [onMyGreenhousesClick] adalah pintu masuk "Greenhouse Saya", tempat petani menambah lahan
 * baru (memulai ulang flow Setup Greenhouse) — sesuai keputusan menempatkan "Tambah Lahan Baru"
 * di Profil, bukan di tombol "+" navbar (yang direservasi untuk "Buat Listing" saat dibuat nanti).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileFarmerScreen(
    farmerName: String,
    farmerRoleLabel: String,
    avatarUrl: String?,
    onEditProfileClick: () -> Unit,
    onMyGreenhousesClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_farmer_top_bar_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.profile_farmer_notifications_content_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            FarmerBottomNavBar(
                currentRoute = currentBottomNavRoute,
                onNavigate = onBottomNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            ProfileHeader(
                farmerName = farmerName,
                farmerRoleLabel = farmerRoleLabel,
                avatarUrl = avatarUrl,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Filled.Person,
                        label = stringResource(R.string.profile_farmer_menu_edit_profile),
                        onClick = onEditProfileClick
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.Eco,
                        label = stringResource(R.string.profile_farmer_menu_my_greenhouses),
                        onClick = onMyGreenhousesClick
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.Language,
                        label = stringResource(R.string.profile_farmer_menu_language),
                        trailingText = stringResource(R.string.profile_farmer_menu_language_value),
                        onClick = onLanguageClick
                    )
                    ProfileMenuItem(
                        icon = Icons.Filled.Notifications,
                        label = stringResource(R.string.profile_farmer_menu_notifications),
                        onClick = onNotificationsClick
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        label = stringResource(R.string.profile_farmer_menu_help),
                        onClick = onHelpClick,
                        showDivider = false
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogoutClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = stringResource(R.string.profile_farmer_menu_logout),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    farmerName: String,
    farmerRoleLabel: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.size(80.dp)) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.profile_farmer_avatar_content_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
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
                        contentDescription = stringResource(R.string.profile_farmer_avatar_content_description),
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = farmerName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = farmerRoleLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp).weight(1f)
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
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ProfileFarmerScreenPreview() {
    SmartgreenhousemobileTheme {
        ProfileFarmerScreen(
            farmerName = "Pak Budi",
            farmerRoleLabel = "Pemilik Greenhouse Cabai",
            avatarUrl = null,
            onEditProfileClick = {},
            onMyGreenhousesClick = {},
            onLanguageClick = {},
            onNotificationsClick = {},
            onHelpClick = {},
            onLogoutClick = {},
            currentBottomNavRoute = Routes.FARMER_PROFILE,
            onBottomNavigate = {}
        )
    }
}
