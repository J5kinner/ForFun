package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionEngineWeightTest {
    private fun u(id: String) = UnitCategory.Weight.units.first { it.id == id }
    private fun conv(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun kgToG() = assertEquals("1,000", conv("1", "kg", "g"))
    @Test fun lbToG() = assertEquals("453.59237", conv("1", "lb", "g"))
    @Test fun ozToG() = assertEquals("28.349523125", conv("1", "oz", "g"))
    @Test fun stToLb() = assertEquals("14", conv("1", "st", "lb"))
    @Test fun kgToLb() = assertEquals("2.20462262185", conv("1", "kg", "lb"))
    @Test fun tToKg() = assertEquals("1,000", conv("1", "t", "kg"))
    @Test fun gToMg() = assertEquals("1,000", conv("1", "g", "mg"))
}
