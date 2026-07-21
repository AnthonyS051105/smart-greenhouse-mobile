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
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.teti2026.smartgreenhouse.repository.BackendRepository
import com.teti2026.smartgreenhouse.ui.farmer.control.MessageTopToast
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHealthCategory
import kotlinx.coroutines.delay

/**
 * Route "Pindai Tanaman": state machine linear Viewfinder -> Menganalisis -> Hasil Analisis untuk
 * pindaian tanaman on-demand lewat kamera HP (bukan siklus ESP32-CAM, yang hardware-nya rusak dan
 * di luar lingkup wiring ini sama sekali), dipakai petani yang ingin tahu kematangan buah seketika
 * di lapangan.
 *
 * Memanggil `POST /vision/analyze` sungguhan lewat [BackendRepository] dengan `file`
 * foto kamera HP langsung (TANPA `image_id`) — model yang ada hanya klasifikasi kematangan buah
 * (`ripeness_class`: Overipe/Ripe/Unripe), BUKAN model penyakit tanaman (`disease_class` belum ada
 * modelnya di backend/AI manapun). [category]/[healthLabel] pada [ScanAnalysisResult] karena itu
 * diturunkan dari `ripeness_class` sebagai proksi (Ripe→GOOD, Unripe→MEDIUM, Overipe→SICK), BUKAN
 * hasil deteksi penyakit sungguhan. [healthScore] dihitung dengan formula yang sama seperti
 * `POST /listings/auto-fill-health-score` di backend (`Ripe`=100/`Unripe`=70/`Overipe`=40 × confidence)
 * karena jalur `file` langsung ini tidak mem-persist ke `crop_images` sehingga tidak bisa memanggil
 * endpoint tersebut secara terpisah.
 */
@Composable
fun ScanPlantRoute(
    onCloseClick: () -> Unit,
    onSaveToHistoryClick: (ScanAnalysisResult) -> Unit,
    onCreateListingClick: (ScanAnalysisResult) -> Unit,
    backendRepository: BackendRepository = BackendRepository()
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
    var analysisErrorMessage by remember { mutableStateOf<String?>(null) }

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
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                analysisErrorMessage = context.getString(R.string.scan_analysis_error)
                stage = ScanPlantStage.VIEWFINDER
                return@LaunchedEffect
            }
            backendRepository.analyzeVisionFile(bytes, mimeType)
                .onSuccess { vision ->
                    analysisResult = toScanAnalysisResult(uri, vision.ripenessClass, vision.confidenceScore)
                    stage = ScanPlantStage.RESULT
                }
                .onFailure {
                    analysisErrorMessage = context.getString(R.string.scan_analysis_error)
                    stage = ScanPlantStage.VIEWFINDER
                }
        }
    }

    var errorToastVisible by remember { mutableStateOf(false) }
    LaunchedEffect(analysisErrorMessage) {
        if (analysisErrorMessage != null) {
            errorToastVisible = true
            delay(2500)
            errorToastVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        MessageTopToast(
            message = analysisErrorMessage.orEmpty(),
            visible = errorToastVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        )
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

/** `Ripe`=100/`Unripe`=70/`Overipe`=40 — sama seperti `RIPENESS_HEALTH_SCORE` backend
 * (`app/routers/ai.py`, dipakai `POST /listings/auto-fill-health-score`). Base 50 untuk nilai
 * `ripeness_class` di luar 3 ini (mengikuti fallback backend). */
private val RIPENESS_BASE_SCORE = mapOf(
    "Ripe" to 100,
    "Unripe" to 70,
    "Overipe" to 40
)

private val RIPENESS_LABEL_ID = mapOf(
    "Ripe" to "Matang",
    "Unripe" to "Belum Matang",
    "Overipe" to "Terlalu Matang"
)

/**
 * Petakan hasil `POST /vision/analyze` (`ripeness_class`/`confidence_score`) ke [ScanAnalysisResult].
 * TIDAK ada model penyakit tanaman yang berjalan (`disease_class` belum diimplementasikan di
 * manapun) — [category]/[healthLabel] diturunkan dari [ripenessClass] sebagai proksi kesehatan
 * visual, bukan diagnosis penyakit sungguhan.
 */
private fun toScanAnalysisResult(
    imageUri: Uri,
    ripenessClass: String,
    confidenceScore: Double
): ScanAnalysisResult {
    val baseScore = RIPENESS_BASE_SCORE[ripenessClass] ?: 50
    val category = when (ripenessClass) {
        "Ripe" -> ImageHealthCategory.GOOD
        "Unripe" -> ImageHealthCategory.MEDIUM
        else -> ImageHealthCategory.SICK
    }
    return ScanAnalysisResult(
        imageUri = imageUri,
        category = category,
        healthScore = (baseScore * confidenceScore).let { Math.round(it).toDouble() },
        ripenessLabel = RIPENESS_LABEL_ID[ripenessClass] ?: ripenessClass,
        healthLabel = when (category) {
            ImageHealthCategory.GOOD -> "Sehat"
            ImageHealthCategory.MEDIUM -> "Perlu Perhatian"
            ImageHealthCategory.SICK -> "Kurang Sehat"
        },
        confidenceScore = confidenceScore,
        productName = "Cabai Rawit Merah"
    )
}
