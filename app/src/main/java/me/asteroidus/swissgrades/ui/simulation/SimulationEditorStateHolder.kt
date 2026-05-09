package me.asteroidus.swissgrades.ui.simulation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

internal const val INVALID_GRADE_MESSAGE = "Enter a grade from 1.0 to 6.0 in 0.25 steps."
internal const val DECIMAL_SEPARATOR_MESSAGE = "Use a dot as the decimal separator, for example 4.5."
internal const val INVALID_CUSTOM_SUBJECT_NAME_MESSAGE = "Enter a subject name."
internal const val DUPLICATE_CUSTOM_SUBJECT_NAME_MESSAGE = "This subject already exists."
private const val INVALID_INPUT_NOTICE = "Some grades are invalid. Fix the highlighted fields to continue editing."

enum class BasketBranchRole(val branchName: String) {
    GERMAN("German"),
    FRENCH("French"),
    MATH("Math"),
    OPTION("Option")
}

interface GradeEntryCollectionKey {
    val entryPrefix: String
}

enum class EditorBranchKey(
    val branchName: String,
    override val entryPrefix: String,
    val basketRole: BasketBranchRole
) : GradeEntryCollectionKey {
    GERMAN(BasketBranchRole.GERMAN.branchName, "german-entry", BasketBranchRole.GERMAN),
    FRENCH(BasketBranchRole.FRENCH.branchName, "french-entry", BasketBranchRole.FRENCH),
    MATH(BasketBranchRole.MATH.branchName, "math-entry", BasketBranchRole.MATH),
    OPTION(BasketBranchRole.OPTION.branchName, "option-entry", BasketBranchRole.OPTION);

    companion object {
        fun fromBasketRole(role: BasketBranchRole): EditorBranchKey {
            return entries.first { it.basketRole == role }
        }
    }
}

private data class CustomSubjectKey(
    val subjectId: String
) : GradeEntryCollectionKey {
    override val entryPrefix: String = "$subjectId-entry"
}

enum class GradeWeightUi(val label: String, val assessmentWeight: AssessmentWeight) {
    FULL("Full", AssessmentWeight.FULL),
    HALF("Half", AssessmentWeight.HALF),
    QUARTER("Quarter", AssessmentWeight.QUARTER)
}

enum class OptionModeUi {
    SIMPLE,
    COMPOSITE
}

enum class SimpleOptionChoice(val optionType: OptionType, val label: String) {
    SPANISH(OptionType.SPANISH, "Spanish"),
    ITALIAN(OptionType.ITALIAN, "Italian"),
    LATIN(OptionType.LATIN, "Latin"),
    MUSIC(OptionType.MUSIC, "Music"),
    PHILOSOPHY(OptionType.PHILOSOPHY, "Philosophy"),
    VISUAL_ARTS(OptionType.VISUAL_ARTS, "Visual Arts")
}

enum class OptionSubSubjectKey(
    val subjectName: String,
    override val entryPrefix: String
) : GradeEntryCollectionKey {
    BIOLOGY("Biology", "option-biology-entry"),
    CHEMISTRY("Chemistry", "option-chemistry-entry"),
    PHYSICS("Physics", "option-physics-entry"),
    APPLICATIONS_OF_MATHEMATICS("Applications of Mathematics", "option-applications-of-mathematics-entry"),
    ECONOMICS("Economics", "option-economics-entry"),
    LAW("Law", "option-law-entry")
}

enum class CompositeOptionChoice(
    val optionType: OptionType,
    val label: String,
    val subSubjectDefinitions: List<Pair<OptionSubSubjectKey, String>>
) {
    BIOLOGY_CHEMISTRY(
        optionType = OptionType.BIOLOGY_CHEMISTRY,
        label = "Biology-Chemistry",
        subSubjectDefinitions = listOf(
            OptionSubSubjectKey.BIOLOGY to "Biology",
            OptionSubSubjectKey.CHEMISTRY to "Chemistry"
        )
    ),
    PHYSICS_AND_APPLICATIONS_OF_MATHEMATICS(
        optionType = OptionType.PHYSICS_AND_MATH_APPLICATIONS,
        label = "Physics and Applications of Mathematics",
        subSubjectDefinitions = listOf(
            OptionSubSubjectKey.PHYSICS to "Physics",
            OptionSubSubjectKey.APPLICATIONS_OF_MATHEMATICS to "Applications of Mathematics"
        )
    ),
    ECONOMICS_AND_LAW(
        optionType = OptionType.ECONOMICS_AND_LAW,
        label = "Economics and Law",
        subSubjectDefinitions = listOf(
            OptionSubSubjectKey.ECONOMICS to "Economics",
            OptionSubSubjectKey.LAW to "Law"
        )
    )
}

