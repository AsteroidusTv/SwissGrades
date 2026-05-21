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

enum class NoteTypeUi(val weight: AssessmentWeight) {
    FULL(AssessmentWeight.FULL),
    HALF(AssessmentWeight.HALF),
    QUARTER(AssessmentWeight.QUARTER)
}

data class NoteUiState(
    val id: String,
    val numericValue: Double,
    val displayValue: String,
    val noteTypeLabel: String,
    val description: String,
    val dateLabel: String,
    val attachments: List<AttachmentUiState> = emptyList()
)

data class AttachmentUiState(
    val id: String,
    val filePath: String
)

data class DraftAttachmentUiState(
    val id: String,
    val filePath: String,
    val isPersisted: Boolean
)

data class SubjectListItemUiState(
    val id: String,
    val title: String,
    val subtitle: String?,
    val averageLabel: String,
    val pointsLabel: String,
    val averageValue: Double?,
    val pointsValue: Double?,
    val colorChoice: SubjectColorChoice,
    val iconChoice: SubjectIconChoice,
    val isInBasket: Boolean,
    val isOptionSubject: Boolean,
    val isCompositeOption: Boolean
)

data class NoteDraftUiState(
    val valueInput: String = "",
    val selectedType: NoteTypeUi = NoteTypeUi.FULL,
    val descriptionInput: String = "",
    val errorMessage: String? = null,
    val editingNoteId: String? = null,
    val attachments: List<DraftAttachmentUiState> = emptyList(),
    val attachmentErrorMessage: String? = null
)

data class SubjectDetailUiState(
    val subjectId: String,
    val title: String,
    val subtitle: String?,
    val isOptionSubject: Boolean = false,
    val notes: List<NoteUiState>,
    val isCompositeOption: Boolean = false,
    val subSubjects: List<CompositeSubSubjectDetailUiState> = emptyList(),
    val officialAverageLabel: String = AppStrings.English.emptyNotes,
    val secondaryAverageTitle: String = AppStrings.English.rawAverage,
    val secondaryAverageLabel: String = AppStrings.English.emptyNotes,
    val pointsLabel: String = AppStrings.English.emptyNotes,
    val statusLabel: String = AppStrings.English.emptyNotes,
    val statusTone: DashboardStatusTone = DashboardStatusTone.NEUTRAL,
    val isAddGradeSheetVisible: Boolean = false,
    val pendingDeleteNoteTitle: String? = null,
    val draft: NoteDraftUiState = NoteDraftUiState(),
    val selectedSubSubjectId: String? = null
)

data class CompositeSubSubjectDetailUiState(
    val id: String,
    val name: String,
    val internalAverageLabel: String,
    val notes: List<NoteUiState>
)

data class AddSubjectFormUiState(
    val isVisible: Boolean = false,
    val editingSubjectId: String? = null,
    val nameInput: String = "",
    val isInBasket: Boolean = false,
    val selectedColor: SubjectColorChoice = SubjectColorChoice.BLUE,
    val selectedIcon: SubjectIconChoice = SubjectIconChoice.BOOK,
    val errorMessage: String? = null
)

data class SettingsUiState(
    val selectedOption: InitialOptionChoice,
    val selectedLanguage: AppLanguage,
    val selectedThemeMode: AppThemeMode,
    val backupFileNameSuggestion: String,
    val pendingImportDisplayName: String? = null,
    val backupMessage: String? = null,
    val backupMessageTone: DashboardStatusTone = DashboardStatusTone.NEUTRAL,
    val isBackupInProgress: Boolean = false
)

data class DashboardSummaryUiState(
    val overallAverageLabel: String,
    val overallAverageValue: Double?,
    val promotionStatusLabel: String,
    val promotionHeadline: String,
    val isPromotionCalculable: Boolean,
    val promotionPointsLabel: String,
    val promotionPointsValue: Double?,
    val basketLabel: String,
    val basketValue: Double?,
    val insufficienciesLabel: String,
    val insufficiencyCount: Int,
    val statusTone: DashboardStatusTone
)

enum class DashboardStatusTone {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

sealed interface ScreenUiState {
    data class Onboarding(
        val selectedOption: InitialOptionChoice? = null
    ) : ScreenUiState

    data class Main(
        val summary: DashboardSummaryUiState,
        val optionSubject: SubjectListItemUiState,
        val userSubjects: List<SubjectListItemUiState>
    ) : ScreenUiState

