package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

object RpnEvaluator {
    private val HUNDRED = BigDecimal.fromInt(100)
    private val NEG_ONE = BigDecimal.fromInt(-1)
    private val DIV_MODE = DecimalMode(decimalPrecision = 34L, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    fun eval(rpn: List<Rpn>): BigDecimal {
        val st = ArrayDeque<BigDecimal>()
        for (e in rpn) {
            when (e) {
                is Rpn.Value -> st.addLast(e.v)
                Rpn.Neg -> st.addLast(pop(st).multiply(NEG_ONE))
                Rpn.PctScale -> st.addLast(divide(pop(st), HUNDRED))
                Rpn.PctAdd -> {
                    val b = pop(st)
                    val a = st.lastOrNull() ?: throw MalformedExpressionException()
                    st.addLast(divide(a.multiply(b), HUNDRED))
                }
                is Rpn.Bin -> {
                    val b = pop(st)
                    val a = pop(st)
                    st.addLast(
                        when (e.op) {
                            Operator.Plus -> a.add(b)
                            Operator.Minus -> a.subtract(b)
                            Operator.Times -> a.multiply(b)
                            Operator.Divide -> divide(a, b)
                        },
                    )
                }
            }
        }
        return st.singleOrNull() ?: throw MalformedExpressionException()
    }

    private fun pop(st: ArrayDeque<BigDecimal>): BigDecimal =
        st.removeLastOrNull() ?: throw MalformedExpressionException()

    private fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.isZero()) throw DivideByZeroException()
        return a.divide(b, DIV_MODE)
    }
}
