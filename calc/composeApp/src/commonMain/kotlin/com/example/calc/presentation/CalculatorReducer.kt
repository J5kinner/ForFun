package com.example.calc.presentation

import com.example.calc.domain.CalcResult
import com.example.calc.domain.MathEngine
import com.example.calc.domain.NumberFormatter
import com.example.calc.domain.Operator
import com.ionspin.kotlin.bignum.decimal.BigDecimal

private val OPERATOR_GLYPHS = charArrayOf('+', '−', '×', '÷')

class CalculatorReducer(private val engine: MathEngine) {

    fun reduce(state: CalculatorState, intent: CalculatorIntent): Reduction = when (intent) {
        is CalculatorIntent.Digit -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + intent.d)
        }
        CalculatorIntent.Decimal -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            val current = base.takeLastWhile { it.isDigit() || it == '.' }
            if (current.contains('.')) {
                Reduction(state)
            } else {
                val glue = base.isEmpty() || base.last() in OPERATOR_GLYPHS || base.last() == '('
                fresh(state, base + if (glue) "0." else ".")
            }
        }
        is CalculatorIntent.Op -> {
            val base = when {
                state.error != null -> ""
                state.justEvaluated && state.result != null -> state.result.toStringExpanded()
                else -> state.input
            }
            if (base.isEmpty()) {
                if (intent.op == Operator.Minus) fresh(state, "−") else Reduction(state)
            } else {
                val newBase = if (base.last() in OPERATOR_GLYPHS) base.dropLast(1) + intent.op.glyph
                else base + intent.op.glyph
                fresh(state, newBase)
            }
        }
        CalculatorIntent.Percent -> {
            val base = if (state.justEvaluated && state.result != null) state.result.toStringExpanded() else state.input
            if (base.isEmpty() || base.last() in OPERATOR_GLYPHS) Reduction(state)
            else fresh(state, base + "%")
        }
        CalculatorIntent.OpenParen -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + "(")
        }
        CalculatorIntent.CloseParen -> {
            val base = state.input
            val open = base.count { it == '(' }
            val close = base.count { it == ')' }
            if (open <= close || base.isEmpty() || base.last() in OPERATOR_GLYPHS || base.last() == '(') Reduction(state)
            else fresh(state, base + ")")
        }
        CalculatorIntent.ToggleSign -> {
            val base = if (state.justEvaluated && state.result != null) state.result.toStringExpanded() else state.input
            if (base.isEmpty()) {
                fresh(state, "−")
            } else {
                var idx = base.length
                while (idx > 0 && (base[idx - 1].isDigit() || base[idx - 1] == '.')) idx--
                if (idx == base.length) {
                    Reduction(state)
                } else {
                    val head = base.substring(0, idx)
                    val num = base.substring(idx)
                    val newBase = if (head.endsWith("−") && (idx - 2 < 0 || !head[idx - 2].isDigit()))
                        head.dropLast(1) + num else head + "−" + num
                    fresh(state, newBase)
                }
            }
        }
        CalculatorIntent.Delete -> {
            val base = state.input
            if (base.isEmpty()) Reduction(state.copy(error = null)) else fresh(state, base.dropLast(1))
        }
        CalculatorIntent.Clear -> Reduction(CalculatorState(memory = state.memory, history = state.history))
        CalculatorIntent.Equals -> equals(state)
        is CalculatorIntent.Memory -> memory(state, intent.action)
        CalculatorIntent.ShowHistory -> Reduction(state.copy(historyVisible = true))
        CalculatorIntent.HideHistory -> Reduction(state.copy(historyVisible = false))
        is CalculatorIntent.InjectExpression -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            val r = fresh(state, base + intent.expr)
            r.copy(state = r.state.copy(historyVisible = false))
        }
        is CalculatorIntent.InjectResult -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            val r = fresh(state, base + intent.value)
            r.copy(state = r.state.copy(historyVisible = false))
        }
        is CalculatorIntent.HistoryLoaded -> Reduction(state.copy(history = intent.items))
        is CalculatorIntent.SwitchMode -> Reduction(state.copy(mode = intent.mode))
    }

    private fun fresh(state: CalculatorState, newInput: String): Reduction {
        val preview = when (val p = engine.preview(newInput)) {
            is CalcResult.Success -> if (hasOperator(newInput)) p.formatted else ""
            else -> ""
        }
        return Reduction(
            state.copy(input = newInput, preview = preview, result = null, error = null, justEvaluated = false),
        )
    }

    private fun hasOperator(s: String): Boolean = s.any { it in OPERATOR_GLYPHS } || s.contains('%')

    private fun equals(state: CalculatorState): Reduction {
        if (state.input.isBlank()) return Reduction(state)
        return when (val r = engine.evaluate(state.input)) {
            is CalcResult.Success -> Reduction(
                state.copy(result = r.value, preview = "", error = null, justEvaluated = true),
                listOf(
                    CalculatorEffect.Haptic,
                    CalculatorEffect.PersistHistory(state.input, NumberFormatter.plain(r.value)),
                ),
            )
            is CalcResult.Error -> Reduction(
                state.copy(error = r.error, result = null, preview = "", justEvaluated = true),
                listOf(CalculatorEffect.ErrorBlip),
            )
            CalcResult.Empty -> Reduction(state)
        }
    }

    private fun currentValue(state: CalculatorState): BigDecimal? = when {
        state.justEvaluated && state.result != null -> state.result
        else -> (engine.evaluate(state.input) as? CalcResult.Success)?.value
    }

    private fun memory(state: CalculatorState, action: MemoryAction): Reduction = when (action) {
        MemoryAction.MC -> Reduction(state.copy(memory = null))
        MemoryAction.MS -> currentValue(state)?.let { Reduction(state.copy(memory = it)) } ?: Reduction(state)
        MemoryAction.MPlus -> currentValue(state)?.let {
            Reduction(state.copy(memory = (state.memory ?: BigDecimal.ZERO).add(it)))
        } ?: Reduction(state)
        MemoryAction.MMinus -> currentValue(state)?.let {
            Reduction(state.copy(memory = (state.memory ?: BigDecimal.ZERO).subtract(it)))
        } ?: Reduction(state)
        MemoryAction.MR -> {
            val m = state.memory ?: return Reduction(state)
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + NumberFormatter.plain(m))
        }
    }
}
