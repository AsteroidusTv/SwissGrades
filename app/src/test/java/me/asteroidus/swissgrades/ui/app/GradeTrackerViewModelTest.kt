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
        assertEquals("Spanish", screen.optionSubject.title)
        assertEquals(null, screen.optionSubject.subtitle)
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
        assertEquals(EMPTY_NOTES_MESSAGE, detail.officialAverageLabel)
        assertEquals(EMPTY_NOTES_MESSAGE, detail.secondaryAverageLabel)
        assertEquals(EMPTY_NOTES_MESSAGE, detail.pointsLabel)
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

        val screen = viewModel.uiState.value.screen as ScreenUiState.AddSubject
        assertEquals(DUPLICATE_SUBJECT_NAME_MESSAGE, screen.form.errorMessage)
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
        assertEquals("5.00", detail.secondaryAverageLabel)
        assertEquals("5.0", detail.officialAverageLabel)
        assertEquals("+1.0", detail.pointsLabel)
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
        assertEquals("PYAM", main.optionSubject.title)
        assertEquals(null, main.optionSubject.subtitle)
        assertTrue(main.optionSubject.isCompositeOption)
        assertFalse(main.userSubjects.any { it.title == "PYAM" })
    }

    @Test
    fun manualBasketSubjectsUnlockPromotionWithoutOfficialSubjectNames() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)

        val literatureId = viewModel.addSubjectWithBasketFlag("Literature", isInBasket = true)
        val scienceId = viewModel.addSubjectWithBasketFlag("Science", isInBasket = true)
        val projectsId = viewModel.addSubjectWithBasketFlag("Projects", isInBasket = true)
        val optionId = (viewModel.uiState.value.screen as ScreenUiState.Main).optionSubject.id

        viewModel.addGradeToSubject(literatureId, "4.0")
        viewModel.addGradeToSubject(scienceId, "4.0")
        viewModel.addGradeToSubject(projectsId, "4.0")
        viewModel.addGradeToSubject(optionId, "4.0")

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("Promoted", screen.summary.promotionStatusLabel)
        assertEquals("16.0 / 16", screen.summary.basketLabel)
        assertEquals("0 / 4", screen.summary.insufficienciesLabel)
    }

    @Test
    fun unmarkedSubjectsDoNotUnlockBasketPromotion() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)

        val literatureId = viewModel.addSubjectWithBasketFlag("Literature", isInBasket = false)
        val scienceId = viewModel.addSubjectWithBasketFlag("Science", isInBasket = false)
        val projectsId = viewModel.addSubjectWithBasketFlag("Projects", isInBasket = false)
        val optionId = (viewModel.uiState.value.screen as ScreenUiState.Main).optionSubject.id

        viewModel.addGradeToSubject(literatureId, "5.0")
        viewModel.addGradeToSubject(scienceId, "5.0")
        viewModel.addGradeToSubject(projectsId, "5.0")
        viewModel.addGradeToSubject(optionId, "5.0")

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("Not calculable yet", screen.summary.promotionStatusLabel)
        assertEquals("", screen.summary.promotionHeadline)
        assertEquals("Not enough grades", screen.summary.basketLabel)
    }

    @Test
    fun moreThanThreeManualBasketSubjectsKeepsPromotionConfigurationExplicit() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)

        listOf("Literature", "Science", "Projects", "History").forEach { name ->
            viewModel.addSubjectWithBasketFlag(name, isInBasket = true)
        }

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("Not calculable yet", screen.summary.promotionStatusLabel)
        assertEquals(
            "Keep exactly three non-option subjects in the basket to unlock promotion status.",
            screen.summary.promotionHeadline
        )
    }

    @Test
    fun restoredStateWithSelectedOptionButMissingOptionSubjectIsRepairedSafely() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                subjects = emptyList()
            )
        )

        val viewModel = GradeTrackerViewModel(repository)

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("BICH", screen.optionSubject.title)
        assertEquals(null, screen.optionSubject.subtitle)
        assertTrue(screen.optionSubject.isCompositeOption)
    }

    private fun GradeTrackerViewModel.addSubjectWithBasketFlag(name: String, isInBasket: Boolean): String {
        showAddSubjectForm()
        updateAddSubjectName(name)
        updateAddSubjectBasketFlag(isInBasket)
        addSubject()

        val screen = uiState.value.screen as ScreenUiState.Main
        return screen.userSubjects.single { it.title == name }.id
    }

    private fun GradeTrackerViewModel.addGradeToSubject(subjectId: String, value: String) {
        openSubject(subjectId)
        updateDraftValue(value)
        addNote()
        backFromDetail()
    }
}
