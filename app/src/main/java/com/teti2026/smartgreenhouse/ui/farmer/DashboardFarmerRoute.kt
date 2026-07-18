package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.teti2026.smartgreenhouse.ui.navigation.Routes

private val CHART_TABS = listOf("Suhu", "Kelembapan", "Tekanan", "Gas")

// TODO: pindahkan state & data ke DashboardViewModel (StateFlow<UiState>) yang mengambil
// data dari FirestoreRepository.getSensorReadings(plotId, range) begitu MOB-T09 dikerjakan.
// [onActuatorToggle] untuk sementara hanya mengubah state lokal — nantinya harus memanggil
// BackendRepository.triggerActuator (POST /irrigation/trigger, override manual).
@Composable
fun DashboardFarmerRoute(
    onImageHistoryClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {}
) {
    var selectedChartTab by remember { mutableStateOf(CHART_TABS.first()) }
    var actuatorItems by remember { mutableStateOf(sampleActuatorItems) }

    DashboardFarmerScreen(
        farmerName = "Pak Budi",
        dateLabel = "Jumat, 19 Mei 2023",
        healthScore = 85.0,
        healthScoreTrendLabel = "+5%",
        sensorItems = sampleSensorItems,
        chartTabs = CHART_TABS,
        selectedChartTab = selectedChartTab,
        onChartTabSelected = { selectedChartTab = it },
        chartPoints = sampleTrendChartPoints,
        harvestDays = sampleHarvestDays,
        harvestPlotLabel = "Cabai Rawit — Plot A",
        harvestEstimateLabel = "24 Mei",
        harvestCountdownDays = 5,
        actuatorItems = actuatorItems,
        onActuatorToggle = { toggled ->
            actuatorItems = actuatorItems.map {
                if (it.labelRes == toggled.labelRes) it.copy(isOn = !it.isOn) else it
            }
        },
        imageHistoryThumbnailUrl = sampleImageHistoryThumbnailUrl,
        onImageHistoryClick = onImageHistoryClick,
        onNotificationsClick = { /* TODO: navigasi ke layar Notifikasi */ },
        currentBottomNavRoute = Routes.FARMER_DASHBOARD,
        onBottomNavigate = onBottomNavigate
    )
}
