package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

sealed interface Rpn {
    data class Value(val v: BigDecimal) : Rpn
    data class Bin(val op: Operator) : Rpn
    data object Neg : Rpn
    data object PctScale : Rpn   // b -> b / 100
    data object PctAdd : Rpn     // (a below top, b top) -> a * b / 100 ; leaves a in place
}
