package com.github.unfixedjuices.convert

import java.math.BigDecimal
import java.math.MathContext

enum class Category(val label: String) {
    Length("Length"),
    Mass("Mass"),
    Volume("Volume"),
    Temperature("Temperature"),
    Area("Area"),
    Speed("Speed"),
    Time("Time"),
    Data("Data"),
}

class ConvertUnit(
    val symbol: String,
    val name: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double,
) {
    companion object {
        fun linear(symbol: String, name: String, factor: Double) =
            ConvertUnit(symbol, name, { it * factor }, { it / factor })
    }
}

object Units {
    fun of(category: Category): List<ConvertUnit> = when (category) {
        Category.Length -> length
        Category.Mass -> mass
        Category.Volume -> volume
        Category.Temperature -> temperature
        Category.Area -> area
        Category.Speed -> speed
        Category.Time -> time
        Category.Data -> data
    }

    fun convert(value: Double, from: ConvertUnit, to: ConvertUnit): Double =
        to.fromBase(from.toBase(value))

    /** Accepts "1,5" and "1 234.5" as well as "1234.5". Null when not a number. */
    fun parse(text: String): Double? {
        val cleaned = text.trim().replace(" ", "").replace(',', '.')
        if (cleaned.count { it == '.' } > 1) return null
        return cleaned.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /** Ten significant digits, no trailing zeros, never scientific notation. */
    fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "–"
        val rounded = BigDecimal(value).round(MathContext(10)).stripTrailingZeros()
        val plain = rounded.toPlainString()
        return if (plain == "-0") "0" else plain
    }

    private val length = listOf(
        ConvertUnit.linear("mm", "Millimetre", 0.001),
        ConvertUnit.linear("cm", "Centimetre", 0.01),
        ConvertUnit.linear("m", "Metre", 1.0),
        ConvertUnit.linear("km", "Kilometre", 1000.0),
        ConvertUnit.linear("in", "Inch", 0.0254),
        ConvertUnit.linear("ft", "Foot", 0.3048),
        ConvertUnit.linear("yd", "Yard", 0.9144),
        ConvertUnit.linear("mi", "Mile", 1609.344),
    )

    private val mass = listOf(
        ConvertUnit.linear("mg", "Milligram", 0.000001),
        ConvertUnit.linear("g", "Gram", 0.001),
        ConvertUnit.linear("kg", "Kilogram", 1.0),
        ConvertUnit.linear("t", "Tonne", 1000.0),
        ConvertUnit.linear("oz", "Ounce", 0.028349523125),
        ConvertUnit.linear("lb", "Pound", 0.45359237),
        ConvertUnit.linear("st", "Stone", 6.35029318),
    )

    private val volume = listOf(
        ConvertUnit.linear("ml", "Millilitre", 0.001),
        ConvertUnit.linear("l", "Litre", 1.0),
        ConvertUnit.linear("tsp", "Teaspoon (US)", 0.00492892159375),
        ConvertUnit.linear("tbsp", "Tablespoon (US)", 0.01478676478125),
        ConvertUnit.linear("fl oz", "Fluid ounce (US)", 0.0295735295625),
        ConvertUnit.linear("cup", "Cup (US)", 0.2365882365),
        ConvertUnit.linear("pt", "Pint (US)", 0.473176473),
        ConvertUnit.linear("qt", "Quart (US)", 0.946352946),
        ConvertUnit.linear("gal", "Gallon (US)", 3.785411784),
    )

    private val temperature = listOf(
        ConvertUnit("°C", "Celsius", { it }, { it }),
        ConvertUnit("°F", "Fahrenheit", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }),
        ConvertUnit("K", "Kelvin", { it - 273.15 }, { it + 273.15 }),
    )

    private val area = listOf(
        ConvertUnit.linear("cm²", "Square centimetre", 0.0001),
        ConvertUnit.linear("m²", "Square metre", 1.0),
        ConvertUnit.linear("ha", "Hectare", 10000.0),
        ConvertUnit.linear("km²", "Square kilometre", 1000000.0),
        ConvertUnit.linear("in²", "Square inch", 0.00064516),
        ConvertUnit.linear("ft²", "Square foot", 0.09290304),
        ConvertUnit.linear("ac", "Acre", 4046.8564224),
        ConvertUnit.linear("mi²", "Square mile", 2589988.110336),
    )

    private val speed = listOf(
        ConvertUnit.linear("km/h", "Kilometres per hour", 1.0 / 3.6),
        ConvertUnit.linear("m/s", "Metres per second", 1.0),
        ConvertUnit.linear("mph", "Miles per hour", 0.44704),
        ConvertUnit.linear("kn", "Knot", 1852.0 / 3600.0),
        ConvertUnit.linear("ft/s", "Feet per second", 0.3048),
    )

    private val time = listOf(
        ConvertUnit.linear("ms", "Millisecond", 0.001),
        ConvertUnit.linear("s", "Second", 1.0),
        ConvertUnit.linear("min", "Minute", 60.0),
        ConvertUnit.linear("h", "Hour", 3600.0),
        ConvertUnit.linear("d", "Day", 86400.0),
        ConvertUnit.linear("wk", "Week", 604800.0),
    )

    private val data = listOf(
        ConvertUnit.linear("bit", "Bit", 0.125),
        ConvertUnit.linear("B", "Byte", 1.0),
        ConvertUnit.linear("kB", "Kilobyte", 1000.0),
        ConvertUnit.linear("MB", "Megabyte", 1000000.0),
        ConvertUnit.linear("GB", "Gigabyte", 1000000000.0),
        ConvertUnit.linear("TB", "Terabyte", 1000000000000.0),
        ConvertUnit.linear("KiB", "Kibibyte", 1024.0),
        ConvertUnit.linear("MiB", "Mebibyte", 1048576.0),
        ConvertUnit.linear("GiB", "Gibibyte", 1073741824.0),
    )
}
