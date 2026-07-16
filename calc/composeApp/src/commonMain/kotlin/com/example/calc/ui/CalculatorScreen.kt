package com.example.calc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calc.presentation.CalculatorIntent
import com.example.calc.presentation.CalculatorViewModel
import com.example.calc.ui.keypad.CalculatorKeypad
import com.example.calc.ui.keypad.standardKeyPad

@Composable
fun CalculatorScreen(vm: CalculatorViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { vm.dispatch(CalculatorIntent.ShowHistory) }) {
                        Icon(
                            imageVector = HistoryIcon,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                CalculatorDisplay(
                    state = state,
                    onSwipeDown = { vm.dispatch(CalculatorIntent.ShowHistory) },
                    modifier = Modifier.weight(1f),
                )
                CalculatorKeypad(pad = standardKeyPad, onKey = { vm.dispatch(it) })
            }
        }
    }
    if (state.historyVisible) {
        HistorySheet(
            history = state.history,
            onInjectExpression = { vm.dispatch(CalculatorIntent.InjectExpression(it)) },
            onInjectResult = { vm.dispatch(CalculatorIntent.InjectResult(it)) },
            onClear = { vm.clearHistory() },
            onDismiss = { vm.dispatch(CalculatorIntent.HideHistory) },
        )
    }
}
