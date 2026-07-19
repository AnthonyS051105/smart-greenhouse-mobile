package com.teti2026.smartgreenhouse.ui.farmer.scan

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Wrapper tipis di atas CameraX (`androidx.camera.*`) untuk kebutuhan pindai tanaman on-demand:
 * live preview di dalam [PreviewView] + ambil satu foto ([ImageCapture]) ke MediaStore. Tidak ada
 * pola CameraX lain di codebase ini sebelumnya (dikonfirmasi lewat eksplorasi codebase) — dibangun
 * baru khusus untuk flow ini, terpisah dari composable UI agar screen tetap stateless/mudah dibaca.
 */
class CameraCaptureController(
    private val context: android.content.Context,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    fun bindTo(previewView: PreviewView, onError: (Throwable) -> Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                val provider = providerFuture.get()
                cameraProvider = provider
                bindUseCases(provider, previewView, onError)
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun toggleLensFacing(previewView: PreviewView, onError: (Throwable) -> Unit) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        cameraProvider?.let { bindUseCases(it, previewView, onError) }
    }

    private fun bindUseCases(
        provider: ProcessCameraProvider,
        previewView: PreviewView,
        onError: (Throwable) -> Unit
    ) {
        try {
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder().build()
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
            imageCapture = capture
        } catch (error: Exception) {
            onError(error)
        }
    }

    fun takePhoto(onSuccess: (Uri) -> Unit, onError: (Throwable) -> Unit) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("Kamera belum siap"))
            return
        }
        val name = "scan_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date())}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/SmartGreenhouse")
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let(onSuccess) ?: onError(IllegalStateException("URI foto kosong"))
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}
