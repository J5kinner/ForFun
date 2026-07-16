package com.example.calc.domain.convert.currency

/** Persistence port for the last successful fetch. Prod impl can be file/DB backed; tests use a fake. */
interface RatesCache {
    suspend fun load(base: String): ExchangeRates?
    suspend fun save(rates: ExchangeRates)
}
