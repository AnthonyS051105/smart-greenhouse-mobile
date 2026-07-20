package com.teti2026.smartgreenhouse.ui.farmer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.farmer.control.IrrigationControlRoute
import com.teti2026.smartgreenhouse.ui.farmer.control.ManualModeTopToast
import com.teti2026.smartgreenhouse.ui.navigation.Routes
import com.teti2026.smartgreenhouse.ui.farmer.control.VentilationControlRoute
import com.teti2026.smartgreenhouse.viewmodel.ProfileUiState
import com.teti2026.smartgreenhouse.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

private val CHART_TABS = listOf("Suhu", "Kelembapan", "Tekanan", "Gas")

/** Aktuator mana yang sedang dibuka lewat kartu "Aktuator" Dashboard, jika ada. */
private enum class OpenActuatorSheet { IRRIGATION, VENTILATION }

// TODO: pindahkan state & data ke DashboardViewModel (StateFlow<UiState>) yang mengambil
// data dari FirestoreRepository.getSensorReadings(plotId, range) begitu MOB-T09 dikerjakan.
// [onActuatorToggle] untuk sementara hanya mengubah state lokal — nantinya harus memanggil
// BackendRepository.triggerActuator (POST /irrigation/trigger, override manual).
@Composable
fun DashboardFarmerRoute(
    onImageHistoryClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onBottomNavigate: (String) -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Hanya [farmerName] (sapaan header) yang diambil sungguhan dari Firestore `users` — seluruh
    // data lain di Dashboard (grafik sensor, health_score, aktuator, estimasi panen) masih sampel
    // statis, menyusul saat MOB-T09 (sensor real-time) dikerjakan terpisah, di luar lingkup Profil.
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val farmerName = (profileState as? ProfileUiState.Success)?.user?.name.orEmpty()

    var selectedChartTab by remember { mutableStateOf(CHART_TABS.first()) }
    var actuatorItems by remember { mutableStateOf(sampleActuatorItems) }
    // Menekan baris "Irigasi"/"Ventilasi" di kartu Aktuator membuka layar kontrolnya masing-
    // masing sebagai `ModalBottomSheet` overlay DI ATAS Dashboard — pola sama seperti
    // [ImageHistoryRoute] menampilkan Detail Analisis Citra (bukan destination NavHost
    // terpisah), supaya Dashboard tetap ada di composition sebagai layar di belakang sheet.
    var openActuatorSheet by remember { mutableStateOf<OpenActuatorSheet?>(null) }

    // Toast "Mode otomatis AI akan dinonaktifkan sementara" dipasang di ROOT Dashboard (BUKAN di
    // dalam konten ModalBottomSheet Kontrol Irigasi/Ventilasi) supaya benar-benar tampil di
    // puncak layar HP, bukan "mengambang di tengah" relatif posisi sheet — dilaporkan user saat
    // testing manual. [manualToastTrigger] increment setiap kali dipicu (bukan Boolean biasa)
    // supaya toast bisa muncul ulang meski user berpindah cepat antar-mode.
    var manualToastTrigger by remember { mutableStateOf(0) }
    var manualToastVisible by remember { mutableStateOf(false) }
    LaunchedEffect(manualToastTrigger) {
        if (manualToastTrigger > 0) {
            manualToastVisible = true
            delay(2500)
            manualToastVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DashboardFarmerScreen(
            farmerName = farmerName,
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
            onActuatorItemClick = { item ->
                openActuatorSheet = when (item.labelRes) {
                    R.string.dashboard_actuator_irrigation -> OpenActuatorSheet.IRRIGATION
                    R.string.dashboard_actuator_ventilation -> OpenActuatorSheet.VENTILATION
                    else -> null
                }
            },
            imageHistoryThumbnailUrl = sampleImageHistoryThumbnailUrl,
            onImageHistoryClick = onImageHistoryClick,
            onNotificationsClick = onNotificationsClick,
            currentBottomNavRoute = Routes.FARMER_DASHBOARD,
            onBottomNavigate = onBottomNavigate
        )

        when (openActuatorSheet) {
            OpenActuatorSheet.IRRIGATION -> IrrigationControlRoute(
                onDismiss = { openActuatorSheet = null },
                onManualModeActivated = { manualToastTrigger++ }
            )
            OpenActuatorSheet.VENTILATION -> VentilationControlRoute(
                onDismiss = { openActuatorSheet = null },
                onManualModeActivated = { manualToastTrigger++ }
            )
            null -> Unit
        }

        // Toast ditaruh PALING TERAKHIR dalam Box supaya digambar di lapisan paling atas,
        // di atas ModalBottomSheet Kontrol Irigasi/Ventilasi sekalipun — `statusBarsPadding()`
        // memastikan tidak tertutup status bar, benar-benar di puncak layar HP.
        ManualModeTopToast(
            visible = manualToastVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        )
    }
}
