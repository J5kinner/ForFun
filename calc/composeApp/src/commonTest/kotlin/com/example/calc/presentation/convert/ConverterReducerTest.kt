package com.example.calc.presentation.convert

import com.example.calc.domain.convert.UnitCategory
import com.example.calc.domain.convert.currency.ExchangeRates
import com.example.calc.domain.convert.currency.RatesSource
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConverterReducerTest {
    private val r = ConverterReducer()
    private fun run(s: ConverterState, vararg i: ConverterIntent): ConverterState =
        i.fold(s) { acc, it -> r.reduce(acc, it).state }

    private fun len(id: String) = UnitCategory.Length.units.first { it.id == id }
    private fun temp(id: String) = UnitCategory.Temperature.units.first { it.id == id }
    private fun cur(id: String) = UnitCategory.Currency.units.first { it.id == id }
    private fun length(a: String, b: String) = ConverterState(category = UnitCategory.Length, unitA = len(a), unitB = len(b))
    private fun fakeUsdTable() = ExchangeRates(
        base = "USD",
        rates = mapOf(
            "USD" to BigDecimal.parseString("1"),
            "EUR" to BigDecimal.parseString("0.9"),
            "GBP" to BigDecimal.parseString("0.8"),
            "JPY" to BigDecimal.parseString("150"),
        ),
        timestamp = 0L,
        source = RatesSource.LIVE,
    )

    @Test fun editingAUpdatesB() {
        val s = run(length("km", "m"), ConverterIntent.Digit('1'))
        assertEquals("1", s.textA)
        assertEquals("1,000", s.textB)
    }

    @Test fun editingBUpdatesA() {
        val s = run(
            length("km", "m"),
            ConverterIntent.SelectField(ConverterField.B),
            ConverterIntent.Digit('5'), ConverterIntent.Digit('0'), ConverterIntent.Digit('0'),
        )
        assertEquals("500", s.textB)
        assertEquals("0.5", s.textA) // 500 m = 0.5 km
    }

    @Test fun changingPassiveUnitReDerives() {
        val s = run(
            length("km", "m"),
            ConverterIntent.Digit('1'),
            ConverterIntent.SelectUnit(ConverterField.B, len("cm")),
        )
        assertEquals("100,000", s.textB) // 1 km = 100,000 cm
    }

    @Test fun swapUnitsRecomputes() {
        val s = run(length("km", "m"), ConverterIntent.Digit('2'), ConverterIntent.SwapUnits)
        assertEquals("km", s.unitB.id)
        assertEquals("m", s.unitA.id)
        assertEquals("2", s.textA)
        assertEquals("0.002", s.textB) // now A = 2 m -> 0.002 km
    }

    @Test fun emptyOrPartialInputClearsPassive() {
        val s = run(length("km", "m"), ConverterIntent.Digit('1'), ConverterIntent.Delete)
        assertEquals("", s.textA)
        assertEquals("", s.textB)
    }

    @Test fun temperatureNegativeConverts() {
        val s = run(
            ConverterState(category = UnitCategory.Temperature, unitA = temp("C"), unitB = temp("F")),
            ConverterIntent.Digit('4'), ConverterIntent.Digit('0'), ConverterIntent.ToggleSign,
        )
        assertEquals("-40", s.textA)
        assertEquals("-40", s.textB)
    }

    @Test fun selectingCurrencyRequestsRates() {
        val red = r.reduce(ConverterState(), ConverterIntent.SelectCategory(UnitCategory.Currency))
        assertTrue(red.effects.any { it is ConverterEffect.RequestRates })
    }

    @Test fun ratesLoadedFillsConversion() {
        val start = ConverterState(category = UnitCategory.Currency, unitA = cur("USD"), unitB = cur("EUR"))
        val typed = run(start, ConverterIntent.Digit('1'), ConverterIntent.Digit('0'), ConverterIntent.Digit('0'))
        assertEquals("", typed.textB) // no rates yet
        val loaded = r.reduce(typed, ConverterIntent.RatesLoaded(fakeUsdTable())).state
        assertEquals("90", loaded.textB) // 100 USD -> 90 EUR
    }
}
