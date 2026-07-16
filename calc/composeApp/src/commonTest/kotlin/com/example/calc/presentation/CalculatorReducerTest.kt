package com.example.calc.presentation

import com.example.calc.domain.CalcError
import com.example.calc.domain.MathEngine
import com.example.calc.domain.Operator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalculatorReducerTest {
    private val reducer = CalculatorReducer(MathEngine())
    private fun run(start: CalculatorState, vararg intents: CalculatorIntent): CalculatorState {
        var s = start
        for (i in intents) s = reducer.reduce(s, i).state
        return s
    }

    @Test fun appendsDigits() {
        val s = run(CalculatorState(), CalculatorIntent.Digit('1'), CalculatorIntent.Digit('2'))
        assertEquals("12", s.input)
    }
    @Test fun livePreviewComputed() {
        val s = run(CalculatorState(), CalculatorIntent.Digit('2'), CalculatorIntent.Op(Operator.Plus), CalculatorIntent.Digit('3'))
        assertEquals("5", s.preview)
    }
    @Test fun replacesTrailingOperator() {
        val s = run(CalculatorState(input = "5+"), CalculatorIntent.Op(Operator.Times))
        assertEquals("5×", s.input)
    }
    @Test fun leadingMinusAllowedOthersIgnored() {
        assertEquals("−", run(CalculatorState(), CalculatorIntent.Op(Operator.Minus)).input)
        assertEquals("", run(CalculatorState(), CalculatorIntent.Op(Operator.Times)).input)
    }
    @Test fun decimalGuards() {
        val s = run(CalculatorState(), CalculatorIntent.Digit('5'), CalculatorIntent.Decimal, CalculatorIntent.Decimal)
        assertEquals("5.", s.input)
    }
    @Test fun equalsSetsResultAndPersists() {
        val red = reducer.reduce(CalculatorState(input = "2+3"), CalculatorIntent.Equals)
        assertTrue(red.state.justEvaluated)
        assertEquals("5", red.state.result?.toStringExpanded())
        assertTrue(red.effects.any { it is CalculatorEffect.PersistHistory && it.expression == "2+3" && it.result == "5" })
    }
    @Test fun digitAfterEqualsStartsFresh() {
        val afterEq = reducer.reduce(CalculatorState(input = "2+3"), CalculatorIntent.Equals).state
        val s = reducer.reduce(afterEq, CalculatorIntent.Digit('7')).state
        assertEquals("7", s.input)
        assertNull(s.result)
    }
    @Test fun operatorAfterEqualsContinues() {
        val afterEq = reducer.reduce(CalculatorState(input = "2+3"), CalculatorIntent.Equals).state
        val s = reducer.reduce(afterEq, CalculatorIntent.Op(Operator.Times)).state
        assertEquals("5×", s.input)
    }
    @Test fun equalsDivByZeroSetsError() {
        val s = reducer.reduce(CalculatorState(input = "5÷0"), CalculatorIntent.Equals).state
        assertEquals(CalcError.DivByZero, s.error)
        assertNull(s.result)
    }
    @Test fun clearResetsInput() {
        val s = reducer.reduce(CalculatorState(input = "9"), CalculatorIntent.Clear).state
        assertEquals("", s.input)
    }
    @Test fun deleteRemovesLastChar() {
        assertEquals("1", reducer.reduce(CalculatorState(input = "12"), CalculatorIntent.Delete).state.input)
    }
    @Test fun injectResultAppendsAndClosesSheet() {
        val s = reducer.reduce(CalculatorState(historyVisible = true), CalculatorIntent.InjectResult("1234.5")).state
        assertEquals("1234.5", s.input)
        assertTrue(!s.historyVisible)
    }
}
