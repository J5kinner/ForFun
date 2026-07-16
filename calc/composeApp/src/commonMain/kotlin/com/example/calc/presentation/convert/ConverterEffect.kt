package com.example.calc.presentation.convert

sealed interface ConverterEffect {
    data object Haptic : ConverterEffect
    data class RequestRates(val base: String) : ConverterEffect
}

data class ConverterReduction(
    val state: ConverterState,
    val effects: List<ConverterEffect> = emptyList(),
)
