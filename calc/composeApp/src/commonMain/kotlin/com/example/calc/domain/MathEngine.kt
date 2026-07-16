package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

class MathEngine {
    private val overflow = BigDecimal.parseString("1E1000")
    private val negOne = BigDecimal.fromInt(-1)
    private val trailing = charArrayOf('+', '−', '×', '÷', '(', '.', '-', '*', '/')

    fun evaluate(expression: String): CalcResult {
        if (expression.isBlank()) return CalcResult.Empty
        return try {
            val value = RpnEvaluator.eval(Parser.toRpn(Tokenizer.tokenize(expression)))
            val abs = if (value.signum() < 0) value.multiply(negOne) else value
            if (abs > overflow) {
                CalcResult.Error(CalcError.Overflow)
            } else {
                CalcResult.Success(value, NumberFormatter.format(value))
            }
        } catch (e: DivideByZeroException) {
            CalcResult.Error(CalcError.DivByZero)
        } catch (e: MalformedExpressionException) {
            CalcResult.Error(CalcError.Malformed)
        } catch (e: ArithmeticException) {
            CalcResult.Error(CalcError.DivByZero)
        } catch (e: Throwable) {
            CalcResult.Error(CalcError.Malformed)
        }
    }

    fun preview(expression: String): CalcResult {
        var s = expression.trimEnd()
        while (s.isNotEmpty() && s.last() in trailing) s = s.dropLast(1)
        if (s.isBlank()) return CalcResult.Empty
        val open = s.count { it == '(' }
        val close = s.count { it == ')' }
        if (open > close) s += ")".repeat(open - close)
        return evaluate(s)
    }
}
