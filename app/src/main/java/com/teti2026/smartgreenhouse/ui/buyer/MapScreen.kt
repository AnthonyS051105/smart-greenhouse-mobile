package com.teti2026.smartgreenhouse.ui.buyer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.BuyerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme
import kotlinx.coroutines.launch

// internal (bukan private): dipakai ulang oleh FarmProductsMapScreen (screen "Produk Lahan - Peta")
// agar kamera awal & level zoom konsisten dengan screen ini.
internal val MAP_DEFAULT_CENTER = LatLng(-7.8014, 110.3644)
internal const val MAP_DEFAULT_ZOOM = 13f
internal const val MAP_MY_LOCATION_ZOOM = 15f
internal const val MAP_FARM_FOCUS_ZOOM = 16f

/**
 * Layar "Peta Marketplace - Pembeli" dari Stitch. Stateless: seluruh data & event di-hoist ke
 * caller (nantinya MapViewModel + FirestoreRepository.getFarmsForMap(), lihat `docs/SDD.md §4.2/§5`).
 *
 * Catatan: [rememberCameraPositionState] & pembacaan lokasi perangkat sengaja dibiarkan di
 * composable ini (bukan di-hoist) karena itu state mekanis milik peta (analog LazyListState),
 * bukan state domain/UI-hasil-bisnis — sejalan dengan konvensi Compose Maps.
 */
