package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal

// Top-level so it is available while the enum constants are being constructed
// (a companion-object member would still be uninitialized at that point).
private fun bd(s: String): BigDecimal = BigDecimal.parseString(s)

/**
 * The seven converter categories.
 *
 * Base units are chosen so every catalogued linear factor is a *terminating*
 * decimal (metre / kilogram / square metre / litre / km-per-hour / Celsius).
 * Temperature uses [Affine]; Currency starts empty and is filled at runtime from
 * a live/bundled rate table.
 */
enum class UnitCategory(val displayName: String, val units: List<UnitDef>) {
    Length(
        "Length",
        listOf(
            UnitDef("km", "km", "Kilometre", Ratio(bd("1000"))),
            UnitDef("m", "m", "Metre", Ratio(bd("1"))),
            UnitDef("cm", "cm", "Centimetre", Ratio(bd("0.01"))),
            UnitDef("mm", "mm", "Millimetre", Ratio(bd("0.001"))),
            UnitDef("mi", "mi", "Mile", Ratio(bd("1609.344"))),
            UnitDef("yd", "yd", "Yard", Ratio(bd("0.9144"))),
            UnitDef("ft", "ft", "Foot", Ratio(bd("0.3048"))),
            UnitDef("in", "in", "Inch", Ratio(bd("0.0254"))),
            UnitDef("nmi", "nmi", "Nautical mile", Ratio(bd("1852"))),
        ),
    ),
    Weight(
        "Weight",
        listOf(
            UnitDef("kg", "kg", "Kilogram", Ratio(bd("1"))),
            UnitDef("g", "g", "Gram", Ratio(bd("0.001"))),
            UnitDef("mg", "mg", "Milligram", Ratio(bd("0.000001"))),
            UnitDef("t", "t", "Tonne", Ratio(bd("1000"))),
            UnitDef("lb", "lb", "Pound", Ratio(bd("0.45359237"))),
            UnitDef("oz", "oz", "Ounce", Ratio(bd("0.028349523125"))),
            UnitDef("st", "st", "Stone", Ratio(bd("6.35029318"))),
        ),
    ),
    Temperature(
        "Temperature",
        listOf(
            UnitDef("C", "°C", "Celsius", Affine(bd("1"), bd("0"), bd("1"))),
            UnitDef("F", "°F", "Fahrenheit", Affine(bd("5"), bd("-160"), bd("9"))), // C = (F*5 - 160)/9
            UnitDef("K", "K", "Kelvin", Affine(bd("100"), bd("-27315"), bd("100"))), // C = (K*100 - 27315)/100
        ),
    ),
    Area(
        "Area",
        listOf(
            UnitDef("km2", "km²", "Square kilometre", Ratio(bd("1000000"))),
            UnitDef("m2", "m²", "Square metre", Ratio(bd("1"))),
            UnitDef("cm2", "cm²", "Square centimetre", Ratio(bd("0.0001"))),
            UnitDef("ha", "ha", "Hectare", Ratio(bd("10000"))),
            UnitDef("acre", "acre", "Acre", Ratio(bd("4046.8564224"))),
            UnitDef("ft2", "ft²", "Square foot", Ratio(bd("0.09290304"))),
            UnitDef("in2", "in²", "Square inch", Ratio(bd("0.00064516"))),
            UnitDef("mi2", "mi²", "Square mile", Ratio(bd("2589988.110336"))),
        ),
    ),
    Volume(
        "Volume",
        listOf(
            UnitDef("L", "L", "Litre", Ratio(bd("1"))),
            UnitDef("mL", "mL", "Millilitre", Ratio(bd("0.001"))),
            UnitDef("m3", "m³", "Cubic metre", Ratio(bd("1000"))),
            UnitDef("gal", "gal", "Gallon (US)", Ratio(bd("3.785411784"))),
            UnitDef("qt", "qt", "Quart (US)", Ratio(bd("0.946352946"))),
            UnitDef("pt", "pt", "Pint (US)", Ratio(bd("0.473176473"))),
            UnitDef("cup", "cup", "Cup (US)", Ratio(bd("0.2365882365"))),
            UnitDef("floz", "fl oz", "Fluid ounce (US)", Ratio(bd("0.0295735295625"))),
        ),
    ),
    Speed(
        "Speed",
        listOf(
            UnitDef("kmh", "km/h", "Kilometres/hour", Ratio(bd("1"))),
            UnitDef("ms", "m/s", "Metres/second", Ratio(bd("3.6"))),
            UnitDef("mph", "mph", "Miles/hour", Ratio(bd("1.609344"))),
            UnitDef("kn", "kn", "Knot", Ratio(bd("1.852"))),
            UnitDef("fts", "ft/s", "Feet/second", Ratio(bd("1.09728"))),
        ),
    ),
    Currency("Currency", emptyList()); // populated at runtime from an ExchangeRates table

    /** First catalogued unit. NOTE: throws for [Currency] (empty until rates load — fill it first). */
    val defaultFrom: UnitDef get() = units.first()

    /** Second catalogued unit (falls back to the first if only one exists). Throws for empty [Currency]. */
    val defaultTo: UnitDef get() = units.getOrElse(1) { units.first() }

    companion object {
        /** Every category that converts via static factors (i.e. all but [Currency]). */
        val linearCategories: List<UnitCategory> = listOf(Length, Weight, Temperature, Area, Volume, Speed)
    }
}