sealed interface BranchIdentifier : GradeEntryCollectionKey {
    val branchId: String
    val branchName: String
    val isBasket: Boolean
    val basketRole: BasketBranchRole?
    val testTagPrefix: String
}

data class FixedBranchIdentifier(
    val key: EditorBranchKey
) : BranchIdentifier {
    override val branchId: String = key.name.lowercase()
    override val branchName: String = key.branchName
    override val isBasket: Boolean = true
    override val basketRole: BasketBranchRole = key.basketRole
    override val entryPrefix: String = key.entryPrefix
    override val testTagPrefix: String = key.name
}

data class CustomBranchIdentifier(
    override val branchId: String,
    override val branchName: String
) : BranchIdentifier {
    override val isBasket: Boolean = false
    override val basketRole: BasketBranchRole? = null
    override val entryPrefix: String = "$branchId-entry"
    override val testTagPrefix: String = branchId
}

data class GradeEntryUiState(
    val entryId: String,
    val gradeInput: String,
    val weight: GradeWeightUi,
    val errorMessage: String? = null
)

data class BranchInputUiState(
    val branch: BranchIdentifier,
    val gradeEntries: List<GradeEntryUiState>
)

data class OptionSubSubjectUiState(
    val key: OptionSubSubjectKey,
    val name: String,
    val gradeEntries: List<GradeEntryUiState>
)

data class OptionEditorUiState(
    val mode: OptionModeUi,
    val simpleOption: SimpleOptionChoice,
    val compositeOption: CompositeOptionChoice,
    val compositeSubSubjects: List<OptionSubSubjectUiState>
)

data class SimulationEditorUiState(
    val branchInputs: List<BranchInputUiState>,
    val optionEditor: OptionEditorUiState,
    val customSubjectNameInput: String,
    val customSubjectNameErrorMessage: String?,
    val summary: PromotionPresentation,
    val inputNoticeMessage: String? = null
)

