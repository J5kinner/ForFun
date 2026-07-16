package com.example.calc.domain.convert.currency

import com.example.calc.support.FakeExchangeRateProvider
import com.example.calc.support.FakeRatesCache
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultExchangeRateRepositoryTest {
    private fun bd(s: String) = BigDecimal.parseString(s)

    private fun table(vararg rates: Pair<String, String>, ts: Long, source: RatesSource): ExchangeRates {
        val m = mutableMapOf("USD" to bd("1"))
        rates.forEach { m[it.first] = bd(it.second) }
        return ExchangeRates(base = "USD", rates = m, timestamp = ts, source = source)
    }

    @Test fun usesLiveWhenAvailableAndCachesIt() = runTest {
        val provider = FakeExchangeRateProvider(result = table("EUR" to "0.9", ts = 10_000L, source = RatesSource.LIVE))
        val cache = FakeRatesCache()
        val repo = DefaultExchangeRateRepository(provider, cache, now = { 10_000L })

        val r = repo.getRates("USD")

        assertEquals(RatesSource.LIVE, r.source)
        assertEquals(1, provider.fetchCount)
        assertEquals(bd("0.9"), cache.load("USD")!!.rates["EUR"]) // persisted
    }

    @Test fun fallsBackToBundledWhenProviderThrowsAndNoCache() = runTest {
        val repo = DefaultExchangeRateRepository(FakeExchangeRateProvider(throwing = true), FakeRatesCache())
        assertEquals(RatesSource.BUNDLED, repo.getRates("USD").source)
    }

    @Test fun prefersStaleCacheOverBundledOnFailure() = runTest {
        val cache = FakeRatesCache()
        cache.save(table("EUR" to "0.85", ts = 0L, source = RatesSource.CACHED))
        val repo = DefaultExchangeRateRepository(
            FakeExchangeRateProvider(throwing = true), cache, ttlSeconds = 60L, now = { 10_000L },
        )

        val r = repo.getRates("USD")

        assertEquals(RatesSource.CACHED, r.source) // stale but real beats bundled
        assertEquals(bd("0.85"), r.rates["EUR"])
    }

    @Test fun freshCacheSkipsProvider() = runTest {
        val cache = FakeRatesCache()
        cache.save(table("EUR" to "0.8", ts = 9_950L, source = RatesSource.CACHED))
        val provider = FakeExchangeRateProvider(throwing = true) // must NOT be called
        val repo = DefaultExchangeRateRepository(provider, cache, ttlSeconds = 100L, now = { 10_000L })

        val r = repo.getRates("USD")

        assertEquals("0.8", r.rates["EUR"]!!.toStringExpanded())
        assertEquals(0, provider.fetchCount)
    }
}
