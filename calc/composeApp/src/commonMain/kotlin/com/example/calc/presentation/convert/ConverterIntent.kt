package com.example.calc.presentation.convert

import com.example.calc.domain.convert.UnitCategory
import com.example.calc.domain.convert.UnitDef
import com.example.calc.domain.convert.currency.ExchangeRates

sealed interface ConverterIntent {
    data class Digit(val d: Char) : ConverterIntent
    data object Decimal : ConverterIntent
    data object Delete : ConverterIntent
    data object Clear : ConverterIntent
    data object ToggleSign : ConverterIntent // temperature allows negatives
    data class SelectField(val field: ConverterField) : ConverterIntent
    data class SelectCategory(val category: UnitCategory) : ConverterIntent
    data class SelectUnit(val field: ConverterField, val unit: UnitDef) : ConverterIntent
    data object SwapUnits : ConverterIntent
    data class RatesLoaded(val rates: ExchangeRates) : ConverterIntent // from the ViewModel async fetch
}