    data class AddSubject(
        val form: AddSubjectFormUiState
    ) : ScreenUiState

    data class BranchDetail(
        val detail: SubjectDetailUiState
    ) : ScreenUiState

    data class Settings(
        val settings: SettingsUiState
    ) : ScreenUiState
}

data class GradeTrackerUiState(
    val screen: ScreenUiState,
    val language: AppLanguage,
    val themeMode: AppThemeMode
)

private data class SubjectComputedMetrics(
    val average: Double?,
    val points: Double?
)

private const val MAX_ATTACHMENTS_PER_GRADE = 5

@OptIn(ExperimentalCoroutinesApi::class)
class GradeTrackerViewModel(
    private val repository: GradeTrackerRepository,
    private val attachmentStorage: GradeAttachmentStorage = NoOpGradeAttachmentStorage,
    private val backupCoordinator: AppBackupCoordinator = NoOpAppBackupCoordinator
) : ViewModel() {
    private val saveDispatcher = Dispatchers.IO.limitedParallelism(1)
    private var state: GradeTrackerAppState = (repository.load() ?: GradeTrackerAppState()).withRequiredOptionSubject()
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
        val optionSubject = createOptionSubject(choice)
        state = state.copy(
            selectedOption = choice,
            subjects = listOf(optionSubject),
            nextSubjectSequence = 2
        )
        onboardingSelection = choice
        currentScreen = InternalScreen.Main
        persistAndPublish()
    }

    fun openSettings() {
        state.selectedOption?.let {
            currentScreen = InternalScreen.Settings()
            publish()
        }
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

    fun changeOption(choice: InitialOptionChoice) {
        val existingOption = state.subjects.firstOrNull { it.isOptionSubject }
        existingOption?.allAttachments()?.takeIf { it.isNotEmpty() }?.let(attachmentStorage::deleteStoredAttachments)
        val replacement = createOptionSubject(choice).copy(
            id = existingOption?.id ?: "subject-1"
        )
        state = state.copy(
            selectedOption = choice,
            subjects = listOf(replacement) + state.subjects.filterNot { it.isOptionSubject }
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
                isVisible = true,
                editingSubjectId = subject.id,
                nameInput = subject.name,
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

    fun updateAddSubjectBasketFlag(isInBasket: Boolean) {
        val screen = currentScreen as? InternalScreen.AddSubject ?: return
        currentScreen = screen.copy(
            addSubjectForm = screen.addSubjectForm.copy(isInBasket = isInBasket)
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
                it.id != form.editingSubjectId && it.name.equals(normalizedName, ignoreCase = true)
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
        ).withRequiredOptionSubject()
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
                val importedState = backupCoordinator.applyPreparedImport(preparedImport).withRequiredOptionSubject()
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

    fun openSubject(subjectId: String) {
        currentScreen = InternalScreen.BranchDetail(subjectId)
        publish()
    }

    fun backFromDetail() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        if (screen.isAddGradeSheetVisible) {
            attachmentStorage.discardNewAttachments(screen.draft.attachments.map { it.toDraftAttachment() })
            screen.isAddGradeSheetVisible = false
            screen.draft = NoteDraftUiState()
            publish()
        } else {
            currentScreen = InternalScreen.Main
            publish()
        }
    }

    fun showAddGradeSheet() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        screen.draft = NoteDraftUiState()
        screen.isAddGradeSheetVisible = true
        publish()
    }

    fun hideAddGradeSheet() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        attachmentStorage.discardNewAttachments(screen.draft.attachments.map { it.toDraftAttachment() })
        screen.draft = NoteDraftUiState()
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
        val grade = Grade(
            value = draft.valueInput.trim().toDouble(),
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
        screen.draft = NoteDraftUiState()
        screen.isAddGradeSheetVisible = false
        persistAndPublish()
    }

    companion object {
        fun factory(
            repository: GradeTrackerRepository,
            attachmentStorage: GradeAttachmentStorage = NoOpGradeAttachmentStorage,
            backupCoordinator: AppBackupCoordinator = NoOpAppBackupCoordinator
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GradeTrackerViewModel(repository, attachmentStorage, backupCoordinator) as T
                }
            }
        }
    }

