package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeTrackerViewModelTest {

    @Test
    fun firstLaunch_startsWithOnboarding() {
        val viewModel = GradeTrackerViewModel(repository = InMemoryGradeTrackerRepository.also { it.save(GradeTrackerAppState()) })

        assertTrue(viewModel.uiState.value.screen is ScreenUiState.Onboarding)
    }

    @Test
    fun completingOnboardingWithSimpleOption_createsVisibleOptionBranch() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)

        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("Option", screen.optionSubject.title)
        assertEquals("Spanish", screen.optionSubject.subtitle)
        assertTrue(screen.optionSubject.isInBasket)
    }

    @Test
    fun completingOnboardingWithCompositeOption_createsCompositeOptionDetail() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)

        viewModel.completeOnboarding(InitialOptionChoice.BIOLOGY_CHEMISTRY)
        val main = viewModel.uiState.value.screen as ScreenUiState.Main

        viewModel.openSubject(main.optionSubject.id)

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertTrue(detail.isCompositeOption)
        assertEquals(listOf("Biology", "Chemistry"), detail.subSubjects.map { it.name })
        assertTrue(detail.metrics.all { it.value == EMPTY_NOTES_MESSAGE })
    }

    @Test
    fun addSubject_rejectsDuplicateName() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.ITALIAN)

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("History")
        viewModel.addSubject()

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("history")
        viewModel.addSubject()

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(DUPLICATE_SUBJECT_NAME_MESSAGE, screen.addSubjectForm.errorMessage)
    }

    @Test
    fun addSubjectAndNote_updatesBranchAverage() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.LATIN)

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("History")
        viewModel.updateAddSubjectBasketFlag(true)
        viewModel.addSubject()

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        val historyId = main.userSubjects.single().id

        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("5.0")
        viewModel.updateDraftDescription("Essay")
        viewModel.addNote()

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals("5.00", detail.metrics.first { it.label == "Raw average" }.value)
        assertEquals("5.0", detail.metrics.first { it.label == "Official rounded average" }.value)
        assertEquals("1.0", detail.metrics.first { it.label == "Promotion points" }.value)
        assertEquals("Essay", detail.notes.single().description)
    }

    @Test
    fun changingOption_canReplaceSimpleWithComposite() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)

        viewModel.openSettings()
        viewModel.changeOption(InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH)

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("Physics and Applications of Mathematics", main.optionSubject.subtitle)
        assertTrue(main.optionSubject.isCompositeOption)
        assertFalse(main.userSubjects.any { it.title == "Option" })
    }
}
