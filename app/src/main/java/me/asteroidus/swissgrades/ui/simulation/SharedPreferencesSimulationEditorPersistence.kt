package me.asteroidus.swissgrades.ui.simulation

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "simulation_editor_prefs"
private const val KEY_EDITOR_STATE = "editor_state"

class SharedPreferencesSimulationEditorPersistence(
    context: Context
) : SimulationEditorPersistence {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): PersistedSimulationEditorState? {
        val serializedState = sharedPreferences.getString(KEY_EDITOR_STATE, null) ?: return null
        return runCatching { decode(serializedState) }.getOrNull()
    }

    override fun save(state: PersistedSimulationEditorState) {
        sharedPreferences.edit {
            putString(KEY_EDITOR_STATE, encode(state))
        }
    }

    private fun encode(state: PersistedSimulationEditorState): String {
        val branchesJson = JSONArray()
        state.branchInputs.forEach { branchInput ->
            val branchJson = JSONObject()
                .put("branchId", branchInput.branchId)
                .put("branchName", branchInput.branchName)
                .put("branchKey", branchInput.branchKey?.name)

            val gradeEntriesJson = JSONArray()
            branchInput.gradeEntries.forEach { gradeEntry ->
                gradeEntriesJson.put(
                    JSONObject()
                        .put("entryId", gradeEntry.entryId)
                        .put("gradeInput", gradeEntry.gradeInput)
                        .put("weight", gradeEntry.weight.name)
                )
            }

            branchJson.put("gradeEntries", gradeEntriesJson)
            branchesJson.put(branchJson)
        }

        val optionSubSubjectsJson = JSONArray()
        state.optionSubSubjects.forEach { subSubject ->
            val subSubjectJson = JSONObject()
                .put("key", subSubject.key.name)
                .put("name", subSubject.name)

            val gradeEntriesJson = JSONArray()
            subSubject.gradeEntries.forEach { gradeEntry ->
                gradeEntriesJson.put(
                    JSONObject()
                        .put("entryId", gradeEntry.entryId)
                        .put("gradeInput", gradeEntry.gradeInput)
                        .put("weight", gradeEntry.weight.name)
                )
            }

            subSubjectJson.put("gradeEntries", gradeEntriesJson)
            optionSubSubjectsJson.put(subSubjectJson)
        }

        return JSONObject()
            .put("branchInputs", branchesJson)
            .put("optionMode", state.optionMode.name)
            .put("simpleOption", state.simpleOption.name)
            .put("compositeOption", state.compositeOption?.name)
            .put("optionSubSubjects", optionSubSubjectsJson)
            .put("customSubjectNameInput", state.customSubjectNameInput)
            .toString()
    }

    private fun decode(serializedState: String): PersistedSimulationEditorState {
        val rootJson = JSONObject(serializedState)
        val branchInputsJson = rootJson.getJSONArray("branchInputs")
        val branchInputs = buildList {
            for (index in 0 until branchInputsJson.length()) {
                val branchJson = branchInputsJson.getJSONObject(index)
                val gradeEntriesJson = branchJson.getJSONArray("gradeEntries")
                val gradeEntries = buildList {
                    for (entryIndex in 0 until gradeEntriesJson.length()) {
                        val gradeEntryJson = gradeEntriesJson.getJSONObject(entryIndex)
                        add(
                            PersistedGradeEntry(
                                entryId = gradeEntryJson.getString("entryId"),
                                gradeInput = gradeEntryJson.getString("gradeInput"),
                                weight = GradeWeightUi.valueOf(gradeEntryJson.getString("weight"))
                            )
                        )
                    }
                }

                add(
                    PersistedBranchInput(
                        branchId = branchJson.optString("branchId")
                            .takeIf { it.isNotBlank() }
                            ?: branchJson.optString("branchKey")
                                .takeIf { it.isNotBlank() }
                                ?.lowercase()
                            ?: BasketBranchRole.valueOf(branchJson.getString("role")).name.lowercase(),
                        branchName = branchJson.optString("branchName")
                            .takeIf { it.isNotBlank() }
                            ?: branchJson.optString("branchKey")
                                .takeIf { it.isNotBlank() }
                                ?.let(EditorBranchKey::valueOf)
                                ?.branchName
                            ?: EditorBranchKey.fromBasketRole(BasketBranchRole.valueOf(branchJson.getString("role"))).branchName,
                        branchKey = branchJson.optString("branchKey")
                            .takeIf { it.isNotBlank() }
                            ?.let(EditorBranchKey::valueOf),
                        gradeEntries = gradeEntries
                    )
                )
            }
        }

        val optionSubSubjectsJson = rootJson.optJSONArray("optionSubSubjects") ?: JSONArray()
        val optionSubSubjects = buildList {
            for (index in 0 until optionSubSubjectsJson.length()) {
                val subSubjectJson = optionSubSubjectsJson.getJSONObject(index)
                val gradeEntriesJson = subSubjectJson.getJSONArray("gradeEntries")
                val gradeEntries = buildList {
                    for (entryIndex in 0 until gradeEntriesJson.length()) {
                        val gradeEntryJson = gradeEntriesJson.getJSONObject(entryIndex)
                        add(
                            PersistedGradeEntry(
                                entryId = gradeEntryJson.getString("entryId"),
                                gradeInput = gradeEntryJson.getString("gradeInput"),
                                weight = GradeWeightUi.valueOf(gradeEntryJson.getString("weight"))
                            )
                        )
                    }
                }

                add(
                    PersistedOptionSubSubjectInput(
                        key = OptionSubSubjectKey.valueOf(subSubjectJson.getString("key")),
                        name = subSubjectJson.optString("name", OptionSubSubjectKey.valueOf(subSubjectJson.getString("key")).subjectName),
                        gradeEntries = gradeEntries
                    )
                )
            }
        }

        return PersistedSimulationEditorState(
            branchInputs = branchInputs,
            optionMode = rootJson.optString("optionMode")
                .takeIf { it.isNotBlank() }
                ?.let(OptionModeUi::valueOf)
                ?: OptionModeUi.SIMPLE,
            simpleOption = rootJson.optString("simpleOption")
                .takeIf { it.isNotBlank() }
                ?.let(SimpleOptionChoice::valueOf)
                ?: SimpleOptionChoice.SPANISH,
            compositeOption = rootJson.optString("compositeOption")
                .takeIf { it.isNotBlank() }
                ?.let(CompositeOptionChoice::valueOf),
            optionSubSubjects = optionSubSubjects,
            customSubjectNameInput = rootJson.optString("customSubjectNameInput")
        )
    }
}
