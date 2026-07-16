package com.example.calc.domain.convert.currency

/** A provider with no network: always fails so the repository falls back to bundled rates. */
object BundledOnlyProvider : ExchangeRateProvider {
    override suspend fun fetchLatest(base: String): ExchangeRates =
        throw UnsupportedOperationException("No live exchange-rate provider configured")
}

/** Simple in-memory cache for the last successful fetch (not persisted across process death). */
class InMemoryRatesCache : RatesCache {
    private var stored: ExchangeRates? = null
    override suspend fun load(base: String): ExchangeRates? = stored?.takeIf { it.base == base }
    override suspend fun save(rates: ExchangeRates) {
        stored = rates
    }
}
