package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

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
        assertTrue(viewModel.uiState.value.screen is ScreenUiState.PeriodPicker)
        viewModel.confirmPeriodSelection()

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("Espagnol", screen.optionSubject.title)
        assertEquals(null, screen.optionSubject.subtitle)
        assertTrue(screen.optionSubject.isInBasket)
        assertTrue(screen.userSubjects.isEmpty())
    }

    @Test
    fun completingOnboardingWithCompositeOption_createsCompositeOptionDetail() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)

        viewModel.completeOnboarding(InitialOptionChoice.BIOLOGY_CHEMISTRY)
        viewModel.confirmPeriodSelection()
        val main = viewModel.uiState.value.screen as ScreenUiState.Main

        viewModel.openSubject(main.optionSubject.id)

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertTrue(detail.isCompositeOption)
        assertEquals(listOf("Biologie", "Chimie"), detail.subSubjects.map { it.name })
        assertEquals(AppStrings.French.emptyNotes, detail.officialAverageLabel)
        assertEquals(AppStrings.French.emptyNotes, detail.secondaryAverageLabel)
        assertEquals(AppStrings.French.emptyNotes, detail.pointsLabel)
    }

    @Test
    fun completingOnboardingWithEconomicsLaw_createsCompositeOptionDetail() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)

        viewModel.completeOnboarding(InitialOptionChoice.ECONOMICS_LAW)
        viewModel.confirmPeriodSelection()
        val main = viewModel.uiState.value.screen as ScreenUiState.Main

        viewModel.openSubject(main.optionSubject.id)

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertTrue(detail.isCompositeOption)
        assertEquals("Économie-droit", detail.title)
        assertEquals(listOf("Économie", "Droit"), detail.subSubjects.map { it.name })
    }

    @Test
    fun loadingStateWithDuplicateSubjectIds_normalizesVisibleIds() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    testStoredOptionSubject(InitialOptionChoice.SPANISH),
                    StoredSubject(
                        id = "subject-2",
                        name = "History",
                        schoolYear = SchoolYear.YEAR_1,
                        isInBasket = false,
                        notes = emptyList()
                    ),
                    StoredSubject(
                        id = "subject-2",
                        name = "Geography",
                        schoolYear = SchoolYear.YEAR_1,
                        isInBasket = false,
                        notes = emptyList()
                    )
                ),
                nextSubjectSequence = 3
            )
        )

        val viewModel = GradeTrackerViewModel(repository)

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        val visibleIds = listOf(main.optionSubject.id) + main.userSubjects.map { it.id }
        assertEquals(visibleIds.distinct(), visibleIds)
    }

    @Test
    fun updatingSubjectTargetAverage_persistsAndExposesItInDetail() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.ITALIAN)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.updateSubjectTargetAverage(historyId, "5,25")

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals("5.25", detail.targetAverageInput)
        assertEquals(5.25, repository.load()?.subjects?.first { it.id == historyId }?.targetAverage)
    }

    @Test
    fun updatingSubjectTargetAverage_blankInputClearsSavedTarget() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.MUSIC,
                subjects = listOf(
                    testStoredOptionSubject(InitialOptionChoice.MUSIC),
                    StoredSubject(
                        id = "subject-2",
                        name = "History",
                        isInBasket = false,
                        targetAverage = 5.0
                    )
                ),
                nextSubjectSequence = 3
            )
        )
        val viewModel = GradeTrackerViewModel(repository)

        viewModel.openSubject("subject-2")
        viewModel.updateSubjectTargetAverage("subject-2", "")

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals(null, detail.targetAverageInput)
        assertEquals(null, repository.load()?.subjects?.first { it.id == "subject-2" }?.targetAverage)
    }

    @Test
    fun updatingSubjectTargetAverage_ignoresInvalidInput() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.ITALIAN)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.updateSubjectTargetAverage(historyId, "6.5")

        assertEquals(null, repository.load()?.subjects?.first { it.id == historyId }?.targetAverage)
    }

    @Test
    fun addSubject_rejectsDuplicateName() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.ITALIAN)
        viewModel.confirmPeriodSelection()

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("History")
        viewModel.addSubject()

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("history")
        viewModel.addSubject()

        val screen = viewModel.uiState.value.screen as ScreenUiState.AddSubject
        assertEquals(AppStrings.French.duplicateSubjectName, screen.form.errorMessage)
    }

    @Test
    fun addSubjectAndNote_updatesBranchAverage() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.LATIN)
        viewModel.confirmPeriodSelection()

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("History")
        viewModel.updateAddSubjectBasketFlag(true)
        viewModel.addSubject()

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        val historyId = main.userSubjects.single { it.title == "History" }.id

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
    fun editingSubject_updatesExistingMetadata() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.LATIN)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.showEditSubjectForm(historyId)
        viewModel.updateAddSubjectName("Geography")
        viewModel.updateAddSubjectBasketFlag(true)
        viewModel.updateAddSubjectColor(SubjectColorChoice.GREEN)
        viewModel.updateAddSubjectIcon(SubjectIconChoice.WORLD)
        viewModel.addSubject()

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals("Geography", detail.title)

        viewModel.backFromDetail()
        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        val updatedSubject = main.userSubjects.single { it.title == "Geography" }
        assertEquals("Geography", updatedSubject.title)
        assertTrue(updatedSubject.isInBasket)
        assertEquals(SubjectColorChoice.GREEN, updatedSubject.colorChoice)
        assertEquals(SubjectIconChoice.WORLD, updatedSubject.iconChoice)
    }

    @Test
    fun uncountedSubject_isExcludedFromResultsAndCannotStayInBasket() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("Projet libre")
        viewModel.updateAddSubjectBasketFlag(true)
        viewModel.updateAddSubjectCountedFlag(false)
        viewModel.addSubject()

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        val subject = main.userSubjects.single { it.title == "Projet libre" }
        assertFalse(subject.isCounted)
        assertFalse(subject.isInBasket)

        viewModel.addGradeToSubject(subject.id, "6.0")

        val updatedMain = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(AppStrings.French.notEnoughGrades, updatedMain.summary.basketLabel)
        assertEquals(AppStrings.French.notCalculableYet, updatedMain.summary.promotionStatusLabel)
        val persistedSubject = repository.load()?.subjects?.single { it.id == subject.id }
        assertFalse(persistedSubject?.isCounted ?: true)
        assertFalse(persistedSubject?.isInBasket ?: true)
    }

    @Test
    fun uncountedSubject_detailStaysNeutralEvenWithGrades() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.showAddSubjectForm()
        viewModel.updateAddSubjectName("Projet libre")
        viewModel.updateAddSubjectCountedFlag(false)
        viewModel.addSubject()

        val subjectId = (viewModel.uiState.value.screen as ScreenUiState.Main)
            .userSubjects
            .single { it.title == "Projet libre" }
            .id

        viewModel.addGradeToSubject(subjectId, "3.5")
        viewModel.openSubject(subjectId)

        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertFalse(detail.isCounted)
        assertEquals(AppStrings.French.notCountedLabel, detail.statusLabel)
        assertEquals("", detail.pointsLabel)
        assertEquals("3.5", detail.officialAverageLabel)
        assertEquals("3.50", detail.secondaryAverageLabel)
    }

    @Test
    fun editingGrade_updatesExistingNoteAndAverage() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("5.0")
        viewModel.updateDraftDescription("Essay")
        viewModel.addNote()

        val initialDetail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        val noteId = initialDetail.notes.single().id

        viewModel.requestEditNote(noteId)
        viewModel.updateDraftValue("4.0")
        viewModel.updateDraftType(NoteTypeUi.HALF)
        viewModel.updateDraftDescription("Updated essay")
        viewModel.addNote()

        val updatedDetail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        val updatedNote = updatedDetail.notes.single()
        assertEquals("4.0", updatedDetail.officialAverageLabel)
        assertEquals("4.00", updatedDetail.secondaryAverageLabel)
        assertEquals("Updated essay", updatedNote.description)
        assertEquals(AppStrings.French.noteTypeHalf, updatedNote.noteTypeLabel)
    }

    @Test
    fun changingSemester_keepsBranchesAndIncludesEarlierSemesterGrades() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("5.0")
        viewModel.addNote()
        viewModel.backFromDetail()

        viewModel.openPeriodPicker()
        viewModel.updatePendingSemester(SchoolSemester.SEMESTER_2)
        viewModel.confirmPeriodSelection()

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("5.0", main.userSubjects.single { it.title == "History" }.averageLabel)
    }

    @Test
    fun secondSemesterAverageCombinesGradesFromBothSemesters() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("5.0")
        viewModel.addNote()
        viewModel.backFromDetail()

        viewModel.openPeriodPicker()
        viewModel.updatePendingSemester(SchoolSemester.SEMESTER_2)
        viewModel.confirmPeriodSelection()

        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("3.0")
        viewModel.addNote()
        viewModel.backFromDetail()

        val persistedSubject = repository.load()?.subjects.orEmpty().single { it.name == "History" }
        assertEquals(setOf(SchoolSemester.SEMESTER_1, SchoolSemester.SEMESTER_2), persistedSubject.notes.map { it.semester }.toSet())
        assertEquals(setOf(5.0, 3.0), persistedSubject.notes.map { it.value }.toSet())
        val secondSemesterMain = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("4.0", secondSemesterMain.userSubjects.single { it.title == "History" }.averageLabel)
        assertEquals("4.0", secondSemesterMain.summary.overallAverageLabel)

        viewModel.openPeriodPicker()
        viewModel.updatePendingSemester(SchoolSemester.SEMESTER_1)
        viewModel.confirmPeriodSelection()
        val firstSemesterMain = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals("5.0", firstSemesterMain.userSubjects.single { it.title == "History" }.averageLabel)
    }

    @Test
    fun addingGradeWithAttachments_persistsOnlyAfterSave() {
        val repository = InMemoryGradeTrackerRepository
        val attachmentStorage = FakeGradeAttachmentStorage()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, attachmentStorage)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.showAddGradeSheet()
        viewModel.importDraftAttachments(listOf("content://image-1", "content://image-2"))

        val detailBeforeSave = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals(2, detailBeforeSave.draft.attachments.size)
        assertTrue(repository.load()?.subjects?.first { it.id == historyId }?.notes?.isEmpty() == true)

        viewModel.updateDraftValue("5.0")
        viewModel.addNote()

        val savedNote = repository.load()
            ?.subjects
            ?.first { it.id == historyId }
            ?.notes
            ?.single()
        assertEquals(2, savedNote?.attachments?.size)
    }

    @Test
    fun cancelingGradeEdit_preservesExistingAttachments() {
        val repository = InMemoryGradeTrackerRepository
        val attachmentStorage = FakeGradeAttachmentStorage()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, attachmentStorage)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.showAddGradeSheet()
        viewModel.importDraftAttachments(listOf("content://image-1"))
        viewModel.updateDraftValue("5.0")
        viewModel.addNote()

        val noteId = ((viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail.notes.single().id)
        viewModel.requestEditNote(noteId)
        val storedAttachmentId = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail.draft.attachments.single().id
        viewModel.removeDraftAttachment(storedAttachmentId)
        viewModel.hideAddGradeSheet()

        val reloadedDetail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals(1, reloadedDetail.notes.single().attachments.size)
        assertTrue(attachmentStorage.deletedStoredAttachments.isEmpty())
    }

    @Test
    fun deletingGrade_removesStoredAttachmentFiles() {
        val repository = InMemoryGradeTrackerRepository
        val attachmentStorage = FakeGradeAttachmentStorage()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, attachmentStorage)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.showAddGradeSheet()
        viewModel.importDraftAttachments(listOf("content://image-1"))
        viewModel.updateDraftValue("5.0")
        viewModel.addNote()

        val noteId = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail.notes.single().id
        viewModel.requestDeleteNote(noteId)
        viewModel.confirmDeleteNote()

        assertEquals(1, attachmentStorage.deletedStoredAttachments.size)
    }

    @Test
    fun resetApp_clearsStateAndReturnsToOnboarding() {
        val repository = InMemoryGradeTrackerRepository
        val attachmentStorage = FakeGradeAttachmentStorage()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, attachmentStorage)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.showAddGradeSheet()
        viewModel.importDraftAttachments(listOf("content://image-1"))
        viewModel.updateDraftValue("5.0")
        viewModel.addNote()

        viewModel.openSettings()
        viewModel.resetApp()

        assertTrue(viewModel.uiState.value.screen is ScreenUiState.Onboarding)
        waitUntil { repository.load() == GradeTrackerAppState() }
        assertEquals(GradeTrackerAppState(), repository.load())
        assertTrue(attachmentStorage.didDeleteAllAttachments)
    }

    @Test
    fun defaultResetAppUseCase_returnsEmptyStateAndDeletesAttachments() {
        val attachmentStorage = FakeGradeAttachmentStorage()
        val resetAppUseCase = DefaultResetAppUseCase(attachmentStorage)

        val resetState = resetAppUseCase.reset()

        assertEquals(GradeTrackerAppState(), resetState)
        assertTrue(attachmentStorage.didDeleteAllAttachments)
    }

    @Test
    fun changingOption_canReplaceSimpleWithComposite() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)
        viewModel.confirmPeriodSelection()

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
        viewModel.confirmPeriodSelection()

        val literatureId = viewModel.addSubjectWithBasketFlag("Literature", isInBasket = true)
        val scienceId = viewModel.addSubjectWithBasketFlag("Science", isInBasket = true)
        val projectsId = viewModel.addSubjectWithBasketFlag("Projects", isInBasket = true)
        val optionId = (viewModel.uiState.value.screen as ScreenUiState.Main).optionSubject.id

        viewModel.addGradeToSubject(literatureId, "4.0")
        viewModel.addGradeToSubject(scienceId, "4.0")
        viewModel.addGradeToSubject(projectsId, "4.0")
        viewModel.addGradeToSubject(optionId, "4.0")

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(AppStrings.French.promotionStatusPromoted, screen.summary.promotionStatusLabel)
        assertEquals("16.0 / 16", screen.summary.basketLabel)
        assertEquals("0 / 4", screen.summary.insufficienciesLabel)
    }

    @Test
    fun promotionSetupAssistantShowsMissingBasketBranches() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        val setup = requireNotNull(screen.promotionSetup)

        assertEquals(AppStrings.French.promotionSetupMissingBasket(3), setup.description)
        assertEquals(PromotionSetupAction.ADD_SUBJECT, setup.action)
        assertNull(setup.actionSubjectId)
        assertEquals(AppStrings.French.promotionSetupBasketProgress(0), setup.items.first().supportingText)
        assertFalse(setup.items.first().isComplete)
    }

    @Test
    fun promotionSetupAssistantShowsTooManyBasketBranches() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        listOf("Literature", "Science", "Projects", "History").forEach { name ->
            viewModel.addSubjectWithBasketFlag(name, isInBasket = true)
        }

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        val setup = requireNotNull(screen.promotionSetup)

        assertEquals(AppStrings.French.promotionSetupTooManyBasket(1), setup.description)
        assertEquals(PromotionSetupAction.OPEN_SUBJECT, setup.action)
        assertEquals(screen.userSubjects.single { it.title == "History" }.id, setup.actionSubjectId)
    }

    @Test
    fun promotionSetupAssistantShowsMissingRequiredGrades() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.addSubjectWithBasketFlag("Literature", isInBasket = true)
        viewModel.addSubjectWithBasketFlag("Science", isInBasket = true)
        viewModel.addSubjectWithBasketFlag("Projects", isInBasket = true)

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        val setup = requireNotNull(screen.promotionSetup)

        assertEquals(PromotionSetupAction.OPEN_SUBJECT, setup.action)
        assertEquals(screen.optionSubject.id, setup.actionSubjectId)
        assertEquals(AppStrings.French.promotionSetupAddGradeAction, setup.actionLabel)
        assertTrue(setup.description.contains("Espagnol"))
        assertTrue(setup.description.contains("Literature"))
    }

    @Test
    fun promotionSetupAssistantHiddenWhenPromotionIsCalculable() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        val literatureId = viewModel.addSubjectWithBasketFlag("Literature", isInBasket = true)
        val scienceId = viewModel.addSubjectWithBasketFlag("Science", isInBasket = true)
        val projectsId = viewModel.addSubjectWithBasketFlag("Projects", isInBasket = true)
        val optionId = (viewModel.uiState.value.screen as ScreenUiState.Main).optionSubject.id

        viewModel.addGradeToSubject(literatureId, "4.0")
        viewModel.addGradeToSubject(scienceId, "4.0")
        viewModel.addGradeToSubject(projectsId, "4.0")
        viewModel.addGradeToSubject(optionId, "4.0")

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertNull(screen.promotionSetup)
    }

    @Test
    fun frenchBlockedPromotionUsesNegativeDashboardTone() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        val literatureId = viewModel.addSubjectWithBasketFlag("Literature", isInBasket = true)
        val scienceId = viewModel.addSubjectWithBasketFlag("Science", isInBasket = true)
        val projectsId = viewModel.addSubjectWithBasketFlag("Projects", isInBasket = true)
        val optionId = (viewModel.uiState.value.screen as ScreenUiState.Main).optionSubject.id

        viewModel.addGradeToSubject(literatureId, "3.0")
        viewModel.addGradeToSubject(scienceId, "3.0")
        viewModel.addGradeToSubject(projectsId, "3.0")
        viewModel.addGradeToSubject(optionId, "3.0")

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(AppStrings.French.promotionStatusBlocked, screen.summary.promotionStatusLabel)
        assertEquals(DashboardStatusTone.NEGATIVE, screen.summary.statusTone)
    }

    @Test
    fun unmarkedSubjectsDoNotUnlockBasketPromotion() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        val literatureId = viewModel.addSubjectWithBasketFlag("Literature", isInBasket = false)
        val scienceId = viewModel.addSubjectWithBasketFlag("Science", isInBasket = false)
        val projectsId = viewModel.addSubjectWithBasketFlag("Projects", isInBasket = false)
        val optionId = (viewModel.uiState.value.screen as ScreenUiState.Main).optionSubject.id

        viewModel.addGradeToSubject(literatureId, "5.0")
        viewModel.addGradeToSubject(scienceId, "5.0")
        viewModel.addGradeToSubject(projectsId, "5.0")
        viewModel.addGradeToSubject(optionId, "5.0")

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(AppStrings.French.notCalculableYet, screen.summary.promotionStatusLabel)
        assertEquals("", screen.summary.promotionHeadline)
        assertEquals(AppStrings.French.notEnoughGrades, screen.summary.basketLabel)
    }

    @Test
    fun moreThanThreeManualBasketSubjectsKeepsPromotionConfigurationExplicit() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        listOf("Literature", "Science", "Projects", "History").forEach { name ->
            viewModel.addSubjectWithBasketFlag(name, isInBasket = true)
        }

        val screen = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(AppStrings.French.notCalculableYet, screen.summary.promotionStatusLabel)
        assertEquals(
            AppStrings.French.unlockPromotionTooMany,
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

    @Test
    fun changingLanguage_updatesSettingsAndPersistsChoice() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.openSettings()
        viewModel.changeLanguage(AppLanguage.FRENCH)

        val repositoryState = repository.load()
        val screen = viewModel.uiState.value.screen as ScreenUiState.Settings
        assertEquals(AppLanguage.FRENCH, repositoryState?.language)
        assertEquals(AppLanguage.FRENCH, screen.settings.selectedLanguage)
    }

    @Test
    fun changingThemeMode_updatesSettingsAndPersistsChoice() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.openSettings()
        viewModel.changeThemeMode(AppThemeMode.DARK)

        val repositoryState = repository.load()
        val screen = viewModel.uiState.value.screen as ScreenUiState.Settings
        assertEquals(AppThemeMode.DARK, repositoryState?.themeMode)
        assertEquals(AppThemeMode.DARK, screen.settings.selectedThemeMode)
    }

    @Test
    fun preparingBackupImport_showsConfirmationWithValidatedBackup() {
        val repository = InMemoryGradeTrackerRepository
        val backupCoordinator = FakeBackupCoordinator()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, backupCoordinator = backupCoordinator)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.openSettings()
        viewModel.prepareBackupImport("content://backup")
        waitUntil { (viewModel.uiState.value.screen as ScreenUiState.Settings).settings.pendingImportDisplayName != null }

        val screen = viewModel.uiState.value.screen as ScreenUiState.Settings
        assertEquals("swissgrades-backup-validated.sgb", screen.settings.pendingImportDisplayName)
    }

    @Test
    fun dismissingPreparedBackupImport_discardsPendingBackup() {
        val repository = InMemoryGradeTrackerRepository
        val backupCoordinator = FakeBackupCoordinator()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, backupCoordinator = backupCoordinator)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.openSettings()
        viewModel.prepareBackupImport("content://backup")
        waitUntil { (viewModel.uiState.value.screen as ScreenUiState.Settings).settings.pendingImportDisplayName != null }
        viewModel.dismissPendingBackupImport()

        val screen = viewModel.uiState.value.screen as ScreenUiState.Settings
        assertEquals(null, screen.settings.pendingImportDisplayName)
        assertEquals(1, backupCoordinator.discardedImports.size)
    }

    @Test
    fun confirmingPreparedBackupImport_replacesPersistedAppState() {
        val repository = InMemoryGradeTrackerRepository
        val backupCoordinator = FakeBackupCoordinator(
            importedState = GradeTrackerAppState(
                selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                subjects = listOf(testStoredOptionSubject(InitialOptionChoice.BIOLOGY_CHEMISTRY)),
                nextSubjectSequence = 2,
                nextNoteSequence = 1,
                language = AppLanguage.ENGLISH,
                themeMode = AppThemeMode.DARK
            )
        )
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, backupCoordinator = backupCoordinator)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.openSettings()
        viewModel.prepareBackupImport("content://backup")
        waitUntil { (viewModel.uiState.value.screen as ScreenUiState.Settings).settings.pendingImportDisplayName != null }
        viewModel.confirmBackupImport()
        waitUntil {
            val screen = viewModel.uiState.value.screen
            screen is ScreenUiState.Settings && screen.settings.backupMessage == AppStrings.English.backupImportSuccess
        }

        val repositoryState = repository.load()
        val screen = viewModel.uiState.value.screen as ScreenUiState.Settings
        assertEquals(InitialOptionChoice.BIOLOGY_CHEMISTRY, repositoryState?.selectedOption)
        assertEquals(AppThemeMode.DARK, repositoryState?.themeMode)
        assertEquals(AppLanguage.ENGLISH, screen.settings.selectedLanguage)
        assertEquals(AppStrings.English.backupImportSuccess, screen.settings.backupMessage)
    }

    @Test
    fun changingLanguage_updatesVisibleCopyAcrossScreens() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        viewModel.openSettings()
        viewModel.changeLanguage(AppLanguage.FRENCH)
        viewModel.closeSettings()

        val main = viewModel.uiState.value.screen as ScreenUiState.Main
        assertEquals(AppStrings.French.notCalculableYet, main.summary.promotionStatusLabel)
        assertEquals(AppStrings.French.notEnoughGrades, main.summary.basketLabel)

        viewModel.openSubject(main.optionSubject.id)
        val detail = (viewModel.uiState.value.screen as ScreenUiState.BranchDetail).detail
        assertEquals(AppStrings.French.emptyNotes, detail.officialAverageLabel)
        assertEquals(AppStrings.French.compositeAverage.takeIf { detail.isCompositeOption } ?: AppStrings.French.rawAverage, detail.secondaryAverageTitle)
    }

    @Test
    fun plusPointsImport_replacesOnlyTargetSemester() {
        val repository = InMemoryGradeTrackerRepository
        val plusPointsCoordinator = FakePlusPointsImportCoordinator(
            importedState = GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    testStoredOptionSubject(InitialOptionChoice.SPANISH),
                    StoredSubject(
                        id = "subject-2",
                        name = "History",
                        isCounted = true,
                        isInBasket = false,
                        notes = listOf(
                            StoredNote(
                                id = "note-10",
                                value = 4.0,
                                weight = AssessmentWeight.FULL,
                                description = "Imported S2",
                                createdAtEpochMillis = 1_000L,
                                semester = SchoolSemester.SEMESTER_2
                            )
                        )
                    )
                ),
                nextSubjectSequence = 3,
                nextNoteSequence = 11,
                selectedSemester = SchoolSemester.SEMESTER_2
            ),
            sourceSemester = SchoolSemester.SEMESTER_2
        )
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, plusPointsImportCoordinator = plusPointsCoordinator)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)
        viewModel.confirmPeriodSelection()

        val historyId = viewModel.addSubjectWithBasketFlag("History", isInBasket = false)
        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("5.0")
        viewModel.addNote()
        viewModel.backFromDetail()
        viewModel.openPeriodPicker()
        viewModel.updatePendingSemester(SchoolSemester.SEMESTER_2)
        viewModel.confirmPeriodSelection()
        viewModel.openSubject(historyId)
        viewModel.updateDraftValue("3.0")
        viewModel.addNote()
        viewModel.backFromDetail()

        viewModel.openSettings()
        viewModel.preparePlusPointsImport("content://pluspoints")
        waitUntil { (viewModel.uiState.value.screen as ScreenUiState.Settings).settings.pendingPlusPointsImportDisplayName != null }
        viewModel.confirmPlusPointsImport()
        waitUntil {
            val screen = viewModel.uiState.value.screen
            screen is ScreenUiState.Settings && screen.settings.backupMessage == AppStrings.French.plusPointsImportSuccess
        }

        val persistedSubject = repository.load()?.subjects.orEmpty().single { it.id == historyId }
        val semester1Notes = persistedSubject.notes.filter { it.semester == SchoolSemester.SEMESTER_1 }
        val semester2Notes = persistedSubject.notes.filter { it.semester == SchoolSemester.SEMESTER_2 }
        assertEquals(listOf(""), semester1Notes.map { it.description })
        assertEquals(listOf("Imported S2"), semester2Notes.map { it.description })
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

    private class FakeGradeAttachmentStorage : GradeAttachmentStorage {
        private var nextSequence = 1
        val deletedStoredAttachments = mutableListOf<StoredAttachment>()
        var didDeleteAllAttachments = false

        override fun stageImportedAttachment(sourceUriString: String): DraftAttachment {
            val id = "draft-${nextSequence++}"
            return DraftAttachment(id = id, filePath = "/tmp/$id.jpg", isPersisted = false)
        }

        override fun createCameraCaptureRequest(): PendingCameraCaptureRequest {
            return PendingCameraCaptureRequest(
                attachmentId = "camera-${nextSequence++}",
                outputUriString = "content://camera",
                filePath = "/tmp/camera.jpg"
            )
        }

        override fun finalizeCameraCapture(
            request: PendingCameraCaptureRequest,
            success: Boolean
        ): DraftAttachment? {
            return if (success) {
                DraftAttachment(request.attachmentId, request.filePath, isPersisted = false)
            } else {
                null
            }
        }

        override fun commitAttachments(noteId: String, attachments: List<DraftAttachment>): List<StoredAttachment> {
            return attachments.map { StoredAttachment(id = it.id, filePath = "/notes/$noteId/${it.id}.jpg") }
        }

        override fun discardNewAttachments(attachments: List<DraftAttachment>) = Unit

        override fun deleteStoredAttachments(attachments: List<StoredAttachment>) {
            deletedStoredAttachments += attachments
        }

        override fun deleteAllAttachments() {
            didDeleteAllAttachments = true
        }
    }

    private class FakeBackupCoordinator(
        private val importedState: GradeTrackerAppState = GradeTrackerAppState(
            selectedOption = InitialOptionChoice.SPANISH,
            subjects = listOf(testStoredOptionSubject(InitialOptionChoice.SPANISH)),
            nextSubjectSequence = 2,
            nextNoteSequence = 1
        )
    ) : AppBackupCoordinator {
        val discardedImports = mutableListOf<PreparedBackupImport>()

        override fun suggestedBackupFileName(now: Date): String = "swissgrades-backup-2026-05-21.sgb"

        override fun exportBackup(state: GradeTrackerAppState, destinationUriString: String) = Unit

        override fun prepareImport(sourceUriString: String): PreparedBackupImport {
            return PreparedBackupImport(
                displayName = "swissgrades-backup-validated.sgb",
                workingDirectoryPath = "/tmp/prepared-import",
                importedState = importedState
            )
        }

        override fun applyPreparedImport(preparedBackupImport: PreparedBackupImport): GradeTrackerAppState {
            return importedState
        }

        override fun discardPreparedImport(preparedBackupImport: PreparedBackupImport) {
            discardedImports += preparedBackupImport
        }
    }

    private class FakePlusPointsImportCoordinator(
        private val importedState: GradeTrackerAppState,
        private val sourceSemester: SchoolSemester?
    ) : PlusPointsImportCoordinator {
        override fun prepareImport(sourceUriString: String): PreparedPlusPointsImport {
            return PreparedPlusPointsImport(
                displayName = "pluspoints-export.PlusPointsExport",
                importedState = importedState,
                sourceSemester = sourceSemester
            )
        }

        override fun discardPreparedImport(preparedImport: PreparedPlusPointsImport) = Unit
    }

    private fun waitUntil(timeoutMillis: Long = 2_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(10)
        }
        check(condition())
    }

}

private fun testStoredOptionSubject(choice: InitialOptionChoice): StoredSubject {
    return StoredSubject(
        id = "subject-1",
        name = choice.label,
        isInBasket = true,
        isOptionSubject = true,
        optionChoice = choice,
        notes = emptyList(),
        subSubjects = choice.compositeSubSubjectNames.mapIndexed { index, name ->
            StoredSubSubject(
                id = "option-subject-${index + 1}",
                name = name,
                notes = emptyList()
            )
        }
    )
}
