package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

sealed interface CalcResult {
    data class Success(val value: BigDecimal, val formatted: String) : CalcResult
    data class Error(val error: CalcError) : CalcResult
    data object Empty : CalcResult
}
