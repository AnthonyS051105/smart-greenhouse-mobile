package com.teti2026.smartgreenhouse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private const val PlaceholderFadeDurationMs = 120

/**
 * Text field dengan placeholder statis (padanan pola input pada desain Stitch), TIDAK
 * memakai floating label — posisi placeholder tetap diam di tengah field, hanya
 * memudar (fade) saat field fokus/terisi, lalu muncul kembali saat kosong & tidak
 * fokus. Pola sama seperti placeholder di `ReviewScreen`/`MapScreen`/`MarketplaceScreen`.
 * Stateless — value & callback dari caller.
 */
@Composable
fun GreenhouseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline
    val borderWidth = if (isFocused) 2.dp else 1.dp

    val placeholderAlpha by animateFloatAsState(
        targetValue = if (value.isEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = PlaceholderFadeDurationMs)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(borderWidth, borderColor),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .alpha(placeholderAlpha)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primaryContainer)
        )

        trailingIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)) {
                it()
            }
        }
    }
}
