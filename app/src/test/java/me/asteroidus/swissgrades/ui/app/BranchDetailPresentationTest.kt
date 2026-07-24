package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BranchDetailPresentationTest {

    @Test
    fun evolutionRequiresAtLeastTwoGrades() {
        assertFalse(shouldShowEvolution(noteCount = 0))
        assertFalse(shouldShowEvolution(noteCount = 1))
        assertTrue(shouldShowEvolution(noteCount = 2))
        assertTrue(shouldShowEvolution(noteCount = 5))
    }
}
