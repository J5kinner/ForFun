package com.example.calc.domain

enum class Radix(val label: String, val base: Int) {
    HEX("HEX", 16),
    DEC("DEC", 10),
    OCT("OCT", 8),
    BIN("BIN", 2);

    /** True if a keypad digit with this numeric value is legal in this radix. */
    fun allowsDigit(value: Int): Boolean = value in 0 until base
}
