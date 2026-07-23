package me.asteroidus.swissgrades.domain

import java.util.Locale
import kotlin.math.ceil

data class TargetSimulationGrade(
    val value: Double,
    val weightCoefficient: Double
)

sealed interface TargetSimulationResult {
    data object Invalid : TargetSimulationResult
    data object AlreadyReached : TargetSimulationResult
    data object Impossible : TargetSimulationResult
    data class Required(
        val requiredGrade: Double,
        val projectedOfficialAverage: Double
    ) : TargetSimulationResult
}

object TargetSimulationCalculator {

    fun compute(
        grades: List<TargetSimulationGrade>,
        targetAverageInput: String,
        nextWeightCoefficient: Double
    ): TargetSimulationResult {
        val targetAverage = OfficialAverageTarget.parse(targetAverageInput)
        if (
            targetAverage == null ||
            nextWeightCoefficient <= 0.0
        ) {
            return TargetSimulationResult.Invalid
        }

        val weightedSum = grades.sumOf { it.value * it.weightCoefficient }
        val totalWeight = grades.sumOf { it.weightCoefficient }
        val rawAverageNeeded = (targetAverage - OFFICIAL_HALF_POINT_THRESHOLD).coerceAtLeast(MIN_GRADE)
        val requiredRawGrade = (
            rawAverageNeeded * (totalWeight + nextWeightCoefficient) - weightedSum
        ) / nextWeightCoefficient

        if (requiredRawGrade <= MIN_GRADE) {
            return TargetSimulationResult.AlreadyReached
        }
        if (requiredRawGrade > MAX_GRADE) {
            return TargetSimulationResult.Impossible
        }

        val requiredGrade = requiredRawGrade.roundUpToQuarter().coerceIn(MIN_GRADE, MAX_GRADE)
        val projectedRawAverage = (
            weightedSum + requiredGrade * nextWeightCoefficient
        ) / (totalWeight + nextWeightCoefficient)
        val projectedOfficialAverage = GradeCalculator.roundToHalf(projectedRawAverage)

        return TargetSimulationResult.Required(
            requiredGrade = requiredGrade,
            projectedOfficialAverage = projectedOfficialAverage
        )
    }

    fun formatGrade(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.1f", value)
        } else {
            String.format(Locale.US, "%.2f", value).trimEnd('0')
        }
    }
}

private const val MIN_GRADE = 1.0
private const val MAX_GRADE = 6.0
private const val OFFICIAL_HALF_POINT_THRESHOLD = 0.25

private fun Double.roundUpToQuarter(): Double {
    return ceil((this * 4.0) - 1e-9) / 4.0
}
