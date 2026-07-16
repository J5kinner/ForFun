package com.example.calc.domain.convert.currency

/**
 * Robust repository that never fails to the caller.
 *
 * Resolution order for [getRates]:
 *  1. **fresh cache** — a cached snapshot newer than [ttlSeconds] (per the injected [now]);
 *  2. **live** — [provider.fetchLatest], saved back to [cache] on success;
 *  3. **stale cache** — any cached snapshot, if the live fetch failed;
 *  4. **bundled** — the compiled-in offline table.
 *
 * [now] returns epoch **seconds**. It is injected (default `{ 0L }` for tests) rather than
 * reading a platform clock here, so the whole class stays pure and unit-testable.
 */
class DefaultExchangeRateRepository(
    private val provider: ExchangeRateProvider,
    private val cache: RatesCache,
    private val bundled: BundledRates = BundledRates,
    private val ttlSeconds: Long = 3600L,
    private val now: () -> Long = { 0L },
) : ExchangeRateRepository {
    override suspend fun getRates(base: String): ExchangeRates {
        cache.load(base)?.let { if (now() - it.timestamp < ttlSeconds) return it } // fresh cache
        return try {
            provider.fetchLatest(base).also { cache.save(it) } // live (and persist)
        } catch (e: Exception) {
            cache.load(base) ?: bundled.rates(base) // stale cache, else bundled
        }
    }
}
