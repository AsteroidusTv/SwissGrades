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
        assertEquals(AppStrings.English.emptyNotes, detail.officialAverageLabel)
        assertEquals(AppStrings.English.emptyNotes, detail.secondaryAverageLabel)
        assertEquals(AppStrings.English.emptyNotes, detail.pointsLabel)
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
        assertEquals(AppStrings.English.duplicateSubjectName, screen.form.errorMessage)
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
    fun editingSubject_updatesExistingMetadata() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.LATIN)

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
        val updatedSubject = main.userSubjects.single()
        assertEquals("Geography", updatedSubject.title)
        assertTrue(updatedSubject.isInBasket)
        assertEquals(SubjectColorChoice.GREEN, updatedSubject.colorChoice)
        assertEquals(SubjectIconChoice.WORLD, updatedSubject.iconChoice)
    }

    @Test
    fun editingGrade_updatesExistingNoteAndAverage() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)

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
        assertEquals(AppStrings.English.noteTypeHalf, updatedNote.noteTypeLabel)
    }

    @Test
    fun addingGradeWithAttachments_persistsOnlyAfterSave() {
        val repository = InMemoryGradeTrackerRepository
        val attachmentStorage = FakeGradeAttachmentStorage()
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository, attachmentStorage)
        viewModel.completeOnboarding(InitialOptionChoice.MUSIC)

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

    @Test
    fun changingLanguage_updatesSettingsAndPersistsChoice() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)

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

        viewModel.openSettings()
        viewModel.changeThemeMode(AppThemeMode.DARK)

        val repositoryState = repository.load()
        val screen = viewModel.uiState.value.screen as ScreenUiState.Settings
        assertEquals(AppThemeMode.DARK, repositoryState?.themeMode)
        assertEquals(AppThemeMode.DARK, screen.settings.selectedThemeMode)
    }

    @Test
    fun changingLanguage_updatesVisibleCopyAcrossScreens() {
        val repository = InMemoryGradeTrackerRepository
        repository.save(GradeTrackerAppState())
        val viewModel = GradeTrackerViewModel(repository)
        viewModel.completeOnboarding(InitialOptionChoice.SPANISH)

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
    }
}