class SimulationEditorStateHolder(
    persistence: SimulationEditorPersistence = NoOpSimulationEditorPersistence,
    initialPersistedState: PersistedSimulationEditorState? = persistence.load()
) {
    private val persistence = persistence
    private val initialBranchInputs = initialPersistedState?.toBranchInputs() ?: defaultBranchInputs()
    private val initialOptionEditor = initialPersistedState?.toOptionEditor() ?: defaultOptionEditor()
    private val initialCustomSubjectNameInput = initialPersistedState?.customSubjectNameInput.orEmpty()

    private val nextEntrySequenceByPrefix = mutableMapOf<String, Int>().apply {
        initializeSequences(initialBranchInputs.flatMap { branchInput ->
            branchInput.gradeEntries.map { it.entryId to branchInput.branch }
        })
        initializeSequences(initialOptionEditor.compositeSubSubjects.flatMap { subSubject ->
            subSubject.gradeEntries.map { it.entryId to subSubject.key }
        })
    }
    private var nextCustomSubjectSequence = initialBranchInputs.mapNotNull { branchInput ->
        (branchInput.branch as? CustomBranchIdentifier)?.branchId?.customSubjectSequenceSuffix()
    }.maxOrNull()?.plus(1) ?: 1

    var uiState by mutableStateOf(
        createUiState(
            branchInputs = initialBranchInputs,
            optionEditor = initialOptionEditor,
            customSubjectNameInput = initialCustomSubjectNameInput
        )
    )
        private set

    fun onGradeInputChanged(branch: BranchIdentifier, entryId: String, input: String) {
        val updatedBranchInputs = updateGradeEntry(branch.branchId, entryId) { gradeEntry ->
            validateEntry(gradeEntry.copy(gradeInput = input))
        }
        updateUiState(updatedBranchInputs, uiState.optionEditor, uiState.customSubjectNameInput)
    }

    fun onGradeInputChanged(role: BasketBranchRole, entryId: String, input: String) {
        onGradeInputChanged(fixedBranch(role), entryId, input)
    }

    fun onWeightChanged(branch: BranchIdentifier, entryId: String, weight: GradeWeightUi) {
        val updatedBranchInputs = updateGradeEntry(branch.branchId, entryId) { gradeEntry ->
            validateEntry(gradeEntry.copy(weight = weight))
        }
        updateUiState(updatedBranchInputs, uiState.optionEditor, uiState.customSubjectNameInput)
    }

    fun onWeightChanged(role: BasketBranchRole, entryId: String, weight: GradeWeightUi) {
        onWeightChanged(fixedBranch(role), entryId, weight)
    }

    fun addGradeEntry(branch: BranchIdentifier) {
        val newEntry = emptyGradeEntry(branch)
        val updatedBranchInputs = uiState.branchInputs.map { branchInput ->
            if (branchInput.branch.branchId == branch.branchId) {
                branchInput.copy(gradeEntries = branchInput.gradeEntries + newEntry)
            } else {
                branchInput
            }
        }
        updateUiState(updatedBranchInputs, uiState.optionEditor, uiState.customSubjectNameInput)
    }

    fun addGradeEntry(role: BasketBranchRole) {
        addGradeEntry(fixedBranch(role))
    }

    fun removeGradeEntry(branch: BranchIdentifier, entryId: String) {
        val updatedBranchInputs = uiState.branchInputs.map { branchInput ->
            if (branchInput.branch.branchId == branch.branchId) {
                val remainingEntries = branchInput.gradeEntries.filterNot { it.entryId == entryId }
                branchInput.copy(
                    gradeEntries = remainingEntries.ifEmpty {
                        listOf(emptyGradeEntry(branch))
                    }
                )
            } else {
                branchInput
            }
        }
        updateUiState(updatedBranchInputs, uiState.optionEditor, uiState.customSubjectNameInput)
    }

    fun removeGradeEntry(role: BasketBranchRole, entryId: String) {
        removeGradeEntry(fixedBranch(role), entryId)
    }

    fun onOptionModeChanged(mode: OptionModeUi) {
        updateUiState(
            branchInputs = uiState.branchInputs,
            optionEditor = uiState.optionEditor.copy(mode = mode),
            customSubjectNameInput = uiState.customSubjectNameInput
        )
    }

    fun onSimpleOptionChanged(option: SimpleOptionChoice) {
        updateUiState(
            branchInputs = uiState.branchInputs,
            optionEditor = uiState.optionEditor.copy(simpleOption = option),
            customSubjectNameInput = uiState.customSubjectNameInput
        )
    }

    fun onCompositeOptionChanged(option: CompositeOptionChoice) {
        updateUiState(
            branchInputs = uiState.branchInputs,
            optionEditor = uiState.optionEditor.copy(
                compositeOption = option,
                compositeSubSubjects = defaultCompositeSubSubjects(option)
            ),
            customSubjectNameInput = uiState.customSubjectNameInput
        )
    }

    fun onCompositeOptionGradeInputChanged(
        subSubjectKey: OptionSubSubjectKey,
        entryId: String,
        input: String
    ) {
        val updatedOptionEditor = updateCompositeSubSubjectEntry(subSubjectKey, entryId) { gradeEntry ->
            validateEntry(gradeEntry.copy(gradeInput = input))
        }
        updateUiState(uiState.branchInputs, updatedOptionEditor, uiState.customSubjectNameInput)
    }

    fun onCompositeOptionWeightChanged(
        subSubjectKey: OptionSubSubjectKey,
        entryId: String,
        weight: GradeWeightUi
    ) {
        val updatedOptionEditor = updateCompositeSubSubjectEntry(subSubjectKey, entryId) { gradeEntry ->
            validateEntry(gradeEntry.copy(weight = weight))
        }
        updateUiState(uiState.branchInputs, updatedOptionEditor, uiState.customSubjectNameInput)
    }

    fun addCompositeOptionGradeEntry(subSubjectKey: OptionSubSubjectKey) {
        val updatedOptionEditor = uiState.optionEditor.copy(
            compositeSubSubjects = uiState.optionEditor.compositeSubSubjects.map { subSubject ->
                if (subSubject.key == subSubjectKey) {
                    subSubject.copy(gradeEntries = subSubject.gradeEntries + emptyGradeEntry(subSubjectKey))
                } else {
                    subSubject
                }
            }
        )
        updateUiState(uiState.branchInputs, updatedOptionEditor, uiState.customSubjectNameInput)
    }

    fun removeCompositeOptionGradeEntry(subSubjectKey: OptionSubSubjectKey, entryId: String) {
        val updatedOptionEditor = uiState.optionEditor.copy(
            compositeSubSubjects = uiState.optionEditor.compositeSubSubjects.map { subSubject ->
                if (subSubject.key == subSubjectKey) {
                    val remainingEntries = subSubject.gradeEntries.filterNot { it.entryId == entryId }
                    subSubject.copy(
                        gradeEntries = remainingEntries.ifEmpty {
                            listOf(emptyGradeEntry(subSubjectKey))
                        }
                    )
                } else {
                    subSubject
                }
            }
        )
        updateUiState(uiState.branchInputs, updatedOptionEditor, uiState.customSubjectNameInput)
    }

    fun onCustomSubjectNameInputChanged(input: String) {
        updateUiState(
            branchInputs = uiState.branchInputs,
            optionEditor = uiState.optionEditor,
            customSubjectNameInput = input
        )
    }

    fun addCustomSubject() {
        val validatedName = validateCustomSubjectName(uiState.customSubjectNameInput, uiState.branchInputs)
        if (validatedName.errorMessage != null) {
            updateUiState(uiState.branchInputs, uiState.optionEditor, uiState.customSubjectNameInput)
            return
        }

        val branch = CustomBranchIdentifier(
            branchId = nextCustomSubjectId(),
            branchName = validatedName.normalizedName
        )

        updateUiState(
            branchInputs = uiState.branchInputs + BranchInputUiState(
                branch = branch,
                gradeEntries = listOf(emptyGradeEntry(branch))
            ),
            optionEditor = uiState.optionEditor,
            customSubjectNameInput = ""
        )
    }

    fun removeCustomSubject(branchId: String) {
        updateUiState(
            branchInputs = uiState.branchInputs.filterNot { it.branch.branchId == branchId },
            optionEditor = uiState.optionEditor,
            customSubjectNameInput = uiState.customSubjectNameInput
        )
    }

    fun updateSummary() {
        updateUiState(uiState.branchInputs, uiState.optionEditor, uiState.customSubjectNameInput)
    }

    private fun createUiState(
        branchInputs: List<BranchInputUiState>,
        optionEditor: OptionEditorUiState,
        customSubjectNameInput: String
    ): SimulationEditorUiState {
        val validatedBranchInputs = validateBranchInputs(branchInputs)
        val validatedOptionEditor = validateOptionEditor(optionEditor)
        val customSubjectNameState = validateCustomSubjectName(customSubjectNameInput, validatedBranchInputs)

        return SimulationEditorUiState(
            branchInputs = validatedBranchInputs,
            optionEditor = validatedOptionEditor,
            customSubjectNameInput = customSubjectNameInput,
            customSubjectNameErrorMessage = customSubjectNameState.errorMessage,
            summary = createPresentation(validatedBranchInputs, validatedOptionEditor),
            inputNoticeMessage = currentInvalidInputNotice(validatedBranchInputs, validatedOptionEditor)
        )
    }

    private fun createPresentation(
        branchInputs: List<BranchInputUiState>,
        optionEditor: OptionEditorUiState
    ): PromotionPresentation {
        val promotionInput = buildPromotionInput(branchInputs, optionEditor)
        return PromotionPresentationMapper.map(PromotionEvaluator.evaluate(promotionInput))
    }

    private fun updateUiState(
        branchInputs: List<BranchInputUiState>,
        optionEditor: OptionEditorUiState,
        customSubjectNameInput: String
    ) {
        uiState = createUiState(branchInputs, optionEditor, customSubjectNameInput)
        persistence.save(uiState.toPersistedState())
    }

    private fun buildPromotionInput(
        branchInputs: List<BranchInputUiState>,
        optionEditor: OptionEditorUiState
    ): PromotionEvaluationInput {
        val assignments = branchInputs.mapNotNull { branchInput ->
            val parsedEntries = parseGradeEntries(branchInput.gradeEntries)
            val grades = if (parsedEntries.hasInvalidEntry) emptyList() else parsedEntries.grades
            val isExplicitlyEmpty = !parsedEntries.hasInvalidEntry && grades.isEmpty()

            when (branchInput.branch.basketRole) {
                BasketBranchRole.GERMAN -> PromotionRoleAssignment.German(
                    Branch.Simple.create(name = branchInput.branch.branchName, grades = grades)
                )

                BasketBranchRole.FRENCH -> PromotionRoleAssignment.French(
                    Branch.Simple.create(name = branchInput.branch.branchName, grades = grades)
                )

                BasketBranchRole.MATH -> PromotionRoleAssignment.Math(
                    Branch.Simple.create(name = branchInput.branch.branchName, grades = grades)
                )

                BasketBranchRole.OPTION -> null
                null -> PromotionRoleAssignment.Additional(
                    branch = Branch.Simple.create(name = branchInput.branch.branchName, grades = grades),
                    isExplicitlyEmpty = isExplicitlyEmpty
                )
            }
        } + PromotionRoleAssignment.Option(buildOptionBranch(branchInputs.optionSimpleBranch(), optionEditor))

        return PromotionEvaluationInput.create(assignments)
    }

    private fun buildOptionBranch(
        simpleBranchInput: BranchInputUiState,
        optionEditor: OptionEditorUiState
    ): Branch {
        return when (optionEditor.mode) {
            OptionModeUi.SIMPLE -> {
                val parsedEntries = parseGradeEntries(simpleBranchInput.gradeEntries)
                val grades = if (parsedEntries.hasInvalidEntry) emptyList() else parsedEntries.grades
                Branch.Simple.create(
                    name = simpleBranchInput.branch.branchName,
                    grades = grades,
                    optionType = optionEditor.simpleOption.optionType
                )
            }

            OptionModeUi.COMPOSITE -> {
                val subSubjects = optionEditor.compositeSubSubjects.map { subSubject ->
                    val parsedEntries = parseGradeEntries(subSubject.gradeEntries)
                    val grades = if (parsedEntries.hasInvalidEntry) emptyList() else parsedEntries.grades
                    SubSubject(name = subSubject.name, grades = grades)
                }
                Branch.Composite.create(
                    name = simpleBranchInput.branch.branchName,
                    optionType = optionEditor.compositeOption.optionType,
                    subSubjects = subSubjects
                )
            }
        }
    }

    private fun parseGradeEntries(gradeEntries: List<GradeEntryUiState>): ParsedEntriesState {
        val parsedEntries = gradeEntries.map { parseGrade(it.gradeInput, it.weight) }
        return ParsedEntriesState(
            grades = parsedEntries.mapNotNull { it.grade },
            hasInvalidEntry = parsedEntries.any { it.errorMessage != null }
        )
    }

    private fun parseGrade(input: String, weight: GradeWeightUi): ParsedGradeState {
        val normalizedInput = input.trim()
        if (normalizedInput.isEmpty()) {
            return ParsedGradeState(grade = null, errorMessage = null)
        }

        if (normalizedInput.contains(',')) {
            return ParsedGradeState(grade = null, errorMessage = DECIMAL_SEPARATOR_MESSAGE)
        }

        val numericValue = normalizedInput.toDoubleOrNull()
            ?: return ParsedGradeState(grade = null, errorMessage = INVALID_GRADE_MESSAGE)

        return try {
            ParsedGradeState(
                grade = Grade(value = numericValue, weight = weight.assessmentWeight),
                errorMessage = null
            )
        } catch (_: IllegalArgumentException) {
            ParsedGradeState(grade = null, errorMessage = INVALID_GRADE_MESSAGE)
        }
    }

    private fun validateEntry(gradeEntry: GradeEntryUiState): GradeEntryUiState {
        return gradeEntry.copy(
            errorMessage = parseGrade(gradeEntry.gradeInput, gradeEntry.weight).errorMessage
        )
    }

    private fun updateGradeEntry(
        branchId: String,
        entryId: String,
        transform: (GradeEntryUiState) -> GradeEntryUiState
    ): List<BranchInputUiState> {
        return uiState.branchInputs.map { branchInput ->
            if (branchInput.branch.branchId == branchId) {
                branchInput.copy(
                    gradeEntries = branchInput.gradeEntries.map { gradeEntry ->
                        if (gradeEntry.entryId == entryId) transform(gradeEntry) else gradeEntry
                    }
                )
            } else {
                branchInput
            }
        }
    }

    private fun updateCompositeSubSubjectEntry(
        subSubjectKey: OptionSubSubjectKey,
        entryId: String,
        transform: (GradeEntryUiState) -> GradeEntryUiState
    ): OptionEditorUiState {
        return uiState.optionEditor.copy(
            compositeSubSubjects = uiState.optionEditor.compositeSubSubjects.map { subSubject ->
                if (subSubject.key == subSubjectKey) {
                    subSubject.copy(
                        gradeEntries = subSubject.gradeEntries.map { gradeEntry ->
                            if (gradeEntry.entryId == entryId) transform(gradeEntry) else gradeEntry
                        }
                    )
                } else {
                    subSubject
                }
            }
        )
    }

    private fun validateBranchInputs(branchInputs: List<BranchInputUiState>): List<BranchInputUiState> {
        return branchInputs.map { branchInput ->
            branchInput.copy(
                gradeEntries = branchInput.gradeEntries.map(::validateEntry)
            )
        }
    }

    private fun validateOptionEditor(optionEditor: OptionEditorUiState): OptionEditorUiState {
        return optionEditor.copy(
            compositeSubSubjects = optionEditor.compositeSubSubjects.map { subSubject ->
                subSubject.copy(
                    gradeEntries = subSubject.gradeEntries.map(::validateEntry)
                )
            }
        )
    }

    private fun currentInvalidInputNotice(
        branchInputs: List<BranchInputUiState>,
        optionEditor: OptionEditorUiState
    ): String? {
        val branchInvalid = branchInputs.any { branchInput ->
            if (branchInput.branch.basketRole == BasketBranchRole.OPTION && optionEditor.mode == OptionModeUi.COMPOSITE) {
                false
            } else {
                branchInput.gradeEntries.any { it.errorMessage != null }
            }
        }
        val optionInvalid = optionEditor.mode == OptionModeUi.COMPOSITE &&
            optionEditor.compositeSubSubjects.any { subSubject ->
                subSubject.gradeEntries.any { it.errorMessage != null }
            }
        return if (branchInvalid || optionInvalid) INVALID_INPUT_NOTICE else null
    }

    private fun validateCustomSubjectName(
        input: String,
        branchInputs: List<BranchInputUiState>
    ): CustomSubjectNameState {
        val normalizedName = input.trim()
        if (normalizedName.isEmpty()) {
            return CustomSubjectNameState(normalizedName, INVALID_CUSTOM_SUBJECT_NAME_MESSAGE)
        }

        val duplicateExists = branchInputs.any { branchInput ->
            !branchInput.branch.isBasket && branchInput.branch.branchName.equals(normalizedName, ignoreCase = true)
        } || BasketBranchRole.entries.any { role ->
            role.branchName.equals(normalizedName, ignoreCase = true)
        }

        return if (duplicateExists) {
            CustomSubjectNameState(normalizedName, DUPLICATE_CUSTOM_SUBJECT_NAME_MESSAGE)
        } else {
            CustomSubjectNameState(normalizedName, null)
        }
    }

    private fun emptyGradeEntry(key: GradeEntryCollectionKey): GradeEntryUiState {
        return GradeEntryUiState(
            entryId = nextEntryId(key),
            gradeInput = "",
            weight = GradeWeightUi.FULL
        )
    }

    private fun nextEntryId(key: GradeEntryCollectionKey): String {
        val nextSequence = nextEntrySequenceByPrefix.getOrDefault(key.entryPrefix, 1)
        nextEntrySequenceByPrefix[key.entryPrefix] = nextSequence + 1
        return key.generatedEntryId(nextSequence)
    }

    private fun nextCustomSubjectId(): String {
        val nextId = "custom-subject-$nextCustomSubjectSequence"
        nextCustomSubjectSequence += 1
        return nextId
    }

    private fun MutableMap<String, Int>.initializeSequences(entries: List<Pair<String, GradeEntryCollectionKey>>) {
        entries.groupBy({ it.second.entryPrefix }, { it.first.sequenceSuffix() })
            .forEach { (entryPrefix, suffixes) ->
                this[entryPrefix] = (suffixes.filterNotNull().maxOrNull() ?: 0) + 1
            }
    }

    companion object {
        fun defaultBranchInputs(): List<BranchInputUiState> {
            return BasketBranchRole.entries.map { role ->
                val branch = fixedBranch(role)
                BranchInputUiState(
                    branch = branch,
                    gradeEntries = listOf(
                        GradeEntryUiState(
                            entryId = branch.generatedEntryId(1),
                            gradeInput = "",
                            weight = GradeWeightUi.FULL
                        )
                    )
                )
            }
        }

        fun defaultOptionEditor(): OptionEditorUiState {
            return OptionEditorUiState(
                mode = OptionModeUi.SIMPLE,
                simpleOption = SimpleOptionChoice.SPANISH,
                compositeOption = CompositeOptionChoice.BIOLOGY_CHEMISTRY,
                compositeSubSubjects = defaultCompositeSubSubjects(CompositeOptionChoice.BIOLOGY_CHEMISTRY)
            )
        }

        private fun defaultCompositeSubSubjects(option: CompositeOptionChoice): List<OptionSubSubjectUiState> {
            return option.subSubjectDefinitions.map { (key, name) ->
                OptionSubSubjectUiState(
                    key = key,
                    name = name,
                    gradeEntries = listOf(
                        GradeEntryUiState(
                            entryId = key.generatedEntryId(1),
                            gradeInput = "",
                            weight = GradeWeightUi.FULL
                        )
                    )
                )
            }
        }
    }

    private data class ParsedGradeState(
        val grade: Grade?,
        val errorMessage: String?
    )

    private data class ParsedEntriesState(
        val grades: List<Grade>,
        val hasInvalidEntry: Boolean
    )

    private data class CustomSubjectNameState(
        val normalizedName: String,
        val errorMessage: String?
    )
}

