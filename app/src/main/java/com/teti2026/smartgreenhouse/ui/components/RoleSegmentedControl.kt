package com.teti2026.smartgreenhouse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.data.model.UserRole

/**
 * Segmented control dua opsi (Petani/Pembeli) — dipakai di layar Login/Register
 * untuk memilih role sebelum autentikasi. Stateless: state role dipegang caller (ViewModel).
 */
@Composable
fun RoleSegmentedControl(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(4.dp)
    ) {
        RoleSegment(
            label = "Petani",
            selected = selectedRole == UserRole.FARMER,
            onClick = { onRoleSelected(UserRole.FARMER) },
            modifier = Modifier.weight(1f)
        )
        RoleSegment(
            label = "Pembeli",
            selected = selectedRole == UserRole.BUYER,
            onClick = { onRoleSelected(UserRole.BUYER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoleSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
        label = "segment_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "segment_content"
    )

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(shadow = Shadow(blurRadius = 0f)),
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}
