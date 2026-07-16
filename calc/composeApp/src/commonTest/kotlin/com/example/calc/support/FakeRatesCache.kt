package com.example.calc.support

import com.example.calc.domain.convert.currency.ExchangeRates
import com.example.calc.domain.convert.currency.RatesCache

/** In-memory [RatesCache] keyed by base code. Records save/load counts for assertions. */
class FakeRatesCache : RatesCache {
    private val store = mutableMapOf<String, ExchangeRates>()
    var saveCount: Int = 0
        private set
    var loadCount: Int = 0
        private set

    override suspend fun load(base: String): ExchangeRates? {
        loadCount++
        return store[base]
    }

    override suspend fun save(rates: ExchangeRates) {
        saveCount++
        store[rates.base] = rates
    }
}
