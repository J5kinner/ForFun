package com.example.calc.domain.convert.currency

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/** Where a rate table came from, most-trusted first. Surfaced in the UI as a badge. */
enum class RatesSource { LIVE, CACHED, BUNDLED }

/**
 * A snapshot of exchange rates.
 *
 * @property base the code all rates are relative to (the app always uses "USD").
 * @property rates code -> units of that code per 1 [base] (the base itself maps to 1).
 * @property timestamp epoch seconds of the data (used for cache TTL; 0 for bundled).
 * @property source provenance of this snapshot.
 */
data class ExchangeRates(
    val base: String,
    val rates: Map<String, BigDecimal>,
    val timestamp: Long,
    val source: RatesSource,
)
