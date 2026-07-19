package com.teti2026.smartgreenhouse.ui.farmer.scan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHealthCategory
import kotlinx.coroutines.delay

/**
 * Route "Pindai Tanaman": state machine linear Viewfinder -> Menganalisis -> Hasil Analisis untuk
 * pindaian tanaman on-demand lewat kamera HP (bukan siklus ESP32-CAM), dipakai petani yang ingin
 * tahu `health_score` seketika saat melihat gejala penyakit di lapangan (lihat diskusi sebelum
 * fitur ini dibuat).
 *
 * TODO (MOB-T09/T10): [analyzeScan] mensimulasikan `POST /vision/analyze`
 * (`docs/data-contracts.md §4.5`) dengan delay + data sampel tetap — ganti dengan panggilan
 * BackendRepository sungguhan begitu endpoint vision analysis dikerjakan. Pola ini konsisten
 * dengan "screen dulu, wiring data nanti" yang dipakai seluruh screen lain sejauh ini.
 */
@Composable
fun ScanPlantRoute(
    onCloseClick: () -> Unit,
    onSaveToHistoryClick: (ScanAnalysisResult) -> Unit,
    onCreateListingClick: (ScanAnalysisResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var stage by remember { mutableStateOf(ScanPlantStage.VIEWFINDER) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var isFlashOn by remember { mutableStateOf(false) }
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    var analysisResult by remember { mutableStateOf<ScanAnalysisResult?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scannedImageUri = uri
                stage = ScanPlantStage.ANALYZING
            }
        }
    )

    // Dibuat sekali per masuk flow ini; CameraX di-bind ulang tiap kali toggle switch camera.
    val previewView = remember { PreviewView(context) }
    val cameraController = remember { CameraCaptureController(context, lifecycleOwner) }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission && stage == ScanPlantStage.VIEWFINDER) {
            cameraController.bindTo(previewView) { /* TODO: tampilkan Snackbar error kamera */ }
        }
    }

    LaunchedEffect(stage) {
        if (stage == ScanPlantStage.ANALYZING) {
            val uri = scannedImageUri ?: return@LaunchedEffect
            analysisResult = analyzeScan(uri)
            delay(SIMULATED_ANALYSIS_DELAY_MS)
            stage = ScanPlantStage.RESULT
        }
    }

    when (stage) {
        ScanPlantStage.VIEWFINDER -> {
            if (hasCameraPermission) {
                ScanPlantViewfinderScreen(
                    previewView = previewView,
                    isFlashOn = isFlashOn,
                    onFlashToggle = { isFlashOn = !isFlashOn },
                    onSwitchCameraClick = {
                        cameraController.toggleLensFacing(previewView) { /* TODO: Snackbar error */ }
                    },
                    onGalleryClick = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCloseClick = onCloseClick,
                    onShutterClick = {
                        cameraController.takePhoto(
                            onSuccess = { uri ->
                                scannedImageUri = uri
                                stage = ScanPlantStage.ANALYZING
                            },
                            onError = { /* TODO: tampilkan Snackbar error pengambilan foto */ }
                        )
                    }
                )
            } else {
                CameraPermissionRequiredContent()
            }
        }
        ScanPlantStage.ANALYZING -> {
            scannedImageUri?.let { uri -> ScanPlantAnalyzingScreen(scannedImageUri = uri) }
        }
        ScanPlantStage.RESULT -> {
            analysisResult?.let { result ->
                ScanPlantResultScreen(
                    result = result,
                    onBackClick = onCloseClick,
                    onSaveToHistoryClick = { onSaveToHistoryClick(result) },
                    onCreateListingClick = { onCreateListingClick(result) },
                    onScanAgainClick = {
                        scannedImageUri = null
                        analysisResult = null
                        stage = ScanPlantStage.VIEWFINDER
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequiredContent() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.scan_camera_permission_required),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
        )
    }
}

private const val SIMULATED_ANALYSIS_DELAY_MS = 2500L

/**
 * Simulasi `POST /vision/analyze` (`docs/data-contracts.md §4.5`) — hasil tetap (bukan
 * dipanggil dari backend sungguhan), konsisten data sampel dipakai seluruh flow "screen dulu,
 * wiring data nanti".
 */
private suspend fun analyzeScan(imageUri: Uri): ScanAnalysisResult {
    return ScanAnalysisResult(
        imageUri = imageUri,
        category = ImageHealthCategory.GOOD,
        healthScore = 87.0,
        ripenessLabel = "Matang",
        healthLabel = "Sehat",
        confidenceScore = 0.94,
        productName = "Cabai Rawit Merah"
    )
}
