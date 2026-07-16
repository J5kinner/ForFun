package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RpnEvaluatorTest {
    private fun eval(s: String) =
        RpnEvaluator.eval(Parser.toRpn(Tokenizer.tokenize(s))).toStringExpanded()

    @Test fun floatingPointIsExact() = assertEquals("0.3", eval("0.1+0.2"))
    @Test fun precedence() = assertEquals("14", eval("2+3×4"))
    @Test fun parentheses() = assertEquals("20", eval("(2+3)×4"))
    @Test fun unaryMinus() = assertEquals("-6", eval("2×−3"))
    @Test fun percentAdditive() = assertEquals("110", eval("100+10%"))
    @Test fun percentSubtractive() = assertEquals("90", eval("100−10%"))
    @Test fun percentMultiplicative() = assertEquals("20", eval("200×10%"))
    @Test fun percentDivide() = assertEquals("2000", eval("200÷10%"))
    @Test fun division() = assertEquals("2.5", eval("5÷2"))
    @Test fun divideByZeroThrows() {
        assertFailsWith<DivideByZeroException> { RpnEvaluator.eval(Parser.toRpn(Tokenizer.tokenize("5÷0"))) }
    }
}
