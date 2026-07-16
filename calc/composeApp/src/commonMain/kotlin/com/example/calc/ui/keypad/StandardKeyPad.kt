package com.example.calc.ui.keypad

import com.example.calc.domain.Operator
import com.example.calc.presentation.CalculatorIntent

private fun digit(c: Char) = Key(c.toString(), KeyStyle.Number, CalculatorIntent.Digit(c))

/**
 * Thumb-friendly layout: utilities on the top row, the number pad forms the
 * lower-left block (0 on the very bottom row), and every operator lives in the
 * right-hand column with "=" at the bottom-right — reachable one-handed or with
 * two thumbs. Uniform square keys for large, consistent touch targets.
 */
val standardKeyPad: KeyPad = listOf(
    listOf(
        Key("C", KeyStyle.Accent, CalculatorIntent.Clear),
        Key("⌫", KeyStyle.Function, CalculatorIntent.Delete),
        Key("%", KeyStyle.Function, CalculatorIntent.Percent),
        Key("÷", KeyStyle.Operator, CalculatorIntent.Op(Operator.Divide)),
    ),
    listOf(digit('7'), digit('8'), digit('9'), Key("×", KeyStyle.Operator, CalculatorIntent.Op(Operator.Times))),
    listOf(digit('4'), digit('5'), digit('6'), Key("−", KeyStyle.Operator, CalculatorIntent.Op(Operator.Minus))),
    listOf(digit('1'), digit('2'), digit('3'), Key("+", KeyStyle.Operator, CalculatorIntent.Op(Operator.Plus))),
    listOf(
        Key("±", KeyStyle.Function, CalculatorIntent.ToggleSign),
        digit('0'),
        Key(".", KeyStyle.Number, CalculatorIntent.Decimal),
        Key("=", KeyStyle.Equals, CalculatorIntent.Equals),
    ),
)
