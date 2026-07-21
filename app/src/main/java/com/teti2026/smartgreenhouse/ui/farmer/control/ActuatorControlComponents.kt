package com.teti2026.smartgreenhouse.ui.farmer.control

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import androidx.compose.foundation.layout.Row as RowLayout

/**
 * Segmented control "Otomatis"/"Manual" dipakai kedua screen Kontrol Irigasi & Kontrol
 * Ventilasi (padanan `<div class="... p-1 rounded-full flex ...">` pada desain Stitch asli).
 */
@Composable
fun ActuatorModeSegmentedControl(
    selectedMode: ActuatorControlMode,
    onModeSelected: (ActuatorControlMode) -> Unit,
    modifier: Modifier = Modifier
) {
    RowLayout(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(percent = 50))
            .padding(4.dp)
    ) {
        ActuatorControlMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            val labelRes = when (mode) {
                ActuatorControlMode.AUTOMATIC -> R.string.actuator_control_mode_automatic
                ActuatorControlMode.MANUAL -> R.string.actuator_control_mode_manual
            }
            Surface(
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(percent = 50),
                color = if (selected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
                shadowElevation = if (selected) 1.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Tombol besar HIDUPKAN/MATIKAN — padanan `<button class="w-full h-[64px] ...">` pada kedua
 * desain Stitch. Warna berbalik saat ON (primer solid, label "MATIKAN") vs OFF (netral,
 * label "HIDUPKAN") sesuai kedua referensi desain.
 */
@Composable
fun ActuatorPowerButton(
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(percent = 50),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = if (isOn) 6.dp else 0.dp
    ) {
        RowLayout(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.PowerSettingsNew, contentDescription = null)
            Text(
                text = stringResource(
                    if (isOn) R.string.actuator_control_turn_off else R.string.actuator_control_turn_on
                ),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Ilustrasi lingkaran ikon louver (jendela ventilasi, lihat `docs/glossary.md`) untuk Kontrol
 * Ventilasi — padanan visual [FaucetIllustration]-style di Kontrol Irigasi (`ui/farmer/control/
 * IrrigationControlScreen.kt`), statis tanpa animasi berputar (kipas asli desain Stitch dihapus
 * atas permintaan eksplisit user, diganti ikon ini menggantikan pilihan "hapus total" sebelumnya).
 */
@Composable
fun LouverIllustration(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(180.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Blinds,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(88.dp)
            )
        }
    }
}

/**
 * Ikon kecil PERSISTEN & BISA DIKLIK di sebelah judul screen ("Kontrol Irigasi"/"Kontrol
 * Ventilasi") selama [ActuatorControlMode.MANUAL] dipilih — pengganti [ManualModeBadge] (pill
 * teks terpisah di bawah segmented control) yang dikoreksi user karena "terlihat aneh dan tidak
 * nyaman dilihat" serta teksnya terlalu panjang. Cukup 1 ikon warna warning tanpa teks tambahan,
 * menempel di baris judul yang sudah ada (tidak menambah ruang/baris baru). Tap memanggil
 * [onClick] — caller menampilkan [ManualModeInfoDialog] berisi penjelasan singkat, sesuai
 * permintaan lanjutan user ("saat icon tersebut diklik baru muncul... dialog informasi kecil").
 * Berbeda dari [ManualModeTopToast] yang hanya tampil sekilas, ikon ini tetap ada selama mode
 * Manual aktif supaya petani tidak lupa AI sedang dibypass.
 */
@Composable
fun ManualModeTitleIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.WarningAmber,
        contentDescription = stringResource(R.string.actuator_control_manual_badge),
        tint = MaterialTheme.colorScheme.secondary,
        modifier = modifier
            .size(20.dp)
            .clickable(onClickLabel = stringResource(R.string.actuator_control_manual_badge), role = Role.Button, onClick = onClick)
    )
}

/**
 * Dialog info singkat dipicu tap [ManualModeTitleIcon] — menjelaskan kenapa mode AI otomatis
 * nonaktif. Dipakai kedua screen Kontrol Irigasi & Kontrol Ventilasi.
 */
@Composable
fun ManualModeInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        title = { Text(text = stringResource(R.string.actuator_control_manual_dialog_title)) },
        text = { Text(text = stringResource(R.string.actuator_control_manual_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.actuator_control_manual_dialog_confirm))
            }
        }
    )
}

/**
 * Toast sekilas ("Mode otomatis AI akan dinonaktifkan sementara"), dirender di [visible] yang
 * dikontrol EKSTERNAL oleh caller — **BUKAN** dipasang di dalam konten [ModalBottomSheet]
 * Kontrol Irigasi/Ventilasi. Sheet hanya menempati sebagian bawah layar, jadi toast di dalamnya
 * terlihat "mengambang di tengah" alih-alih di bagian atas layar HP sungguhan (ditemukan user
 * saat testing manual). Dipasang oleh [com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute]
 * sebagai overlay `Box` di root Dashboard (`Alignment.TopCenter`, di ATAS status bar area lewat
 * `statusBarsPadding()`), supaya benar-benar berada di puncak layar terlepas dari posisi scroll
 * sheet. [ManualModeBadge] di dalam sheet tetap ada sebagai pengingat permanen setelah toast ini
 * hilang otomatis.
 */
@Composable
fun ManualModeTopToast(visible: Boolean, modifier: Modifier = Modifier) {
    MessageTopToast(
        message = stringResource(R.string.actuator_control_manual_warning),
        visible = visible,
        modifier = modifier
    )
}

/**
 * Toast generik puncak layar (pesan bebas) — dipakai [ManualModeTopToast] dan juga error trigger
 * aktuator (`POST /irrigation/trigger` gagal) dari [com.teti2026.smartgreenhouse.ui.farmer.
 * DashboardFarmerRoute], pola tampilan identik.
 */
@Composable
fun MessageTopToast(message: String, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 4.dp
        ) {
            RowLayout(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
