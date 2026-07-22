package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertEquals
import org.junit.Test

class TargetSimulationCardTest {

    @Test
    fun synchronizedTargetSimulationInput_updatesUntouchedExpandedInput() {
        assertEquals(
            "5.25",
            synchronizedTargetSimulationInput(
                currentInput = "5.0",
                lastSyncedInput = "5.0",
                nextSyncedInput = "5.25",
                isExpanded = true
            )
        )
    }

    @Test
    fun synchronizedTargetSimulationInput_keepsManualExpandedInput() {
        assertEquals(
            "5.75",
            synchronizedTargetSimulationInput(
                currentInput = "5.75",
                lastSyncedInput = "5.0",
                nextSyncedInput = "5.25",
                isExpanded = true
            )
        )
    }

    @Test
    fun synchronizedTargetSimulationInput_resetsCollapsedInputToSavedTarget() {
        assertEquals(
            "5.25",
            synchronizedTargetSimulationInput(
                currentInput = "5.75",
                lastSyncedInput = "5.0",
                nextSyncedInput = "5.25",
                isExpanded = false
            )
        )
    }
}
