package me.asteroidus.swissgrades.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetSimulationCalculatorTest {

    @Test
    fun compute_usesOfficialHalfPointThreshold() {
        val result = TargetSimulationCalculator.compute(
            grades = listOf(TargetSimulationGrade(value = 5.0, weightCoefficient = 1.0)),
            targetAverageInput = "5.5",
            plannedGradeWeightCoefficient = 1.0
        )

        assertTrue(result is TargetSimulationResult.Required)
        result as TargetSimulationResult.Required
        assertEquals(5.5, result.requiredAverage, 0.0)
        assertEquals(5.5, result.projectedOfficialAverage, 0.0)
    }

    @Test
    fun compute_acceptsCommaDecimalInput() {
        val result = TargetSimulationCalculator.compute(
            grades = listOf(TargetSimulationGrade(value = 4.75, weightCoefficient = 1.0)),
            targetAverageInput = "5,0",
            plannedGradeWeightCoefficient = 1.0
        )

        assertTrue(result is TargetSimulationResult.Required)
    }

    @Test
    fun compute_returnsAlreadyReachedWhenMinimumNextGradeIsEnough() {
        val result = TargetSimulationCalculator.compute(
            grades = List(5) { TargetSimulationGrade(value = 6.0, weightCoefficient = 1.0) },
            targetAverageInput = "5.0",
            plannedGradeWeightCoefficient = 1.0
        )

        assertSame(TargetSimulationResult.AlreadyReached, result)
    }

    @Test
    fun compute_returnsImpossibleWhenTargetCannotBeReached() {
        val result = TargetSimulationCalculator.compute(
            grades = listOf(TargetSimulationGrade(value = 2.0, weightCoefficient = 1.0)),
            targetAverageInput = "6.0",
            plannedGradeWeightCoefficient = 1.0
        )

        assertSame(TargetSimulationResult.Impossible, result)
    }

    @Test
    fun compute_returnsInvalidForOutOfRangeTarget() {
        val result = TargetSimulationCalculator.compute(
            grades = emptyList(),
            targetAverageInput = "6.5",
            plannedGradeWeightCoefficient = 1.0
        )

        assertSame(TargetSimulationResult.Invalid, result)
    }

    @Test
    fun compute_returnsInvalidForNonOfficialTargetStep() {
        assertSame(
            TargetSimulationResult.Invalid,
            TargetSimulationCalculator.compute(
                grades = emptyList(),
                targetAverageInput = "5.25",
                plannedGradeWeightCoefficient = 1.0
            )
        )
        assertSame(
            TargetSimulationResult.Invalid,
            TargetSimulationCalculator.compute(
                grades = emptyList(),
                targetAverageInput = "5.99",
                plannedGradeWeightCoefficient = 1.0
            )
        )
    }

    @Test
    fun formatGrade_keepsOneDecimalForWholeGrades() {
        assertEquals("5.0", TargetSimulationCalculator.formatGrade(5.0))
        assertEquals("5.25", TargetSimulationCalculator.formatGrade(5.25))
        assertEquals("5.13", TargetSimulationCalculator.formatGrade(5.125))
    }

    @Test
    fun compute_roundsProjectedOfficialAverageAtHalfPointThresholds() {
        assertProjectedAverage(
            grades = emptyList(),
            targetAverageInput = "5.5",
            expectedRequiredGrade = 5.25,
            expectedProjectedAverage = 5.5
        )
        assertProjectedAverage(
            grades = listOf(TargetSimulationGrade(value = 5.48, weightCoefficient = 1.0)),
            targetAverageInput = "5.5",
            expectedRequiredGrade = 5.25,
            expectedProjectedAverage = 5.5
        )
        assertProjectedAverage(
            grades = listOf(TargetSimulationGrade(value = 5.5, weightCoefficient = 1.0)),
            targetAverageInput = "6.0",
            expectedRequiredGrade = 6.0,
            expectedProjectedAverage = 6.0
        )
    }

    @Test
    fun compute_returnsRequiredAverageAcrossTwoEqualWeightGrades() {
        val result = TargetSimulationCalculator.compute(
            grades = listOf(TargetSimulationGrade(value = 4.0, weightCoefficient = 1.0)),
            targetAverageInput = "5.0",
            plannedGradeWeightCoefficient = 1.0,
            plannedGradeCount = 2
        )

        assertTrue(result is TargetSimulationResult.Required)
        result as TargetSimulationResult.Required
        assertEquals(5.125, result.requiredAverage, 0.0)
        assertEquals(5.0, result.projectedOfficialAverage, 0.0)
    }

    @Test
    fun compute_supportsThreeGradePlanAtUpperBoundary() {
        val result = TargetSimulationCalculator.compute(
            grades = listOf(TargetSimulationGrade(value = 3.0, weightCoefficient = 1.0)),
            targetAverageInput = "5.5",
            plannedGradeWeightCoefficient = 1.0,
            plannedGradeCount = 3
        )

        assertTrue(result is TargetSimulationResult.Required)
        result as TargetSimulationResult.Required
        assertEquals(6.0, result.requiredAverage, 0.0)
        assertEquals(5.5, result.projectedOfficialAverage, 0.0)
    }

    @Test
    fun compute_roundsThreeGradeAverageToAchievableTwelfthStep() {
        val result = TargetSimulationCalculator.compute(
            grades = listOf(TargetSimulationGrade(value = 4.0, weightCoefficient = 1.0)),
            targetAverageInput = "5.5",
            plannedGradeWeightCoefficient = 1.0,
            plannedGradeCount = 3
        )

        assertTrue(result is TargetSimulationResult.Required)
        result as TargetSimulationResult.Required
        assertEquals(17.0 / 3.0, result.requiredAverage, 0.000_000_1)
        assertEquals(5.5, result.projectedOfficialAverage, 0.0)
    }

    @Test
    fun compute_returnsInvalidForUnsupportedPlannedGradeCount() {
        listOf(0, 4).forEach { count ->
            assertSame(
                TargetSimulationResult.Invalid,
                TargetSimulationCalculator.compute(
                    grades = emptyList(),
                    targetAverageInput = "5.0",
                    plannedGradeWeightCoefficient = 1.0,
                    plannedGradeCount = count
                )
            )
        }
    }

    private fun assertProjectedAverage(
        grades: List<TargetSimulationGrade>,
        targetAverageInput: String,
        expectedRequiredGrade: Double,
        expectedProjectedAverage: Double
    ) {
        val result = TargetSimulationCalculator.compute(
            grades = grades,
            targetAverageInput = targetAverageInput,
            plannedGradeWeightCoefficient = 1.0
        )

        assertTrue(result is TargetSimulationResult.Required)
        result as TargetSimulationResult.Required
        assertEquals(expectedRequiredGrade, result.requiredAverage, 0.0)
        assertEquals(expectedProjectedAverage, result.projectedOfficialAverage, 0.0)
    }
}
