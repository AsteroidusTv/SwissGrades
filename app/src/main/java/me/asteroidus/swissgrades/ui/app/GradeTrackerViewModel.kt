package me.asteroidus.swissgrades.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import me.asteroidus.swissgrades.domain.GradeCalculator
import me.asteroidus.swissgrades.domain.PromotionEvaluator
import me.asteroidus.swissgrades.domain.PromotionPresentationMapper
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.OptionType
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationInput
import me.asteroidus.swissgrades.domain.model.PromotionPresentation
import me.asteroidus.swissgrades.domain.model.PromotionRoleAssignment
import me.asteroidus.swissgrades.domain.model.SubSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class SubjectComputedMetrics(
    val average: Double?,
    val points: Double?
)

private const val MAX_ATTACHMENTS_PER_GRADE = 5

@OptIn(ExperimentalCoroutinesApi::class)
class GradeTrackerViewModel(
    private val repository: GradeTrackerRepository,
    private val attachmentStorage: GradeAttachmentStorage = NoOpGradeAttachmentStorage,
    private val backupCoordinator: AppBackupCoordinator = NoOpAppBackupCoordinator,
    private val plusPointsImportCoordinator: PlusPointsImportCoordinator = NoOpPlusPointsImportCoordinator
) : ViewModel() {
    private val saveDispatcher = Dispatchers.IO.limitedParallelism(1)
    private var state: GradeTrackerAppState = (repository.load() ?: GradeTrackerAppState()).withSharedSubjects()
    private var currentScreen: InternalScreen = if (state.isOnboardingCompleted) InternalScreen.Main else InternalScreen.Onboarding
    private var onboardingSelection: InitialOptionChoice? = state.selectedOption

    private val _uiState = MutableStateFlow(createUiState())
    val uiState: StateFlow<GradeTrackerUiState> = _uiState.asStateFlow()

    private val strings: AppStrings
        get() = state.language.strings

    fun selectInitialOption(choice: InitialOptionChoice) {
        if (currentScreen is InternalScreen.Onboarding) {
            onboardingSelection = choice
            publish()
        }
    }

    fun completeOnboarding(choice: InitialOptionChoice) {
        var nextOptionSubjectId = 1
        val subjects = SchoolYear.entries.map { year ->
            createStoredOptionSubject(
                choice = choice,
                schoolYear = year,
                id = "subject-${nextOptionSubjectId++}"
            )
        }
        val nextSubjectSequence = subjects
            .mapNotNull { subject -> subject.id.removePrefix("subject-").toIntOrNull() }
            .maxOrNull()
            ?.plus(1)
            ?: 1
        state = state.copy(
            selectedOption = choice,
            subjects = subjects,
            nextSubjectSequence = nextSubjectSequence
        )
        onboardingSelection = choice
        currentScreen = InternalScreen.PeriodPicker(
            selectedYear = state.selectedYear,
            selectedSemester = state.selectedSemester
        )
        persistAndPublish()
    }

    fun openSettings() {
        state.selectedOption?.let {
            currentScreen = InternalScreen.Settings()
            publish()
        }
    }

    fun openPeriodPicker() {
        currentScreen = InternalScreen.PeriodPicker(
            selectedYear = state.selectedYear,
            selectedSemester = state.selectedSemester
        )
        publish()
    }

    fun closePeriodPicker() {
        if (currentScreen !is InternalScreen.PeriodPicker) return
        currentScreen = InternalScreen.Main
        publish()
    }

    fun updatePendingYear(year: SchoolYear) {
        val screen = currentScreen as? InternalScreen.PeriodPicker ?: return
        screen.selectedYear = year
        publish()
    }

    fun updatePendingSemester(semester: SchoolSemester) {
        val screen = currentScreen as? InternalScreen.PeriodPicker ?: return
        screen.selectedSemester = semester
        publish()
    }

    fun confirmPeriodSelection() {
        val screen = currentScreen as? InternalScreen.PeriodPicker ?: return
        state = state.copy(
            selectedYear = screen.selectedYear,
            selectedSemester = screen.selectedSemester
        )
        currentScreen = InternalScreen.Main
        persistAndPublish()
    }

    fun closeSettings() {
        clearPendingSettingsImport()
        currentScreen = InternalScreen.Main
        publish()
    }

    fun changeLanguage(language: AppLanguage) {
        if (state.language == language) return
        state = state.copy(language = language)
        persistAndPublish()
    }

    fun changeThemeMode(themeMode: AppThemeMode) {
        if (state.themeMode == themeMode) return
        state = state.copy(themeMode = themeMode)
        persistAndPublish()
    }

    fun changeSelectedYear(year: SchoolYear) {
        if (state.selectedYear == year) return
        state = state.copy(selectedYear = year)
        persistAndPublish()
    }

    fun changeOption(choice: InitialOptionChoice) {
        val existingOptions = state.subjects.filter { it.isOptionSubject }
        existingOptions.flatMap { it.allAttachments() }.takeIf { it.isNotEmpty() }?.let(attachmentStorage::deleteStoredAttachments)
        val replacementsByYear = SchoolYear.entries.associateWith { year ->
            val existingOption = existingOptions.firstOrNull { it.schoolYear == year }
            createStoredOptionSubject(
                choice = choice,
                schoolYear = year,
                id = existingOption?.id ?: "subject-${state.nextSubjectSequence + year.ordinal}"
            )
        }
        state = state.copy(
            selectedOption = choice,
            subjects = state.subjects.mapNotNull { subject ->
                if (subject.isOptionSubject) null else subject
            } + SchoolYear.entries.map { replacementsByYear.getValue(it) }
        )
        currentScreen = InternalScreen.Main
        persistAndPublish()
    }

    fun showAddSubjectForm() {
        if (currentScreen !is InternalScreen.Main) return
        currentScreen = InternalScreen.AddSubject()
        publish()
    }

    fun showEditSubjectForm(subjectId: String) {
        val subject = state.subjects.firstOrNull { it.id == subjectId && !it.isOptionSubject } ?: return
        currentScreen = InternalScreen.AddSubject(
            addSubjectForm = AddSubjectFormUiState(
                editingSubjectId = subject.id,
                nameInput = subject.name,
                isCounted = subject.isCounted,
                isInBasket = subject.isInBasket,
                selectedColor = subject.subjectColor,
                selectedIcon = subject.subjectIcon
            ),
            returnToSubjectId = subject.id
        )
        publish()
    }

    fun hideAddSubjectForm() {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.returnToSubjectId?.let(InternalScreen::BranchDetail) ?: InternalScreen.Main
        publish()
    }

    fun updateAddSubjectName(input: String) {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.copy(
            addSubjectForm = screen.addSubjectForm.copy(nameInput = input, errorMessage = null)
        )
        publish()
    }

    fun updateAddSubjectCountedFlag(isCounted: Boolean) {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.copy(
            addSubjectForm = screen.addSubjectForm.copy(
                isCounted = isCounted,
                isInBasket = if (isCounted) screen.addSubjectForm.isInBasket else false
            )
        )
        publish()
    }

    fun updateAddSubjectBasketFlag(isInBasket: Boolean) {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.copy(
            addSubjectForm = screen.addSubjectForm.copy(isInBasket = isInBasket && screen.addSubjectForm.isCounted)
        )
        publish()
    }

    fun updateAddSubjectColor(colorChoice: SubjectColorChoice) {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.copy(
            addSubjectForm = screen.addSubjectForm.copy(selectedColor = colorChoice)
        )
        publish()
    }

    fun updateAddSubjectIcon(iconChoice: SubjectIconChoice) {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.copy(
            addSubjectForm = screen.addSubjectForm.copy(selectedIcon = iconChoice)
        )
        publish()
    }

    fun addSubject() {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        val form = screen.addSubjectForm
        val normalizedName = form.nameInput.trim()
        val error = when {
            normalizedName.isEmpty() -> strings.emptySubjectName
            state.subjects.any {
                it.id != form.editingSubjectId &&
                    it.schoolYear == state.selectedYear &&
                    it.name.equals(normalizedName, ignoreCase = true)
            } -> strings.duplicateSubjectName
            else -> null
        }
        if (error != null) {
            currentScreen = screen.copy(addSubjectForm = form.copy(errorMessage = error))
            publish()
            return
        }

        val editingSubjectId = form.editingSubjectId
        if (editingSubjectId == null) {
            val subject = StoredSubject(
                id = "subject-${state.nextSubjectSequence}",
                name = normalizedName,
                schoolYear = state.selectedYear,
                isCounted = form.isCounted,
                isInBasket = form.isInBasket,
                subjectColor = form.selectedColor,
                subjectIcon = form.selectedIcon
            )
            state = state.copy(
                subjects = state.subjects + subject,
                nextSubjectSequence = state.nextSubjectSequence + 1
            )
        } else {
            state = state.copy(
                subjects = state.subjects.map { subject ->
                    if (subject.id == editingSubjectId) {
                        subject.copy(
                            name = normalizedName,
                            isCounted = form.isCounted,
                            isInBasket = form.isInBasket,
                            subjectColor = form.selectedColor,
                            subjectIcon = form.selectedIcon
                        )
                    } else {
                        subject
                    }
                }
            )
        }
        currentScreen = screen.returnToSubjectId?.let(InternalScreen::BranchDetail) ?: InternalScreen.Main
        persistAndPublish()
    }

    fun requestEditNote(noteId: String) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val target = findStoredNoteTarget(screen.subjectId, noteId) ?: return
        screen.selectedSubSubjectId = target.subSubjectId ?: screen.selectedSubSubjectId
        screen.draft = NoteDraftUiState(
            valueInput = formatOneOrTwoDecimals(target.note.value),
            selectedType = target.note.weight.toNoteTypeUi(),
            selectedSemester = target.note.semester,
            descriptionInput = target.note.description,
            editingNoteId = target.note.id,
            attachments = target.note.attachments.map {
                DraftAttachmentUiState(
                    id = it.id,
                    filePath = it.filePath,
                    isPersisted = true
                )
            }
        )
        screen.isAddGradeSheetVisible = true
        publish()
    }

    fun deleteSubject(subjectId: String) {
        val subjectToDelete = state.subjects.firstOrNull { it.id == subjectId } ?: return
        if (subjectToDelete.isOptionSubject) return
        subjectToDelete.allAttachments().takeIf { it.isNotEmpty() }?.let(attachmentStorage::deleteStoredAttachments)

        state = state.copy(
            subjects = state.subjects.filterNot { it.id == subjectId }
        )
        currentScreen = InternalScreen.Main
        persistAndPublish()
    }

    fun exportBackup(destinationUriString: String) {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        val stateSnapshot = state
        screen.isBackupInProgress = true
        screen.backupMessage = null
        publish()

        viewModelScope.launch(saveDispatcher) {
            runCatching {
                backupCoordinator.exportBackup(stateSnapshot, destinationUriString)
            }.onSuccess {
                if (currentScreen === screen) {
                    screen.backupMessage = strings.backupExportSuccess
                    screen.backupMessageTone = DashboardStatusTone.POSITIVE
                }
            }.onFailure {
                if (currentScreen === screen) {
                    screen.backupMessage = strings.backupExportFailure
                    screen.backupMessageTone = DashboardStatusTone.NEGATIVE
                }
            }
            if (currentScreen === screen) {
                screen.isBackupInProgress = false
            }
            publish()
        }
    }

    fun preparePlusPointsImport(sourceUriString: String) {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        clearPendingSettingsImport()
        screen.isBackupInProgress = true
        screen.backupMessage = null
        publish()

        viewModelScope.launch(saveDispatcher) {
            runCatching {
                plusPointsImportCoordinator.prepareImport(sourceUriString)
            }.onSuccess { preparedImport ->
                if (currentScreen === screen) {
                    screen.pendingPreparedPlusPointsImport = preparedImport
                    screen.pendingPlusPointsTargetSemester = preparedImport.sourceSemester ?: SchoolSemester.SEMESTER_1
                } else {
                    plusPointsImportCoordinator.discardPreparedImport(preparedImport)
                }
            }.onFailure {
                if (currentScreen === screen) {
                    screen.backupMessage = strings.plusPointsImportFailure
                    screen.backupMessageTone = DashboardStatusTone.NEGATIVE
                }
            }
            if (currentScreen === screen) {
                screen.isBackupInProgress = false
            }
            publish()
        }
    }

    fun prepareBackupImport(sourceUriString: String) {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        clearPendingSettingsImport()
        screen.isBackupInProgress = true
        screen.backupMessage = null
        publish()

        viewModelScope.launch(saveDispatcher) {
            runCatching {
                backupCoordinator.prepareImport(sourceUriString)
            }.onSuccess { preparedImport ->
                if (currentScreen === screen) {
                    screen.pendingPreparedImport = preparedImport
                } else {
                    backupCoordinator.discardPreparedImport(preparedImport)
                }
            }.onFailure {
                if (currentScreen === screen) {
                    screen.backupMessage = strings.backupImportInvalid
                    screen.backupMessageTone = DashboardStatusTone.NEGATIVE
                }
            }
            if (currentScreen === screen) {
                screen.isBackupInProgress = false
            }
            publish()
        }
    }

    fun dismissPendingBackupImport() {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        screen.pendingPreparedImport?.let(backupCoordinator::discardPreparedImport)
        screen.pendingPreparedImport = null
        publish()
    }

    fun confirmBackupImport() {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        val preparedImport = screen.pendingPreparedImport ?: return
        screen.isBackupInProgress = true
        publish()

        viewModelScope.launch(saveDispatcher) {
            runCatching {
                val importedState = backupCoordinator.applyPreparedImport(preparedImport).withSharedSubjects()
                state = importedState
                repository.save(importedState)
                importedState
            }.onSuccess { importedState ->
                val nextSettingsScreen = if (importedState.isOnboardingCompleted) {
                    InternalScreen.Settings(
                        backupMessage = importedState.language.strings.backupImportSuccess,
                        backupMessageTone = DashboardStatusTone.POSITIVE
                    )
                } else {
                    InternalScreen.Onboarding
                }
                currentScreen = nextSettingsScreen
            }.onFailure {
                screen.backupMessage = strings.backupImportFailure
                screen.backupMessageTone = DashboardStatusTone.NEGATIVE
            }

            if (currentScreen is InternalScreen.Settings) {
                val settingsScreen = currentScreen as InternalScreen.Settings
                settingsScreen.pendingPreparedImport = null
                settingsScreen.isBackupInProgress = false
            }
            publish()
        }
    }

    fun dismissPendingPlusPointsImport() {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        screen.pendingPreparedPlusPointsImport?.let(plusPointsImportCoordinator::discardPreparedImport)
        screen.pendingPreparedPlusPointsImport = null
        screen.pendingPlusPointsTargetSemester = null
        publish()
    }

    fun updatePendingPlusPointsTargetSemester(semester: SchoolSemester) {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        if (screen.pendingPreparedPlusPointsImport == null) return
        screen.pendingPlusPointsTargetSemester = semester
        publish()
    }

    fun confirmPlusPointsImport() {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        val preparedImport = screen.pendingPreparedPlusPointsImport ?: return
        val targetSemester = screen.pendingPlusPointsTargetSemester ?: SchoolSemester.SEMESTER_1
        val targetYear = state.selectedYear
        screen.isBackupInProgress = true
        publish()

        viewModelScope.launch(saveDispatcher) {
            runCatching {
                val mergedState = mergePlusPointsImport(
                    currentState = state,
                    importedState = preparedImport.importedState,
                    targetYear = targetYear,
                    targetSemester = targetSemester
                ).withSharedSubjects()
                attachmentsToDeleteForPlusPointsImport(
                    currentState = state,
                    importedState = preparedImport.importedState,
                    targetYear = targetYear,
                    targetSemester = targetSemester
                ).takeIf { it.isNotEmpty() }?.let(attachmentStorage::deleteStoredAttachments)
                state = mergedState
                repository.save(mergedState)
                mergedState
            }.onSuccess {
                currentScreen = InternalScreen.Settings(
                    backupMessage = strings.plusPointsImportSuccess,
                    backupMessageTone = DashboardStatusTone.POSITIVE
                )
            }.onFailure {
                screen.backupMessage = strings.plusPointsImportFailure
                screen.backupMessageTone = DashboardStatusTone.NEGATIVE
            }.also {
                plusPointsImportCoordinator.discardPreparedImport(preparedImport)
            }

            if (currentScreen is InternalScreen.Settings) {
                val settingsScreen = currentScreen as InternalScreen.Settings
                settingsScreen.pendingPreparedPlusPointsImport = null
                settingsScreen.pendingPlusPointsTargetSemester = null
                settingsScreen.isBackupInProgress = false
            }
            publish()
        }
    }

    fun openSubject(subjectId: String) {
        currentScreen = InternalScreen.BranchDetail(
            subjectId = subjectId,
            draft = NoteDraftUiState(
                selectedSemester = state.selectedSemester
            )
        )
        publish()
    }

    fun backFromDetail() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        if (screen.isAddGradeSheetVisible) {
            attachmentStorage.discardNewAttachments(screen.draft.attachments.map { it.toDraftAttachment() })
            screen.isAddGradeSheetVisible = false
            screen.draft = NoteDraftUiState(
                selectedSemester = state.selectedSemester
            )
            publish()
        } else {
            currentScreen = InternalScreen.Main
            publish()
        }
    }

    fun showAddGradeSheet() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.draft = NoteDraftUiState(
            selectedSemester = state.selectedSemester
        )
        screen.isAddGradeSheetVisible = true
        publish()
    }

    fun hideAddGradeSheet() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        attachmentStorage.discardNewAttachments(screen.draft.attachments.map { it.toDraftAttachment() })
        screen.draft = NoteDraftUiState(
            selectedSemester = state.selectedSemester
        )
        screen.isAddGradeSheetVisible = false
        publish()
    }

    fun requestDeleteNote(noteId: String) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val noteTitle = findNoteTitle(screen.subjectId, noteId)
        screen.pendingDeleteNoteId = noteId
        screen.pendingDeleteNoteTitle = noteTitle
        publish()
    }

    fun dismissDeleteNoteDialog() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.pendingDeleteNoteId = null
        screen.pendingDeleteNoteTitle = null
        publish()
    }

    fun confirmDeleteNote() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val noteId = screen.pendingDeleteNoteId ?: return
        state = state.copy(
            subjects = state.subjects.map { subject ->
                if (subject.id != screen.subjectId) {
                    subject
                } else if (subject.subSubjects.isEmpty()) {
                    val noteToDelete = subject.notes.firstOrNull { it.id == noteId }
                    noteToDelete?.let { attachmentStorage.deleteStoredAttachments(it.attachments) }
                    subject.copy(notes = subject.notes.filterNot { it.id == noteId })
                } else {
                    subject.copy(
                        subSubjects = subject.subSubjects.map { subSubject ->
                            val noteToDelete = subSubject.notes.firstOrNull { it.id == noteId }
                            noteToDelete?.let { attachmentStorage.deleteStoredAttachments(it.attachments) }
                            subSubject.copy(notes = subSubject.notes.filterNot { it.id == noteId })
                        }
                    )
                }
            }
        )
        screen.pendingDeleteNoteId = null
        screen.pendingDeleteNoteTitle = null
        persistAndPublish()
    }

    fun updateDraftValue(input: String) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.draft = screen.draft.copy(valueInput = input, errorMessage = validateDraftValue(input))
        publish()
    }

    fun updateDraftType(type: NoteTypeUi) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.draft = screen.draft.copy(selectedType = type)
        publish()
    }

    fun updateDraftDescription(input: String) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.draft = screen.draft.copy(descriptionInput = input)
        publish()
    }

    fun importDraftAttachments(sourceUriStrings: List<String>) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val remainingSlots = MAX_ATTACHMENTS_PER_GRADE - screen.draft.attachments.size
        if (remainingSlots <= 0) {
            screen.draft = screen.draft.copy(attachmentErrorMessage = strings.maxAttachmentsReached(MAX_ATTACHMENTS_PER_GRADE))
            publish()
            return
        }

        val importedAttachments = sourceUriStrings
            .take(remainingSlots)
            .mapNotNull { uriString ->
                attachmentStorage.stageImportedAttachment(uriString)?.toDraftAttachmentUiState()
            }

        val errorMessage = when {
            importedAttachments.isEmpty() -> strings.importAttachmentFailed
            sourceUriStrings.size > remainingSlots -> strings.maxAttachmentsReached(MAX_ATTACHMENTS_PER_GRADE)
            else -> null
        }

        screen.draft = screen.draft.copy(
            attachments = screen.draft.attachments + importedAttachments,
            attachmentErrorMessage = errorMessage
        )
        publish()
    }

    fun prepareCameraCapture(): PendingCameraCaptureRequest? {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return null
        if (screen.draft.attachments.size >= MAX_ATTACHMENTS_PER_GRADE) {
            screen.draft = screen.draft.copy(
                attachmentErrorMessage = strings.maxAttachmentsReached(MAX_ATTACHMENTS_PER_GRADE)
            )
            publish()
            return null
        }
        return attachmentStorage.createCameraCaptureRequest()
    }

    fun completeCameraCapture(request: PendingCameraCaptureRequest, success: Boolean) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val draftAttachment = attachmentStorage.finalizeCameraCapture(request, success)
        if (draftAttachment == null) {
            if (success) {
                screen.draft = screen.draft.copy(attachmentErrorMessage = strings.importAttachmentFailed)
                publish()
            }
            return
        }
        screen.draft = screen.draft.copy(
            attachments = screen.draft.attachments + draftAttachment.toDraftAttachmentUiState(),
            attachmentErrorMessage = null
        )
        publish()
    }

    fun removeDraftAttachment(attachmentId: String) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val target = screen.draft.attachments.firstOrNull { it.id == attachmentId } ?: return
        if (!target.isPersisted) {
            attachmentStorage.discardNewAttachments(listOf(target.toDraftAttachment()))
        }
        screen.draft = screen.draft.copy(
            attachments = screen.draft.attachments.filterNot { it.id == attachmentId },
            attachmentErrorMessage = null
        )
        publish()
    }

    fun selectCompositeSubSubject(subSubjectId: String) {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.selectedSubSubjectId = subSubjectId
        publish()
    }

    fun addNote() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        val draft = screen.draft
        val error = validateDraftValue(draft.valueInput)
        if (error != null) {
            screen.draft = draft.copy(errorMessage = error)
            publish()
            return
        }
        val normalizedValueInput = normalizeGradeInput(draft.valueInput)
        val grade = Grade(
            value = normalizedValueInput.toDouble(),
            weight = draft.selectedType.weight
        )
        val editingNoteId = draft.editingNoteId
        if (editingNoteId == null) {
            val noteId = "note-${state.nextNoteSequence}"
            val note = StoredNote(
                id = noteId,
                value = grade.value,
                weight = grade.weight,
                description = draft.descriptionInput.trim(),
                createdAtEpochMillis = System.currentTimeMillis(),
                semester = draft.selectedSemester,
                attachments = attachmentStorage.commitAttachments(
                    noteId = noteId,
                    attachments = draft.attachments.map { it.toDraftAttachment() }
                )
            )
            state = state.copy(
                subjects = state.subjects.map { subject ->
                    if (subject.id != screen.subjectId) {
                        subject
                    } else if (subject.subSubjects.isEmpty()) {
                        subject.copy(notes = subject.notes + note)
                    } else {
                        val targetSubSubjectId = screen.selectedSubSubjectId ?: subject.subSubjects.first().id
                        subject.copy(
                            subSubjects = subject.subSubjects.map { subSubject ->
                                if (subSubject.id == targetSubSubjectId) {
                                    subSubject.copy(notes = subSubject.notes + note)
                                } else {
                                    subSubject
                                }
                            }
                        )
                    }
                },
                nextNoteSequence = state.nextNoteSequence + 1
            )
        } else {
            val target = findStoredNoteTarget(screen.subjectId, editingNoteId) ?: return
            val removedStoredAttachments = target.note.attachments.filter { storedAttachment ->
                draft.attachments.none { it.id == storedAttachment.id }
            }
            val updatedNote = target.note.copy(
                value = grade.value,
                weight = grade.weight,
                description = draft.descriptionInput.trim(),
                semester = draft.selectedSemester,
                attachments = attachmentStorage.commitAttachments(
                    noteId = editingNoteId,
                    attachments = draft.attachments.map { it.toDraftAttachment() }
                )
            )
            state = state.copy(
                subjects = state.subjects.map { subject ->
                    if (subject.id != screen.subjectId) {
                        subject
                    } else if (target.subSubjectId == null) {
                        subject.copy(
                            notes = subject.notes.map { note ->
                                if (note.id == editingNoteId) updatedNote else note
                            }
                        )
                    } else {
                        subject.copy(
                            subSubjects = subject.subSubjects.map { subSubject ->
                                if (subSubject.id == target.subSubjectId) {
                                    subSubject.copy(
                                        notes = subSubject.notes.map { note ->
                                            if (note.id == editingNoteId) updatedNote else note
                                        }
                                    )
                                } else {
                                    subSubject
                                }
                            }
                        )
                    }
                }
            )
            attachmentStorage.deleteStoredAttachments(removedStoredAttachments)
        }
        screen.draft = NoteDraftUiState(
            selectedSemester = state.selectedSemester
        )
        screen.isAddGradeSheetVisible = false
        persistAndPublish()
    }

    companion object {
        fun factory(
            repository: GradeTrackerRepository,
            attachmentStorage: GradeAttachmentStorage = NoOpGradeAttachmentStorage,
            backupCoordinator: AppBackupCoordinator = NoOpAppBackupCoordinator,
            plusPointsImportCoordinator: PlusPointsImportCoordinator = NoOpPlusPointsImportCoordinator
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GradeTrackerViewModel(
                        repository,
                        attachmentStorage,
                        backupCoordinator,
                        plusPointsImportCoordinator
                    ) as T
                }
            }
        }
    }

    private fun createUiState(): GradeTrackerUiState {
        val screen = when (val target = currentScreen) {
            is InternalScreen.Onboarding -> ScreenUiState.Onboarding(
                selectedOption = onboardingSelection
            )

            is InternalScreen.PeriodPicker -> ScreenUiState.PeriodPicker(
                selectedYear = target.selectedYear,
                selectedSemester = target.selectedSemester
            )

            is InternalScreen.Main -> {
                val currentYearSubjects = state.subjectsForSelectedYear()
                val subjectMetrics = currentYearSubjects.associate { subject ->
                    subject.id to subject.computeMetrics(state.selectedSemester)
                }
                val summary = createDashboardSummary(subjectMetrics)
                val optionStoredSubject = requireNotNull(currentYearSubjects.firstOrNull { it.isOptionSubject })
                val optionSubject = subjectToListItem(
                    optionStoredSubject,
                    requireNotNull(subjectMetrics[optionStoredSubject.id])
                )
                val userSubjects = currentYearSubjects
                    .filterNot { it.isOptionSubject }
                    .map { subject ->
                        subjectToListItem(subject, requireNotNull(subjectMetrics[subject.id]))
                    }
                ScreenUiState.Main(
                    selectedYear = state.selectedYear,
                    selectedSemester = state.selectedSemester,
                    summary = summary,
                    optionSubject = optionSubject,
                    userSubjects = userSubjects
                )
            }

            is InternalScreen.AddSubject -> ScreenUiState.AddSubject(
                form = target.addSubjectForm
            )

            is InternalScreen.BranchDetail -> ScreenUiState.BranchDetail(
                detail = createSubjectDetail(
                    target.subjectId,
                    target.draft,
                    target.selectedSubSubjectId,
                    target.isAddGradeSheetVisible,
                    target.pendingDeleteNoteTitle
                )
            )

            is InternalScreen.Settings -> ScreenUiState.Settings(
                settings = SettingsUiState(
                    selectedOption = requireNotNull(state.selectedOption),
                    selectedSemester = state.selectedSemester,
                    selectedLanguage = state.language,
                    selectedThemeMode = state.themeMode,
                    backupFileNameSuggestion = backupCoordinator.suggestedBackupFileName(),
                    pendingImportDisplayName = target.pendingPreparedImport?.displayName,
                    pendingPlusPointsImportDisplayName = target.pendingPreparedPlusPointsImport?.displayName,
                    pendingPlusPointsTargetSemester = target.pendingPlusPointsTargetSemester,
                    backupMessage = target.backupMessage,
                    backupMessageTone = target.backupMessageTone,
                    isBackupInProgress = target.isBackupInProgress
                )
            )
        }
        return GradeTrackerUiState(
            screen = screen,
            language = state.language,
            themeMode = state.themeMode
        )
    }

    private fun createDashboardSummary(
        subjectMetrics: Map<String, SubjectComputedMetrics>
    ): DashboardSummaryUiState {
        val currentYearSubjects = state.subjectsForSelectedYear()
        val calculableAverages = currentYearSubjects
            .filter { it.isCounted || it.isOptionSubject }
            .mapNotNull { subjectMetrics[it.id]?.average }
        val overallAverage = calculableAverages.takeIf { it.isNotEmpty() }?.average()
        val promotion = buildPromotionPresentation()
        val totalPromotionPoints = state.totalPromotionPoints(subjectMetrics)
        val basketTotal = state.currentBasketTotal(subjectMetrics)
        val insufficiencyCount = state.insufficiencyCount(subjectMetrics)
        return if (promotion != null) {
            DashboardSummaryUiState(
                overallAverageLabel = overallAverage?.let(::formatOneOrTwoDecimals) ?: strings.emptyNotes,
                overallAverageValue = overallAverage,
                promotionStatusLabel = promotion.statusLabel,
                promotionHeadline = promotion.headline,
                isPromotionCalculable = !promotion.basketTotal.valueLabel.equals("Not available", ignoreCase = true),
                promotionPointsLabel = totalPromotionPoints?.let(::formatSignedOneOrTwoDecimals) ?: strings.emptyNotes,
                promotionPointsValue = totalPromotionPoints,
                basketLabel = basketTotal?.let { "${formatOneOrTwoDecimals(it)} / 16" } ?: strings.notEnoughGrades,
                basketValue = basketTotal,
                insufficienciesLabel = "$insufficiencyCount / 4",
                insufficiencyCount = insufficiencyCount,
                statusTone = promotion.statusLabel.toDashboardStatusTone()
            )
        } else {
            DashboardSummaryUiState(
                overallAverageLabel = overallAverage?.let(::formatOneOrTwoDecimals) ?: strings.emptyNotes,
                overallAverageValue = overallAverage,
                promotionStatusLabel = strings.notCalculableYet,
                promotionHeadline = promotionUnavailableHeadline(),
                isPromotionCalculable = false,
                promotionPointsLabel = totalPromotionPoints?.let(::formatSignedOneOrTwoDecimals) ?: strings.emptyNotes,
                promotionPointsValue = totalPromotionPoints,
                basketLabel = basketTotal?.let { "${formatOneOrTwoDecimals(it)} / 16" } ?: strings.notEnoughGrades,
                basketValue = basketTotal,
                insufficienciesLabel = "$insufficiencyCount / 4",
                insufficiencyCount = insufficiencyCount,
                statusTone = DashboardStatusTone.NEUTRAL
            )
        }
    }

    private fun createSubjectDetail(
        subjectId: String,
        draft: NoteDraftUiState,
        selectedSubSubjectId: String?,
        isAddGradeSheetVisible: Boolean,
        pendingDeleteNoteTitle: String?
    ): SubjectDetailUiState {
        val subject = requireNotNull(state.subjects.firstOrNull { it.id == subjectId })
        if (subject.subSubjects.isNotEmpty()) {
            val compositeBranch = subject.toCompositeBranch(state.selectedSemester)
            val firstAverage = GradeCalculator.weightedAverage(compositeBranch.subSubjects[0].grades)
                ?.let(GradeCalculator::roundToHundredth)
            val secondAverage = GradeCalculator.weightedAverage(compositeBranch.subSubjects[1].grades)
                ?.let(GradeCalculator::roundToHundredth)
            val finalAverage = if (firstAverage != null && secondAverage != null) {
                GradeCalculator.roundToHundredth((firstAverage + secondAverage) / 2.0)
            } else {
                null
            }
            val roundedAverage = GradeCalculator.computeCompositeOptionAverage(compositeBranch)
            val promotionPoints = roundedAverage?.let(GradeCalculator::computePromotionPoints)
            val statusLabel = roundedAverage.toBranchStatusLabel(strings)

            return SubjectDetailUiState(
                subjectId = subject.id,
                title = subject.name,
                subtitle = subject.optionChoice?.label,
                isCounted = subject.isCounted,
                isOptionSubject = subject.isOptionSubject,
                isCompositeOption = true,
                officialAverageLabel = roundedAverage?.let(::formatOneOrTwoDecimals) ?: strings.emptyNotes,
                secondaryAverageTitle = strings.compositeAverage,
                secondaryAverageLabel = finalAverage?.let(::formatTwoDecimals) ?: strings.emptyNotes,
                pointsLabel = promotionPoints?.let(::formatSignedOneOrTwoDecimals) ?: strings.emptyNotes,
                statusLabel = statusLabel,
                statusTone = statusLabel.toDetailStatusTone(),
                isAddGradeSheetVisible = isAddGradeSheetVisible,
                pendingDeleteNoteTitle = pendingDeleteNoteTitle,
                subSubjects = subject.subSubjects.map { subSubject ->
                    CompositeSubSubjectDetailUiState(
                        id = subSubject.id,
                        name = subSubject.name,
                        internalAverageLabel = subSubject.toInternalAverageLabel(strings, state.selectedSemester),
                        notes = subSubject.notes
                            .filter { it.isIncludedIn(state.selectedSemester) }
                            .map(::toNoteUiState)
                    )
                },
                notes = emptyList(),
                draft = draft,
                selectedSubSubjectId = selectedSubSubjectId ?: subject.subSubjects.first().id
            )
        }

        val branch = subject.toSimpleBranch(state.selectedSemester)
        val rawAverage = GradeCalculator.weightedAverage(branch.grades)
        val officialAverage = GradeCalculator.computeBranchAverage(branch)
        val isExcludedFromResults = !subject.isCounted && !subject.isOptionSubject
        val points = if (isExcludedFromResults) null else officialAverage?.let(GradeCalculator::computePromotionPoints)
        val statusLabel = if (isExcludedFromResults) {
            strings.notCountedLabel
        } else {
            officialAverage.toBranchStatusLabel(strings)
        }

        return SubjectDetailUiState(
            subjectId = subject.id,
            title = subject.name,
            subtitle = subject.optionChoice?.label,
            isCounted = subject.isCounted,
            isOptionSubject = subject.isOptionSubject,
            notes = subject.notes.filter { it.isIncludedIn(state.selectedSemester) }.map(::toNoteUiState),
            officialAverageLabel = officialAverage?.let(::formatOneOrTwoDecimals) ?: strings.emptyNotes,
            secondaryAverageTitle = strings.rawAverage,
            secondaryAverageLabel = rawAverage?.let(::formatTwoDecimals) ?: strings.emptyNotes,
            pointsLabel = points?.let(::formatSignedOneOrTwoDecimals).orEmpty(),
            statusLabel = statusLabel,
            statusTone = statusLabel.toDetailStatusTone(),
            isAddGradeSheetVisible = isAddGradeSheetVisible,
            pendingDeleteNoteTitle = pendingDeleteNoteTitle,
            draft = draft
        )
    }

    private fun buildPromotionPresentation(): PromotionPresentation? {
        val currentYearSubjects = state.subjectsForSelectedYear()
        val option = currentYearSubjects.firstOrNull { it.isOptionSubject } ?: return null
        val basketSubjects = currentYearSubjects.filter { it.isCounted && it.isInBasket && !it.isOptionSubject }
        if (basketSubjects.size != 3) return null

        val firstBasketSubject = basketSubjects[0]
        val secondBasketSubject = basketSubjects[1]
        val thirdBasketSubject = basketSubjects[2]
        val basketSubjectIds = setOf(
            firstBasketSubject.id,
            secondBasketSubject.id,
            thirdBasketSubject.id,
            option.id
        )

        val assignments = buildList {
            add(PromotionRoleAssignment.German(firstBasketSubject.toSimpleBranch(state.selectedSemester)))
            add(PromotionRoleAssignment.French(secondBasketSubject.toSimpleBranch(state.selectedSemester)))
            add(PromotionRoleAssignment.Math(thirdBasketSubject.toSimpleBranch(state.selectedSemester)))
            add(PromotionRoleAssignment.Option(option.toBranch(state.selectedSemester)))
            currentYearSubjects
                .filter { it.isCounted && !it.isOptionSubject && it.id !in basketSubjectIds }
                .forEach { subject ->
                    add(
                        PromotionRoleAssignment.Additional(
                            branch = subject.toSimpleBranch(state.selectedSemester),
                            isExplicitlyEmpty = subject.notes.none { it.isIncludedIn(state.selectedSemester) }
                        )
                    )
                }
        }
        return PromotionPresentationMapper
            .map(PromotionEvaluator.evaluate(PromotionEvaluationInput.create(assignments)))
            .localized(strings)
    }

    private fun promotionUnavailableHeadline(): String {
        val basketSubjectCount = state.subjectsForSelectedYear()
            .count { it.isCounted && it.isInBasket && !it.isOptionSubject }
        return when {
            basketSubjectCount < 3 -> ""
            basketSubjectCount > 3 -> strings.unlockPromotionTooMany
            else -> strings.unlockPromotionMissingGrades
        }
    }

    private fun subjectToListItem(
        subject: StoredSubject,
        metrics: SubjectComputedMetrics
    ): SubjectListItemUiState {
        val average = metrics.average
        val points = metrics.points.takeIf { subject.isCounted || subject.isOptionSubject }
        return SubjectListItemUiState(
            id = subject.id,
            title = subject.name,
            subtitle = null,
            averageLabel = average?.let(::formatOneOrTwoDecimals) ?: strings.emptyNotes,
            pointsLabel = points?.let(::formatSignedOneOrTwoDecimals) ?: strings.emptyNotes,
            averageValue = average,
            pointsValue = points,
            colorChoice = subject.subjectColor,
            iconChoice = subject.subjectIcon,
            isCounted = subject.isCounted,
            isInBasket = subject.isInBasket,
            isOptionSubject = subject.isOptionSubject,
            isCompositeOption = subject.subSubjects.isNotEmpty()
        )
    }

    private fun validateDraftValue(input: String): String? {
        val normalized = normalizeGradeInput(input)
        if (normalized.isEmpty()) {
            return strings.invalidGradeValue
        }
        val numericValue = normalized.toDoubleOrNull() ?: return strings.invalidGradeValue
        return try {
            Grade(value = numericValue, weight = AssessmentWeight.FULL)
            null
        } catch (_: IllegalArgumentException) {
            strings.invalidGradeValue
        }
    }

    private fun normalizeGradeInput(input: String): String {
        return input.trim().replace(',', '.')
    }

    private fun persistAndPublish() {
        val stateSnapshot = state
        if (repository === InMemoryGradeTrackerRepository) {
            repository.save(stateSnapshot)
        } else {
            viewModelScope.launch(saveDispatcher) {
                repository.save(stateSnapshot)
            }
        }
        publish()
    }

    private fun findNoteTitle(subjectId: String, noteId: String): String {
        val noteDescription = findStoredNoteTarget(subjectId, noteId)?.note?.description
        return noteDescription?.takeIf { it.isNotBlank() } ?: strings.gradeFallbackDescription
    }

    private fun findStoredNoteTarget(subjectId: String, noteId: String): StoredNoteTarget? {
        val subject = state.subjects.firstOrNull { it.id == subjectId } ?: return null
        subject.notes.firstOrNull { it.id == noteId }?.let { note ->
            return StoredNoteTarget(note = note, subSubjectId = null)
        }
        subject.subSubjects.forEach { subSubject ->
            subSubject.notes.firstOrNull { it.id == noteId }?.let { note ->
                return StoredNoteTarget(note = note, subSubjectId = subSubject.id)
            }
        }
        return null
    }

    private fun toNoteUiState(note: StoredNote): NoteUiState {
        return NoteUiState(
            id = note.id,
            numericValue = note.value,
            displayValue = formatOneOrTwoDecimals(note.value),
            noteTypeLabel = strings.noteTypeLabel(note.weight),
            description = note.description,
            dateLabel = note.createdAtEpochMillis.toDateLabel(),
            attachments = note.attachments.map { AttachmentUiState(id = it.id, filePath = it.filePath) }
        )
    }

    private fun publish() {
        _uiState.value = createUiState()
    }

    private fun clearPendingSettingsImport() {
        val screen = currentScreen as? InternalScreen.Settings ?: return
        screen.pendingPreparedImport?.let(backupCoordinator::discardPreparedImport)
        screen.pendingPreparedImport = null
        screen.pendingPreparedPlusPointsImport?.let(plusPointsImportCoordinator::discardPreparedImport)
        screen.pendingPreparedPlusPointsImport = null
        screen.pendingPlusPointsTargetSemester = null
    }
}

