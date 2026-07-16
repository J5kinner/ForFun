package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionEngineTemperatureTest {
    private fun u(id: String) = UnitCategory.Temperature.units.first { it.id == id }
    private fun c(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun freezingCtoF() = assertEquals("32", c("0", "C", "F"))
    @Test fun boilingCtoF() = assertEquals("212", c("100", "C", "F"))
    @Test fun freezingCtoK() = assertEquals("273.15", c("0", "C", "K"))
    @Test fun fToCzero() = assertEquals("0", c("32", "F", "C"))
    @Test fun fToChundred() = assertEquals("100", c("212", "F", "C"))
    @Test fun kToCzero() = assertEquals("0", c("273.15", "K", "C"))
    @Test fun minus40Equal() = assertEquals("-40", c("-40", "C", "F")) // the crossover point
    @Test fun bodyTemp() = assertEquals("98.6", c("37", "C", "F"))
    @Test fun kToF() = assertEquals("32", c("273.15", "K", "F"))
}
