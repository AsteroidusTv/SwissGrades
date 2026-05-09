package me.asteroidus.swissgrades.ui.simulation

interface SimulationEditorPersistence {
    fun load(): PersistedSimulationEditorState?

    fun save(state: PersistedSimulationEditorState)
}

data class PersistedSimulationEditorState(
    val branchInputs: List<PersistedBranchInput>,
    val optionMode: OptionModeUi = OptionModeUi.SIMPLE,
    val simpleOption: SimpleOptionChoice = SimpleOptionChoice.SPANISH,
    val compositeOption: CompositeOptionChoice? = null,
    val optionSubSubjects: List<PersistedOptionSubSubjectInput> = emptyList(),
    val customSubjectNameInput: String = ""
)

data class PersistedBranchInput(
    val branchId: String,
    val branchName: String,
    val branchKey: EditorBranchKey? = null,
    val gradeEntries: List<PersistedGradeEntry>
)

data class PersistedGradeEntry(
    val entryId: String,
    val gradeInput: String,
    val weight: GradeWeightUi
)

data class PersistedOptionSubSubjectInput(
    val key: OptionSubSubjectKey,
    val name: String,
    val gradeEntries: List<PersistedGradeEntry>
)

object NoOpSimulationEditorPersistence : SimulationEditorPersistence {
    override fun load(): PersistedSimulationEditorState? = null

    override fun save(state: PersistedSimulationEditorState) = Unit
}
