package com.github.unfixedjuices.convert

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnitsTest {
    private fun unit(category: Category, symbol: String) =
        Units.of(category).first { it.symbol == symbol }

    private fun check(category: Category, from: String, to: String, value: Double, expected: String) {
        val result = Units.convert(value, unit(category, from), unit(category, to))
        assertEquals(expected, Units.format(result), "$value $from -> $to")
    }

    @Test fun lengthKilometreToMile() = check(Category.Length, "km", "mi", 1.0, "0.6213711922")
    @Test fun lengthInchToCentimetre() = check(Category.Length, "in", "cm", 1.0, "2.54")
    @Test fun massPoundToKilogram() = check(Category.Mass, "lb", "kg", 1.0, "0.45359237")
    @Test fun massStoneToPound() = check(Category.Mass, "st", "lb", 1.0, "14")
    @Test fun volumeGallonToLitre() = check(Category.Volume, "gal", "l", 1.0, "3.785411784")
    @Test fun volumeCupToTablespoon() = check(Category.Volume, "cup", "tbsp", 1.0, "16")
    @Test fun temperatureCelsiusToFahrenheit() = check(Category.Temperature, "°C", "°F", 100.0, "212")
    @Test fun temperatureFahrenheitToCelsius() = check(Category.Temperature, "°F", "°C", -40.0, "-40")
    @Test fun temperatureKelvinToCelsius() = check(Category.Temperature, "K", "°C", 0.0, "-273.15")
    @Test fun areaAcreToHectare() = check(Category.Area, "ac", "ha", 1.0, "0.4046856422")
    @Test fun speedMphToKmh() = check(Category.Speed, "mph", "km/h", 60.0, "96.56064")
    @Test fun speedKnotToKmh() = check(Category.Speed, "kn", "km/h", 1.0, "1.852")
    @Test fun timeDayToHour() = check(Category.Time, "d", "h", 1.0, "24")
    @Test fun dataMebibyteToKilobyte() = check(Category.Data, "MiB", "kB", 1.0, "1048.576")
    @Test fun dataByteToBit() = check(Category.Data, "B", "bit", 1.0, "8")

    @Test
    fun everyUnitRoundTripsThroughItsBase() {
        for (category in Category.entries) {
            for (unit in Units.of(category)) {
                val back = unit.fromBase(unit.toBase(12.5))
                assertTrue(kotlin.math.abs(back - 12.5) < 1e-9, "${category.label} ${unit.symbol}")
            }
        }
    }

    @Test
    fun everyCategoryHasAtLeastTwoUnitsWithUniqueSymbols() {
        for (category in Category.entries) {
            val units = Units.of(category)
            assertTrue(units.size >= 2, category.label)
            assertEquals(units.size, units.map { it.symbol }.toSet().size, category.label)
        }
    }

    @Test
    fun parseAcceptsCommaDecimalAndSpaces() {
        assertEquals(1.5, Units.parse("1,5"))
        assertEquals(1234.5, Units.parse("1 234.5"))
        assertEquals(-2.0, Units.parse(" -2 "))
    }

    @Test
    fun parseRejectsGarbage() {
        assertNull(Units.parse("abc"))
        assertNull(Units.parse("1.2.3"))
        assertNull(Units.parse(""))
    }

    @Test
    fun formatTrimsZerosAndAvoidsScientificNotation() {
        assertEquals("1", Units.format(1.0))
        assertEquals("0.1", Units.format(0.1))
        assertEquals("1000000000000", Units.format(1e12))
        assertEquals("0", Units.format(-0.0))
        assertEquals("0.3", Units.format(0.1 + 0.2))
    }
}
