package com.example.calc.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * A single-line value that never wraps: right-aligned when it fits, and
 * horizontally scrollable (pinned to the latest/right-most characters) when it
 * overflows — the industry-standard calculator display behaviour.
 */
@Composable
fun ScrollingLine(
    text: String,
    fontSizeSp: Int,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    val scroll = rememberScrollState()
    LaunchedEffect(text) { scroll.scrollTo(scroll.maxValue) }
    Box(
        modifier = modifier.fillMaxWidth().horizontalScroll(scroll),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSizeSp.sp,
            maxLines = 1,
            softWrap = false,
            fontWeight = fontWeight,
        )
    }
}
