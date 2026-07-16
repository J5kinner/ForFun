package com.example.calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.calc.data.DatabaseDriverFactory
import com.example.calc.data.SqlDelightHistoryRepository
import com.example.calc.data.createDatabase
import com.example.calc.data.net.KtorExchangeRateProvider
import com.example.calc.domain.convert.currency.DefaultExchangeRateRepository
import com.example.calc.domain.convert.currency.InMemoryRatesCache
import com.example.calc.platform.epochSeconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = SqlDelightHistoryRepository(
            createDatabase(DatabaseDriverFactory(applicationContext)),
        )
        val rateRepo = DefaultExchangeRateRepository(
            provider = KtorExchangeRateProvider(),
            cache = InMemoryRatesCache(),
            now = ::epochSeconds,
        )
        setContent { App(repository, rateRepo) }
    }
}
