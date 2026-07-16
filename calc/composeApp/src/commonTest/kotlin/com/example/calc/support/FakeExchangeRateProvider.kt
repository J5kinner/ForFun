package com.example.calc.support

import com.example.calc.domain.convert.currency.ExchangeRateProvider
import com.example.calc.domain.convert.currency.ExchangeRates

/**
 * Test double for [ExchangeRateProvider]. Either returns a fixed [result] or, when
 * [throwing] is set, simulates a network failure. Records how often it was called.
 */
class FakeExchangeRateProvider(
    var result: ExchangeRates? = null,
    var throwing: Boolean = false,
) : ExchangeRateProvider {
    var fetchCount: Int = 0
        private set

    override suspend fun fetchLatest(base: String): ExchangeRates {
        fetchCount++
        if (throwing) throw RuntimeException("simulated network failure")
        return result ?: throw IllegalStateException("FakeExchangeRateProvider has no result configured")
    }
}
