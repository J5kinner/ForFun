package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Maps a unit's value to/from the category's canonical base unit.
 *
 * [toBase] is exact where possible (multiplication, or division by an exact
 * denominator for the canonical temperature checkpoints); the caller supplies a
 * [DecimalMode] to [fromBase] so the final display rounding happens there.
 */
sealed interface UnitConversion {
    fun toBase(value: BigDecimal): BigDecimal
    fun fromBase(base: BigDecimal, mode: DecimalMode): BigDecimal
}

/** Linear: base = value * factor. `factor` is chosen exact (see [UnitCategory]). */
data class Ratio(val factor: BigDecimal) : UnitConversion {
    override fun toBase(value: BigDecimal): BigDecimal = value.multiply(factor)
    override fun fromBase(base: BigDecimal, mode: DecimalMode): BigDecimal = base.divide(factor, mode)
}

/**
 * Affine (temperature): base = (value * mulNum + addNum) / den, all constants exact.
 *
 * Celsius is the base, so Fahrenheit is `C = (F*5 - 160)/9` and Kelvin is
 * `C = (K*100 - 27315)/100`, which makes the canonical checkpoints
 * (0/32/100/212/273.15, and the -40 crossover) bit-exact.
 */
data class Affine(val mulNum: BigDecimal, val addNum: BigDecimal, val den: BigDecimal) : UnitConversion {
    override fun toBase(value: BigDecimal): BigDecimal =
        value.multiply(mulNum).add(addNum).divide(den, EXACTISH)

    override fun fromBase(base: BigDecimal, mode: DecimalMode): BigDecimal =
        base.multiply(den).subtract(addNum).divide(mulNum, mode)

    companion object {
        private val EXACTISH = DecimalMode(50L, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    }
}
