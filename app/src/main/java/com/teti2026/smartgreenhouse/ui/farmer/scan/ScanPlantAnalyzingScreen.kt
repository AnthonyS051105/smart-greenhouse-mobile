package com.teti2026.smartgreenhouse.ui.farmer.scan

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Pindai Tanaman - Menganalisis": foto yang baru diambil jadi background (scrim gelap),
 * kartu mengambang di tengah dengan indikator progres — transisi otomatis ke Hasil Analisis
 * dikontrol [ScanPlantRoute] (simulasi delay, lihat TODO di sana untuk `POST /vision/analyze`
 * sungguhan saat MOB-T09/T10 dikerjakan). Tidak ada navigasi/tombol di layar ini (transaksional).
 */
@Composable
fun ScanPlantAnalyzingScreen(
    scannedImageUri: Uri,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = scannedImageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    strokeWidth = 4.dp
                )
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }
            Text(
                text = stringResource(R.string.scan_analyzing_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.scan_analyzing_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScanPlantAnalyzingScreenPreview() {
    SmartgreenhousemobileTheme {
        ScanPlantAnalyzingScreen(scannedImageUri = Uri.EMPTY)
    }
}
