package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionEngineSpeedTest {
    private fun u(id: String) = UnitCategory.Speed.units.first { it.id == id }
    private fun conv(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun msToKmh() = assertEquals("3.6", conv("1", "ms", "kmh"))
    @Test fun kmhToMs() = assertEquals("10", conv("36", "kmh", "ms"))
    @Test fun mphToKmh() = assertEquals("1.609344", conv("1", "mph", "kmh"))
    @Test fun knToKmh() = assertEquals("1.852", conv("1", "kn", "kmh"))
    @Test fun mphToKmh60() = assertEquals("96.56064", conv("60", "mph", "kmh"))
    @Test fun ftsToKmh() = assertEquals("1.09728", conv("1", "fts", "kmh"))
}
