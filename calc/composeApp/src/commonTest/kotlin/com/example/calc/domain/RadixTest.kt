package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RadixTest {
    @Test fun binAllowsOnlyZeroOne() {
        assertTrue(Radix.BIN.allowsDigit(0))
        assertTrue(Radix.BIN.allowsDigit(1))
        assertFalse(Radix.BIN.allowsDigit(2))
    }
    @Test fun octRejectsEightAndNine() {
        assertTrue(Radix.OCT.allowsDigit(7))
        assertFalse(Radix.OCT.allowsDigit(8))
    }
    @Test fun decRejectsTenPlus() {
        assertTrue(Radix.DEC.allowsDigit(9))
        assertFalse(Radix.DEC.allowsDigit(10))
    }
    @Test fun hexAllowsAtoF() {
        assertTrue(Radix.HEX.allowsDigit(15))
        assertFalse(Radix.HEX.allowsDigit(16))
    }
    @Test fun wordSizeBits() {
        assertEquals(8, WordSize.BYTE.bits)
        assertEquals(16, WordSize.WORD.bits)
        assertEquals(32, WordSize.DWORD.bits)
        assertEquals(64, WordSize.QWORD.bits)
    }
}
