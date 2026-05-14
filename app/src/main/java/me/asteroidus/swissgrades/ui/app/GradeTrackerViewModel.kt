package me.asteroidus.swissgrades.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal const val EMPTY_NOTES_MESSAGE = "No grades yet"
internal const val INVALID_NOTE_VALUE_MESSAGE = "Enter a grade from 1.0 to 6.0 in 0.25 steps."
internal const val EMPTY_SUBJECT_NAME_MESSAGE = "Enter a subject name."
internal const val DUPLICATE_SUBJECT_NAME_MESSAGE = "This subject already exists."

enum class NoteTypeUi(val label: String, val weight: AssessmentWeight) {
    FULL("Full grade", AssessmentWeight.FULL),
    HALF("Half grade", AssessmentWeight.HALF),
    QUARTER("Quarter grade", AssessmentWeight.QUARTER)
}

data class NoteUiState(
    val id: String,
    val numericValue: Double,
    val displayValue: String,
    val noteTypeLabel: String,
    val description: String,
    val dateLabel: String
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
    val editingNoteId: String? = null
)

data class SubjectDetailUiState(
    val subjectId: String,
    val title: String,
    val subtitle: String?,
    val isOptionSubject: Boolean = false,
    val notes: List<NoteUiState>,
    val isCompositeOption: Boolean = false,
    val subSubjects: List<CompositeSubSubjectDetailUiState> = emptyList(),
    val officialAverageLabel: String = EMPTY_NOTES_MESSAGE,
    val secondaryAverageTitle: String = "Raw average",
    val secondaryAverageLabel: String = EMPTY_NOTES_MESSAGE,
    val pointsLabel: String = EMPTY_NOTES_MESSAGE,
    val statusLabel: String = EMPTY_NOTES_MESSAGE,
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
    val selectedOption: InitialOptionChoice
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
    val screen: ScreenUiState
)

