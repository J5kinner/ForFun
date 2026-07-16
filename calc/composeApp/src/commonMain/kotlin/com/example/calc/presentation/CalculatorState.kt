package com.example.calc.presentation

import com.example.calc.data.HistoryRecord
import com.example.calc.domain.CalcError
import com.example.calc.domain.NumberFormatter
import com.ionspin.kotlin.bignum.decimal.BigDecimal

enum class CalcMode { Standard }

data class CalculatorState(
    val input: String = "",
    val preview: String = "",
    val result: BigDecimal? = null,
    val error: CalcError? = null,
    val justEvaluated: Boolean = false,
    val memory: BigDecimal? = null,
    val history: List<HistoryRecord> = emptyList(),
    val historyVisible: Boolean = false,
    val mode: CalcMode = CalcMode.Standard,
)

fun CalculatorState.displayText(): String = when {
    error != null -> when (error) {
        CalcError.DivByZero -> "Can't divide by 0"
        CalcError.Overflow -> "Number too large"
        CalcError.Malformed -> "Invalid expression"
    }
    justEvaluated && result != null -> NumberFormatter.format(result)
    else -> input.ifEmpty { "0" }
}

fun CalculatorState.resultToInject(): String? = result?.let { NumberFormatter.plain(it) }
