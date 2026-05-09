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
    val isInBasket: Boolean,
    val isOptionSubject: Boolean,
    val isCompositeOption: Boolean
)

data class BranchMetricUiState(
    val label: String,
    val value: String
)

data class NoteDraftUiState(
    val valueInput: String = "",
    val selectedType: NoteTypeUi = NoteTypeUi.FULL,
    val descriptionInput: String = "",
    val errorMessage: String? = null
)

data class SubjectDetailUiState(
    val subjectId: String,
    val title: String,
    val subtitle: String?,
    val notes: List<NoteUiState>,
    val metrics: List<BranchMetricUiState>,
    val isCompositeOption: Boolean = false,
    val subSubjects: List<CompositeSubSubjectDetailUiState> = emptyList(),
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
    val nameInput: String = "",
    val isInBasket: Boolean = false,
    val errorMessage: String? = null
)

data class SettingsUiState(
    val selectedOption: InitialOptionChoice
)

data class DashboardSummaryUiState(
    val overallAverageLabel: String,
    val promotionStatusLabel: String,
    val promotionHeadline: String,
    val isPromotionCalculable: Boolean
)

sealed interface ScreenUiState {
    data class Onboarding(
        val selectedOption: InitialOptionChoice? = null
    ) : ScreenUiState

    data class Main(
        val summary: DashboardSummaryUiState,
        val optionSubject: SubjectListItemUiState,
        val userSubjects: List<SubjectListItemUiState>,
        val addSubjectForm: AddSubjectFormUiState
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
    private var state: GradeTrackerAppState = repository.load() ?: GradeTrackerAppState()
    private var currentScreen: InternalScreen = if (state.isOnboardingCompleted) InternalScreen.Main() else InternalScreen.Onboarding
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
        currentScreen = InternalScreen.Main()
        persistAndPublish()
    }

    fun openSettings() {
        state.selectedOption?.let {
            currentScreen = InternalScreen.Settings
            publish()
        }
    }

    fun closeSettings() {
        currentScreen = InternalScreen.Main()
        publish()
    }

    fun changeOption(choice: InitialOptionChoice) {
        val existingOption = state.subjects.firstOrNull { it.isOptionSubject }
        val replacement = createOptionSubject(choice).copy(
            id = existingOption?.id ?: "subject-1"
        )
        state = state.copy(
            selectedOption = choice,
            subjects = state.subjects.map { subject ->
                if (subject.isOptionSubject) replacement else subject
            }
        )
        currentScreen = InternalScreen.Main()
        persistAndPublish()
    }

    fun showAddSubjectForm() {
        if (currentScreen !is InternalScreen.Main) return
        currentScreen = InternalScreen.Main(
            addSubjectForm = currentAddSubjectForm().copy(isVisible = true)
        )
        publish()
    }

    fun hideAddSubjectForm() {
        if (currentScreen !is InternalScreen.Main) return
        currentScreen = InternalScreen.Main()
        publish()
    }

    fun updateAddSubjectName(input: String) {
        if (currentScreen !is InternalScreen.Main) return
        currentScreen = InternalScreen.Main(
            addSubjectForm = currentAddSubjectForm().copy(nameInput = input, errorMessage = null)
        )
        publish()
    }

    fun updateAddSubjectBasketFlag(isInBasket: Boolean) {
        if (currentScreen !is InternalScreen.Main) return
        currentScreen = InternalScreen.Main(
            addSubjectForm = currentAddSubjectForm().copy(isInBasket = isInBasket)
        )
        publish()
    }

