package com.example.calc.domain.convert.currency

/**
 * Network port for fetching live rates. The concrete HTTP client is injected
 * (a later phase); the domain only depends on this interface.
 */
interface ExchangeRateProvider {
    /** Fetches the latest rates for [base]. Throws on any network/parse failure. */
    suspend fun fetchLatest(base: String): ExchangeRates
}
