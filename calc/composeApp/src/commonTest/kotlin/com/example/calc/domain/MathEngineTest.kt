package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathEngineTest {
    private val engine = MathEngine()

    @Test fun evaluatesAndFormats() {
        val r = engine.evaluate("0.1+0.2")
        assertTrue(r is CalcResult.Success)
        assertEquals("0.3", (r as CalcResult.Success).formatted)
    }
    @Test fun blankIsEmpty() = assertEquals(CalcResult.Empty, engine.evaluate("  "))
    @Test fun divByZeroIsError() {
        assertEquals(CalcResult.Error(CalcError.DivByZero), engine.evaluate("5÷0"))
    }
    @Test fun previewTrimsTrailingOperator() {
        val r = engine.preview("2+3+")
        assertEquals("5", (r as CalcResult.Success).formatted)
    }
    @Test fun previewBalancesParens() {
        val r = engine.preview("(2+3")
        assertEquals("5", (r as CalcResult.Success).formatted)
    }
    @Test fun previewOfBareNumberIsSuccess() {
        assertTrue(engine.preview("7") is CalcResult.Success)
    }
}
