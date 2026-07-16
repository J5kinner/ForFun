package com.example.calc.ui.keypad

import com.example.calc.domain.Operator
import com.example.calc.presentation.CalculatorIntent

private fun digit(c: Char) = Key(c.toString(), KeyStyle.Number, CalculatorIntent.Digit(c))

val standardKeyPad: KeyPad = listOf(
    listOf(
        Key("C", KeyStyle.Accent, CalculatorIntent.Clear),
        Key("(", KeyStyle.Function, CalculatorIntent.OpenParen),
        Key(")", KeyStyle.Function, CalculatorIntent.CloseParen),
        Key("⌫", KeyStyle.Function, CalculatorIntent.Delete),
    ),
    listOf(digit('7'), digit('8'), digit('9'), Key("÷", KeyStyle.Operator, CalculatorIntent.Op(Operator.Divide))),
    listOf(digit('4'), digit('5'), digit('6'), Key("×", KeyStyle.Operator, CalculatorIntent.Op(Operator.Times))),
    listOf(digit('1'), digit('2'), digit('3'), Key("−", KeyStyle.Operator, CalculatorIntent.Op(Operator.Minus))),
    listOf(
        Key("±", KeyStyle.Function, CalculatorIntent.ToggleSign),
        digit('0'),
        Key(".", KeyStyle.Number, CalculatorIntent.Decimal),
        Key("+", KeyStyle.Operator, CalculatorIntent.Op(Operator.Plus)),
    ),
    listOf(
        Key("%", KeyStyle.Function, CalculatorIntent.Percent, span = 2),
        Key("=", KeyStyle.Equals, CalculatorIntent.Equals, span = 2),
    ),
)
