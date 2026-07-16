package com.example.calc.presentation

/**
 * Root UI state hosting all calculator modes. Each mode owns an independent
 * sub-state so switching modes never disturbs the others.
 */
data class AppState(
    val mode: CalcMode = CalcMode.Standard,
    val standard: CalculatorState = CalculatorState(),
    val programmer: ProgrammerState = ProgrammerState(),
    // converter sub-state is added in a later phase as its type lands.
)
