package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

class UnitConversionTest {
    private val mode = DecimalMode(30L, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    private fun bd(n: Int) = BigDecimal.fromInt(n)

    @Test fun ratioToBase() {
        val km = Ratio(BigDecimal.parseString("1000"))
        assertEquals("2000", km.toBase(bd(2)).toStringExpanded()) // 2 km -> 2000 m
    }

    @Test fun ratioRoundTrips() {
        val km = Ratio(BigDecimal.parseString("1000"))
        val base = km.toBase(bd(2))
        assertEquals("2", km.fromBase(base, mode).toStringExpanded())
    }

    @Test fun affineFahrenheitToBase() {
        val f = Affine(mulNum = bd(5), addNum = bd(-160), den = bd(9)) // C = (F*5 - 160)/9
        assertEquals("0", f.toBase(bd(32)).toStringExpanded()) // 32F -> 0C
    }

    @Test fun affineFahrenheitFromBase() {
        val f = Affine(mulNum = bd(5), addNum = bd(-160), den = bd(9))
        assertEquals("32", f.fromBase(bd(0), mode).toStringExpanded()) // 0C -> 32F
    }
}