private fun GradeTrackerAppState.withSharedSubjects(): GradeTrackerAppState {
    val selectedOption = selectedOption ?: return this
    val sharedSubjects = subjects
        .groupBy { "${it.schoolYear.name}:${annualSubjectKey(it)}" }
        .values
        .map(::mergeSharedSubjects)
    val existingByYear = sharedSubjects.groupBy { it.schoolYear }
    val subjectIdSeed = sharedSubjects
        .mapNotNull { it.id.removePrefix("subject-").toIntOrNull() }
        .maxOrNull()
        ?.plus(1)
        ?: 1
    var nextId = subjectIdSeed

    val normalizedSubjects = buildList {
        SchoolYear.entries.forEach { year ->
            val yearSubjects = existingByYear[year].orEmpty()
            val normalizedOption = yearSubjects.firstOrNull { it.isOptionSubject }?.copy(
                name = selectedOption.label,
                optionChoice = selectedOption,
                schoolYear = year
            ) ?: createStoredOptionSubject(
                choice = selectedOption,
                schoolYear = year,
                id = "subject-${nextId++}"
            )
            add(normalizedOption)
            addAll(yearSubjects.filterNot { it.isOptionSubject })
        }
    }

    return copy(
        subjects = normalizedSubjects,
        nextSubjectSequence = maxOf(nextSubjectSequence, nextId)
    )
}

