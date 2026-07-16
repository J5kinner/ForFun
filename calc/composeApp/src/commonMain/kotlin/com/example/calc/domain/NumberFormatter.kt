package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

object NumberFormatter {
    private val NEG_ONE = BigDecimal.fromInt(-1)

    fun plain(value: BigDecimal): String = value.toStringExpanded()

    fun format(value: BigDecimal): String {
        if (value.isZero()) return "0"
        val negative = value.signum() < 0
        val abs = if (negative) value.multiply(NEG_ONE) else value
        val plain = abs.toStringExpanded()
        val dot = plain.indexOf('.')
        val intPart = if (dot >= 0) plain.substring(0, dot) else plain
        val fracPart = if (dot >= 0) plain.substring(dot + 1) else ""
        val intDigits = intPart.trimStart('0').ifEmpty { "0" }
        val leadingFracZeros = if (intDigits == "0") fracPart.takeWhile { it == '0' }.length else 0
        val useSci = intDigits.length > 16 || (intDigits == "0" && fracPart.isNotEmpty() && leadingFracZeros >= 6)
        val body = if (useSci) toScientific(intPart, fracPart) else groupAndTrim(intDigits, fracPart)
        return if (negative) "-$body" else body
    }

    private fun groupAndTrim(intDigits: String, fracPart: String): String {
        val grouped = intDigits.reversed().chunked(3).joinToString(",").reversed()
        val trimmedFrac = fracPart.trimEnd('0')
        return if (trimmedFrac.isEmpty()) grouped else "$grouped.$trimmedFrac"
    }

    private fun toScientific(intPart: String, fracPart: String): String {
        val all = intPart + fracPart
        val first = all.indexOfFirst { it != '0' }
        if (first < 0) return "0"
        val exp = (intPart.length - 1) - first
        val sig = all.substring(first).trimEnd('0').ifEmpty { "0" }
        val mantissa = if (sig.length == 1) sig else "${sig[0]}.${sig.substring(1)}"
        val sign = if (exp >= 0) "+" else "-"
        val mag = if (exp >= 0) exp else -exp
        return "${mantissa}E$sign$mag"
    }
}
