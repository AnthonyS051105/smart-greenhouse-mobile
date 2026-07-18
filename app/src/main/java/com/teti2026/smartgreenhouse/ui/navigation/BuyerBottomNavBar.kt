package com.teti2026.smartgreenhouse.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teti2026.smartgreenhouse.ui.theme.MintTint

/**
 * Tab bawah App Pembeli. Sesuai `docs/UIUX-Flow.md §2`: Marketplace | Peta | Pesanan | Profil.
 * [Routes.BUYER_MARKETPLACE] & [Routes.BUYER_MAP] sudah punya destination — tab Pesanan/Profil
 * tampil (sesuai desain Stitch) tapi belum menavigasi ke mana pun (lihat TODO di NavGraph.kt).
 */
enum class BuyerBottomNavItem(val route: String, val label: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector) {
    MARKETPLACE(Routes.BUYER_MARKETPLACE, "Pasar", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    MAP(Routes.BUYER_MAP, "Peta", Icons.Filled.Map, Icons.Outlined.Map),
    ORDERS("buyer/orders", "Pesanan", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    PROFILE("buyer/profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround
        ) {
            BuyerBottomNavItem.entries.forEach { item ->
                val selected = item.route == currentRoute
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNavigate(item.route) }
                        .background(
                            color = if (selected) MintTint else Color.Transparent,
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.label,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selected) {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
