package com.example.calc.presentation.convert

import com.example.calc.domain.convert.ConversionEngine
import com.example.calc.domain.convert.ConversionFormatter
import com.example.calc.domain.convert.UnitCategory
import com.example.calc.domain.convert.UnitDef
import com.example.calc.domain.convert.currency.CurrencyConversion
import com.ionspin.kotlin.bignum.decimal.BigDecimal

private const val BASE_CURRENCY = "USD"

/**
 * Pure dual-input converter reducer. The edited field keeps the user's raw text;
 * the passive field is always derived from it. Changing units/category/swap
 * re-derives the passive field from the active one.
 */
class ConverterReducer {
    fun reduce(s: ConverterState, i: ConverterIntent): ConverterReduction = when (i) {
        is ConverterIntent.Digit -> editActive(s, appendDigit(s.activeText(), i.d))
        ConverterIntent.Decimal -> editActive(s, appendDecimal(s.activeText()))
        ConverterIntent.Delete -> editActive(s, s.activeText().dropLast(1))
        ConverterIntent.Clear -> ConverterReduction(s.copy(textA = "", textB = ""))
        ConverterIntent.ToggleSign -> editActive(s, toggleSign(s.activeText()))
        is ConverterIntent.SelectField -> ConverterReduction(s.copy(editing = i.field))
        is ConverterIntent.SelectCategory -> selectCategory(s, i.category)
        is ConverterIntent.SelectUnit -> recompute(setUnit(s, i.field, i.unit))
        ConverterIntent.SwapUnits -> recompute(swap(s))
        is ConverterIntent.RatesLoaded -> recompute(s.copy(rates = i.rates, ratesSource = i.rates.source))
    }

    /** Write raw text into the edited field, then derive the other field. */
    private fun editActive(s: ConverterState, newText: String): ConverterReduction {
        val ns = if (s.editing == ConverterField.A) s.copy(textA = newText) else s.copy(textB = newText)
        return recompute(ns)
    }

    /** Single source of truth: derive the passive field from the active field's parsed value. */
    private fun recompute(s: ConverterState): ConverterReduction {
        val parsed = s.activeText().toConverterBigDecimalOrNull()
        val out = if (parsed == null) {
            ""
        } else {
            val (from, to) = if (s.editing == ConverterField.A) s.unitA to s.unitB else s.unitB to s.unitA
            val result: BigDecimal? = if (s.category == UnitCategory.Currency) {
                s.rates?.let { CurrencyConversion.convert(parsed, from.id, to.id, it) }
            } else {
                ConversionEngine.convert(parsed, from, to)
            }
            result?.let { ConversionFormatter.format(it) } ?: ""
        }
        val ns = if (s.editing == ConverterField.A) s.copy(textB = out) else s.copy(textA = out)
        val effects = if (s.category == UnitCategory.Currency && s.rates == null) {
            listOf(ConverterEffect.RequestRates(BASE_CURRENCY))
        } else {
            emptyList()
        }
        return ConverterReduction(ns, effects)
    }

    private fun selectCategory(s: ConverterState, cat: UnitCategory): ConverterReduction {
        val base = s.copy(
            category = cat,
            unitA = cat.defaultFrom,
            unitB = cat.defaultTo,
            editing = ConverterField.A,
            textB = "",
        )
        return if (cat == UnitCategory.Currency) {
            ConverterReduction(base.copy(rates = null, ratesSource = null), listOf(ConverterEffect.RequestRates(BASE_CURRENCY)))
        } else {
            recompute(base)
        }
    }

    private fun setUnit(s: ConverterState, field: ConverterField, unit: UnitDef): ConverterState =
        if (field == ConverterField.A) s.copy(unitA = unit) else s.copy(unitB = unit)

    private fun swap(s: ConverterState): ConverterState =
        s.copy(unitA = s.unitB, unitB = s.unitA)
}

private fun appendDigit(text: String, d: Char): String = if (text == "0") d.toString() else text + d

private fun appendDecimal(text: String): String = when {
    text.contains('.') -> text
    text.isEmpty() || text == "-" -> text + "0."
    else -> "$text."
}

private fun toggleSign(text: String): String =
    if (text.startsWith("-")) text.drop(1) else "-$text"

private fun String.toConverterBigDecimalOrNull(): BigDecimal? {
    if (isEmpty() || this == "-" || this == "." || this == "-." || endsWith(".")) return null
    return try {
        BigDecimal.parseString(this)
    } catch (e: Throwable) {
        null
    }
}
