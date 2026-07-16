package com.example.calc.domain

/**
 * Fixed-width two's-complement arithmetic over a Long canonical store.
 * The canonical form of every value is "masked": the low `ws.bits` bits hold the
 * value and all higher bits are zero. `signed()` reinterprets it as a signed Long.
 */
object Bits {

    /** Keep only the low `ws.bits` bits (high bits zeroed). QWORD is identity. */
    fun mask(value: Long, ws: WordSize): Long =
        if (ws == WordSize.QWORD) value else value and ((1L shl ws.bits) - 1L)

    /** Sign-extend a masked value to a full signed Long (for DEC display / signed div). */
    fun signed(masked: Long, ws: WordSize): Long {
        if (ws == WordSize.QWORD) return masked
        val shift = 64 - ws.bits
        return (masked shl shift) shr shift // arithmetic right shift extends the sign
    }

    // --- Bitwise ---
    fun and(a: Long, b: Long, ws: WordSize) = mask(a and b, ws)
    fun or(a: Long, b: Long, ws: WordSize) = mask(a or b, ws)
    fun xor(a: Long, b: Long, ws: WordSize) = mask(a xor b, ws)
    fun not(a: Long, ws: WordSize) = mask(a.inv(), ws)
    fun nand(a: Long, b: Long, ws: WordSize) = mask((a and b).inv(), ws)
    fun nor(a: Long, b: Long, ws: WordSize) = mask((a or b).inv(), ws)
    fun xnor(a: Long, b: Long, ws: WordSize) = mask((a xor b).inv(), ws)

    /** Two's-complement negate: mask(-value). NEG of the min value stays itself (overflow). */
    fun negate(a: Long, ws: WordSize) = mask(-signed(a, ws), ws)

    // --- Shifts. n is clamped to [0, bits]; guards Kotlin's shift-count-mod-64 trap. ---
    fun shl(a: Long, n: Int, ws: WordSize): Long {
        if (n <= 0) return mask(a, ws)
        if (n >= ws.bits) return 0L
        return mask(a shl n, ws)
    }

    /** Logical right shift: zero-fill. Operates on the unsigned masked pattern. */
    fun shrLogical(a: Long, n: Int, ws: WordSize): Long {
        if (n <= 0) return mask(a, ws)
        if (n >= ws.bits) return 0L
        return mask(mask(a, ws) ushr n, ws) // mask first so QWORD bit-63 is treated unsigned
    }

    /** Arithmetic right shift: sign-fill. */
    fun shrArithmetic(a: Long, n: Int, ws: WordSize): Long {
        if (n <= 0) return mask(a, ws)
        val s = signed(a, ws)
        if (n >= ws.bits) return mask(s shr 63, ws) // all-sign result
        return mask(s shr n, ws)
    }

    // --- Integer arithmetic (masked; wraps like hardware). Signed division/truncation. ---
    fun add(a: Long, b: Long, ws: WordSize) = mask(a + b, ws)
    fun sub(a: Long, b: Long, ws: WordSize) = mask(a - b, ws)
    fun mul(a: Long, b: Long, ws: WordSize) = mask(a * b, ws)

    /** Signed truncating division. Throws ProgArithmeticException on divide-by-zero. */
    fun div(a: Long, b: Long, ws: WordSize): Long {
        if (mask(b, ws) == 0L) throw ProgArithmeticException()
        return mask(signed(a, ws) / signed(b, ws), ws)
    }

    fun rem(a: Long, b: Long, ws: WordSize): Long {
        if (mask(b, ws) == 0L) throw ProgArithmeticException()
        return mask(signed(a, ws) % signed(b, ws), ws)
    }

    /** Append one radix digit to an entry, masked (used by the reducer). */
    fun appendDigit(entry: Long, digit: Int, radix: Radix, ws: WordSize): Long =
        mask(entry * radix.base + digit, ws)

    /** Remove the last radix digit (backspace). */
    fun dropDigit(entry: Long, radix: Radix, ws: WordSize): Long {
        // Divide the *unsigned* low-bits representation by the base.
        val u = mask(entry, ws)
        return mask(u.toULong().div(radix.base.toULong()).toLong(), ws)
    }
}

class ProgArithmeticException : Exception("Programmer arithmetic error")
