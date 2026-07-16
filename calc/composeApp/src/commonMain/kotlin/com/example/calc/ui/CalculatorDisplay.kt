package com.example.calc.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.presentation.CalculatorState
import com.example.calc.presentation.displayText

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 32f) onSwipeDown() }
            },
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        val showPreview = state.preview.isNotEmpty() && !state.justEvaluated && state.error == null
        ScrollingLine(
            text = state.displayText(),
            fontSizeSp = if (state.error != null) 30 else 56,
            color = if (state.error != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onBackground,
        )
        ScrollingLine(
            text = if (showPreview) "= ${state.preview}" else " ",
            fontSizeSp = 24,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
        )
    }
}

/**
 * A single-line value that never wraps: right-aligned when it fits, and
 * horizontally scrollable (kept pinned to the latest/right-most digits) when
 * it overflows — the industry-standard calculator display behaviour.
 */
@Composable
private fun ScrollingLine(
    text: String,
    fontSizeSp: Int,
    color: androidx.compose.ui.graphics.Color,
) {
    val scroll = rememberScrollState()
    LaunchedEffect(text) { scroll.scrollTo(scroll.maxValue) }
    Box(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scroll),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSizeSp.sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}
