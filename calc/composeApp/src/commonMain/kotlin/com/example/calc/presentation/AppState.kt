package com.example.calc.presentation

import com.example.calc.presentation.convert.ConverterState

/**
 * Root UI state hosting all calculator modes. Each mode owns an independent
 * sub-state so switching modes never disturbs the others.
 */
data class AppState(
    val mode: CalcMode = CalcMode.Standard,
    val standard: CalculatorState = CalculatorState(),
    val programmer: ProgrammerState = ProgrammerState(),
    val converter: ConverterState = ConverterState(),
)
