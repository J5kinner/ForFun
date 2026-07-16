package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NumberFormatterTest {
    private fun fmt(s: String) = NumberFormatter.format(BigDecimal.parseString(s))
    @Test fun zero() = assertEquals("0", fmt("0"))
    @Test fun groupsThousands() = assertEquals("1,000,000", fmt("1000000"))
    @Test fun trimsTrailingZeros() = assertEquals("2.5", fmt("2.50"))
    @Test fun keepsFraction() = assertEquals("1,234.5", fmt("1234.5"))
    @Test fun negative() = assertEquals("-42", fmt("-42"))
    @Test fun largeGoesScientific() = assertTrue(fmt("123400000000000000").contains("E"))
    @Test fun tinyGoesScientific() = assertTrue(fmt("0.0000000123").contains("E"))
}
