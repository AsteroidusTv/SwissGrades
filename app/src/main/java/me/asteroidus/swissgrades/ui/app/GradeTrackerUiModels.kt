package me.asteroidus.swissgrades.ui.app

import androidx.compose.runtime.Immutable
import me.asteroidus.swissgrades.domain.model.AssessmentWeight

enum class NoteTypeUi(val weight: AssessmentWeight) {
    FULL(AssessmentWeight.FULL),
    HALF(AssessmentWeight.HALF),
    QUARTER(AssessmentWeight.QUARTER)
}

@Immutable
data class NoteUiState(
    val id: String,
    val numericValue: Double,
    val weightCoefficient: Double,
    val displayValue: String,
    val noteTypeLabel: String,
    val description: String,
    val dateLabel: String,
    val attachments: List<AttachmentUiState> = emptyList()
)

@Immutable
data class AttachmentUiState(
    val id: String,
    val filePath: String
)

@Immutable
data class DraftAttachmentUiState(
    val id: String,
    val filePath: String,
    val isPersisted: Boolean
)

@Immutable
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
    val isCounted: Boolean,
    val isInBasket: Boolean,
    val isOptionSubject: Boolean,
    val isCompositeOption: Boolean
)

@Immutable
data class NoteDraftUiState(
    val valueInput: String = "",
    val selectedType: NoteTypeUi = NoteTypeUi.FULL,
    val selectedSemester: SchoolSemester = SchoolSemester.SEMESTER_1,
    val descriptionInput: String = "",
    val errorMessage: String? = null,
    val editingNoteId: String? = null,
    val savedGradeImpact: GradeImpactUiState? = null,
    val attachments: List<DraftAttachmentUiState> = emptyList(),
    val attachmentErrorMessage: String? = null
)

@Immutable
data class GradeImpactUiState(
    val withGradeAverage: Double,
    val withoutGradeAverage: Double?,
    val officialAverageDelta: Double?
)

@Immutable
data class SubjectDetailUiState(
    val subjectId: String,
    val title: String,
    val subtitle: String?,
    val isCounted: Boolean = true,
    val isOptionSubject: Boolean = false,
    val notes: List<NoteUiState>,
    val isCompositeOption: Boolean = false,
    val subSubjects: List<CompositeSubSubjectDetailUiState> = emptyList(),
    val targetAverageInput: String? = null,
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

@Immutable
data class CompositeSubSubjectDetailUiState(
    val id: String,
    val name: String,
    val internalAverageLabel: String,
    val notes: List<NoteUiState>
)

@Immutable
data class AddSubjectFormUiState(
    val editingSubjectId: String? = null,
    val nameInput: String = "",
    val isCounted: Boolean = true,
    val isInBasket: Boolean = false,
    val selectedColor: SubjectColorChoice = SubjectColorChoice.BLUE,
    val selectedIcon: SubjectIconChoice = SubjectIconChoice.BOOK,
    val errorMessage: String? = null
)

@Immutable
data class SettingsUiState(
    val selectedOption: InitialOptionChoice,
    val selectedYear: SchoolYear,
    val selectedSemester: SchoolSemester,
    val selectedLanguage: AppLanguage,
    val selectedThemeMode: AppThemeMode,
    val backupFileNameSuggestion: String,
    val gradeReportFileNameSuggestion: String,
    val pendingImportDisplayName: String? = null,
    val pendingPlusPointsImportDisplayName: String? = null,
    val pendingPlusPointsTargetSemester: SchoolSemester? = null,
    val backupMessage: String? = null,
    val backupMessageTone: DashboardStatusTone = DashboardStatusTone.NEUTRAL,
    val isBackupInProgress: Boolean = false,
    val gradeReportMessage: String? = null,
    val gradeReportMessageTone: DashboardStatusTone = DashboardStatusTone.NEUTRAL,
    val isGradeReportExportInProgress: Boolean = false
)

@Immutable
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

@Immutable
data class PromotionSetupUiState(
    val title: String,
    val description: String,
    val actionLabel: String,
    val action: PromotionSetupAction,
    val actionSubjectId: String?,
    val items: List<PromotionSetupChecklistItemUiState>
)

enum class PromotionSetupAction {
    ADD_SUBJECT,
    OPEN_SUBJECT
}

@Immutable
data class PromotionSetupChecklistItemUiState(
    val label: String,
    val supportingText: String,
    val isComplete: Boolean
)

enum class DashboardStatusTone {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

@Immutable
sealed interface ScreenUiState {
    data class Onboarding(
        val selectedOption: InitialOptionChoice? = null
    ) : ScreenUiState

    data class Main(
        val selectedYear: SchoolYear,
        val selectedSemester: SchoolSemester,
        val summary: DashboardSummaryUiState,
        val promotionSetup: PromotionSetupUiState?,
        val optionSubject: SubjectListItemUiState,
        val userSubjects: List<SubjectListItemUiState>
    ) : ScreenUiState

    data class PeriodPicker(
        val selectedYear: SchoolYear,
        val selectedSemester: SchoolSemester
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

@Immutable
data class GradeTrackerUiState(
    val screen: ScreenUiState,
    val language: AppLanguage,
    val themeMode: AppThemeMode
)
