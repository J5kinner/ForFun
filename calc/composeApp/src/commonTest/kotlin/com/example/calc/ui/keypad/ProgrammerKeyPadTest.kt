package com.example.calc.ui.keypad

import com.example.calc.domain.Radix
import com.example.calc.presentation.ProgrammerState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgrammerKeyPadTest {
    private fun key(state: ProgrammerState, label: String) =
        programmerKeyPad(state).flatten().first { it.label == label }

    @Test fun binaryRadixDisablesNonBinaryDigits() {
        val s = ProgrammerState(radix = Radix.BIN)
        assertTrue(key(s, "0").enabled)
        assertTrue(key(s, "1").enabled)
        assertFalse(key(s, "2").enabled)
        assertFalse(key(s, "A").enabled)
    }

    @Test fun octalRadixDisablesEightNine() {
        val s = ProgrammerState(radix = Radix.OCT)
        assertTrue(key(s, "7").enabled)
        assertFalse(key(s, "8").enabled)
        assertFalse(key(s, "9").enabled)
    }

    @Test fun hexRadixEnablesAtoF() {
        val s = ProgrammerState(radix = Radix.HEX)
        assertTrue(key(s, "A").enabled)
        assertTrue(key(s, "F").enabled)
    }

    @Test fun decimalRadixDisablesHexDigits() {
        val s = ProgrammerState(radix = Radix.DEC)
        assertTrue(key(s, "9").enabled)
        assertFalse(key(s, "F").enabled)
    }
}