    private fun createUiState(): GradeTrackerUiState {
        val screen = when (val target = currentScreen) {
            is InternalScreen.Onboarding -> ScreenUiState.Onboarding(
                selectedOption = onboardingSelection
            )

            is InternalScreen.Main -> {
                val subjectMetrics = state.subjects.associate { subject ->
                    subject.id to subject.computeMetrics()
                }
                val summary = createDashboardSummary(subjectMetrics)
                val optionStoredSubject = requireNotNull(state.subjects.firstOrNull { it.isOptionSubject })
                val optionSubject = subjectToListItem(
                    optionStoredSubject,
                    requireNotNull(subjectMetrics[optionStoredSubject.id])
                )
                val userSubjects = state.subjects
                    .filterNot { it.isOptionSubject }
                    .map { subject ->
                        subjectToListItem(subject, requireNotNull(subjectMetrics[subject.id]))
                    }
                ScreenUiState.Main(
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
                    selectedLanguage = state.language,
                    selectedThemeMode = state.themeMode,
                    backupFileNameSuggestion = backupCoordinator.suggestedBackupFileName(),
                    pendingImportDisplayName = target.pendingPreparedImport?.displayName,
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
        val calculableAverages = subjectMetrics.values.mapNotNull { it.average }
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
            val compositeBranch = subject.toCompositeBranch()
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
                        internalAverageLabel = subSubject.toInternalAverageLabel(strings),
                        notes = subSubject.notes.map(::toNoteUiState)
                    )
                },
                notes = emptyList(),
                draft = draft,
                selectedSubSubjectId = selectedSubSubjectId ?: subject.subSubjects.first().id
            )
        }

        val branch = subject.toSimpleBranch()
        val rawAverage = GradeCalculator.weightedAverage(branch.grades)
        val officialAverage = GradeCalculator.computeBranchAverage(branch)
        val points = officialAverage?.let(GradeCalculator::computePromotionPoints)
        val statusLabel = officialAverage.toBranchStatusLabel(strings)

        return SubjectDetailUiState(
            subjectId = subject.id,
            title = subject.name,
            subtitle = subject.optionChoice?.label,
            isOptionSubject = subject.isOptionSubject,
            notes = subject.notes.map(::toNoteUiState),
            officialAverageLabel = officialAverage?.let(::formatOneOrTwoDecimals) ?: strings.emptyNotes,
            secondaryAverageTitle = strings.rawAverage,
            secondaryAverageLabel = rawAverage?.let(::formatTwoDecimals) ?: strings.emptyNotes,
            pointsLabel = points?.let(::formatSignedOneOrTwoDecimals) ?: strings.emptyNotes,
            statusLabel = statusLabel,
            statusTone = statusLabel.toDetailStatusTone(),
            isAddGradeSheetVisible = isAddGradeSheetVisible,
            pendingDeleteNoteTitle = pendingDeleteNoteTitle,
            draft = draft
        )
    }

    private fun buildPromotionPresentation(): PromotionPresentation? {
        val option = state.subjects.firstOrNull { it.isOptionSubject } ?: return null
        val basketSubjects = state.nonOptionBasketSubjects()
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
            add(PromotionRoleAssignment.German(firstBasketSubject.toSimpleBranch()))
            add(PromotionRoleAssignment.French(secondBasketSubject.toSimpleBranch()))
            add(PromotionRoleAssignment.Math(thirdBasketSubject.toSimpleBranch()))
            add(PromotionRoleAssignment.Option(option.toBranch()))
            state.subjects
                .filterNot { it.id in basketSubjectIds }
                .forEach { subject ->
                    add(
                        PromotionRoleAssignment.Additional(
                            branch = subject.toSimpleBranch(),
                            isExplicitlyEmpty = subject.notes.isEmpty()
                        )
                    )
                }
        }
        return PromotionPresentationMapper
            .map(PromotionEvaluator.evaluate(PromotionEvaluationInput.create(assignments)))
            .localized(strings)
    }

    private fun promotionUnavailableHeadline(): String {
        val basketSubjectCount = state.nonOptionBasketSubjects().size
        return when {
            basketSubjectCount < 3 -> ""
            basketSubjectCount > 3 -> strings.unlockPromotionTooMany
            else -> strings.unlockPromotionMissingGrades
        }
    }

    private fun subjectToListItem(subject: StoredSubject): SubjectListItemUiState {
        return subjectToListItem(subject, subject.computeMetrics())
    }

    private fun subjectToListItem(
        subject: StoredSubject,
        metrics: SubjectComputedMetrics
    ): SubjectListItemUiState {
        val average = metrics.average
        val points = metrics.points
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
            isInBasket = subject.isInBasket,
            isOptionSubject = subject.isOptionSubject,
            isCompositeOption = subject.subSubjects.isNotEmpty()
        )
    }

    private fun createOptionSubject(choice: InitialOptionChoice): StoredSubject {
        return createStoredOptionSubject(choice)
    }

    private fun validateDraftValue(input: String): String? {
        val normalized = input.trim()
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
    }
}

