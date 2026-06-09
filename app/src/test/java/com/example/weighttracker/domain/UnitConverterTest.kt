package com.example.weighttracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {
    @Test
    fun convertsWeightRoundTrip() {
        val pounds = UnitConverter.weightFromKg(78.2, UnitSystem.Imperial)
        val kg = UnitConverter.weightToKg(pounds, UnitSystem.Imperial)

        assertEquals(172.4, pounds, 0.1)
        assertEquals(78.2, kg, 0.001)
    }

    @Test
    fun convertsLengthRoundTrip() {
        val inches = UnitConverter.lengthFromCm(178.0, UnitSystem.Imperial)
        val cm = UnitConverter.lengthToCm(inches, UnitSystem.Imperial)

        assertEquals(70.1, inches, 0.1)
        assertEquals(178.0, cm, 0.001)
    }
}
