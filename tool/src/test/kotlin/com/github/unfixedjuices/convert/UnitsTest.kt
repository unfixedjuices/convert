package com.github.unfixedjuices.convert

import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnitsTest {
    private val us = Locale.US
    private val de = Locale.GERMANY

    private fun unit(category: Category, symbol: String) =
        category.units.first { it.symbol == symbol }

    private fun check(category: Category, from: String, to: String, value: Double, expected: Double) {
        val result = Units.convert(value, unit(category, from), unit(category, to))
        val tolerance = max(1e-9, abs(expected) * 1e-12)
        assertEquals(expected, result, tolerance, "$value $from -> $to")
    }

    @Test fun lengthKilometreToMile() = check(Category.Length, "km", "mi", 1.0, 0.621371192237334)
    @Test fun lengthInchToCentimetre() = check(Category.Length, "in", "cm", 1.0, 2.54)
    @Test fun massPoundToKilogram() = check(Category.Mass, "lb", "kg", 1.0, 0.45359237)
    @Test fun massStoneToPound() = check(Category.Mass, "st", "lb", 1.0, 14.0)
    @Test fun volumeGallonToLitre() = check(Category.Volume, "gal", "l", 1.0, 3.785411784)
    @Test fun volumeCupToTablespoon() = check(Category.Volume, "cup", "tbsp", 1.0, 16.0)
    @Test fun temperatureCelsiusToFahrenheit() = check(Category.Temperature, "°C", "°F", 100.0, 212.0)
    @Test fun temperatureFahrenheitToCelsius() = check(Category.Temperature, "°F", "°C", -40.0, -40.0)
    @Test fun temperatureKelvinToCelsius() = check(Category.Temperature, "K", "°C", 0.0, -273.15)
    @Test fun areaAcreToHectare() = check(Category.Area, "ac", "ha", 1.0, 0.40468564224)
    @Test fun speedMphToKmh() = check(Category.Speed, "mph", "km/h", 60.0, 96.56064)
    @Test fun speedKnotToKmh() = check(Category.Speed, "kn", "km/h", 1.0, 1.852)
    @Test fun timeDayToHour() = check(Category.Time, "d", "h", 1.0, 24.0)
    @Test fun dataMebibyteToKilobyte() = check(Category.Data, "MiB", "kB", 1.0, 1048.576)
    @Test fun dataByteToBit() = check(Category.Data, "B", "bit", 1.0, 8.0)

    @Test
    fun everyUnitRoundTripsThroughItsBase() {
        for (category in Category.entries) {
            for (unit in category.units) {
                val back = unit.fromBase(unit.toBase(12.5))
                assertTrue(abs(back - 12.5) < 1e-9, "${category.label} ${unit.symbol}")
            }
        }
    }

    @Test
    fun everyCategoryHasAtLeastTwoUnitsWithUniqueSymbols() {
        for (category in Category.entries) {
            val units = category.units
            assertTrue(units.size >= 2, category.label)
            assertEquals(units.size, units.map { it.symbol }.toSet().size, category.label)
        }
    }

    @Test
    fun parseFollowsTheLocaleSeparators() {
        assertEquals(1000.0, Units.parse("1,000", us))
        assertEquals(1000.5, Units.parse("1,000.5", us))
        assertEquals(1234.5, Units.parse("1 234.5", us))
        assertEquals(-2.0, Units.parse(" -2 ", us))
        assertEquals(0.5, Units.parse(".5", us))
        assertEquals(1.5, Units.parse("1,5", de))
        assertEquals(1000.0, Units.parse("1.000", de))
        assertEquals(1000.5, Units.parse("1.000,5", de))
    }

    @Test
    fun parseRejectsAnythingThatIsNotPlainDigits() {
        assertNull(Units.parse("abc", us))
        assertNull(Units.parse("12a", us))
        assertNull(Units.parse("5f", us))
        assertNull(Units.parse("1e300", us))
        assertNull(Units.parse("0x1p4", us))
        assertNull(Units.parse("1.2.3", us))
        assertNull(Units.parse("--1", us))
        assertNull(Units.parse("", us))
    }

    @Test
    fun formatTrimsZerosAndAvoidsScientificNotation() {
        assertEquals("1", Units.format(1.0, us))
        assertEquals("0.1", Units.format(0.1, us))
        assertEquals("1000000000000", Units.format(1e12, us))
        assertEquals("0", Units.format(-0.0, us))
        assertEquals("0.3", Units.format(0.1 + 0.2, us))
        assertEquals("0.6213711922", Units.format(0.621371192237334, us))
    }

    @Test
    fun formatKeepsEveryIntegerDigitAndRoundsHalfUp() {
        assertEquals("12345678901", Units.format(12345678901.0, us))
        assertEquals("823.9808755", Units.format(823.98087545, us))
        assertEquals("–", Units.format(Double.NaN, us))
    }

    @Test
    fun formatUsesTheLocaleDecimalSeparator() {
        assertEquals("1,5", Units.format(1.5, de))
        assertEquals("1000000", Units.format(1e6, de))
    }
}
