package com.example.calc.domain

import com.example.calc.domain.WordSize.BYTE
import com.example.calc.domain.WordSize.DWORD
import com.example.calc.domain.WordSize.QWORD
import kotlin.test.Test
import kotlin.test.assertEquals

class RadixFormatterTest {
    @Test fun byteAllRadixes() {
        assertEquals("FF", RadixFormatter.format(0xFFL, Radix.HEX, BYTE))
        assertEquals("-1", RadixFormatter.format(0xFFL, Radix.DEC, BYTE))
        assertEquals("377", RadixFormatter.format(0xFFL, Radix.OCT, BYTE))
        assertEquals("1111 1111", RadixFormatter.format(0xFFL, Radix.BIN, BYTE))
    }
    @Test fun binaryNibbleGrouping() {
        assertEquals("111 1011", RadixFormatter.format(0x7BL, Radix.BIN, BYTE))
    }
    @Test fun qwordAllOnesHex() {
        assertEquals("FFFFFFFFFFFFFFFF", RadixFormatter.format(-1L, Radix.HEX, QWORD))
    }
    @Test fun allRadixesMap() {
        val m = RadixFormatter.all(0x0AL, DWORD)
        assertEquals("A", m[Radix.HEX])
        assertEquals("10", m[Radix.DEC])
        assertEquals("12", m[Radix.OCT])
        assertEquals("1010", m[Radix.BIN])
    }
    @Test fun zeroFormats() {
        assertEquals("0", RadixFormatter.format(0L, Radix.HEX, BYTE))
        assertEquals("0", RadixFormatter.format(0L, Radix.BIN, BYTE))
    }
}
