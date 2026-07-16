package com.example.calc.ui.keypad

import com.example.calc.presentation.ProgBinOp
import com.example.calc.presentation.ProgUnaryOp
import com.example.calc.presentation.ProgrammerIntent
import com.example.calc.presentation.ProgrammerState

/**
 * Radix-aware Programmer keypad: bitwise + shift ops, then a full hex digit pad.
 * Digits illegal in the current radix render disabled (e.g. A–F outside HEX).
 */
fun programmerKeyPad(state: ProgrammerState): KeyPad<ProgrammerIntent> {
    fun d(label: String, v: Int, span: Int = 1): Key<ProgrammerIntent> =
        Key(label, KeyStyle.Number, ProgrammerIntent.Digit(v), span = span, enabled = state.radix.allowsDigit(v))
    fun bin(label: String, op: ProgBinOp, style: KeyStyle = KeyStyle.Operator): Key<ProgrammerIntent> =
        Key(label, style, ProgrammerIntent.Binary(op))
    fun un(label: String, op: ProgUnaryOp): Key<ProgrammerIntent> =
        Key(label, KeyStyle.Function, ProgrammerIntent.Unary(op))

    return listOf(
        listOf(
            bin("AND", ProgBinOp.And, KeyStyle.Function),
            bin("OR", ProgBinOp.Or, KeyStyle.Function),
            bin("XOR", ProgBinOp.Xor, KeyStyle.Function),
            un("NOT", ProgUnaryOp.Not),
        ),
        listOf(
            bin("NAND", ProgBinOp.Nand, KeyStyle.Function),
            bin("NOR", ProgBinOp.Nor, KeyStyle.Function),
            bin("XNOR", ProgBinOp.Xnor, KeyStyle.Function),
            un("NEG", ProgUnaryOp.Negate),
        ),
        listOf(
            bin("<<", ProgBinOp.Shl, KeyStyle.Function),
            bin(">>", ProgBinOp.ShrArith, KeyStyle.Function),
            bin(">>>", ProgBinOp.ShrLogical, KeyStyle.Function),
            bin("MOD", ProgBinOp.Mod, KeyStyle.Function),
        ),
        listOf(d("D", 13), d("E", 14), d("F", 15), bin("÷", ProgBinOp.Div)),
        listOf(d("A", 10), d("B", 11), d("C", 12), bin("×", ProgBinOp.Mul)),
        listOf(d("7", 7), d("8", 8), d("9", 9), bin("−", ProgBinOp.Sub)),
        listOf(d("4", 4), d("5", 5), d("6", 6), bin("+", ProgBinOp.Add)),
        listOf(d("1", 1), d("2", 2), d("3", 3), Key("=", KeyStyle.Equals, ProgrammerIntent.Equals)),
        listOf(
            Key("AC", KeyStyle.Accent, ProgrammerIntent.Clear),
            Key("⌫", KeyStyle.Function, ProgrammerIntent.Delete),
            d("0", 0, span = 2),
        ),
    )
}