private fun mergeSharedSubjects(subjects: List<StoredSubject>): StoredSubject {
    val first = subjects.first()
    if (subjects.size == 1) return first
    val subSubjectsByName = subjects
        .flatMap { it.subSubjects }
        .groupBy { it.name.lowercase(Locale.ROOT) }
    return first.copy(
        notes = subjects.flatMap { it.notes },
        subSubjects = subSubjectsByName.values.map { matchingSubSubjects ->
            matchingSubSubjects.first().copy(notes = matchingSubSubjects.flatMap { it.notes })
        }
    )
}

private sealed interface InternalScreen {
    data object Onboarding : InternalScreen
    data object Main : InternalScreen
    data class PeriodPicker(
        var selectedYear: SchoolYear,
        var selectedSemester: SchoolSemester
    ) : InternalScreen
    data class AddSubject(
        val addSubjectForm: AddSubjectFormUiState = AddSubjectFormUiState(),
        val returnToSubjectId: String? = null
    ) : InternalScreen
    data class BranchDetail(
        val subjectId: String,
        var draft: NoteDraftUiState = NoteDraftUiState(),
        var selectedSubSubjectId: String? = null,
        var isAddGradeSheetVisible: Boolean = false,
        var pendingDeleteNoteId: String? = null,
        var pendingDeleteNoteTitle: String? = null
    ) : InternalScreen
    data class Settings(
        var pendingPreparedImport: PreparedBackupImport? = null,
        var pendingPreparedPlusPointsImport: PreparedPlusPointsImport? = null,
        var pendingPlusPointsTargetSemester: SchoolSemester? = null,
        var backupMessage: String? = null,
        var backupMessageTone: DashboardStatusTone = DashboardStatusTone.NEUTRAL,
        var isBackupInProgress: Boolean = false
    ) : InternalScreen
}

