package com.example.calc.domain.convert

import com.example.calc.domain.NumberFormatter
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/** Formats a conversion result for display, reusing the Standard-mode formatter. */
object ConversionFormatter {
    /** Delegates to [NumberFormatter.format] for grouping, trailing-zero trim, and scientific fallback. */
    fun format(value: BigDecimal): String = NumberFormatter.format(value)
}
