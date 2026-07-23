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
        val requiredAverage: Double,
        val projectedOfficialAverage: Double
    ) : TargetSimulationResult
}

object TargetSimulationCalculator {

    fun compute(
        grades: List<TargetSimulationGrade>,
        targetAverageInput: String,
        plannedGradeWeightCoefficient: Double,
        plannedGradeCount: Int = 1
    ): TargetSimulationResult {
        val targetAverage = OfficialAverageTarget.parse(targetAverageInput)
        if (
            targetAverage == null ||
            plannedGradeWeightCoefficient <= 0.0 ||
            plannedGradeCount !in MIN_PLANNED_GRADE_COUNT..MAX_PLANNED_GRADE_COUNT
        ) {
            return TargetSimulationResult.Invalid
        }

        val weightedSum = grades.sumOf { it.value * it.weightCoefficient }
        val totalWeight = grades.sumOf { it.weightCoefficient }
        val plannedTotalWeight = plannedGradeWeightCoefficient * plannedGradeCount
        val rawAverageNeeded = (targetAverage - OFFICIAL_HALF_POINT_THRESHOLD).coerceAtLeast(MIN_GRADE)
        val requiredRawAverage = (
            rawAverageNeeded * (totalWeight + plannedTotalWeight) - weightedSum
        ) / plannedTotalWeight

        if (requiredRawAverage <= MIN_GRADE) {
            return TargetSimulationResult.AlreadyReached
        }
        if (requiredRawAverage > MAX_GRADE) {
            return TargetSimulationResult.Impossible
        }

        val requiredAverage = requiredRawAverage
            .roundUpToAchievableAverage(plannedGradeCount)
            .coerceIn(MIN_GRADE, MAX_GRADE)
        val projectedRawAverage = (
            weightedSum + requiredAverage * plannedTotalWeight
        ) / (totalWeight + plannedTotalWeight)
        val projectedOfficialAverage = GradeCalculator.roundToHalf(projectedRawAverage)

        return TargetSimulationResult.Required(
            requiredAverage = requiredAverage,
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
private const val MIN_PLANNED_GRADE_COUNT = 1
private const val MAX_PLANNED_GRADE_COUNT = 3
private const val OFFICIAL_HALF_POINT_THRESHOLD = 0.25

private fun Double.roundUpToAchievableAverage(plannedGradeCount: Int): Double {
    val quarterStepsAcrossPlan = 4.0 * plannedGradeCount
    return ceil((this * quarterStepsAcrossPlan) - 1e-9) / quarterStepsAcrossPlan
}
