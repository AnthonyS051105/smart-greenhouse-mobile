package com.teti2026.smartgreenhouse.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.MintTint

/**
 * Tab bawah App Petani, sesuai `docs/UIUX-Flow.md §2`: Dashboard | Listing | Chat | Profil
 * (ditambah pintasan "Tambah" sesuai desain Stitch "Dashboard Monitoring (Revised)").
 *
 * Disusun ulang dari referensi Stitch (yang memakai FAB melayang `-top-3` di luar bar,
 * memotong batas atas) menjadi 5 item sejajar rata tinggi dalam satu baris — tetap
 * mempertahankan aksen lingkaran hijau solid untuk "Tambah" di tengah (item paling sering
 * dipakai petani untuk membuat listing baru), tapi dikurung rapi di dalam tinggi bar yang
 * sama supaya komposisi keseluruhan tetap simetris, bukan menonjol keluar seperti FAB lepas.
 */
enum class FarmerBottomNavItem(
    val route: String,
    val labelRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    DASHBOARD(Routes.FARMER_DASHBOARD, R.string.dashboard_nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    HISTORY("farmer/history", R.string.dashboard_nav_history, Icons.Filled.History, Icons.Outlined.History),
    CREATE_LISTING("farmer/listing/create", R.string.dashboard_nav_create_listing, Icons.Filled.Add, Icons.Filled.Add),
    CHAT("farmer/chat", R.string.dashboard_nav_chat, Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    PROFILE("farmer/profile", R.string.dashboard_nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun FarmerBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FarmerBottomNavItem.entries.forEach { item ->
                val selected = item.route == currentRoute
                if (item == FarmerBottomNavItem.CREATE_LISTING) {
                    CreateListingNavItem(
                        item = item,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    StandardNavItem(
                        item = item,
                        selected = selected,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardNavItem(
    item: FarmerBottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (selected) MintTint else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 8.dp, horizontal = 2.dp)
    ) {
        Icon(
            imageVector = if (selected) item.filledIcon else item.outlinedIcon,
            contentDescription = stringResource(item.labelRes),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * Item "Tambah" tengah — aksen lingkaran hijau solid (padanan FAB desain Stitch), tapi
 * ditata di dalam ketinggian bar yang sama (bukan melayang `-top-3` ke luar) agar baris
 * bottom nav tetap satu garis simetris dari ujung ke ujung.
 */
@Composable
private fun CreateListingNavItem(
    item: FarmerBottomNavItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        ) {
            Icon(
                imageVector = item.filledIcon,
                contentDescription = stringResource(item.labelRes),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
