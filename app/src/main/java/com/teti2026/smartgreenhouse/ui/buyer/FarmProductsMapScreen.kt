package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.HealthScoreBadge
import com.teti2026.smartgreenhouse.ui.components.healthScoreTierOf
import com.teti2026.smartgreenhouse.ui.navigation.BuyerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle

/**
 * Layar "Produk Lahan - Peta - Pembeli" (HTML referensi Stitch: "Peta Marketplace - AgriMarket",
 * bottom sheet varian farm-specific). Dijangkau dari tap kebun di `MapScreen` — bukan lagi
 * langsung ke satu listing utama (lihat catatan `MapFarmItem`), melainkan ke screen ini yang
 * menampilkan SELURUH produk kebun tersebut. Stateless: seluruh data & event di-hoist ke caller
 * (`FarmProductsMapRoute`), sama seperti `MapScreen`.
 */
@Composable
fun FarmProductsMapScreen(
    farm: MapFarmItem,
    products: List<ListingDetailItem>,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onSeeAllProductsClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val camera = rememberCameraState(
        firstPosition = CameraPosition(target = farm.position, zoom = MAP_FARM_FOCUS_ZOOM)
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BuyerBottomNavBar(
                currentRoute = currentBottomNavRoute,
                onNavigate = onBottomNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(MAP_STYLE_URL),
                cameraState = camera,
                options = MapOptions(ornamentOptions = OrnamentOptions(isCompassEnabled = false))
            )

            MapMarkerOverlay(
                cameraState = camera,
                position = farm.position,
                anchorSize = FARM_MARKER_SELECTED_SIZE
            ) {
                FarmMapMarker(
                    contentDescription = stringResource(R.string.map_marker_content_description, farm.farmName),
                    selected = true
                )
            }

            FarmProductsTopBar(
                farmName = farm.farmName,
                onBackClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            )

            FarmProductsBottomSheet(
                farm = farm,
                products = products,
                onProductClick = onProductClick,
                onSeeAllProductsClick = onSeeAllProductsClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Gaya pill sama seperti [MapSearchBar] (mengambang, semi-transparan) tapi kontennya tombol
 * back + nama kebun — screen ini di-push (bukan tab), jadi butuh jalan kembali eksplisit ke
 * `MapScreen`, beda dari mockup Stitch yang memakai ikon menu (hamburger) generik di seluruh
 * shell "Peta Marketplace"-nya.
 */
@Composable
private fun FarmProductsTopBar(
    farmName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.farm_products_back_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = farmName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            )
        }
    }
}

@Composable
private fun FarmProductsBottomSheet(
    farm: MapFarmItem,
    products: List<ListingDetailItem>,
    onProductClick: (String) -> Unit,
    onSeeAllProductsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val averageHealthScore = if (products.isNotEmpty()) products.map { it.healthScore }.average() else 0.0
    val healthTier = healthScoreTierOf(averageHealthScore)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(percent = 50))
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = farm.farmName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            // distanceLabel null (lokasi pembeli belum diketahui) → tampilkan
                            // locationLabel saja, tanpa separator "•" yang menggantung.
                            text = farm.distanceLabel?.let {
                                stringResource(R.string.farm_products_location_distance_format, farm.locationLabel, it)
                            } ?: farm.locationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                HealthScoreBadge(tier = healthTier)
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    FarmProductMiniCard(product = product, onClick = { onProductClick(product.id) })
                }
            }
            Button(
                onClick = onSeeAllProductsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.farm_products_cta_see_all),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/** Kartu produk mini (140dp, foto persegi) — padanan "Mini Listing Card" mockup Stitch. */
@Composable
private fun FarmProductMiniCard(
    product: ListingDetailItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                val imageUrl = product.imageUrls.firstOrNull()
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.imageContentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = stringResource(R.string.farm_products_image_placeholder_content_description),
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.cropName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = product.pricePerKgLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.checkout_price_per_unit_suffix, product.unitLabel.lowercase()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun FarmProductsMapScreenPreview() {
    SmartgreenhousemobileTheme {
        val farm = sampleNearbyFarms.first()
        FarmProductsMapScreen(
            farm = farm,
            products = listingsForFarm(farm.id),
            onBackClick = {},
            onProductClick = {},
            onSeeAllProductsClick = {},
            currentBottomNavRoute = Routes.BUYER_MAP,
            onBottomNavigate = {}
        )
    }
}
