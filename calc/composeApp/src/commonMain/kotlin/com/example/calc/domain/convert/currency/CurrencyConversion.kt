package com.example.calc.domain.convert.currency

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Pure currency conversion over a rate table where `rate(code)` = units of `code`
 * per 1 base: `amountTo = amountFrom * rate(to) / rate(from)`.
 */
object CurrencyConversion {
    private val INTERMEDIATE = DecimalMode(24L, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    private val DISPLAY = DecimalMode(12L, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    fun convert(amount: BigDecimal, from: String, to: String, rates: ExchangeRates): BigDecimal {
        val rf = rates.rates[from] ?: throw IllegalArgumentException("Unknown currency: $from")
        val rt = rates.rates[to] ?: throw IllegalArgumentException("Unknown currency: $to")
        // Divide with guard digits, then round once to 12 significant digits for display.
        return amount.multiply(rt).divide(rf, INTERMEDIATE).roundSignificand(DISPLAY)
    }
}
