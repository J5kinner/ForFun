package com.example.calc.ui.keypad

import com.example.calc.presentation.convert.ConverterIntent

/**
 * Numeric keypad for the Unit Converter. No operators — just number entry.
 * The sign toggle is enabled only when the active category allows negatives
 * (i.e. Temperature).
 */
fun converterKeyPad(signAllowed: Boolean): KeyPad<ConverterIntent> {
    fun d(label: String, c: Char, span: Int = 1): Key<ConverterIntent> =
        Key(label, KeyStyle.Number, ConverterIntent.Digit(c), span = span)
    return listOf(
        listOf(d("7", '7'), d("8", '8'), d("9", '9'), Key("⌫", KeyStyle.Function, ConverterIntent.Delete)),
        listOf(d("4", '4'), d("5", '5'), d("6", '6'), Key("AC", KeyStyle.Accent, ConverterIntent.Clear)),
        listOf(
            d("1", '1'), d("2", '2'), d("3", '3'),
            Key("±", KeyStyle.Function, ConverterIntent.ToggleSign, enabled = signAllowed),
        ),
        listOf(
            d("0", '0', span = 2),
            Key(".", KeyStyle.Number, ConverterIntent.Decimal, span = 2),
        ),
    )
}
