package com.teti2026.smartgreenhouse.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teti2026.smartgreenhouse.ui.theme.MintTint

/**
 * Tab bawah App Pembeli. Sesuai `docs/UIUX-Flow.md §2` (ditambah tab "Pesan", padanan
 * `FARMER_CHAT` sisi Petani): Marketplace | Peta | Pesanan | Pesan | Profil. Kelima tab
 * ([Routes.BUYER_MARKETPLACE], [Routes.BUYER_MAP], [Routes.BUYER_ORDERS],
 * [Routes.BUYER_CHAT_LIST] & [Routes.BUYER_PROFILE]) sudah punya destination di NavGraph.kt.
 *
 * Struktur & gaya visual item disamakan PERSIS dengan `StandardNavItem` di [FarmerBottomNavBar]
 * (permintaan user, sebelumnya berbeda): setiap item `Modifier.weight(1f)` (lebar sama rata di
 * dalam `Row` `Arrangement.SpaceBetween`, sehingga kotak highlight seragam ukurannya persis
 * seperti Petani — sebelumnya `Arrangement.SpaceAround` tanpa `weight` membuat kotak hanya
 * sebesar konten), kotak highlight terpilih `RoundedCornerShape(14.dp)` (sebelumnya pill
 * `percent = 50`), dan label teks SELALU tampil di bawah ikon baik terpilih maupun tidak
 * (sebelumnya hanya muncul saat item terpilih). App Pembeli tidak punya padanan item "Tambah"
 * (FAB lingkaran) — kelima item di sini semuanya "standar", tidak ada item yang dirender beda.
 */
enum class BuyerBottomNavItem(val route: String, val label: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector) {
    MARKETPLACE(Routes.BUYER_MARKETPLACE, "Pasar", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    MAP(Routes.BUYER_MAP, "Peta", Icons.Filled.Map, Icons.Outlined.Map),
    ORDERS(Routes.BUYER_ORDERS, "Pesanan", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    CHAT(Routes.BUYER_CHAT_LIST, "Pesan", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    PROFILE(Routes.BUYER_PROFILE, "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BuyerBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BuyerBottomNavItem.entries.forEach { item ->
                val selected = item.route == currentRoute
                BuyerStandardNavItem(
                    item = item,
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Padanan persis `StandardNavItem` di [FarmerBottomNavBar]. */
@Composable
private fun BuyerStandardNavItem(
    item: BuyerBottomNavItem,
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
            contentDescription = item.label,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = item.label,
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