private fun PersistedSimulationEditorState.toBranchInputs(): List<BranchInputUiState> {
    val fixedBranches = BasketBranchRole.entries.map { role ->
        val branch = fixedBranch(role)
        branchInputs.firstOrNull { it.branchIdentity().branchId == branch.branchId }?.toBranchInputUiState(branch)
            ?: BranchInputUiState(
                branch = branch,
                gradeEntries = listOf(
                    GradeEntryUiState(
                        entryId = branch.generatedEntryId(1),
                        gradeInput = "",
                        weight = GradeWeightUi.FULL
                    )
                )
            )
    }

    val customBranches = branchInputs.mapNotNull { persistedBranchInput ->
        val branch = persistedBranchInput.branchIdentity()
        if (branch.isBasket) null else persistedBranchInput.toBranchInputUiState(branch)
    }

    return fixedBranches + customBranches
}

private fun PersistedSimulationEditorState.toOptionEditor(): OptionEditorUiState {
    val compositeSelection = compositeOption ?: CompositeOptionChoice.BIOLOGY_CHEMISTRY
    val persistedByKey = optionSubSubjects.associateBy { it.key }
    val restoredSubSubjects = compositeSelection.subSubjectDefinitions.map { (key, name) ->
        persistedByKey[key]?.toOptionSubSubjectUiState(name)
            ?: OptionSubSubjectUiState(
                key = key,
                name = name,
                gradeEntries = listOf(
                    GradeEntryUiState(
                        entryId = key.generatedEntryId(1),
                        gradeInput = "",
                        weight = GradeWeightUi.FULL
                    )
                )
            )
    }

    return OptionEditorUiState(
        mode = optionMode,
        simpleOption = simpleOption,
        compositeOption = compositeSelection,
        compositeSubSubjects = restoredSubSubjects
    )
}

