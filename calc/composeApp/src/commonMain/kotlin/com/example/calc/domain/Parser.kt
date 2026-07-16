package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

object Parser {
    private sealed interface StackItem {
        data class Binary(val op: Operator) : StackItem
        data object Unary : StackItem
        data object LParen : StackItem
    }

    fun toRpn(tokens: List<Token>): List<Rpn> {
        val output = mutableListOf<Rpn>()
        val ops = ArrayDeque<StackItem>()

        fun emit(item: StackItem) {
            when (item) {
                is StackItem.Binary -> output += Rpn.Bin(item.op)
                StackItem.Unary -> output += Rpn.Neg
                StackItem.LParen -> throw MalformedExpressionException()
            }
        }

        for (t in tokens) {
            when (t) {
                is Token.Num -> output += Rpn.Value(parse(t.text))
                Token.UnaryMinus -> ops.addLast(StackItem.Unary) // right-assoc, binds tightest
                is Token.Op -> {
                    while (ops.isNotEmpty()) {
                        val top = ops.last()
                        val pop = when (top) {
                            StackItem.LParen -> false
                            StackItem.Unary -> true
                            is StackItem.Binary -> top.op.precedence >= t.operator.precedence
                        }
                        if (pop) emit(ops.removeLast()) else break
                    }
                    ops.addLast(StackItem.Binary(t.operator))
                }
                Token.Percent -> output += percentOp(ops)
                Token.LParen -> ops.addLast(StackItem.LParen)
                Token.RParen -> {
                    while (ops.isNotEmpty() && ops.last() != StackItem.LParen) emit(ops.removeLast())
                    if (ops.isEmpty()) throw MalformedExpressionException()
                    ops.removeLast() // discard LParen
                }
            }
        }
        while (ops.isNotEmpty()) emit(ops.removeLast())
        return output
    }

    private fun percentOp(ops: ArrayDeque<StackItem>): Rpn {
        for (k in ops.indices.reversed()) {
            when (val it = ops[k]) {
                StackItem.Unary -> continue
                is StackItem.Binary ->
                    return if (it.op == Operator.Plus || it.op == Operator.Minus) Rpn.PctAdd else Rpn.PctScale
                StackItem.LParen -> return Rpn.PctScale
            }
        }
        return Rpn.PctScale
    }

    private fun parse(text: String): BigDecimal =
        try {
            BigDecimal.parseString(text)
        } catch (e: Throwable) {
            throw MalformedExpressionException()
        }
}
