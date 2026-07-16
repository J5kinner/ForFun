package com.example.calc.domain.convert.currency

/** Supplies exchange rates, never failing to the caller. */
interface ExchangeRateRepository {
    /** Returns rates for [base] via fresh-cache → live → stale-cache → bundled, in that order. */
    suspend fun getRates(base: String): ExchangeRates
}