private fun SimulationEditorUiState.toPersistedState(): PersistedSimulationEditorState {
    return PersistedSimulationEditorState(
        branchInputs = branchInputs.map { branchInput ->
            PersistedBranchInput(
                branchId = branchInput.branch.branchId,
                branchName = branchInput.branch.branchName,
                branchKey = (branchInput.branch as? FixedBranchIdentifier)?.key,
                gradeEntries = branchInput.gradeEntries.map { gradeEntry ->
                    PersistedGradeEntry(
                        entryId = gradeEntry.entryId,
                        gradeInput = gradeEntry.gradeInput,
                        weight = gradeEntry.weight
                    )
                }
            )
        },
        optionMode = optionEditor.mode,
        simpleOption = optionEditor.simpleOption,
        compositeOption = optionEditor.compositeOption,
        optionSubSubjects = optionEditor.compositeSubSubjects.map { subSubject ->
            PersistedOptionSubSubjectInput(
                key = subSubject.key,
                name = subSubject.name,
                gradeEntries = subSubject.gradeEntries.map { gradeEntry ->
                    PersistedGradeEntry(
                        entryId = gradeEntry.entryId,
                        gradeInput = gradeEntry.gradeInput,
                        weight = gradeEntry.weight
                    )
                }
            )
        },
        customSubjectNameInput = customSubjectNameInput
    )
}

