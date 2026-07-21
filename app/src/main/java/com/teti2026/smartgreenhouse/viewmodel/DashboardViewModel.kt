package com.teti2026.smartgreenhouse.viewmodel

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.data.model.Plot
import com.teti2026.smartgreenhouse.data.model.SensorReading
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.components.SensorChartPoint
import com.teti2026.smartgreenhouse.ui.farmer.DashboardSensorItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val plot: Plot,
        val deviceId: String,
        val sensorItems: List<DashboardSensorItem>,
        val chartPointsBySensor: Map<String, List<SensorChartPoint>>
    ) : DashboardUiState
    data class Error(val messageResId: Int) : DashboardUiState
}

/** Kunci tab grafik ↔ field `sensor_readings` — dipakai [DashboardFarmerRoute] untuk memilih seri chart. */
enum class SensorChartTab(val labelRes: Int) {
    TEMPERATURE(R.string.dashboard_sensor_temperature),
    HUMIDITY(R.string.dashboard_sensor_humidity),
    SOIL_MOISTURE(R.string.dashboard_sensor_soil_moisture),
    LIGHT(R.string.dashboard_sensor_light)
}

/**
 * Ambil plot milik petani lalu pasang listener realtime `sensor_readings` (`FirestoreRepository.
 * observeSensorReadings`) — kartu sensor & grafik Dashboard ikut ter-update otomatis begitu
 * backend menulis dokumen baru (dari IoT via MQTT), TANPA restart app atau polling manual
 * (`docs/Architecture.md §3`). [healthScore]/jadwal panen TIDAK ditangani di sini — belum ada
 * sumber data backend untuk itu, tetap sampel di [com.teti2026.smartgreenhouse.ui.farmer.
 * DashboardFarmerRoute] sampai fitur terkait dikerjakan terpisah.
 */
class DashboardViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = DashboardUiState.Error(R.string.auth_error_generic)
            return
        }
        viewModelScope.launch {
            val plotResult = firestoreRepository.getFarmAndPlotForOwner(uid)
            val plot = plotResult.getOrNull()?.second
            if (plot == null) {
                Log.e("DashboardViewModel", "getFarmAndPlotForOwner($uid) gagal", plotResult.exceptionOrNull())
                _state.value = DashboardUiState.Error(R.string.dashboard_error_load_failed)
                return@launch
            }
            firestoreRepository.observeSensorReadings(plot.id)
                .catch { error ->
                    Log.e("DashboardViewModel", "observeSensorReadings gagal untuk plot ${plot.id}", error)
                    _state.value = DashboardUiState.Error(R.string.dashboard_error_load_failed)
                }
                .collect { readings ->
                    _state.value = DashboardUiState.Success(
                        plot = plot,
                        deviceId = plot.deviceId,
                        sensorItems = buildSensorItems(readings.firstOrNull()),
                        chartPointsBySensor = buildChartPoints(readings)
                    )
                }
        }
    }

    private fun buildSensorItems(latest: SensorReading?): List<DashboardSensorItem> = listOf(
        DashboardSensorItem(
            labelRes = R.string.dashboard_sensor_temperature,
            valueText = latest?.temperature?.let { "%.0f".format(it) } ?: "--",
            unitText = "°C",
            icon = Icons.Filled.Thermostat,
            levelFraction = ((latest?.temperature ?: 0.0) / 50.0).coerceIn(0.0, 1.0).toFloat()
        ),
        DashboardSensorItem(
            labelRes = R.string.dashboard_sensor_humidity,
            valueText = latest?.humidity?.let { "%.0f".format(it) } ?: "--",
            unitText = "%",
            icon = Icons.Filled.WaterDrop,
            levelFraction = ((latest?.humidity ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat()
        ),
        DashboardSensorItem(
            labelRes = R.string.dashboard_sensor_soil_moisture,
            valueText = latest?.soilMoisture?.let { "%.0f".format(it) } ?: "--",
            unitText = "%",
            icon = Icons.Filled.Grass,
            levelFraction = ((latest?.soilMoisture ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat()
        ),
        DashboardSensorItem(
            labelRes = R.string.dashboard_sensor_light,
            valueText = latest?.lightIntensity?.let { "%.0f".format(it) } ?: "--",
            unitText = "lux",
            icon = Icons.Filled.WbSunny,
            levelFraction = ((latest?.lightIntensity ?: 0.0) / 2000.0).coerceIn(0.0, 1.0).toFloat()
        )
    )

    private fun buildChartPoints(readings: List<SensorReading>): Map<String, List<SensorChartPoint>> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val ascending = readings.sortedBy { it.timestampMillis }
        fun points(selector: (SensorReading) -> Double?) = ascending.mapNotNull { reading ->
            selector(reading)?.let { value ->
                SensorChartPoint(timeFormat.format(Date(reading.timestampMillis)), value.toFloat())
            }
        }
        return mapOf(
            "temperature" to points { it.temperature },
            "humidity" to points { it.humidity },
            "soil_moisture" to points { it.soilMoisture },
            "light" to points { it.lightIntensity }
        )
    }
}
