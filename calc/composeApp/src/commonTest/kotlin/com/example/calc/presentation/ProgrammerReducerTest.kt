package com.example.calc.presentation

import com.example.calc.domain.Radix
import com.example.calc.domain.WordSize
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgrammerReducerTest {
    private val reducer = ProgrammerReducer()
    private fun run(start: ProgrammerState, vararg intents: ProgrammerIntent): ProgrammerState {
        var s = start
        for (i in intents) s = reducer.reduce(s, i)
        return s
    }

    private val dec = ProgrammerState(radix = Radix.DEC)
    private val hex = ProgrammerState(radix = Radix.HEX)
    private val hexByte = ProgrammerState(radix = Radix.HEX, wordSize = WordSize.BYTE)

    // --- entry ---
    @Test fun appendsDecimalDigits() {
        val s = run(dec, ProgrammerIntent.Digit(5), ProgrammerIntent.Digit(3))
        assertEquals(53L, s.entry)
    }
    @Test fun illegalDigitIsNoOp() {
        val s = run(ProgrammerState(radix = Radix.BIN), ProgrammerIntent.Digit(2))
        assertEquals(0L, s.entry)
    }
    @Test fun deleteDropsLastHexDigit() {
        val s = run(hexByte, ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(15), ProgrammerIntent.Delete)
        assertEquals(0x0FL, s.entry)
    }
    @Test fun clearPreservesRadixAndWordSize() {
        val s = run(hexByte, ProgrammerIntent.Digit(15), ProgrammerIntent.Clear)
        assertEquals(0L, s.entry)
        assertEquals(Radix.HEX, s.radix)
        assertEquals(WordSize.BYTE, s.wordSize)
    }

    // --- ops ---
    @Test fun addDecimal() {
        val s = run(dec, ProgrammerIntent.Digit(5), ProgrammerIntent.Binary(ProgBinOp.Add), ProgrammerIntent.Digit(3), ProgrammerIntent.Equals)
        assertEquals(8L, s.entry)
    }
    @Test fun andHex() {
        val s = run(
            hex,
            ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(0), // F0
            ProgrammerIntent.Binary(ProgBinOp.And),
            ProgrammerIntent.Digit(0), ProgrammerIntent.Digit(15), // 0F
            ProgrammerIntent.Equals,
        )
        assertEquals(0L, s.entry)
    }
    @Test fun chainingFoldsPending() {
        val s = run(
            hex,
            ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(0), // F0
            ProgrammerIntent.Binary(ProgBinOp.And),
            ProgrammerIntent.Digit(0), ProgrammerIntent.Digit(15), // 0F  -> folds to 0
            ProgrammerIntent.Binary(ProgBinOp.Or),
            ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(15), // FF
            ProgrammerIntent.Equals,
        )
        assertEquals(0xFFL, s.entry)
    }
    @Test fun unaryNotImmediate() {
        val s = run(hexByte, ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(0), ProgrammerIntent.Unary(ProgUnaryOp.Not))
        assertEquals(0x0FL, s.entry)
    }
    @Test fun shiftLeft() {
        val s = run(hex, ProgrammerIntent.Digit(1), ProgrammerIntent.Binary(ProgBinOp.Shl), ProgrammerIntent.Digit(4), ProgrammerIntent.Equals)
        assertEquals(0x10L, s.entry)
    }
    @Test fun logicalVsArithmeticShift() {
        val logical = run(hexByte, ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(15), ProgrammerIntent.Binary(ProgBinOp.ShrLogical), ProgrammerIntent.Digit(1), ProgrammerIntent.Equals)
        assertEquals(0x7FL, logical.entry)
        val arith = run(hexByte, ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(15), ProgrammerIntent.Binary(ProgBinOp.ShrArith), ProgrammerIntent.Digit(1), ProgrammerIntent.Equals)
        assertEquals(0xFFL, arith.entry)
    }
    @Test fun repeatedEqualsThenContinue() {
        val s = run(
            dec,
            ProgrammerIntent.Digit(2), ProgrammerIntent.Binary(ProgBinOp.Add), ProgrammerIntent.Digit(3), ProgrammerIntent.Equals, // 5
            ProgrammerIntent.Binary(ProgBinOp.Add), ProgrammerIntent.Digit(1), ProgrammerIntent.Digit(0), ProgrammerIntent.Equals, // +10
        )
        assertEquals(15L, s.entry)
    }

    // --- mode ---
    @Test fun wordSizeTruncates() {
        val s = run(hex, ProgrammerIntent.Digit(1), ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(15), ProgrammerIntent.SetWordSize(WordSize.BYTE))
        assertEquals(0xFFL, s.entry) // 0x1FF masked to BYTE
    }
    @Test fun wordSizeChangeFlipsSignedDecDisplay() {
        val byte = run(hexByte, ProgrammerIntent.Digit(15), ProgrammerIntent.Digit(15), ProgrammerIntent.SetRadix(Radix.DEC))
        assertEquals("-1", byte.displayValue())
        val word = reducer.reduce(byte, ProgrammerIntent.SetWordSize(WordSize.WORD))
        assertEquals("255", word.displayValue())
    }
    @Test fun divByZeroSetsError() {
        val s = run(dec, ProgrammerIntent.Digit(5), ProgrammerIntent.Binary(ProgBinOp.Div), ProgrammerIntent.Digit(0), ProgrammerIntent.Equals)
        assertEquals(ProgError.DivByZero, s.error)
    }
    @Test fun digitAfterErrorClearsIt() {
        val err = run(dec, ProgrammerIntent.Digit(5), ProgrammerIntent.Binary(ProgBinOp.Div), ProgrammerIntent.Digit(0), ProgrammerIntent.Equals)
        val s = reducer.reduce(err, ProgrammerIntent.Digit(7))
        assertEquals(7L, s.entry)
        assertEquals(null, s.error)
    }
}
