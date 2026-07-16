package com.example.calc.presentation

sealed interface CalculatorEffect {
    data object Haptic : CalculatorEffect
    data object ErrorBlip : CalculatorEffect
    data class PersistHistory(val expression: String, val result: String) : CalculatorEffect
}

data class Reduction(
    val state: CalculatorState,
    val effects: List<CalculatorEffect> = emptyList(),
)
