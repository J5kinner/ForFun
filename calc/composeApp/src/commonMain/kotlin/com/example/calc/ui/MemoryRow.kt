package com.example.calc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.presentation.MemoryAction

@Composable
fun MemoryRow(
    hasMemory: Boolean,
    onMemory: (MemoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        "MC" to MemoryAction.MC,
        "MR" to MemoryAction.MR,
        "M+" to MemoryAction.MPlus,
        "M−" to MemoryAction.MMinus,
        "MS" to MemoryAction.MS,
    )
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        for ((label, action) in items) {
            val enabled = !(action == MemoryAction.MC || action == MemoryAction.MR) || hasMemory
            TextButton(onClick = { onMemory(action) }, enabled = enabled) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
    }
}
