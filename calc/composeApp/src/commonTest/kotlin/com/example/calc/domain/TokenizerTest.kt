package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenizerTest {
    @Test fun tokenizesNumbersAndOperators() {
        assertEquals(
            listOf(Token.Num("12"), Token.Op(Operator.Plus), Token.Num("3.5")),
            Tokenizer.tokenize("12+3.5"),
        )
    }
    @Test fun acceptsGlyphOperators() {
        assertEquals(
            listOf(Token.Num("2"), Token.Op(Operator.Times), Token.Num("4")),
            Tokenizer.tokenize("2×4"),
        )
    }
    @Test fun detectsLeadingUnaryMinus() {
        assertEquals(listOf(Token.UnaryMinus, Token.Num("3")), Tokenizer.tokenize("−3"))
    }
    @Test fun detectsUnaryMinusAfterOperator() {
        assertEquals(
            listOf(Token.Num("2"), Token.Op(Operator.Times), Token.UnaryMinus, Token.Num("3")),
            Tokenizer.tokenize("2×−3"),
        )
    }
    @Test fun binaryMinusBetweenNumbers() {
        assertEquals(
            listOf(Token.Num("5"), Token.Op(Operator.Minus), Token.Num("2")),
            Tokenizer.tokenize("5−2"),
        )
    }
    @Test fun handlesParensAndPercent() {
        assertEquals(
            listOf(Token.LParen, Token.Num("50"), Token.Percent, Token.RParen),
            Tokenizer.tokenize("(50%)"),
        )
    }
}
