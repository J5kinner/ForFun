package com.example.calc.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.presentation.CalculatorState
import com.example.calc.presentation.displayText

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 24f) onSwipeDown() }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount -> if (dragAmount < -24f) onSwipeLeft() }
            },
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        val showPreview = state.preview.isNotEmpty() && !state.justEvaluated && state.error == null
        Text(
            text = state.displayText(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 56.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        )
        Text(
            text = if (showPreview) "= ${state.preview}" else " ",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
            fontSize = 24.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        )
    }
}