    fun addSubject() {
        if (currentScreen !is InternalScreen.Main) return
        val form = currentAddSubjectForm()
        val normalizedName = form.nameInput.trim()
        val error = when {
            normalizedName.isEmpty() -> EMPTY_SUBJECT_NAME_MESSAGE
            state.subjects.any { it.name.equals(normalizedName, ignoreCase = true) } -> DUPLICATE_SUBJECT_NAME_MESSAGE
            else -> null
        }
        if (error != null) {
            currentScreen = InternalScreen.Main(addSubjectForm = form.copy(errorMessage = error))
            publish()
            return
        }

        val subject = StoredSubject(
            id = "subject-${state.nextSubjectSequence}",
            name = normalizedName,
            isInBasket = form.isInBasket
        )
        state = state.copy(
            subjects = state.subjects + subject,
            nextSubjectSequence = state.nextSubjectSequence + 1
        )
        currentScreen = InternalScreen.Main()
        persistAndPublish()
    }

    fun deleteSubject(subjectId: String) {
        state = state.copy(
            subjects = state.subjects.filterNot { it.id == subjectId || it.isOptionSubject && it.id == subjectId }
        )
        if (state.subjects.none { it.isOptionSubject }) {
            state = state.copy(subjects = listOf(createOptionSubject(requireNotNull(state.selectedOption))))
        }
        currentScreen = InternalScreen.Main()
        persistAndPublish()
    }

    fun openSubject(subjectId: String) {
        currentScreen = InternalScreen.BranchDetail(subjectId)
        publish()
    }

