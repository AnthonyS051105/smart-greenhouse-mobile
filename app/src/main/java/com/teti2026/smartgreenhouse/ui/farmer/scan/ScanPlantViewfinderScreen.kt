package com.teti2026.smartgreenhouse.ui.farmer.scan

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Pindai Tanaman - Viewfinder": live preview kamera HP (CameraX) dengan kotak fokus
 * bergaya viewfinder Stitch (bracket sudut + scrim gelap di luar kotak). Stateless — event
 * (shutter/flash/switch camera/galeri/tutup) naik lewat lambda ke [ScanPlantRoute].
 *
 * [previewView] disediakan caller (dibuat sekali via `remember { PreviewView(context) }`) supaya
 * lifecycle CameraX di-drive dari Route, bukan dari screen — screen ini murni menampilkan &
 * meneruskan interaksi.
 */
@Composable
fun ScanPlantViewfinderScreen(
    previewView: PreviewView,
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCloseClick: () -> Unit,
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView.apply { layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ) } },
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: tutup, judul, toggle flash.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.scan_viewfinder_close_content_description),
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.scan_viewfinder_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                IconButton(onClick = onFlashToggle) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = stringResource(R.string.scan_viewfinder_flash_content_description),
                        tint = Color.White
                    )
                }
            }

            // Kotak viewfinder di tengah + caption instruksi.
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ViewfinderBracketBox()
                Text(
                    text = stringResource(R.string.scan_viewfinder_instruction),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Bottom bar: caption pencahayaan + galeri/shutter/switch camera.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.scan_viewfinder_lighting_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScanRoundIconButton(
                        icon = Icons.Filled.PhotoLibrary,
                        contentDescription = stringResource(R.string.scan_viewfinder_gallery_content_description),
                        onClick = onGalleryClick,
                        size = 48.dp
                    )
                    ShutterButton(onClick = onShutterClick)
                    ScanRoundIconButton(
                        icon = Icons.Filled.Cameraswitch,
                        contentDescription = stringResource(R.string.scan_viewfinder_switch_camera_content_description),
                        onClick = onSwitchCameraClick,
                        size = 48.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewfinderBracketBox() {
    val bracketColor = Color.White
    Box(
        modifier = Modifier
            .size(256.dp)
            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        val cornerSize = 32.dp
        val strokeWidth = 4.dp
        Box(
            Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
                .border(
                    androidx.compose.foundation.BorderStroke(strokeWidth, bracketColor),
                    RoundedCornerShape(topStart = 16.dp)
                )
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
                .border(
                    androidx.compose.foundation.BorderStroke(strokeWidth, bracketColor),
                    RoundedCornerShape(topEnd = 16.dp)
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
                .border(
                    androidx.compose.foundation.BorderStroke(strokeWidth, bracketColor),
                    RoundedCornerShape(bottomStart = 16.dp)
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
                .border(
                    androidx.compose.foundation.BorderStroke(strokeWidth, bracketColor),
                    RoundedCornerShape(bottomEnd = 16.dp)
                )
        )
    }
}

@Composable
private fun ScanRoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = Color.White)
        }
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White)
        )
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {}
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScanPlantViewfinderScreenPreview() {
    SmartgreenhousemobileTheme {
        // Preview tanpa CameraX (butuh device sungguhan) — placeholder Box hitam sebagai pengganti.
        Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
    }
}