class GradeTrackerViewModel(
    private val repository: GradeTrackerRepository
) : ViewModel() {
    private var state: GradeTrackerAppState = (repository.load() ?: GradeTrackerAppState()).withRequiredOptionSubject()
    private var currentScreen: InternalScreen = if (state.isOnboardingCompleted) InternalScreen.Main else InternalScreen.Onboarding
    private var onboardingSelection: InitialOptionChoice? = state.selectedOption

    private val _uiState = MutableStateFlow(createUiState())
    val uiState: StateFlow<GradeTrackerUiState> = _uiState.asStateFlow()

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
            currentScreen = InternalScreen.Settings
            publish()
        }
    }

    fun closeSettings() {
        currentScreen = InternalScreen.Main
        publish()
    }

    fun changeOption(choice: InitialOptionChoice) {
        val existingOption = state.subjects.firstOrNull { it.isOptionSubject }
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
            normalizedName.isEmpty() -> EMPTY_SUBJECT_NAME_MESSAGE
            state.subjects.any {
                it.id != form.editingSubjectId && it.name.equals(normalizedName, ignoreCase = true)
            } -> DUPLICATE_SUBJECT_NAME_MESSAGE
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
            editingNoteId = target.note.id
        )
        screen.isAddGradeSheetVisible = true
        publish()
    }

    fun deleteSubject(subjectId: String) {
        if (state.subjects.any { it.id == subjectId && it.isOptionSubject }) return

        state = state.copy(
            subjects = state.subjects.filterNot { it.id == subjectId }
        ).withRequiredOptionSubject()
        currentScreen = InternalScreen.Main
        persistAndPublish()
    }

    fun openSubject(subjectId: String) {
        currentScreen = InternalScreen.BranchDetail(subjectId)
        publish()
    }

    fun backFromDetail() {
        val screen = currentScreen as? InternalScreen.BranchDetail ?: return
        if (screen.isAddGradeSheetVisible) {
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
                    subject.copy(notes = subject.notes.filterNot { it.id == noteId })
                } else {
                    subject.copy(
                        subSubjects = subject.subSubjects.map { subSubject ->
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
            val note = StoredNote(
                id = "note-${state.nextNoteSequence}",
                value = grade.value,
                weight = grade.weight,
                description = draft.descriptionInput.trim(),
                createdAtEpochMillis = System.currentTimeMillis()
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
            val updatedNote = target.note.copy(
                value = grade.value,
                weight = grade.weight,
                description = draft.descriptionInput.trim()
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
        }
        screen.draft = NoteDraftUiState()
        screen.isAddGradeSheetVisible = false
        persistAndPublish()
    }

    companion object {
        fun factory(repository: GradeTrackerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GradeTrackerViewModel(repository) as T
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
                val summary = createDashboardSummary()
                val optionSubject = subjectToListItem(requireNotNull(state.subjects.firstOrNull { it.isOptionSubject }))
                val userSubjects = state.subjects
                    .filterNot { it.isOptionSubject }
                    .map(::subjectToListItem)
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
                settings = SettingsUiState(requireNotNull(state.selectedOption))
            )
        }
        return GradeTrackerUiState(screen = screen)
    }

    private fun createDashboardSummary(): DashboardSummaryUiState {
        val calculableAverages = state.subjects.mapNotNull(::storedSubjectAverageValue)
        val overallAverage = calculableAverages.takeIf { it.isNotEmpty() }?.average()
        val promotion = buildPromotionPresentation()
        val totalPromotionPoints = state.totalPromotionPoints()
        val basketTotal = state.currentBasketTotal()
        val insufficiencyCount = state.insufficiencyCount()
        return if (promotion != null) {
            DashboardSummaryUiState(
                overallAverageLabel = overallAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                overallAverageValue = overallAverage,
                promotionStatusLabel = promotion.statusLabel,
                promotionHeadline = promotion.headline,
                isPromotionCalculable = !promotion.basketTotal.valueLabel.equals("Not available", ignoreCase = true),
                promotionPointsLabel = totalPromotionPoints?.let(::formatSignedOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                promotionPointsValue = totalPromotionPoints,
                basketLabel = basketTotal?.let { "${formatOneOrTwoDecimals(it)} / 16" } ?: "Not enough grades",
                basketValue = basketTotal,
                insufficienciesLabel = "$insufficiencyCount / 4",
                insufficiencyCount = insufficiencyCount,
                statusTone = promotion.statusLabel.toDashboardStatusTone()
            )
        } else {
            DashboardSummaryUiState(
                overallAverageLabel = overallAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                overallAverageValue = overallAverage,
                promotionStatusLabel = "Not calculable yet",
                promotionHeadline = promotionUnavailableHeadline(),
                isPromotionCalculable = false,
                promotionPointsLabel = totalPromotionPoints?.let(::formatSignedOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                promotionPointsValue = totalPromotionPoints,
                basketLabel = basketTotal?.let { "${formatOneOrTwoDecimals(it)} / 16" } ?: "Not enough grades",
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
            val statusLabel = roundedAverage.toBranchStatusLabel()

            return SubjectDetailUiState(
                subjectId = subject.id,
                title = subject.name,
                subtitle = subject.optionChoice?.label,
                isOptionSubject = subject.isOptionSubject,
                isCompositeOption = true,
                officialAverageLabel = roundedAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                secondaryAverageTitle = "Composite average",
                secondaryAverageLabel = finalAverage?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                pointsLabel = promotionPoints?.let(::formatSignedOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                statusLabel = statusLabel,
                statusTone = statusLabel.toDetailStatusTone(),
                isAddGradeSheetVisible = isAddGradeSheetVisible,
                pendingDeleteNoteTitle = pendingDeleteNoteTitle,
                subSubjects = subject.subSubjects.map { subSubject ->
                    CompositeSubSubjectDetailUiState(
                        id = subSubject.id,
                        name = subSubject.name,
                        internalAverageLabel = subSubject.toInternalAverageLabel(),
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
        val statusLabel = officialAverage.toBranchStatusLabel()

        return SubjectDetailUiState(
            subjectId = subject.id,
            title = subject.name,
            subtitle = subject.optionChoice?.label,
            isOptionSubject = subject.isOptionSubject,
            notes = subject.notes.map(::toNoteUiState),
            officialAverageLabel = officialAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
            secondaryAverageTitle = "Raw average",
            secondaryAverageLabel = rawAverage?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
            pointsLabel = points?.let(::formatSignedOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
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
        return PromotionPresentationMapper.map(PromotionEvaluator.evaluate(PromotionEvaluationInput.create(assignments)))
    }

    private fun promotionUnavailableHeadline(): String {
        val basketSubjectCount = state.nonOptionBasketSubjects().size
        return when {
            basketSubjectCount < 3 -> ""
            basketSubjectCount > 3 -> "Keep exactly three non-option subjects in the basket to unlock promotion status."
            else -> "Add grades to every basket subject and the Option branch to unlock promotion status."
        }
    }

    private fun subjectToListItem(subject: StoredSubject): SubjectListItemUiState {
        val average = storedSubjectAverageValue(subject)
        val points = average?.let(GradeCalculator::computePromotionPoints)
        return SubjectListItemUiState(
            id = subject.id,
            title = subject.name,
            subtitle = null,
            averageLabel = average?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
            pointsLabel = points?.let(::formatSignedOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
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
            return INVALID_NOTE_VALUE_MESSAGE
        }
        val numericValue = normalized.toDoubleOrNull() ?: return INVALID_NOTE_VALUE_MESSAGE
        return try {
            Grade(value = numericValue, weight = AssessmentWeight.FULL)
            null
        } catch (_: IllegalArgumentException) {
            INVALID_NOTE_VALUE_MESSAGE
        }
    }

    private fun persistAndPublish() {
        repository.save(state)
        publish()
    }

    private fun findNoteTitle(subjectId: String, noteId: String): String {
        val noteDescription = findStoredNoteTarget(subjectId, noteId)?.note?.description
        return noteDescription?.takeIf { it.isNotBlank() } ?: "this grade"
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

    private fun publish() {
        _uiState.value = createUiState()
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
    data object Settings : InternalScreen
}

private data class StoredNoteTarget(
    val note: StoredNote,
    val subSubjectId: String?
)

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

private fun GradeTrackerAppState.currentBasketTotal(): Double? {
    val optionSubject = subjects.firstOrNull { it.isOptionSubject } ?: return null
    val basketSubjects = nonOptionBasketSubjects()
    if (basketSubjects.size != 3) return null

    val averages = listOfNotNull(
        basketSubjects.getOrNull(0)?.let(::storedSubjectAverageValue),
        basketSubjects.getOrNull(1)?.let(::storedSubjectAverageValue),
        basketSubjects.getOrNull(2)?.let(::storedSubjectAverageValue),
        storedSubjectAverageValue(optionSubject)
    )
    if (averages.size != 4) return null

    return averages.sum()
}

private fun GradeTrackerAppState.totalPromotionPoints(): Double? {
    val pointValues = subjects.mapNotNull { subject ->
        storedSubjectAverageValue(subject)?.let(GradeCalculator::computePromotionPoints)
    }
    return pointValues.takeIf { it.isNotEmpty() }?.sum()
}

private fun GradeTrackerAppState.insufficiencyCount(): Int {
    return subjects.count { subject ->
        storedSubjectAverageValue(subject)?.let { average -> average < 4.0 } == true
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

private fun StoredSubSubject.toInternalAverageLabel(): String {
    val average = GradeCalculator.weightedAverage(notes.map { it.toGrade() })?.let(GradeCalculator::roundToHundredth)
    return average?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE
}

private fun toNoteUiState(note: StoredNote): NoteUiState {
    return NoteUiState(
        id = note.id,
        numericValue = note.value,
        displayValue = formatOneOrTwoDecimals(note.value),
        noteTypeLabel = when (note.weight) {
            AssessmentWeight.FULL -> NoteTypeUi.FULL.label
            AssessmentWeight.HALF -> NoteTypeUi.HALF.label
            AssessmentWeight.QUARTER -> NoteTypeUi.QUARTER.label
        },
        description = note.description,
        dateLabel = note.createdAtEpochMillis.toDateLabel()
    )
}

private fun Long.toDateLabel(): String {
    if (this <= 0L) return ""
    return SimpleDateFormat("dd.MM.yyyy", Locale.US).format(Date(this))
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
        "Promoted" -> DashboardStatusTone.POSITIVE
        "Blocked" -> DashboardStatusTone.NEGATIVE
        else -> DashboardStatusTone.NEUTRAL
    }
}

private fun Double?.toBranchStatusLabel(): String {
    return when {
        this == null -> "Not enough grades"
        this >= 4.0 -> "Promoted"
        else -> "Insufficient"
    }
}

private fun String.toDetailStatusTone(): DashboardStatusTone {
    return when (this) {
        "Promoted" -> DashboardStatusTone.POSITIVE
        "Insufficient" -> DashboardStatusTone.NEGATIVE
        else -> DashboardStatusTone.NEUTRAL
    }
}