    fun backFromDetail() {
        currentScreen = InternalScreen.Main()
        publish()
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
        screen.draft = NoteDraftUiState()
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
                    userSubjects = userSubjects,
                    addSubjectForm = target.addSubjectForm
                )
            }

            is InternalScreen.BranchDetail -> ScreenUiState.BranchDetail(
                detail = createSubjectDetail(target.subjectId, target.draft, target.selectedSubSubjectId)
            )

            is InternalScreen.Settings -> ScreenUiState.Settings(
                settings = SettingsUiState(requireNotNull(state.selectedOption))
            )
        }
        return GradeTrackerUiState(screen = screen)
    }

    private fun createDashboardSummary(): DashboardSummaryUiState {
        val calculableAverages = state.subjects.mapNotNull { subjectAverageValue(it) }
        val overallAverage = calculableAverages.takeIf { it.isNotEmpty() }?.average()
        val promotion = buildPromotionPresentation()
        return if (promotion != null) {
            DashboardSummaryUiState(
                overallAverageLabel = overallAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                promotionStatusLabel = promotion.statusLabel,
                promotionHeadline = promotion.headline,
                isPromotionCalculable = !promotion.basketTotal.valueLabel.equals("Not available", ignoreCase = true)
            )
        } else {
            DashboardSummaryUiState(
                overallAverageLabel = overallAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
                promotionStatusLabel = "Not calculable yet",
                promotionHeadline = "Add German, French, Math, and Option grades to unlock promotion status.",
                isPromotionCalculable = false
            )
        }
    }

    private fun createSubjectDetail(
        subjectId: String,
        draft: NoteDraftUiState,
        selectedSubSubjectId: String?
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

            return SubjectDetailUiState(
                subjectId = subject.id,
                title = subject.name,
                subtitle = subject.optionChoice?.label,
                isCompositeOption = true,
                subSubjects = subject.subSubjects.map { subSubject ->
                    CompositeSubSubjectDetailUiState(
                        id = subSubject.id,
                        name = subSubject.name,
                        internalAverageLabel = subSubject.toInternalAverageLabel(),
                        notes = subSubject.notes.map(::toNoteUiState)
                    )
                },
                metrics = listOf(
                    BranchMetricUiState("First sub-subject average", firstAverage?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE),
                    BranchMetricUiState("Second sub-subject average", secondAverage?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE),
                    BranchMetricUiState("Composite final average", finalAverage?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE),
                    BranchMetricUiState("Official rounded option average", roundedAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE)
                ),
                notes = emptyList(),
                draft = draft,
                selectedSubSubjectId = selectedSubSubjectId ?: subject.subSubjects.first().id
            )
        }

        val branch = subject.toSimpleBranch()
        val rawAverage = GradeCalculator.weightedAverage(branch.grades)
        val officialAverage = GradeCalculator.computeBranchAverage(branch)
        val points = officialAverage?.let(GradeCalculator::computePromotionPoints)

        return SubjectDetailUiState(
            subjectId = subject.id,
            title = subject.name,
            subtitle = subject.optionChoice?.label,
            notes = subject.notes.map(::toNoteUiState),
            metrics = listOf(
                BranchMetricUiState("Raw average", rawAverage?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE),
                BranchMetricUiState("Official rounded average", officialAverage?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE),
                BranchMetricUiState("Promotion points", points?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE)
            ),
            draft = draft
        )
    }

    private fun buildPromotionPresentation(): PromotionPresentation? {
        val option = state.subjects.firstOrNull { it.isOptionSubject } ?: return null
        val german = state.subjects.firstOrNull { it.name.equals("German", ignoreCase = true) } ?: return null
        val french = state.subjects.firstOrNull { it.name.equals("French", ignoreCase = true) } ?: return null
        val math = state.subjects.firstOrNull { it.name.equals("Math", ignoreCase = true) } ?: return null

        val assignments = buildList {
            add(PromotionRoleAssignment.German(german.toSimpleBranch()))
            add(PromotionRoleAssignment.French(french.toSimpleBranch()))
            add(PromotionRoleAssignment.Math(math.toSimpleBranch()))
            add(PromotionRoleAssignment.Option(option.toBranch()))
            state.subjects
                .filterNot { it.id in setOf(german.id, french.id, math.id, option.id) }
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

    private fun subjectToListItem(subject: StoredSubject): SubjectListItemUiState {
        val average = subjectAverageValue(subject)
        val points = average?.let(GradeCalculator::computePromotionPoints)
        return SubjectListItemUiState(
            id = subject.id,
            title = subject.name,
            subtitle = subject.optionChoice?.label?.takeIf { subject.isOptionSubject },
            averageLabel = average?.let(::formatOneOrTwoDecimals) ?: EMPTY_NOTES_MESSAGE,
            pointsLabel = points?.let(::formatOneOrTwoDecimals) ?: "0.0",
            isInBasket = subject.isInBasket,
            isOptionSubject = subject.isOptionSubject,
            isCompositeOption = subject.subSubjects.isNotEmpty()
        )
    }

    private fun subjectAverageValue(subject: StoredSubject): Double? {
        return when {
            subject.subSubjects.isNotEmpty() -> GradeCalculator.computeCompositeOptionAverage(subject.toCompositeBranch())
            else -> GradeCalculator.computeBranchAverage(subject.toSimpleBranch())
        }
    }

    private fun createOptionSubject(choice: InitialOptionChoice): StoredSubject {
        return StoredSubject(
            id = "subject-1",
            name = "Option",
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

    private fun currentAddSubjectForm(): AddSubjectFormUiState {
        return (currentScreen as? InternalScreen.Main)?.addSubjectForm ?: AddSubjectFormUiState(isVisible = true)
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

    private fun publish() {
        _uiState.value = createUiState()
    }
}

private sealed interface InternalScreen {
    data object Onboarding : InternalScreen
    data class Main(
        val addSubjectForm: AddSubjectFormUiState = AddSubjectFormUiState()
    ) : InternalScreen
    data class BranchDetail(
        val subjectId: String,
        var draft: NoteDraftUiState = NoteDraftUiState(),
        var selectedSubSubjectId: String? = null
    ) : InternalScreen
    data object Settings : InternalScreen
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

private fun StoredNote.toGrade(): Grade {
    return Grade(value = value, weight = weight)
}

private fun StoredSubSubject.toInternalAverageLabel(): String {
    val average = GradeCalculator.weightedAverage(notes.map { it.toGrade() })?.let(GradeCalculator::roundToHundredth)
    return average?.let(::formatTwoDecimals) ?: EMPTY_NOTES_MESSAGE
}

private fun toNoteUiState(note: StoredNote): NoteUiState {
    return NoteUiState(
        id = note.id,
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
