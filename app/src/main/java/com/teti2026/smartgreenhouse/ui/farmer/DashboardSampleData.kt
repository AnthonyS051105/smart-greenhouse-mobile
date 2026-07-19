package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.SensorChartPoint

/**
 * Data statis Dashboard Petani untuk pratinjau & sebelum DashboardViewModel + FirestoreRepository
 * tersambung (MOB-T09). Nilai meniru satu `sensor_readings` terbaru (`docs/data-contracts.md
 * §3.4`) — [DashboardSensorItem.levelFraction] adalah placeholder visual, bukan skala kalibrasi.
 */
val sampleSensorItems = listOf(
    DashboardSensorItem(
        labelRes = R.string.dashboard_sensor_temperature,
        valueText = "28",
        unitText = "°C",
        icon = Icons.Filled.Thermostat,
        levelFraction = 0.7f
    ),
    DashboardSensorItem(
        labelRes = R.string.dashboard_sensor_humidity,
        valueText = "65",
        unitText = "%",
        icon = Icons.Filled.WaterDrop,
        levelFraction = 0.65f
    ),
    DashboardSensorItem(
        labelRes = R.string.dashboard_sensor_soil_moisture,
        valueText = "45",
        unitText = "%",
        icon = Icons.Filled.Grass,
        levelFraction = 0.45f
    ),
    DashboardSensorItem(
        labelRes = R.string.dashboard_sensor_light,
        valueText = "850",
        unitText = "lux",
        icon = Icons.Filled.WbSunny,
        levelFraction = 0.6f
    )
)

val sampleHarvestDays = listOf(
    HarvestDay(dayNumber = 19, monthLabel = "Mei", isToday = true),
    HarvestDay(dayNumber = 20, monthLabel = "Mei"),
    HarvestDay(dayNumber = 21, monthLabel = "Mei"),
    HarvestDay(dayNumber = 22, monthLabel = "Mei", isHarvestDay = true),
    HarvestDay(dayNumber = 23, monthLabel = "Mei"),
    HarvestDay(dayNumber = 24, monthLabel = "Mei", isHarvestDay = true)
)

val sampleActuatorItems = listOf(
    ActuatorStatusItem(
        labelRes = R.string.dashboard_actuator_irrigation,
        isOn = true,
        statusDetailText = "Terakhir aktif: 3 mnt lalu (AI)",
        icon = Icons.Filled.WaterDrop
    ),
    ActuatorStatusItem(
        labelRes = R.string.dashboard_actuator_ventilation,
        isOn = false,
        statusDetailText = "Mati",
        icon = Icons.Filled.Air
    )
)

/** Tren suhu 24 jam (00:00-24:00) untuk kartu "Tren Historis" — padanan `SensorLineChart`. */
val sampleTrendChartPoints = listOf(
    SensorChartPoint("00:00", 24f),
    SensorChartPoint("06:00", 22f),
    SensorChartPoint("12:00", 29f),
    SensorChartPoint("18:00", 31f),
    SensorChartPoint("24:00", 27f)
)

const val sampleImageHistoryThumbnailUrl =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuA-uOIPhySo0IUr-9yumy8uWfm8gEi5Egy5zAyNB9dZje9IY0asHrJQc-AxjJ3cRnCmSkAnwmbUTv8AmRi4AStfB4M_cR-u8cfH6HhmuRscxmaqzjGWLyk4HElHkzsWHQpmpYzFqJfqvC1nmgwsHBbuszZZTbIsiKHcJOO1W95VyRzxCiEtRAnXatGtcBklQiaUgqBoi-UjOlf4PTg--8z_Off0oolRI8J-kd7R_ZahIH3oXc1bPFUv9OHLz8SCqZj5vMgxRBhpIQ"
