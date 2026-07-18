package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.SensorChartPoint
import com.teti2026.smartgreenhouse.ui.components.SensorLineChart
import com.teti2026.smartgreenhouse.ui.navigation.FarmerBottomNavBar
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.theme.ErrorRedAccent
import com.teti2026.smartgreenhouse.ui.theme.InfoBlue
import com.teti2026.smartgreenhouse.ui.theme.Outline as OutlineColor
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Dashboard Monitoring (Revised) - Petani" dari Stitch. Stateless: seluruh data & event
 * di-hoist ke caller (nantinya DashboardViewModel + FirestoreRepository, `docs/SDD.md §4.2/§5`).
 * Bottom nav dirapikan dari referensi Stitch (FAB melayang di luar bar) menjadi
 * [FarmerBottomNavBar] simetris 5-item sejajar — lihat catatan di file tersebut.
 */
@Composable
fun DashboardFarmerScreen(
    farmerName: String,
    dateLabel: String,
    healthScore: Double,
    healthScoreTrendLabel: String,
    sensorItems: List<DashboardSensorItem>,
    chartTabs: List<String>,
    selectedChartTab: String,
    onChartTabSelected: (String) -> Unit,
    chartPoints: List<SensorChartPoint>,
    harvestDays: List<HarvestDay>,
    harvestPlotLabel: String,
    harvestEstimateLabel: String,
    harvestCountdownDays: Int,
    actuatorItems: List<ActuatorStatusItem>,
    onActuatorToggle: (ActuatorStatusItem) -> Unit,
    imageHistoryThumbnailUrl: String,
    onImageHistoryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    currentBottomNavRoute: String,
    onBottomNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DashboardTopBar(
                farmerName = farmerName,
                dateLabel = dateLabel,
                onNotificationsClick = onNotificationsClick
            )
        },
        bottomBar = {
            FarmerBottomNavBar(
                currentRoute = currentBottomNavRoute,
                onNavigate = onBottomNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardHealthScoreCard(
                    healthScore = healthScore,
                    trendLabel = healthScoreTrendLabel
                )
            }

            item { SensorGrid(sensorItems) }

            item {
                DashboardTrendChartCard(
                    tabs = chartTabs,
                    selectedTab = selectedChartTab,
                    onTabSelected = onChartTabSelected,
                    points = chartPoints
                )
            }

            item {
                HarvestScheduleCard(
                    days = harvestDays,
                    plotLabel = harvestPlotLabel,
                    harvestEstimateLabel = harvestEstimateLabel,
                    countdownDays = harvestCountdownDays
                )
            }

            item {
                ActuatorStatusCard(
                    items = actuatorItems,
                    onToggle = onActuatorToggle
                )
            }

            item {
                ImageHistoryShortcutCard(
                    thumbnailUrl = imageHistoryThumbnailUrl,
                    onClick = onImageHistoryClick
                )
            }
        }
    }
}

@Composable
private fun DashboardTopBar(
    farmerName: String,
    dateLabel: String,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_greeting, farmerName),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = stringResource(R.string.dashboard_realtime_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.dashboard_notifications_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Grid 2 kolom x 2 baris kartu sensor (suhu/kelembapan/tekanan/gas), disusun manual dengan
 * Row+Column (bukan LazyVerticalGrid) karena hanya 4 item tetap — menghindari nested-scroll
 * di dalam LazyColumn induk. Warna aksen tiap kartu dipasangkan berdasarkan urutan item.
 */
@Composable
private fun SensorGrid(items: List<DashboardSensorItem>, modifier: Modifier = Modifier) {
    val accentColors = listOf(
        ErrorRedAccent to MaterialTheme.colorScheme.errorContainer,
        InfoBlue to InfoBlue.copy(alpha = 0.16f),
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        OutlineColor to MaterialTheme.colorScheme.surfaceVariant
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val index = rowIndex * 2 + columnIndex
                    val (accentColor, accentContainerColor) = accentColors.getOrElse(index) { accentColors.last() }
                    DashboardSensorCard(
                        item = item,
                        accentColor = accentColor,
                        accentContainerColor = accentContainerColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 2) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DashboardTrendChartCard(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    points: List<SensorChartPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_chart_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                tabs.forEach { tab ->
                    val selected = tab == selectedTab
                    Surface(
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
                        shadowElevation = if (selected) 1.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            SensorLineChart(
                points = points,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun DashboardFarmerScreenPreview() {
    var selectedTab by remember { mutableStateOf("Suhu") }
    var actuators by remember { mutableStateOf(sampleActuatorItems) }

    SmartgreenhousemobileTheme {
        DashboardFarmerScreen(
            farmerName = "Pak Budi",
            dateLabel = "Jumat, 19 Mei 2023",
            healthScore = 85.0,
            healthScoreTrendLabel = "+5%",
            sensorItems = sampleSensorItems,
            chartTabs = listOf("Suhu", "Kelembapan", "Tekanan", "Gas"),
            selectedChartTab = selectedTab,
            onChartTabSelected = { selectedTab = it },
            chartPoints = sampleTrendChartPoints,
            harvestDays = sampleHarvestDays,
            harvestPlotLabel = "Cabai Rawit — Plot A",
            harvestEstimateLabel = "24 Mei",
            harvestCountdownDays = 5,
            actuatorItems = actuators,
            onActuatorToggle = { toggled ->
                actuators = actuators.map { if (it.labelRes == toggled.labelRes) it.copy(isOn = !it.isOn) else it }
            },
            imageHistoryThumbnailUrl = sampleImageHistoryThumbnailUrl,
            onImageHistoryClick = {},
            onNotificationsClick = {},
            currentBottomNavRoute = Routes.FARMER_DASHBOARD,
            onBottomNavigate = {}
        )
    }
}
