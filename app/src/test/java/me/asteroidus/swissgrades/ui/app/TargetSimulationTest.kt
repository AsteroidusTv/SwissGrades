package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetSimulationTest {

    @Test
    fun targetSimulation_usesOfficialHalfPointThreshold() {
        val result = computeTargetSimulation(
            notes = listOf(testNote(value = 5.0)),
            targetInput = "5.5",
            nextTestType = NoteTypeUi.FULL
        )

        assertTrue(result is TargetSimulationResult.Required)
        result as TargetSimulationResult.Required
        assertEquals("5.5", result.requiredGradeLabel)
        assertEquals("5.5", result.projectedAverageLabel)
    }

    private fun testNote(value: Double): NoteUiState {
        return NoteUiState(
            id = "note-$value",
            numericValue = value,
            weightCoefficient = 1.0,
            displayValue = value.toString(),
            noteTypeLabel = "Full grade",
            description = "Test",
            dateLabel = "01.01.2026"
        )
    }
}
