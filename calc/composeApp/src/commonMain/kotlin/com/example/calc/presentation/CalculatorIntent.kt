package com.example.calc.presentation

import com.example.calc.data.HistoryRecord
import com.example.calc.domain.Operator

sealed interface CalculatorIntent {
    data class Digit(val d: Char) : CalculatorIntent
    data object Decimal : CalculatorIntent
    data class Op(val op: Operator) : CalculatorIntent
    data object Percent : CalculatorIntent
    data object OpenParen : CalculatorIntent
    data object CloseParen : CalculatorIntent
    data object ToggleSign : CalculatorIntent
    data object Delete : CalculatorIntent
    data object Clear : CalculatorIntent
    data object Equals : CalculatorIntent
    data object ShowHistory : CalculatorIntent
    data object HideHistory : CalculatorIntent
    data class InjectExpression(val expr: String) : CalculatorIntent
    data class InjectResult(val value: String) : CalculatorIntent
    data class HistoryLoaded(val items: List<HistoryRecord>) : CalculatorIntent
    data class SwitchMode(val mode: CalcMode) : CalculatorIntent
}
