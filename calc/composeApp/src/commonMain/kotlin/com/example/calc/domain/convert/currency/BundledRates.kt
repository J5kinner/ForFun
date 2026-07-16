package com.example.calc.domain.convert.currency

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Compiled-in USD-based offline fallback.
 *
 * The app always requests base "USD", so the table is returned as-is (no rebase).
 * Values are approximate and only used when both live and cached rates are
 * unavailable; the [ExchangeRates.source] marks them [RatesSource.BUNDLED].
 */
object BundledRates {
    private val TABLE: Map<String, BigDecimal> = mapOf(
        "USD" to bd("1"),
        "EUR" to bd("0.92"),
        "GBP" to bd("0.79"),
        "JPY" to bd("157"),
        "CAD" to bd("1.37"),
        "AUD" to bd("1.52"),
        "CHF" to bd("0.88"),
        "CNY" to bd("7.25"),
        "INR" to bd("83.5"),
        "MXN" to bd("18.2"),
    )

    /** The bundled snapshot. [base] is accepted for symmetry but the app always uses "USD". */
    fun rates(base: String): ExchangeRates =
        ExchangeRates(base = "USD", rates = TABLE, timestamp = 0L, source = RatesSource.BUNDLED)

    private fun bd(s: String): BigDecimal = BigDecimal.parseString(s)
}
