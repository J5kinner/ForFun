package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionEngineAreaTest {
    private fun u(id: String) = UnitCategory.Area.units.first { it.id == id }
    private fun conv(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun m2ToCm2() = assertEquals("10,000", conv("1", "m2", "cm2"))
    @Test fun haToM2() = assertEquals("10,000", conv("1", "ha", "m2"))
    @Test fun acreToM2() = assertEquals("4,046.8564224", conv("1", "acre", "m2"))
    @Test fun km2ToM2() = assertEquals("1,000,000", conv("1", "km2", "m2"))
    @Test fun ft2ToIn2() = assertEquals("144", conv("1", "ft2", "in2"))
    // 1 mi2 = 2,589,988.110336 m2 exactly, but 12-significant-digit display rounds the tail.
    @Test fun mi2ToM2() = assertEquals("2,589,988.11034", conv("1", "mi2", "m2"))
}