private data class StoredNoteTarget(
    val note: StoredNote,
    val subSubjectId: String?
)

private fun DraftAttachmentUiState.toDraftAttachment(): DraftAttachment {
    return DraftAttachment(
        id = id,
        filePath = filePath,
        isPersisted = isPersisted
    )
}

private fun DraftAttachment.toDraftAttachmentUiState(): DraftAttachmentUiState {
    return DraftAttachmentUiState(
        id = id,
        filePath = filePath,
        isPersisted = isPersisted
    )
}

private fun StoredSubject.toBranch(semester: SchoolSemester): Branch {
    return if (subSubjects.isEmpty()) toSimpleBranch(semester) else toCompositeBranch(semester)
}

private fun StoredSubject.toSimpleBranch(semester: SchoolSemester): Branch.Simple {
    return Branch.Simple.create(
        name = name,
        grades = notes.filter { it.isIncludedIn(semester) }.map { it.toGrade() },
        optionType = optionChoice?.optionType
    )
}

private fun StoredSubject.toCompositeBranch(semester: SchoolSemester): Branch.Composite {
    return Branch.Composite.create(
        name = name,
        optionType = requireNotNull(optionChoice?.optionType),
        subSubjects = subSubjects.map { subSubject ->
            SubSubject(
                name = subSubject.name,
                grades = subSubject.notes.filter { it.isIncludedIn(semester) }.map { it.toGrade() }
            )
        }
    )
}

