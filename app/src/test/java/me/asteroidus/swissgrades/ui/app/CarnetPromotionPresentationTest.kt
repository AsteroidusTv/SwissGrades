package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CarnetPromotionPresentationTest {
    @Test
    fun incompleteSetupNeverMarksConditionsAsMet() {
        val summary = summary(emptyList())
        assertFalse(summary.conditions.any { it.status == ConditionStatus.MET })
        assertEquals(4, summary.conditions.size)
    }

    @Test
    fun incompleteBasketStillShowsKnownAverageBelowThreeAsUnmet() {
        val summary = summaryForSubjects(listOf(
            subject("math", 2.5),
            subject("sport", 1.0, basket = false).copy(isCounted = false)
        ))
        assertFalse(summary.isPromotionCalculable)
        val minimum = summary.conditions.single { it.kind == PromotionConditionKind.MINIMUM_AVERAGE }
        assertEquals(2.5, minimum.value!!, 0.0)
        assertEquals(ConditionStatus.UNMET, minimum.status)
    }

    @Test
    fun negativeCompensationHasItsOwnUnmetRowDespiteSufficientBasket() {
        val summary = summary(listOf(subject("history", 3.5, basket = false)))
        val conditions = summary.conditions.associateBy { it.kind }
        assertEquals(ConditionStatus.MET, conditions.getValue(PromotionConditionKind.BASKET).status)
        assertEquals(ConditionStatus.UNMET, conditions.getValue(PromotionConditionKind.COMPENSATION).status)
        assertEquals(-1.0, conditions.getValue(PromotionConditionKind.COMPENSATION).value!!, 0.0)
        assertEquals(ConditionStatus.MET, conditions.getValue(PromotionConditionKind.MINIMUM_AVERAGE).status)
    }

    @Test
    fun excludedLowGradeDoesNotChangeTheMinimumCondition() {
        val summary = summary(listOf(subject("sport", 1.0, basket = false).copy(isCounted = false)))
        val minimum = summary.conditions.single { it.kind == PromotionConditionKind.MINIMUM_AVERAGE }
        assertEquals(4.0, minimum.value!!, 0.0)
        assertEquals(ConditionStatus.MET, minimum.status)
    }

    private fun summary(extra: List<StoredSubject>): DashboardSummaryUiState {
        val subjects = if (extra.isEmpty()) emptyList() else listOf(
            subject("language1", 4.0), subject("language2", 4.0), subject("math", 4.0),
            subject("option", 4.0).copy(isOptionSubject = true, optionChoice = InitialOptionChoice.SPANISH)
        ) + extra
        return summaryForSubjects(subjects)
    }

    private fun summaryForSubjects(subjects: List<StoredSubject>): DashboardSummaryUiState {
        val repository = object : GradeTrackerRepository {
            override fun load() = GradeTrackerAppState(selectedOption = InitialOptionChoice.SPANISH, subjects = subjects)
            override fun save(state: GradeTrackerAppState) = error("Reading a summary must not save data")
        }
        return (GradeTrackerViewModel(repository).uiState.value.screen as ScreenUiState.Main).summary
    }

    private fun subject(id: String, grade: Double, basket: Boolean = true) = StoredSubject(
        id, id, isInBasket = basket,
        notes = listOf(StoredNote("note-$id", grade, AssessmentWeight.FULL, "", 1L))
    )
}
