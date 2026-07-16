package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParserTest {
    private fun rpn(s: String) = Parser.toRpn(Tokenizer.tokenize(s)).joinToString(" ") {
        when (it) {
            is Rpn.Value -> it.v.toStringExpanded()
            is Rpn.Bin -> it.op.glyph.toString()
            Rpn.Neg -> "neg"
            Rpn.PctScale -> "pctS"
            Rpn.PctAdd -> "pctA"
        }
    }

    @Test fun precedence() = assertEquals("2 3 4 × +", rpn("2+3×4"))
    @Test fun parentheses() = assertEquals("2 3 + 4 ×", rpn("(2+3)×4"))
    @Test fun unaryMinus() = assertEquals("3 neg", rpn("−3"))
    @Test fun percentMultiplicativeIsScale() = assertEquals("200 10 pctS ×", rpn("200×10%"))
    @Test fun percentAdditiveIsPctAdd() = assertEquals("100 10 pctA +", rpn("100+10%"))
    @Test fun barePercentIsScale() = assertEquals("50 pctS", rpn("50%"))
    @Test fun unbalancedParensThrow() {
        assertFailsWith<MalformedExpressionException> { Parser.toRpn(Tokenizer.tokenize("(2+3")) }
    }
}