private fun annualSubjectKey(subject: StoredSubject): String {
    return if (subject.isOptionSubject) {
        "option:${subject.optionChoice?.name ?: subject.name.lowercase(Locale.ROOT)}"
    } else {
        "subject:${subject.name.lowercase(Locale.ROOT)}"
    }
}

private fun GradeTrackerAppState.subjectsForSelectedYear(): List<StoredSubject> {
    return subjects.filter { it.schoolYear == selectedYear }
}

private fun GradeTrackerAppState.currentBasketTotal(
    subjectMetrics: Map<String, SubjectComputedMetrics>
): Double? {
    val currentYearSubjects = subjectsForSelectedYear()
    val optionSubject = currentYearSubjects.firstOrNull { it.isOptionSubject } ?: return null
    val basketSubjects = currentYearSubjects.filter { it.isCounted && it.isInBasket && !it.isOptionSubject }
    if (basketSubjects.size != 3) return null

    val averages = listOfNotNull(
        basketSubjects.getOrNull(0)?.let { subjectMetrics[it.id]?.average },
        basketSubjects.getOrNull(1)?.let { subjectMetrics[it.id]?.average },
        basketSubjects.getOrNull(2)?.let { subjectMetrics[it.id]?.average },
        subjectMetrics[optionSubject.id]?.average
    )
    if (averages.size != 4) return null

    return averages.sum()
}