@Composable
fun MapScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    quickFilters: List<String>,
    selectedQuickFilter: String?,
    onQuickFilterSelected: (String) -> Unit,
    farms: List<MapFarmItem>,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onFarmClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MAP_DEFAULT_CENTER, MAP_DEFAULT_ZOOM)
    }

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
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = false
                )
            ) {
                farms.forEach { farm ->
                    MarkerComposable(
                        keys = arrayOf(farm.id),
                        state = rememberUpdatedMarkerState(position = farm.position),
                        title = farm.farmName,
                        onClick = {
                            onFarmClick(farm.id)
                            true
                        }
                    ) {
                        FarmMapMarker(contentDescription = stringResource(R.string.map_marker_content_description, farm.farmName))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                MapSearchBar(query = searchQuery, onQueryChange = onSearchQueryChange)
                MapQuickFilterRow(
                    filters = quickFilters,
                    selectedFilter = selectedQuickFilter,
                    onFilterSelected = onQuickFilterSelected,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            MyLocationButton(
                onClick = {
                    if (hasLocationPermission) {
                        lastKnownLocation(context)?.let { latLng ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(latLng, MAP_MY_LOCATION_ZOOM)
                                )
                            }
                        }
                    } else {
                        onRequestLocationPermission()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 140.dp, end = 20.dp)
            )

            NearbyFarmsBottomSheet(
                farms = farms,
                onFarmClick = onFarmClick,
                onSeeAllClick = onSeeAllClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Lokasi terakhir dari provider yang tersedia (GPS/Network), tanpa dependensi
 * FusedLocationProviderClient. Null bila izin belum diberikan/belum ada fix sama sekali.
 * Internal (bukan private) — dipakai ulang [MapRoute]/[FarmProductsMapRoute] untuk menghitung
 * [distanceLabelFrom] bagi [MapFarmItem.distanceLabel].
 */
internal fun lastKnownLocation(context: Context): LatLng? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return null
    }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .firstOrNull()
        ?.let { LatLng(it.latitude, it.longitude) }
}

/**
 * Jarak lurus (bukan rute jalan, sekadar estimasi cepat) dari [userLocation] pembeli ke [target]
 * kebun, format "1.2 km". Null bila [userLocation] belum diketahui — dipakai [MapRoute]/
 * [FarmProductsMapRoute] untuk mengisi [MapFarmItem.distanceLabel] di layer UI (bukan ViewModel/
 * Firestore) karena bergantung sensor lokasi PERANGKAT saat ini, lihat catatan di [MapFarmItem].
 */
internal fun distanceLabelFrom(userLocation: LatLng?, target: LatLng): String? {
    if (userLocation == null) return null
    val results = FloatArray(1)
    Location.distanceBetween(userLocation.latitude, userLocation.longitude, target.latitude, target.longitude, results)
    return "%.1f km".format(results[0] / 1000f)
}

/**
 * Marker kebun di peta — gaya lingkaran (bukan pin+ekor) mengikuti mockup Stitch "Peta
 * Marketplace": [selected] = kebun yang sedang difokuskan (dipakai `FarmProductsMapScreen` untuk
 * satu-satunya marker relevan di screen itu — lingkaran besar terisi penuh + cincin berdenyut,
 * padanan `.animate-ping`), state default (dipakai `MapScreen` untuk seluruh kebun terdekat,
 * belum ada yang "difokuskan") = cincin kosong bg putih. Internal (bukan private) — dipakai ulang
 * oleh `FarmProductsMapScreen` di file lain, package yang sama.
 */
@Composable
internal fun FarmMapMarker(
    contentDescription: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (selected) {
            val pulseTransition = rememberInfiniteTransition(label = "farmMarkerPulse")
            val pulseScale by pulseTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "farmMarkerPulseScale"
            )
            val pulseAlpha by pulseTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "farmMarkerPulseAlpha"
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale, alpha = pulseAlpha)
                    .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                    .border(width = 2.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                    .border(width = 2.dp, color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * internal (bukan private) — dipakai ulang oleh [FarmProductsMapScreen] sebagai dasar gaya top
 * bar (pill mengambang semi-transparan) walau kontennya diganti tombol back + nama kebun,
 * bukan input pencarian. Warna [MaterialTheme.colorScheme.surface] diberi alpha 0.92f (bukan
 * blur sungguhan — `RenderEffect` backdrop-blur baru tersedia API 31+, sementara `minSdk` app ini
 * 24, lihat `mobile/docs/SRS.md MOB-NFR-04`) untuk mendekati efek "bg-surface/90 backdrop-blur"
 * pada mockup Stitch tanpa melanggar kompatibilitas versi minimum.
 */
@Composable
internal fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.map_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = stringResource(R.string.map_filter_button_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MapQuickFilterRow(
    filters: List<String>,
    selectedFilter: String?,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        filters.forEach { filter ->
            val selected = filter == selectedFilter
            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(percent = 50),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// shadowElevation 1dp — disamakan dengan elemen chrome mengambang lain (MapSearchBar,
// FarmProductsTopBar, MapQuickFilterRow) agar seluruh floating UI di kedua screen Peta
// terlihat konsisten satu level ketinggian.
@Composable
private fun MyLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = stringResource(R.string.map_my_location_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NearbyFarmsBottomSheet(
    farms: List<MapFarmItem>,
    onFarmClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(
                    text = stringResource(R.string.map_bottom_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.map_bottom_sheet_subtitle, farms.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                items(farms, key = { it.id }) { farm ->
                    NearbyFarmCard(farm = farm, onClick = { onFarmClick(farm.id) })
                }
            }
            // Tombol "Lihat Semua Produk" — gaya pill primer penuh-lebar disamakan PERSIS dengan
            // tombol CTA FarmProductsBottomSheet (FarmProductsMapScreen.kt, screen "Produk Lahan -
            // Peta"), dipindah dari TextButton kecil di header ke posisi bawah (sama seperti
            // Screen 2) — bukan lagi tombol teks kecil bersisian dengan judul.
            Button(
                onClick = onSeeAllClick,
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
                    text = stringResource(R.string.map_see_all_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Gaya kartu (radius 12dp, warna surfaceContainerLow, border tipis, shadow 1dp) disamakan persis
// dengan FarmProductMiniCard (FarmProductsMapScreen.kt, screen "Produk Lahan - Peta") — sebelumnya
// beda (radius 16dp, surfaceContainerLowest, tanpa border/shadow) sehingga kedua screen Peta
// terlihat seperti dua design system berbeda. Lebar diubah dari widthIn(max=280dp) (variabel,
// menyusut sesuai konten) ke width tetap 200dp agar seluruh kartu di LazyRow seragam lebarnya.
@Composable
private fun NearbyFarmCard(farm: MapFarmItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(128.dp)) {
                if (farm.imageUrl != null) {
                    AsyncImage(
                        model = farm.imageUrl,
                        contentDescription = farm.imageContentDescription,
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
                            contentDescription = stringResource(R.string.map_farm_image_placeholder_content_description),
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                if (farm.rating != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(R.string.map_rating_content_description, farm.rating.toString()),
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = farm.rating.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = farm.farmName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        // Fallback ke locationLabel (nama kota) saat distanceLabel belum bisa
                        // dihitung (lokasi pembeli belum diketahui) — tetap ada informasi
                        // lokasi yang ditampilkan, bukan baris kosong.
                        text = farm.distanceLabel ?: farm.locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    farm.cropTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MapScreenPreview() {
    SmartgreenhousemobileTheme {
        MapScreen(
            searchQuery = "",
            onSearchQueryChange = {},
            quickFilters = listOf("Sayuran", "Buah", "Terdekat"),
            selectedQuickFilter = null,
            onQuickFilterSelected = {},
            farms = sampleNearbyFarms,
            hasLocationPermission = false,
            onRequestLocationPermission = {},
            onFarmClick = {},
            onSeeAllClick = {},
            currentBottomNavRoute = Routes.BUYER_MAP,
            onBottomNavigate = {}
        )
    }
}
