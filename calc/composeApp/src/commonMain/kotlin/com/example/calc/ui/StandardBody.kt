package com.example.calc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calc.presentation.CalculatorIntent
import com.example.calc.presentation.CalculatorState
import com.example.calc.ui.keypad.CalculatorKeypad
import com.example.calc.ui.keypad.standardKeyPad

@Composable
fun StandardBody(
    state: CalculatorState,
    dispatch: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        CalculatorDisplay(
            state = state,
            onSwipeDown = { dispatch(CalculatorIntent.ShowHistory) },
            modifier = Modifier.weight(1f),
        )
        CalculatorKeypad(pad = standardKeyPad, onKey = dispatch)
    }
}
