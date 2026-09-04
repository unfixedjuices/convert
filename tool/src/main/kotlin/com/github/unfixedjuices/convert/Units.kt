package com.github.unfixedjuices.convert

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class Category(val label: String) {
    Length("Length"),
    Mass("Mass"),
    Volume("Volume"),
    Temperature("Temperature"),
    Area("Area"),
    Speed("Speed"),
    Time("Time"),
    Data("Data"),
    ;

    val units: List<ConvertUnit> get() = Units.of(this)
}

/** Every unit maps onto its category's base unit as base = value * scale + offset. */
data class ConvertUnit(
    val symbol: String,
    val name: String,
    val scale: Double,
    val offset: Double = 0.0,
) {
    fun toBase(value: Double): Double = value * scale + offset
    fun fromBase(base: Double): Double = (base - offset) / scale
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

    private val number = Regex("""-?(\d+\.?\d*|\.\d+)""")

    /**
     * Digits with the locale's decimal separator; the other of "." and "," is a
     * grouping mark and is dropped, as are spaces. No exponents, no letters.
     * Null when the text is not a number.
     */
    fun parse(text: String, locale: Locale = Locale.getDefault()): Double? {
        val decimal = DecimalFormatSymbols.getInstance(locale).decimalSeparator
        val grouping = if (decimal == ',') '.' else ','
        val cleaned = text.trim()
            .replace(" ", "")
            .replace(grouping.toString(), "")
            .replace(decimal, '.')
        if (!number.matches(cleaned)) return null
        return cleaned.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /**
     * Ten significant digits, never fewer than the integer part, no trailing
     * zeros, never scientific notation, the locale's decimal separator.
     */
    fun format(value: Double, locale: Locale = Locale.getDefault()): String {
        if (value.isNaN() || value.isInfinite()) return "–"
        val exact = BigDecimal.valueOf(value)
        val integerDigits = exact.precision() - exact.scale()
        val rounded = if (integerDigits >= 10) {
            exact.setScale(0, RoundingMode.HALF_UP)
        } else {
            exact.round(MathContext(10, RoundingMode.HALF_UP))
        }
        val plain = rounded.stripTrailingZeros().toPlainString()
        val text = if (plain == "-0") "0" else plain
        val decimal = DecimalFormatSymbols.getInstance(locale).decimalSeparator
        return if (decimal == '.') text else text.replace('.', decimal)
    }

    private val length = listOf(
        ConvertUnit("mm", "Millimetre", 0.001),
        ConvertUnit("cm", "Centimetre", 0.01),
        ConvertUnit("m", "Metre", 1.0),
        ConvertUnit("km", "Kilometre", 1000.0),
        ConvertUnit("in", "Inch", 0.0254),
        ConvertUnit("ft", "Foot", 0.3048),
        ConvertUnit("yd", "Yard", 0.9144),
        ConvertUnit("mi", "Mile", 1609.344),
    )

    private val mass = listOf(
        ConvertUnit("mg", "Milligram", 0.000001),
        ConvertUnit("g", "Gram", 0.001),
        ConvertUnit("kg", "Kilogram", 1.0),
        ConvertUnit("t", "Tonne", 1000.0),
        ConvertUnit("oz", "Ounce", 0.028349523125),
        ConvertUnit("lb", "Pound", 0.45359237),
        ConvertUnit("st", "Stone", 6.35029318),
    )

    private val volume = listOf(
        ConvertUnit("ml", "Millilitre", 0.001),
        ConvertUnit("l", "Litre", 1.0),
        ConvertUnit("tsp", "Teaspoon (US)", 0.00492892159375),
        ConvertUnit("tbsp", "Tablespoon (US)", 0.01478676478125),
        ConvertUnit("fl oz", "Fluid ounce (US)", 0.0295735295625),
        ConvertUnit("cup", "Cup (US)", 0.2365882365),
        ConvertUnit("pt", "Pint (US)", 0.473176473),
        ConvertUnit("qt", "Quart (US)", 0.946352946),
        ConvertUnit("gal", "Gallon (US)", 3.785411784),
    )

    private val temperature = listOf(
        ConvertUnit("°C", "Celsius", 1.0),
        ConvertUnit("°F", "Fahrenheit", 5.0 / 9.0, -160.0 / 9.0),
        ConvertUnit("K", "Kelvin", 1.0, -273.15),
    )

    private val area = listOf(
        ConvertUnit("cm²", "Square centimetre", 0.0001),
        ConvertUnit("m²", "Square metre", 1.0),
        ConvertUnit("ha", "Hectare", 10000.0),
        ConvertUnit("km²", "Square kilometre", 1000000.0),
        ConvertUnit("in²", "Square inch", 0.00064516),
        ConvertUnit("ft²", "Square foot", 0.09290304),
        ConvertUnit("ac", "Acre", 4046.8564224),
        ConvertUnit("mi²", "Square mile", 2589988.110336),
    )

    private val speed = listOf(
        ConvertUnit("km/h", "Kilometres per hour", 1.0 / 3.6),
        ConvertUnit("m/s", "Metres per second", 1.0),
        ConvertUnit("mph", "Miles per hour", 0.44704),
        ConvertUnit("kn", "Knot", 1852.0 / 3600.0),
        ConvertUnit("ft/s", "Feet per second", 0.3048),
    )

    private val time = listOf(
        ConvertUnit("ms", "Millisecond", 0.001),
        ConvertUnit("s", "Second", 1.0),
        ConvertUnit("min", "Minute", 60.0),
        ConvertUnit("h", "Hour", 3600.0),
        ConvertUnit("d", "Day", 86400.0),
        ConvertUnit("wk", "Week", 604800.0),
    )

    private val data = listOf(
        ConvertUnit("bit", "Bit", 0.125),
        ConvertUnit("B", "Byte", 1.0),
        ConvertUnit("kB", "Kilobyte", 1000.0),
        ConvertUnit("MB", "Megabyte", 1000000.0),
        ConvertUnit("GB", "Gigabyte", 1000000000.0),
        ConvertUnit("TB", "Terabyte", 1000000000000.0),
        ConvertUnit("KiB", "Kibibyte", 1024.0),
        ConvertUnit("MiB", "Mebibyte", 1048576.0),
        ConvertUnit("GiB", "Gibibyte", 1073741824.0),
    )
}
