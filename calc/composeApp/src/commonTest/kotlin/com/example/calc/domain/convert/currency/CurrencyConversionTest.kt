package com.example.calc.domain.convert.currency

import com.example.calc.domain.convert.ConversionFormatter
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyConversionTest {
    private fun bd(s: String) = BigDecimal.parseString(s)

    private val table = ExchangeRates(
        base = "USD",
        rates = mapOf("USD" to bd("1"), "EUR" to bd("0.9"), "GBP" to bd("0.8"), "JPY" to bd("150")),
        timestamp = 0L,
        source = RatesSource.BUNDLED,
    )

    private fun c(v: String, from: String, to: String) =
        ConversionFormatter.format(CurrencyConversion.convert(bd(v), from, to, table))

    @Test fun usdToEur() = assertEquals("90", c("100", "USD", "EUR"))
    @Test fun usdToJpy() = assertEquals("15,000", c("100", "USD", "JPY"))
    @Test fun eurToUsd() = assertEquals("100", c("90", "EUR", "USD"))
    @Test fun eurToGbp() = assertEquals("88.8888888889", c("100", "EUR", "GBP")) // 100*0.8/0.9
    @Test fun usdToUsd() = assertEquals("100", c("100", "USD", "USD"))

    @Test fun unknownCurrencyThrows() {
        assertFailsWith<IllegalArgumentException> {
            CurrencyConversion.convert(bd("1"), "USD", "XYZ", table)
        }
    }
}
