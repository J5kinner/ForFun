package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Pure unit conversion within a single category: `value` in `from` → the same
 * quantity expressed in `to`, routed through the category base unit.
 *
 * The division runs at [INTERMEDIATE] precision (guard digits) and the final
 * value is rounded once to [DISPLAY_MODE] (12 significant digits, half-away).
 * Rounding in a single step avoids the last-ULP double-rounding error that
 * ionspin's `divide` shows when asked to round directly at 12 digits (e.g.
 * 1 kg -> lb would otherwise land on 2.20462262184 instead of ...185).
 */
object ConversionEngine {
    private val INTERMEDIATE = DecimalMode(24L, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    private val DISPLAY_MODE = DecimalMode(12L, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    fun convert(value: BigDecimal, from: UnitDef, to: UnitDef): BigDecimal =
        to.conversion.fromBase(from.conversion.toBase(value), INTERMEDIATE)
            .roundSignificand(DISPLAY_MODE)
}
