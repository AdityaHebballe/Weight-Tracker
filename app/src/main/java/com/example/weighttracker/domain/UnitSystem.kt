package com.example.weighttracker.domain

enum class UnitSystem(
    val storageValue: String,
    val weightLabel: String,
    val lengthLabel: String,
) {
    Metric("METRIC", "kg", "cm"),
    Imperial("IMPERIAL", "lb", "in");

    companion object {
        fun fromStorage(value: String?): UnitSystem =
            entries.firstOrNull { it.storageValue == value } ?: Metric
    }
}

object UnitConverter {
    private const val poundsPerKg = 2.2046226218
    private const val cmPerInch = 2.54

    fun weightFromKg(weightKg: Double, unitSystem: UnitSystem): Double =
        if (unitSystem == UnitSystem.Imperial) weightKg * poundsPerKg else weightKg

    fun weightToKg(weight: Double, unitSystem: UnitSystem): Double =
        if (unitSystem == UnitSystem.Imperial) weight / poundsPerKg else weight

    fun lengthFromCm(lengthCm: Double, unitSystem: UnitSystem): Double =
        if (unitSystem == UnitSystem.Imperial) lengthCm / cmPerInch else lengthCm

    fun lengthToCm(length: Double, unitSystem: UnitSystem): Double =
        if (unitSystem == UnitSystem.Imperial) length * cmPerInch else length
}