private fun String.sequenceSuffix(): Int? {
    return substringAfterLast('-', missingDelimiterValue = "").toIntOrNull()
}

private fun String.customSubjectSequenceSuffix(): Int? {
    return removePrefix("custom-subject-").toIntOrNull()
}

private fun PersistedBranchInput.branchIdentity(): BranchIdentifier {
    branchKey?.let { return FixedBranchIdentifier(it) }
    return when (branchId) {
        fixedBranch(BasketBranchRole.GERMAN).branchId -> fixedBranch(BasketBranchRole.GERMAN)
        fixedBranch(BasketBranchRole.FRENCH).branchId -> fixedBranch(BasketBranchRole.FRENCH)
        fixedBranch(BasketBranchRole.MATH).branchId -> fixedBranch(BasketBranchRole.MATH)
        fixedBranch(BasketBranchRole.OPTION).branchId -> fixedBranch(BasketBranchRole.OPTION)
        else -> CustomBranchIdentifier(
            branchId = branchId,
            branchName = branchName.ifBlank { "Subject" }
        )
    }
}

private fun PersistedBranchInput.toBranchInputUiState(branch: BranchIdentifier = branchIdentity()): BranchInputUiState {
    return BranchInputUiState(
        branch = branch,
        gradeEntries = normalizePersistedGradeEntries(gradeEntries, branch)
    )
}

