package com.example.calc.domain

object Tokenizer {
    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) i++
                    tokens += Token.Num(input.substring(start, i))
                }
                c == '(' -> { tokens += Token.LParen; i++ }
                c == ')' -> { tokens += Token.RParen; i++ }
                c == '%' -> { tokens += Token.Percent; i++ }
                else -> {
                    val op = when (c) {
                        '+' -> Operator.Plus
                        '-', '−' -> Operator.Minus
                        '*', '×' -> Operator.Times
                        '/', '÷' -> Operator.Divide
                        else -> null
                    }
                    if (op == null) {
                        i++
                    } else {
                        val prev = tokens.lastOrNull()
                        val unary = op == Operator.Minus &&
                            (prev == null || prev is Token.Op || prev is Token.LParen || prev is Token.UnaryMinus)
                        tokens += if (unary) Token.UnaryMinus else Token.Op(op)
                        i++
                    }
                }
            }
        }
        return tokens
    }
}
