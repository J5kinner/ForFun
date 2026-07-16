package com.example.calc.ui.keypad

import com.example.calc.presentation.CalculatorIntent

enum class KeyStyle { Number, Operator, Function, Accent, Equals }

data class Key(
    val label: String,
    val style: KeyStyle,
    val intent: CalculatorIntent,
    val span: Int = 1,
)

typealias KeyPad = List<List<Key>>
