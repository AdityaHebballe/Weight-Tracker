package com.aditya.weighttracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WaistToHeightCalculatorTest {
    @Test
    fun returnsNullWhenWaistOrHeightIsMissingOrInvalid() {
        assertNull(WaistToHeightCalculator.calculate(waistCm = null, heightCm = 178.0))
        assertNull(WaistToHeightCalculator.calculate(waistCm = 88.0, heightCm = null))
        assertNull(WaistToHeightCalculator.calculate(waistCm = 0.0, heightCm = 178.0))
        assertNull(WaistToHeightCalculator.calculate(waistCm = 88.0, heightCm = 0.0))
    }

    @Test
    fun calculatesRatioToExpectedValue() {
        val summary = WaistToHeightCalculator.calculate(waistCm = 88.0, heightCm = 178.0)

        assertEquals(0.49, summary!!.value, 0.005)
        assertEquals(WaistToHeightCategory.InRange, summary.category)
    }

    @Test
    fun mapsCategoryBoundaries() {
        assertEquals(WaistToHeightCategory.Low, WaistToHeightCategory.from(0.39))
        assertEquals(WaistToHeightCategory.InRange, WaistToHeightCategory.from(0.4))
        assertEquals(WaistToHeightCategory.Increased, WaistToHeightCategory.from(0.5))
        assertEquals(WaistToHeightCategory.High, WaistToHeightCategory.from(0.6))
    }
}