private fun GradeTrackerAppState.totalPromotionPoints(
    subjectMetrics: Map<String, SubjectComputedMetrics>
): Double? {
    val pointValues = subjectsForSelectedYear()
        .filter { it.isCounted || it.isOptionSubject }
        .mapNotNull { subject -> subjectMetrics[subject.id]?.points }
    return pointValues.takeIf { it.isNotEmpty() }?.sum()
}

private fun GradeTrackerAppState.insufficiencyCount(
    subjectMetrics: Map<String, SubjectComputedMetrics>
): Int {
    return subjectsForSelectedYear().count { subject ->
        (subject.isCounted || subject.isOptionSubject) &&
        subjectMetrics[subject.id]?.average?.let { average -> average < 4.0 } == true
    }
}

private fun createStoredOptionSubject(
    choice: InitialOptionChoice,
    schoolYear: SchoolYear,
    id: String
): StoredSubject {
    return StoredSubject(
        id = id,
        name = choice.label,
        schoolYear = schoolYear,
        isCounted = true,
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

private fun StoredSubject.allAttachments(): List<StoredAttachment> {
    return notes.flatMap { it.attachments } + subSubjects.flatMap { subSubject ->
        subSubject.notes.flatMap { it.attachments }
    }
}

private fun StoredSubject.allAttachmentsInSemester(semester: SchoolSemester): List<StoredAttachment> {
    return notes.filter { it.semester == semester }.flatMap { it.attachments } +
        subSubjects.flatMap { subSubject ->
            subSubject.notes.filter { it.semester == semester }.flatMap { it.attachments }
        }
}

private fun StoredSubject.allAttachmentsInPeriod(
    schoolYear: SchoolYear,
    semester: SchoolSemester
): List<StoredAttachment> {
    if (this.schoolYear != schoolYear) return emptyList()
    return allAttachmentsInSemester(semester)
}

private fun StoredSubject.withoutSemesterNotes(semester: SchoolSemester): StoredSubject {
    return copy(
        notes = notes.filterNot { it.semester == semester },
        subSubjects = subSubjects.map { subSubject ->
            subSubject.copy(notes = subSubject.notes.filterNot { it.semester == semester })
        }
    )
}

private fun mergePlusPointsImport(
    currentState: GradeTrackerAppState,
    importedState: GradeTrackerAppState,
    targetYear: SchoolYear,
    targetSemester: SchoolSemester
): GradeTrackerAppState {
    val importedSubjectsByKey = importedState.subjects
        .map { it.reassignPeriod(targetYear, targetSemester) }
        .associateBy(::subjectMergeKey)
    val currentSubjectsByKey = currentState.subjects.associateBy(::subjectMergeKey)
    val mergedSubjects = buildList {
        val orderedKeys = buildList {
            addAll(importedSubjectsByKey.keys)
            currentSubjectsByKey.keys.forEach { key ->
                if (key !in importedSubjectsByKey) add(key)
            }
        }
        orderedKeys.forEach { key ->
            val importedSubject = importedSubjectsByKey[key]
            val existingSubject = currentSubjectsByKey[key]
            val mergedSubject = when {
                importedSubject != null && existingSubject != null -> {
                    existingSubject.withoutSemesterNotes(targetSemester)
                        .overlayImportedSemester(importedSubject, targetSemester)
                }
                importedSubject != null -> {
                    importedSubject
                }
                existingSubject != null -> {
                    existingSubject.withoutSemesterNotes(targetSemester)
                }
                else -> null
            }
            mergedSubject?.let(::add)
        }
    }

    val nextSubjectSequence = mergedSubjects
        .mapNotNull { it.id.removePrefix("subject-").toIntOrNull() }
        .maxOrNull()
        ?.plus(1)
        ?: importedState.nextSubjectSequence.coerceAtLeast(currentState.nextSubjectSequence)
    val nextNoteSequence = mergedSubjects
        .flatMap { subject -> subject.notes + subject.subSubjects.flatMap { it.notes } }
        .mapNotNull { it.id.removePrefix("note-").toIntOrNull() }
        .maxOrNull()
        ?.plus(1)
        ?: importedState.nextNoteSequence.coerceAtLeast(currentState.nextNoteSequence)

    return GradeTrackerAppState(
        selectedOption = importedState.selectedOption,
        subjects = mergedSubjects,
        nextSubjectSequence = nextSubjectSequence,
        nextNoteSequence = nextNoteSequence,
        selectedYear = targetYear,
        selectedSemester = targetSemester,
        language = currentState.language,
        themeMode = currentState.themeMode
    )
}

private fun attachmentsToDeleteForPlusPointsImport(
    currentState: GradeTrackerAppState,
    importedState: GradeTrackerAppState,
    targetYear: SchoolYear,
    targetSemester: SchoolSemester
): List<StoredAttachment> {
    val semesterAttachments = currentState.subjects.flatMap { it.allAttachmentsInPeriod(targetYear, targetSemester) }
    val optionChanged = currentState.selectedOption != null &&
        importedState.selectedOption != null &&
        currentState.selectedOption != importedState.selectedOption
    if (!optionChanged) return semesterAttachments
    val optionAttachments = currentState.subjects
        .firstOrNull { it.isOptionSubject && it.schoolYear == targetYear }
        ?.allAttachments()
        .orEmpty()
    return (semesterAttachments + optionAttachments).distinctBy { it.id to it.filePath }
}

private fun StoredSubject.overlayImportedSemester(
    importedSubject: StoredSubject,
    targetSemester: SchoolSemester
): StoredSubject {
    if (isOptionSubject && importedSubject.isOptionSubject && optionChoice != importedSubject.optionChoice) {
        return importedSubject.reassignSemester(targetSemester)
    }
    return copy(
        name = importedSubject.name,
        isCounted = importedSubject.isCounted,
        isInBasket = importedSubject.isInBasket,
        isOptionSubject = importedSubject.isOptionSubject,
        optionChoice = importedSubject.optionChoice,
        subjectColor = importedSubject.subjectColor,
        subjectIcon = importedSubject.subjectIcon,
        notes = notes + importedSubject.notes.map { it.copy(semester = targetSemester) },
        subSubjects = mergeSubSubjectsForSemester(
            existing = subSubjects,
            imported = importedSubject.subSubjects,
            targetSemester = targetSemester
        )
    )
}

private fun mergeSubSubjectsForSemester(
    existing: List<StoredSubSubject>,
    imported: List<StoredSubSubject>,
    targetSemester: SchoolSemester
): List<StoredSubSubject> {
    val importedByName = imported.associateBy { it.name.lowercase(Locale.ROOT) }
    val existingByName = existing.associateBy { it.name.lowercase(Locale.ROOT) }
    return buildList {
        val orderedKeys = buildList {
            addAll(importedByName.keys)
            existingByName.keys.forEach { key ->
                if (key !in importedByName) add(key)
            }
        }
        orderedKeys.forEachIndexed { index, key ->
            val importedSub = importedByName[key]
            val existingSub = existingByName[key]
            when {
                importedSub != null && existingSub != null -> add(
                    existingSub.copy(
                        name = importedSub.name,
                        notes = existingSub.notes + importedSub.notes.map { it.copy(semester = targetSemester) }
                    )
                )
                importedSub != null -> add(
                    importedSub.copy(
                        id = importedSub.id.ifBlank { "option-subject-${index + 1}" },
                        notes = importedSub.notes.map { it.copy(semester = targetSemester) }
                    )
                )
                existingSub != null -> add(existingSub)
            }
        }
    }
}

private fun StoredSubject.reassignSemester(targetSemester: SchoolSemester): StoredSubject {
    return copy(
        notes = notes.map { it.copy(semester = targetSemester) },
        subSubjects = subSubjects.map { subSubject ->
            subSubject.copy(notes = subSubject.notes.map { it.copy(semester = targetSemester) })
        }
    )
}

private fun StoredSubject.reassignPeriod(
    targetYear: SchoolYear,
    targetSemester: SchoolSemester
): StoredSubject {
    return reassignSemester(targetSemester).copy(schoolYear = targetYear)
}

private fun subjectMergeKey(subject: StoredSubject): String {
    return when {
        subject.isOptionSubject -> "year:${subject.schoolYear.name}:option:${subject.optionChoice?.name ?: subject.name.lowercase(Locale.ROOT)}"
        else -> "year:${subject.schoolYear.name}:subject:${subject.name.lowercase(Locale.ROOT)}"
    }
}

private fun StoredNote.toGrade(): Grade {
    return Grade(value = value, weight = weight)
}

private fun AssessmentWeight.toNoteTypeUi(): NoteTypeUi {
    return when (this) {
        AssessmentWeight.FULL -> NoteTypeUi.FULL
        AssessmentWeight.HALF -> NoteTypeUi.HALF
        AssessmentWeight.QUARTER -> NoteTypeUi.QUARTER
    }
}

private fun StoredSubject.computeMetrics(semester: SchoolSemester): SubjectComputedMetrics {
    val average = when {
        subSubjects.isNotEmpty() -> GradeCalculator.computeCompositeOptionAverage(toCompositeBranch(semester))
        else -> GradeCalculator.computeBranchAverage(toSimpleBranch(semester))
    }
    return SubjectComputedMetrics(
        average = average,
        points = average?.let(GradeCalculator::computePromotionPoints)
    )
}

private fun StoredSubSubject.toInternalAverageLabel(strings: AppStrings, semester: SchoolSemester): String {
    val average = GradeCalculator.weightedAverage(
        notes.filter { it.isIncludedIn(semester) }.map { it.toGrade() }
    )?.let(GradeCalculator::roundToHundredth)
    return average?.let(::formatTwoDecimals) ?: strings.emptyNotes
}

private fun StoredNote.isIncludedIn(selectedSemester: SchoolSemester): Boolean {
    return selectedSemester == SchoolSemester.SEMESTER_2 || semester == SchoolSemester.SEMESTER_1
}

private fun Long.toDateLabel(): String {
    if (this <= 0L) return ""
    return NoteDateFormatter.format(Date(this))
}

private object NoteDateFormatter {
    private val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)

    @Synchronized
    fun format(date: Date): String = formatter.format(date)
}

private fun formatOneOrTwoDecimals(value: Double): String {
    return if (value % 1.0 == 0.0) "%.1f".format(Locale.US, value) else "%.2f".format(Locale.US, value).trimEnd('0')
}

private fun formatTwoDecimals(value: Double): String {
    return "%.2f".format(Locale.US, value)
}

private fun formatSignedOneOrTwoDecimals(value: Double): String {
    val prefix = if (value > 0.0) "+" else ""
    return prefix + formatOneOrTwoDecimals(value)
}

private fun String.toDashboardStatusTone(): DashboardStatusTone {
    return when (this) {
        "Promoted",
        "Promu" -> DashboardStatusTone.POSITIVE
        "Blocked",
        "Bloque" -> DashboardStatusTone.NEGATIVE
        else -> DashboardStatusTone.NEUTRAL
    }
}

private fun Double?.toBranchStatusLabel(strings: AppStrings): String {
    return when {
        this == null -> strings.notEnoughGrades
        this >= 4.0 -> strings.branchPromoted
        else -> strings.branchInsufficient
    }
}

private fun String.toDetailStatusTone(): DashboardStatusTone {
    return when (this) {
        "Promoted",
        "Promu" -> DashboardStatusTone.POSITIVE
        "Insufficient",
        "Insuffisant" -> DashboardStatusTone.NEGATIVE
        "Not counted",
        "Non comptée" -> DashboardStatusTone.NEUTRAL
        else -> DashboardStatusTone.NEUTRAL
    }
}

private fun PromotionPresentation.localized(strings: AppStrings): PromotionPresentation {
    return copy(
        statusLabel = when (statusLabel) {
            "Promoted" -> strings.promotionStatusPromoted
            "Blocked" -> strings.promotionStatusBlocked
            "Incomplete" -> strings.promotionStatusIncomplete
            else -> statusLabel
        },
        headline = when (headline) {
            "Promotion requirements are currently satisfied." -> strings.promotionHeadlinePromoted
            "Promotion requirements are not satisfied." -> strings.promotionHeadlineBlocked
            "Promotion cannot be decided yet because some data is missing." -> strings.promotionHeadlineIncomplete
            else -> headline
        }
    )
}
