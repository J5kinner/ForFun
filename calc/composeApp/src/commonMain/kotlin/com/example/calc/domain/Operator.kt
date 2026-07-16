package com.example.calc.domain

enum class Operator(val glyph: Char, val precedence: Int) {
    Plus('+', 1),
    Minus('−', 1),
    Times('×', 2),
    Divide('÷', 2),
}
