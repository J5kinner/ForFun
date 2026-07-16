package com.example.calc.ui.programmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.domain.Radix
import com.example.calc.domain.RadixFormatter
import com.example.calc.domain.WordSize
import com.example.calc.presentation.ProgrammerIntent
import com.example.calc.presentation.ProgrammerState
import com.example.calc.presentation.displayValue
import com.example.calc.ui.ScrollingLine

@Composable
fun ProgrammerDisplay(
    state: ProgrammerState,
    onIntent: (ProgrammerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (state.error != null) {
            ScrollingLine(
                text = state.displayValue(),
                fontSizeSp = 28,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            val all = RadixFormatter.all(state.entry, state.wordSize)
            for (r in Radix.entries) {
                RadixRow(
                    label = r.label,
                    value = all[r] ?: "0",
                    active = r == state.radix,
                    onClick = { onIntent(ProgrammerIntent.SetRadix(r)) },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        WordSizeRow(selected = state.wordSize, onSelect = { onIntent(ProgrammerIntent.SetWordSize(it)) })
    }
}

@Composable
private fun RadixRow(label: String, value: String, active: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(46.dp),
            color = if (active) scheme.primary else scheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
        ScrollingLine(
            text = value,
            fontSizeSp = if (active) 30 else 17,
            color = if (active) scheme.onBackground else scheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun WordSizeRow(selected: WordSize, onSelect: (WordSize) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (ws in WordSize.entries) {
            val active = ws == selected
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (active) scheme.primary else scheme.surfaceVariant)
                    .clickable { onSelect(ws) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ws.label,
                    color = if (active) scheme.onPrimary else scheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
