package com.example.calc

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.example.calc.data.DatabaseDriverFactory
import com.example.calc.data.SqlDelightHistoryRepository
import com.example.calc.data.createDatabase
import com.example.calc.data.net.KtorExchangeRateProvider
import com.example.calc.domain.convert.currency.DefaultExchangeRateRepository
import com.example.calc.domain.convert.currency.InMemoryRatesCache
import com.example.calc.platform.epochSeconds

fun MainViewController() = ComposeUIViewController {
    val repository = remember {
        SqlDelightHistoryRepository(createDatabase(DatabaseDriverFactory()))
    }
    val rateRepo = remember {
        DefaultExchangeRateRepository(
            provider = KtorExchangeRateProvider(),
            cache = InMemoryRatesCache(),
            now = ::epochSeconds,
        )
    }
    App(repository, rateRepo)
}
