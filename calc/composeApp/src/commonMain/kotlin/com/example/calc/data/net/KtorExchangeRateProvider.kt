package com.example.calc.data.net

import com.example.calc.domain.convert.currency.ExchangeRateProvider
import com.example.calc.domain.convert.currency.ExchangeRates
import com.example.calc.domain.convert.currency.RatesSource
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ExchangeRateResponse(
    val result: String = "",
    @SerialName("base_code") val baseCode: String = "USD",
    @SerialName("time_last_update_unix") val timeLastUpdateUnix: Long = 0L,
    val rates: Map<String, Double> = emptyMap(),
)

/**
 * Live rates from the free, no-key ExchangeRate-API. Doubles from the wire are
 * converted to BigDecimal via toString() so the math pipeline never touches
 * binary floats. Throws on any network/parse failure — the repository catches
 * it and falls back to cached/bundled rates.
 */
class KtorExchangeRateProvider(
    private val client: HttpClient = defaultClient(),
) : ExchangeRateProvider {

    override suspend fun fetchLatest(base: String): ExchangeRates {
        val resp: ExchangeRateResponse = client.get("https://open.er-api.com/v6/latest/$base").body()
        val rates = resp.rates.mapValues { BigDecimal.parseString(it.value.toString()) }
        return ExchangeRates(
            base = resp.baseCode,
            rates = rates,
            timestamp = resp.timeLastUpdateUnix,
            source = RatesSource.LIVE,
        )
    }

    companion object {
        fun defaultClient(): HttpClient = HttpClient(httpClientEngine()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }
}
