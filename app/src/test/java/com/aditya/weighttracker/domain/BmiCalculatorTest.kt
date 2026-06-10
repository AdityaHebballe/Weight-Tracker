package com.aditya.weighttracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BmiCalculatorTest {
    @Test
    fun returnsNullWhenHeightOrWeightIsMissingOrInvalid() {
        assertNull(BmiCalculator.calculate(weightKg = null, heightCm = 178.0))
        assertNull(BmiCalculator.calculate(weightKg = 78.0, heightCm = null))
        assertNull(BmiCalculator.calculate(weightKg = 0.0, heightCm = 178.0))
        assertNull(BmiCalculator.calculate(weightKg = 78.0, heightCm = 0.0))
    }

    @Test
    fun calculatesBmiToExpectedValue() {
        val bmi = BmiCalculator.calculate(weightKg = 78.2, heightCm = 178.0)

        assertEquals(24.7, bmi!!.value, 0.05)
        assertEquals(BmiCategory.InRange, bmi.category)
    }

    @Test
    fun mapsCategoryBoundaries() {
        assertEquals(BmiCategory.Light, BmiCategory.from(18.4))
        assertEquals(BmiCategory.InRange, BmiCategory.from(18.5))
        assertEquals(BmiCategory.GettingHeavy, BmiCategory.from(25.0))
        assertEquals(BmiCategory.High, BmiCategory.from(30.0))
        assertEquals(BmiCategory.VeryHigh, BmiCategory.from(35.0))
    }
}
