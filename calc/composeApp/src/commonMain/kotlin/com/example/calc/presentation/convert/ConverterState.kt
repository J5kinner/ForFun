package com.example.calc.presentation.convert

import com.example.calc.domain.convert.UnitCategory
import com.example.calc.domain.convert.UnitDef
import com.example.calc.domain.convert.currency.ExchangeRates
import com.example.calc.domain.convert.currency.RatesSource

enum class ConverterField { A, B }

data class ConverterState(
    val category: UnitCategory = UnitCategory.Length,
    val unitA: UnitDef = UnitCategory.Length.defaultFrom,
    val unitB: UnitDef = UnitCategory.Length.defaultTo,
    val editing: ConverterField = ConverterField.A,
    val textA: String = "", // the edited field holds raw user text
    val textB: String = "", // the passive field is always derived
    val rates: ExchangeRates? = null, // only for Currency
    val ratesSource: RatesSource? = null, // shown as a live/offline badge
)

fun ConverterState.activeText(): String = if (editing == ConverterField.A) textA else textB
