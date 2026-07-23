package me.asteroidus.swissgrades.domain

import kotlin.math.abs
import kotlin.math.round

object OfficialAverageTarget {

    fun parse(input: String): Double? {
        val value = input.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return value.takeIf(::isValid)
    }

    fun isValid(value: Double): Boolean {
        if (!value.isFinite() || value !in MIN_TARGET..MAX_TARGET) return false
        val doubledValue = value * 2.0
        return abs(doubledValue - round(doubledValue)) < STEP_TOLERANCE
    }

    fun normalizeLegacy(value: Double): Double? {
        if (!value.isFinite() || value !in MIN_TARGET..MAX_TARGET) return null
        return GradeCalculator.roundToHalf(value)
    }
}

private const val MIN_TARGET = 1.0
private const val MAX_TARGET = 6.0
private const val STEP_TOLERANCE = 1e-9
