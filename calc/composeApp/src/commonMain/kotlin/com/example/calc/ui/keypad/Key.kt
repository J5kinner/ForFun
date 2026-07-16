package com.example.calc.ui.keypad

enum class KeyStyle { Number, Operator, Function, Accent, Equals, Toggle, ToggleActive }

data class Key<I>(
    val label: String,
    val style: KeyStyle,
    val intent: I,
    val span: Int = 1,
    val enabled: Boolean = true,
)

typealias KeyPad<I> = List<List<Key<I>>>
