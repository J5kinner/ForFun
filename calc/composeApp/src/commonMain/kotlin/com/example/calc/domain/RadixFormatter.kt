package com.example.calc.domain

object RadixFormatter {

    fun format(masked: Long, radix: Radix, ws: WordSize): String = when (radix) {
        Radix.DEC -> Bits.signed(masked, ws).toString()
        Radix.HEX -> unsignedString(masked, ws, 16).uppercase()
        Radix.OCT -> unsignedString(masked, ws, 8)
        Radix.BIN -> groupBinary(unsignedString(masked, ws, 2))
    }

    /** All four radixes at once, for the multi-radix panel. */
    fun all(masked: Long, ws: WordSize): Map<Radix, String> =
        Radix.entries.associateWith { format(masked, it, ws) }

    private fun unsignedString(masked: Long, ws: WordSize, base: Int): String {
        val u = Bits.mask(masked, ws).toULong()
        return u.toString(base) // ULong.toString(radix) is unsigned, no leading zeros, "0" when zero
    }

    /** Space-group binary into nibbles from the right: 1111011 -> "111 1011". */
    private fun groupBinary(bin: String): String =
        bin.reversed().chunked(4).joinToString(" ").reversed()
}
