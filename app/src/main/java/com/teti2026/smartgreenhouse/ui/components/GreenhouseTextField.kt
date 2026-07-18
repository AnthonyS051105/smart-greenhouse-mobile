package com.teti2026.smartgreenhouse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Text field dengan floating label, padanan pola input pada desain Stitch
 * (label pindah ke atas saat fokus atau terisi). Stateless — value & callback dari caller.
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
    val labelFloated = isFocused || value.isNotEmpty()

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline
    val borderWidth = if (isFocused) 2.dp else 1.dp

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

        val labelColor = when {
            isFocused -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        val labelStyle = if (labelFloated) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
        val labelBackground = if (labelFloated) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent
        val labelModifier = if (labelFloated) {
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 12.dp, y = (-9).dp)
                .background(labelBackground)
                .padding(horizontal = 4.dp)
        } else {
            Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        }

        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            modifier = labelModifier
        )

        trailingIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)) {
                it()
            }
        }
    }
}
