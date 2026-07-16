package com.example.calc.presentation

import com.example.calc.domain.Bits
import com.example.calc.domain.ProgArithmeticException

/** Pure immediate-execution accumulator reducer for Programmer mode. */
class ProgrammerReducer {

    fun reduce(state: ProgrammerState, intent: ProgrammerIntent): ProgrammerState = when (intent) {
        is ProgrammerIntent.Digit -> {
            if (!state.radix.allowsDigit(intent.value)) {
                state // illegal digit for this radix: no-op
            } else {
                val base = if (state.freshEntry || state.error != null) 0L else state.entry
                state.copy(
                    entry = Bits.appendDigit(base, intent.value, state.radix, state.wordSize),
                    freshEntry = false,
                    error = null,
                )
            }
        }

        is ProgrammerIntent.Binary -> {
            val folded = if (state.pending != null && !state.freshEntry) {
                applyPending(state)
            } else {
                state.copy(acc = state.entry)
            }
            folded.copy(pending = intent.op, freshEntry = true, error = null)
        }

        is ProgrammerIntent.Unary -> {
            val v = when (intent.op) {
                ProgUnaryOp.Not -> Bits.not(state.entry, state.wordSize)
                ProgUnaryOp.Negate -> Bits.negate(state.entry, state.wordSize)
            }
            state.copy(entry = v, freshEntry = false, error = null)
        }

        ProgrammerIntent.Equals ->
            if (state.pending == null) state
            else applyPending(state).copy(pending = null, freshEntry = true)

        ProgrammerIntent.Delete ->
            if (state.freshEntry) state
            else state.copy(entry = Bits.dropDigit(state.entry, state.radix, state.wordSize))

        ProgrammerIntent.Clear -> ProgrammerState(wordSize = state.wordSize, radix = state.radix)

        is ProgrammerIntent.SetRadix -> state.copy(radix = intent.radix)

        is ProgrammerIntent.SetWordSize -> state.copy(
            wordSize = intent.wordSize,
            entry = Bits.mask(state.entry, intent.wordSize),
            acc = Bits.mask(state.acc, intent.wordSize),
        )
    }

    private fun applyPending(state: ProgrammerState): ProgrammerState {
        val op = state.pending ?: return state
        val ws = state.wordSize
        val a = state.acc
        val b = state.entry
        return try {
            val r = when (op) {
                ProgBinOp.And -> Bits.and(a, b, ws)
                ProgBinOp.Or -> Bits.or(a, b, ws)
                ProgBinOp.Xor -> Bits.xor(a, b, ws)
                ProgBinOp.Nand -> Bits.nand(a, b, ws)
                ProgBinOp.Nor -> Bits.nor(a, b, ws)
                ProgBinOp.Xnor -> Bits.xnor(a, b, ws)
                ProgBinOp.Shl -> Bits.shl(a, b.toInt(), ws)
                ProgBinOp.ShrLogical -> Bits.shrLogical(a, b.toInt(), ws)
                ProgBinOp.ShrArith -> Bits.shrArithmetic(a, b.toInt(), ws)
                ProgBinOp.Add -> Bits.add(a, b, ws)
                ProgBinOp.Sub -> Bits.sub(a, b, ws)
                ProgBinOp.Mul -> Bits.mul(a, b, ws)
                ProgBinOp.Div -> Bits.div(a, b, ws)
                ProgBinOp.Mod -> Bits.rem(a, b, ws)
            }
            state.copy(entry = r, acc = r, error = null)
        } catch (e: ProgArithmeticException) {
            state.copy(error = ProgError.DivByZero, freshEntry = true)
        }
    }
}
