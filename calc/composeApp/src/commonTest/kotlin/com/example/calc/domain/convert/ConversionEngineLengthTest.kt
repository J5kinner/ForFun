package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionEngineLengthTest {
    private fun u(id: String) = UnitCategory.Length.units.first { it.id == id }
    private fun conv(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun kmToM() = assertEquals("1,000", conv("1", "km", "m"))
    @Test fun miToM() = assertEquals("1,609.344", conv("1", "mi", "m"))
    @Test fun mToKm() = assertEquals("5", conv("5000", "m", "km"))
    @Test fun inToCm() = assertEquals("2.54", conv("1", "in", "cm"))
    @Test fun ftToIn() = assertEquals("12", conv("1", "ft", "in"))
    @Test fun cmToM() = assertEquals("1", conv("100", "cm", "m"))
    @Test fun nmiToM() = assertEquals("1,852", conv("1", "nmi", "m"))
    @Test fun ydToFt() = assertEquals("3", conv("1", "yd", "ft"))
}
