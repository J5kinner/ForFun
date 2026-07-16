package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnitCategoryTest {
    @Test fun sevenCategories() {
        assertEquals(7, UnitCategory.entries.size)
    }

    @Test fun everyLinearUnitHasRatioOrAffine() {
        UnitCategory.linearCategories.flatMap { it.units }.forEach { u ->
            assertTrue(u.conversion is Ratio || u.conversion is Affine, "${u.id} lacks a conversion")
        }
    }

    @Test fun defaultsAreDistinct() {
        UnitCategory.entries.filter { it.units.size >= 2 }.forEach {
            assertTrue(it.defaultFrom != it.defaultTo, "${it.name} defaults collide")
        }
    }

    @Test fun lengthContainsMetreAsBase() {
        val m = UnitCategory.Length.units.first { it.id == "m" }
        assertEquals(BigDecimal.parseString("1"), (m.conversion as Ratio).factor)
    }

    @Test fun currencyOffersStaticCodes() {
        val ids = UnitCategory.Currency.units.map { it.id }
        assertTrue("USD" in ids && "EUR" in ids)
        // defaultFrom/defaultTo must be usable (no longer throw on empty list)
        assertTrue(UnitCategory.Currency.defaultFrom.id != UnitCategory.Currency.defaultTo.id)
    }

    @Test fun linearCategoriesExcludeCurrency() {
        assertTrue(!UnitCategory.linearCategories.contains(UnitCategory.Currency))
    }
}