private fun GradeTrackerAppState.withRequiredOptionSubject(): GradeTrackerAppState {
    val selectedOption = selectedOption ?: return this
    val existingOption = subjects.firstOrNull { it.isOptionSubject }
    if (existingOption != null) {
        val normalizedOption = existingOption.copy(
            name = selectedOption.label,
            optionChoice = selectedOption
        )
        return copy(
            subjects = listOf(normalizedOption) + subjects.filterNot { it.isOptionSubject }
        )
    }

    return copy(
        subjects = listOf(createStoredOptionSubject(selectedOption)) + subjects,
        nextSubjectSequence = nextSubjectSequence.coerceAtLeast(2)
    )
}

private sealed interface InternalScreen {
    data object Onboarding : InternalScreen
    data object Main : InternalScreen
    data class AddSubject(
        val addSubjectForm: AddSubjectFormUiState = AddSubjectFormUiState(isVisible = true),
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

private fun StoredSubject.toBranch(): Branch {
    return if (subSubjects.isEmpty()) toSimpleBranch() else toCompositeBranch()
}

private fun StoredSubject.toSimpleBranch(): Branch.Simple {
    return Branch.Simple.create(
        name = name,
        grades = notes.map { it.toGrade() },
        optionType = optionChoice?.optionType
    )
}

private fun StoredSubject.toCompositeBranch(): Branch.Composite {
    return Branch.Composite.create(
        name = name,
        optionType = requireNotNull(optionChoice?.optionType),
        subSubjects = subSubjects.map { subSubject ->
            SubSubject(name = subSubject.name, grades = subSubject.notes.map { it.toGrade() })
        }
    )
}

private fun GradeTrackerAppState.nonOptionBasketSubjects(): List<StoredSubject> {
    return subjects.filter { it.isInBasket && !it.isOptionSubject }
}

private fun GradeTrackerAppState.currentBasketTotal(
    subjectMetrics: Map<String, SubjectComputedMetrics>
): Double? {
    val optionSubject = subjects.firstOrNull { it.isOptionSubject } ?: return null
    val basketSubjects = nonOptionBasketSubjects()
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
    val pointValues = subjects.mapNotNull { subject -> subjectMetrics[subject.id]?.points }
    return pointValues.takeIf { it.isNotEmpty() }?.sum()
}

private fun GradeTrackerAppState.insufficiencyCount(
    subjectMetrics: Map<String, SubjectComputedMetrics>
): Int {
    return subjects.count { subject ->
        subjectMetrics[subject.id]?.average?.let { average -> average < 4.0 } == true
    }
}

private fun createStoredOptionSubject(choice: InitialOptionChoice): StoredSubject {
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

private fun StoredSubject.allAttachments(): List<StoredAttachment> {
    return notes.flatMap { it.attachments } + subSubjects.flatMap { subSubject ->
        subSubject.notes.flatMap { it.attachments }
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

private fun storedSubjectAverageValue(subject: StoredSubject): Double? {
    return when {
        subject.subSubjects.isNotEmpty() -> GradeCalculator.computeCompositeOptionAverage(subject.toCompositeBranch())
        else -> GradeCalculator.computeBranchAverage(subject.toSimpleBranch())
    }
}

private fun StoredSubject.computeMetrics(): SubjectComputedMetrics {
    val average = storedSubjectAverageValue(this)
    return SubjectComputedMetrics(
        average = average,
        points = average?.let(GradeCalculator::computePromotionPoints)
    )
}

private fun StoredSubSubject.toInternalAverageLabel(strings: AppStrings): String {
    val average = GradeCalculator.weightedAverage(notes.map { it.toGrade() })?.let(GradeCalculator::roundToHundredth)
    return average?.let(::formatTwoDecimals) ?: strings.emptyNotes
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
