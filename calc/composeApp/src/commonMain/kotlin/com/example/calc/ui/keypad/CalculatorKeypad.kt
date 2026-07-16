package com.example.calc.ui.keypad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <I> CalculatorKeypad(
    pad: KeyPad<I>,
    onKey: (I) -> Unit,
    modifier: Modifier = Modifier,
    keyAspect: Float = 1f,
) {
    val haptics = LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in pad) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (key in row) {
                    KeyButton(
                        key = key,
                        modifier = Modifier.weight(key.span.toFloat()),
                        keyAspect = keyAspect,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKey(key.intent)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun <I> KeyButton(key: Key<I>, modifier: Modifier, keyAspect: Float, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg = when (key.style) {
        KeyStyle.Number -> scheme.surfaceVariant
        KeyStyle.Operator -> scheme.secondaryContainer
        KeyStyle.Function -> scheme.surface
        KeyStyle.Accent -> scheme.errorContainer
        KeyStyle.Equals -> scheme.primary
        KeyStyle.Toggle -> scheme.surfaceVariant
        KeyStyle.ToggleActive -> scheme.primary
    }
    val fg = when (key.style) {
        KeyStyle.Number -> scheme.onSurface
        KeyStyle.Operator -> scheme.onSecondaryContainer
        KeyStyle.Function -> scheme.onSurface
        KeyStyle.Accent -> scheme.onErrorContainer
        KeyStyle.Equals -> scheme.onPrimary
        KeyStyle.Toggle -> scheme.onSurfaceVariant
        KeyStyle.ToggleActive -> scheme.onPrimary
    }
    val aspect = if (key.span == 1) keyAspect else keyAspect * 2.1f
    Surface(
        color = if (key.enabled) bg else bg.copy(alpha = 0.35f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .aspectRatio(aspect)
            .then(if (key.enabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key.label,
                color = if (key.enabled) fg else fg.copy(alpha = 0.35f),
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
