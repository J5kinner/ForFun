package com.example.calc.domain

import com.example.calc.domain.WordSize.BYTE
import com.example.calc.domain.WordSize.DWORD
import com.example.calc.domain.WordSize.QWORD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BitsTest {
    // --- mask / signed ---
    @Test fun maskKeepsLowBits() {
        assertEquals(0xFFL, Bits.mask(0x1FFL, BYTE))
        assertEquals(-1L, Bits.mask(-1L, QWORD))
    }
    @Test fun signExtend() {
        assertEquals(-1L, Bits.signed(0xFFL, BYTE))
        assertEquals(-128L, Bits.signed(0x80L, BYTE))
        assertEquals(-2147483648L, Bits.signed(0x8000_0000L, DWORD))
        assertEquals(255L, Bits.mask(0xFFL, BYTE)) // sanity: unsigned pattern preserved
    }

    // --- bitwise ---
    @Test fun bitwise() {
        assertEquals(0x00L, Bits.and(0xF0L, 0x0FL, BYTE))
        assertEquals(0xFFL, Bits.or(0xF0L, 0x0FL, BYTE))
        assertEquals(0xFFL, Bits.xor(0xF0L, 0x0FL, BYTE))
        assertEquals(0x0FL, Bits.not(0xF0L, BYTE))
        assertEquals(-1L, Bits.not(0x00L, QWORD))
        assertEquals(0xFFL, Bits.nand(0xF0L, 0x0FL, BYTE))
        assertEquals(0x00L, Bits.nor(0xF0L, 0x0FL, BYTE))
        assertEquals(0x00L, Bits.xnor(0xF0L, 0x0FL, BYTE))
    }
    @Test fun negateMinValueOverflows() {
        assertEquals(0x80L, Bits.negate(0x80L, BYTE)) // -(-128) wraps to -128
        assertEquals(0xFFL, Bits.negate(0x01L, BYTE)) // -1 as byte
    }

    // --- shifts ---
    @Test fun shifts() {
        assertEquals(0x10L, Bits.shl(0x01L, 4, BYTE))
        assertEquals(0x00L, Bits.shl(0x01L, 8, BYTE)) // n>=bits -> 0
        assertEquals(0x0FL, Bits.shrLogical(0xF0L, 4, BYTE))
        assertEquals(0xFFL, Bits.shrArithmetic(0xF0L, 4, BYTE)) // sign-fills
        assertEquals(Long.MAX_VALUE, Bits.shrLogical(-1L, 1, QWORD))
    }

    // --- arithmetic (masked, signed div) ---
    @Test fun arithmetic() {
        assertEquals(0x80L, Bits.add(0x7FL, 0x01L, BYTE)) // wraps 127->128 pattern
        assertEquals(0xFEL, Bits.div(0xF8L, 3L, BYTE)) // -8 / 3 = -2 = 0xFE
        assertFailsWith<ProgArithmeticException> { Bits.div(5L, 0L, BYTE) }
        assertFailsWith<ProgArithmeticException> { Bits.rem(5L, 0L, BYTE) }
    }

    // --- entry building ---
    @Test fun appendSixteenHexFsMakesAllOnes() {
        var e = 0L
        repeat(16) { e = Bits.appendDigit(e, 15, Radix.HEX, QWORD) }
        assertEquals(-1L, e)
    }
    @Test fun dropDigitUnsigned() {
        assertEquals(0x0FFFFFFFFFFFFFFFL, Bits.dropDigit(-1L, Radix.HEX, QWORD))
        assertEquals(0x0FL, Bits.dropDigit(0xFFL, Radix.HEX, BYTE))
    }
}
