package com.example.calc.domain.convert

/** One convertible unit: a stable [id], a display [symbol], a human [name], and its [conversion]. */
data class UnitDef(
    val id: String,
    val symbol: String,
    val name: String,
    val conversion: UnitConversion,
)
