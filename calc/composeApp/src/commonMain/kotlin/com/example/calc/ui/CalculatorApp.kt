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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calc.presentation.AppViewModel
import com.example.calc.presentation.CalcMode
import com.example.calc.presentation.CalculatorIntent

@Composable
fun CalculatorApp(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier.fillMaxSize().widthIn(max = 560.dp).padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ModeSwitcher(selected = state.mode, onSelect = vm::selectMode)
                    if (state.mode == CalcMode.Standard) {
                        IconButton(onClick = { vm.onStandard(CalculatorIntent.ShowHistory) }) {
                            Icon(
                                imageVector = HistoryIcon,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
                when (state.mode) {
                    CalcMode.Standard -> StandardBody(
                        state = state.standard,
                        dispatch = vm::onStandard,
                        modifier = Modifier.weight(1f),
                    )
                    CalcMode.Programmer -> PlaceholderBody("Programmer", Modifier.weight(1f))
                    CalcMode.Converter -> PlaceholderBody("Converter", Modifier.weight(1f))
                }
            }
        }
    }
    if (state.mode == CalcMode.Standard && state.standard.historyVisible) {
        HistorySheet(
            history = state.standard.history,
            onInjectExpression = { vm.onStandard(CalculatorIntent.InjectExpression(it)) },
            onInjectResult = { vm.onStandard(CalculatorIntent.InjectResult(it)) },
            onClear = { vm.clearHistory() },
            onDismiss = { vm.onStandard(CalculatorIntent.HideHistory) },
        )
    }
}

@Composable
private fun PlaceholderBody(label: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(label, color = MaterialTheme.colorScheme.onBackground)
    }
}
