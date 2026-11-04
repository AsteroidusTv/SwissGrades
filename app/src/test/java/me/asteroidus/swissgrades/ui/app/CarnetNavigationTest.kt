package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.*
import org.junit.Test

class CarnetNavigationTest {
    @Test
    fun resettingAndConfiguringAgainReturnsToSubjects() {
        InMemoryGradeTrackerRepository.save(GradeTrackerAppState(selectedOption = InitialOptionChoice.SPANISH))
        val viewModel = GradeTrackerViewModel(InMemoryGradeTrackerRepository)
        viewModel.openMainSection(MainSection.PROMOTION)
        viewModel.openSettings()
        viewModel.resetApp()
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()
        assertEquals(MainSection.SUBJECTS, (viewModel.uiState.value.screen as ScreenUiState.Main).selectedSection)
    }

    @Test
    fun selectingTabsAndReturningFromDetailDoesNotWriteAcademicData() {
        val repository = RecordingRepository()
        val viewModel = GradeTrackerViewModel(repository)
        val initial = repository.state
        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(MainSection.SUBJECTS, main.selectedSection)
        viewModel.openMainSection(MainSection.PROMOTION)
        viewModel.openSubject(main.optionSubject.id)
        viewModel.backFromDetail()
        assertEquals(MainSection.PROMOTION, (viewModel.uiState.value.screen as ScreenUiState.Main).selectedSection)
        viewModel.openSettings()
        viewModel.openMainSection(MainSection.SUBJECTS)
        assertEquals(MainSection.SUBJECTS, (viewModel.uiState.value.screen as ScreenUiState.Main).selectedSection)
        assertEquals(initial, repository.state)
        assertEquals(0, repository.saveCount)
    }

    @Test
    fun quickGradeEntryOpensAnUnsavedDraftAndInvalidSubjectIsIgnored() {
        val repository = RecordingRepository()
        val viewModel = GradeTrackerViewModel(repository)
        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        viewModel.openGradeEditor("missing")
        assertTrue(viewModel.uiState.value.screen is ScreenUiState.Main)
        viewModel.openGradeEditor(main.optionSubject.id)
        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertTrue(detail.isAddGradeSheetVisible)
        assertEquals(SchoolSemester.SEMESTER_2, detail.draft.selectedSemester)
        assertEquals("", detail.draft.valueInput)
        viewModel.backFromDetail()
        assertFalse((viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail.isAddGradeSheetVisible)
        assertEquals(0, repository.saveCount)
    }

    @Test
    fun compositeQuickEntryWaitsForExplicitSubSubjectSelection() {
        val repository = RecordingRepository(InitialOptionChoice.BIOLOGY_CHEMISTRY)
        val viewModel = GradeTrackerViewModel(repository)
        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        viewModel.openGradeEditor(main.optionSubject.id)
        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertTrue(detail.isCompositeOption)
        assertFalse(detail.isAddGradeSheetVisible)
        assertEquals(0, repository.saveCount)
    }

    private class RecordingRepository(option: InitialOptionChoice = InitialOptionChoice.SPANISH) : GradeTrackerRepository {
        var state = GradeTrackerAppState(selectedOption = option, selectedSemester = SchoolSemester.SEMESTER_2)
        var saveCount = 0
        override fun load() = state
        override fun save(state: GradeTrackerAppState) { this.state = state; saveCount++ }
    }
}