private fun PersistedOptionSubSubjectInput.toOptionSubSubjectUiState(nameOverride: String): OptionSubSubjectUiState {
    return OptionSubSubjectUiState(
        key = key,
        name = nameOverride,
        gradeEntries = normalizePersistedGradeEntries(gradeEntries, key)
    )
}

private fun normalizePersistedGradeEntries(
    gradeEntries: List<PersistedGradeEntry>,
    key: GradeEntryCollectionKey
): List<GradeEntryUiState> {
    val validSequences = gradeEntries.mapNotNull { gradeEntry ->
        gradeEntry.entryId.takeIf { it.isValidEntryIdFor(key) }?.sequenceSuffix()
    }
    var nextGeneratedSequence = (validSequences.maxOrNull() ?: 0) + 1
    val usedEntryIds = mutableSetOf<String>()

    return if (gradeEntries.isEmpty()) {
        listOf(
            GradeEntryUiState(
                entryId = key.generatedEntryId(1),
                gradeInput = "",
                weight = GradeWeightUi.FULL
            )
        )
    } else {
        gradeEntries.map { gradeEntry ->
            val normalizedEntryId = if (gradeEntry.entryId.isValidEntryIdFor(key) && usedEntryIds.add(gradeEntry.entryId)) {
                gradeEntry.entryId
            } else {
                generateSequence {
                    key.generatedEntryId(nextGeneratedSequence++)
                }.first { candidateId -> usedEntryIds.add(candidateId) }
            }

            GradeEntryUiState(
                entryId = normalizedEntryId,
                gradeInput = gradeEntry.gradeInput,
                weight = gradeEntry.weight
            )
        }
    }
}

private fun String.isValidEntryIdFor(key: GradeEntryCollectionKey): Boolean {
    return startsWith("${key.entryPrefix}-") && (sequenceSuffix() ?: 0) > 0
}

private fun GradeEntryCollectionKey.generatedEntryId(sequence: Int): String {
    return "$entryPrefix-$sequence"
}

private fun List<BranchInputUiState>.optionSimpleBranch(): BranchInputUiState {
    return first { it.branch.basketRole == BasketBranchRole.OPTION }
}

private fun fixedBranch(role: BasketBranchRole): FixedBranchIdentifier {
    return FixedBranchIdentifier(EditorBranchKey.fromBasketRole(role))
}
