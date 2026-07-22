package com.teti2026.smartgreenhouse.ui.farmer.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.navigation.FarmerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Screen "Riwayat Citra - Petani" dari Stitch: filter chips + grid 2 kolom kartu citra + FAB
 * "Buat Listing" + navbar (tab "Riwayat" aktif). Stateless: filter & data di-hoist ke caller
 * ([ImageHistoryRoute]), sesuai pola MVVM+UDF di `docs/SDD.md §5`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageHistoryScreen(
    items: List<CropImageHistoryItem>,
    selectedFilter: ImageHistoryFilter,
    onFilterSelected: (ImageHistoryFilter) -> Unit,
    onItemClick: (String) -> Unit,
    onCreateListingClick: () -> Unit,
    onBackClick: () -> Unit,
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
                        text = stringResource(R.string.image_history_top_bar_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.image_history_back_content_description)
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
        },
        floatingActionButton = {
            Button(
                onClick = onCreateListingClick,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = stringResource(R.string.image_history_fab_scan_plant),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.image_history_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        CropImageHistoryCard(item = item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: ImageHistoryFilter,
    onFilterSelected: (ImageHistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        ImageHistoryFilter.ALL to R.string.image_history_filter_all,
        ImageHistoryFilter.HEALTHY to R.string.image_history_filter_healthy,
        ImageHistoryFilter.SICK to R.string.image_history_filter_sick,
        ImageHistoryFilter.THIS_WEEK to R.string.image_history_filter_this_week
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, labelRes) ->
            val selected = filter == selectedFilter
            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(percent = 50),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = if (selected) {
                    null
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CropImageHistoryCard(
    item: CropImageHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 3.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                HealthCategoryBadge(
                    category = item.category,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.timestampLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.plotLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
internal fun HealthCategoryBadge(
    category: ImageHealthCategory,
    modifier: Modifier = Modifier
) {
    val (icon, labelRes, containerColor, contentColor) = when (category) {
        ImageHealthCategory.GOOD -> BadgeStyle(
            Icons.Filled.Eco,
            R.string.health_score_label_good,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        ImageHealthCategory.MEDIUM -> BadgeStyle(
            Icons.Filled.Warning,
            R.string.health_score_label_medium,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        ImageHealthCategory.SICK -> BadgeStyle(
            Icons.Filled.Coronavirus,
            R.string.image_history_category_sick,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor.copy(alpha = 0.92f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

private data class BadgeStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ImageHistoryScreenPreview() {
    SmartgreenhousemobileTheme {
        ImageHistoryScreen(
            items = sampleCropImageHistoryItems,
            selectedFilter = ImageHistoryFilter.ALL,
            onFilterSelected = {},
            onItemClick = {},
            onCreateListingClick = {},
            onBackClick = {},
            currentBottomNavRoute = Routes.FARMER_IMAGE_HISTORY,
            onBottomNavigate = {}
        )
    }
}
