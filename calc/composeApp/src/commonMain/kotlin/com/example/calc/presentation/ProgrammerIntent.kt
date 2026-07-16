package com.example.calc.presentation

import com.example.calc.domain.Radix
import com.example.calc.domain.WordSize

enum class ProgBinOp { And, Or, Xor, Nand, Nor, Xnor, Shl, ShrLogical, ShrArith, Add, Sub, Mul, Div, Mod }
enum class ProgUnaryOp { Not, Negate }

sealed interface ProgrammerIntent {
    data class Digit(val value: Int) : ProgrammerIntent // 0..15 (A=10 … F=15)
    data class Binary(val op: ProgBinOp) : ProgrammerIntent
    data class Unary(val op: ProgUnaryOp) : ProgrammerIntent
    data object Equals : ProgrammerIntent
    data object Delete : ProgrammerIntent
    data object Clear : ProgrammerIntent
    data class SetRadix(val radix: Radix) : ProgrammerIntent
    data class SetWordSize(val wordSize: WordSize) : ProgrammerIntent
}
