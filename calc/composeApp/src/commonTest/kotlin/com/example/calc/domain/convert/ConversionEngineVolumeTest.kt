package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionEngineVolumeTest {
    private fun u(id: String) = UnitCategory.Volume.units.first { it.id == id }
    private fun conv(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun lToMl() = assertEquals("1,000", conv("1", "L", "mL"))
    @Test fun m3ToL() = assertEquals("1,000", conv("1", "m3", "L"))
    @Test fun galToL() = assertEquals("3.785411784", conv("1", "gal", "L"))
    @Test fun galToQt() = assertEquals("4", conv("1", "gal", "qt"))
    @Test fun galToFloz() = assertEquals("128", conv("1", "gal", "floz"))
}
