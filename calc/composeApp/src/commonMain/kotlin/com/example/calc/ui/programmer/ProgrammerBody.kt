package com.example.calc.ui.programmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calc.presentation.ProgrammerIntent
import com.example.calc.presentation.ProgrammerState
import com.example.calc.ui.keypad.CalculatorKeypad
import com.example.calc.ui.keypad.programmerKeyPad

@Composable
fun ProgrammerBody(
    state: ProgrammerState,
    onIntent: (ProgrammerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        ProgrammerDisplay(state = state, onIntent = onIntent, modifier = Modifier.weight(1f))
        CalculatorKeypad(pad = programmerKeyPad(state), onKey = onIntent, keyAspect = 1.55f)
    }
}
