package me.asteroidus.swissgrades.ui.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import me.asteroidus.swissgrades.domain.model.OptionType

private const val PREFS_NAME = "grade_tracker_app_prefs"
private const val KEY_APP_STATE = "app_state"

enum class InitialOptionChoice(
    val label: String,
    val categoryLabel: String,
    val optionType: OptionType?,
    val compositeSubSubjectNames: List<String> = emptyList()
) {
    PHYSICS_AND_APPLICATIONS_OF_MATH(
        label = "PYAM",
        categoryLabel = "Experimental sciences",
        optionType = OptionType.PHYSICS_AND_MATH_APPLICATIONS,
        compositeSubSubjectNames = listOf("Physics", "Applications of Mathematics")
    ),
    BIOLOGY_CHEMISTRY(
        label = "BICH",
        categoryLabel = "Experimental sciences",
        optionType = OptionType.BIOLOGY_CHEMISTRY,
        compositeSubSubjectNames = listOf("Biology", "Chemistry")
    ),
    ECONOMICS_LAW(
        label = "Economics-Law",
        categoryLabel = "Management & society",
        optionType = OptionType.ECONOMICS_LAW
    ),
    SPANISH(
        label = "Spanish",
        categoryLabel = "Modern languages",
        optionType = OptionType.SPANISH
    ),
    ITALIAN(
        label = "Italian",
        categoryLabel = "Modern languages",
        optionType = OptionType.ITALIAN
    ),
    LATIN(
        label = "Latin",
        categoryLabel = "Classical languages",
        optionType = OptionType.LATIN
    ),
    MUSIC(
        label = "Music",
        categoryLabel = "Arts",
        optionType = OptionType.MUSIC
    ),
    PHILOSOPHY(
        label = "Philosophy",
        categoryLabel = "Humanities",
        optionType = OptionType.PHILOSOPHY
    ),
    VISUAL_ARTS(
        label = "Visual Arts",
        categoryLabel = "Arts",
        optionType = OptionType.VISUAL_ARTS
    ),
    OTHER(
        label = "Other",
        categoryLabel = "Custom option",
        optionType = OptionType.OTHER
    );

    val isComposite: Boolean
        get() = compositeSubSubjectNames.isNotEmpty()
}

data class StoredNote(
    val id: String,
    val value: Double,
    val weight: AssessmentWeight,
    val description: String,
    val createdAtEpochMillis: Long
)

data class StoredSubSubject(
    val id: String,
    val name: String,
    val notes: List<StoredNote>
)

enum class SubjectColorChoice {
    BLUE,
    RED,
    TEAL,
    SLATE,
    PURPLE,
    PINK,
    GREEN,
    AMBER,
    ORANGE
}

enum class SubjectIconChoice {
    BOOK,
    SCIENCE,
    LANGUAGE,
    MUSIC,
    ART,
    MIND,
    BALANCE,
    CATEGORY,
    HISTORY,
    MATH,
    WORLD,
    SPORT
}

data class StoredSubject(
    val id: String,
    val name: String,
    val isInBasket: Boolean,
    val isOptionSubject: Boolean = false,
    val optionChoice: InitialOptionChoice? = null,
    val subjectColor: SubjectColorChoice = SubjectColorChoice.BLUE,
    val subjectIcon: SubjectIconChoice = SubjectIconChoice.BOOK,
    val notes: List<StoredNote> = emptyList(),
    val subSubjects: List<StoredSubSubject> = emptyList()
)

data class GradeTrackerAppState(
    val selectedOption: InitialOptionChoice? = null,
    val subjects: List<StoredSubject> = emptyList(),
    val nextSubjectSequence: Int = 1,
    val nextNoteSequence: Int = 1
) {
    val isOnboardingCompleted: Boolean
        get() = selectedOption != null
}

interface GradeTrackerRepository {
    fun load(): GradeTrackerAppState?

    fun save(state: GradeTrackerAppState)
}

class SharedPreferencesGradeTrackerRepository(
    context: Context
) : GradeTrackerRepository {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): GradeTrackerAppState? {
        val serializedState = sharedPreferences.getString(KEY_APP_STATE, null) ?: return null
        return runCatching { decode(serializedState) }.getOrNull()
    }

    override fun save(state: GradeTrackerAppState) {
        sharedPreferences.edit()
            .putString(KEY_APP_STATE, encode(state))
            .apply()
    }

    private fun encode(state: GradeTrackerAppState): String {
        val subjectsJson = JSONArray()
        state.subjects.forEach { subject ->
            val subjectJson = JSONObject()
                .put("id", subject.id)
                .put("name", subject.name)
                .put("isInBasket", subject.isInBasket)
                .put("isOptionSubject", subject.isOptionSubject)
                .put("optionChoice", subject.optionChoice?.name)
                .put("subjectColor", subject.subjectColor.name)
                .put("subjectIcon", subject.subjectIcon.name)

            val notesJson = JSONArray()
            subject.notes.forEach { note ->
                notesJson.put(note.toJson())
            }
            subjectJson.put("notes", notesJson)

            val subSubjectsJson = JSONArray()
            subject.subSubjects.forEach { subSubject ->
                val subSubjectJson = JSONObject()
                    .put("id", subSubject.id)
                    .put("name", subSubject.name)
                val subSubjectNotesJson = JSONArray()
                subSubject.notes.forEach { note ->
                    subSubjectNotesJson.put(note.toJson())
                }
                subSubjectJson.put("notes", subSubjectNotesJson)
                subSubjectsJson.put(subSubjectJson)
            }
            subjectJson.put("subSubjects", subSubjectsJson)
            subjectsJson.put(subjectJson)
        }

        return JSONObject()
            .put("selectedOption", state.selectedOption?.name)
            .put("nextSubjectSequence", state.nextSubjectSequence)
            .put("nextNoteSequence", state.nextNoteSequence)
            .put("subjects", subjectsJson)
            .toString()
    }

    private fun decode(serializedState: String): GradeTrackerAppState {
        val root = JSONObject(serializedState)
        val subjectsJson = root.optJSONArray("subjects") ?: JSONArray()
        val subjects = buildList {
            for (index in 0 until subjectsJson.length()) {
                val subjectJson = subjectsJson.getJSONObject(index)
                val notesJson = subjectJson.optJSONArray("notes") ?: JSONArray()
                val notes = buildList {
                    for (noteIndex in 0 until notesJson.length()) {
                        add(notesJson.getJSONObject(noteIndex).toStoredNote())
                    }
                }

                val subSubjectsJson = subjectJson.optJSONArray("subSubjects") ?: JSONArray()
                val subSubjects = buildList {
                    for (subIndex in 0 until subSubjectsJson.length()) {
                        val subSubjectJson = subSubjectsJson.getJSONObject(subIndex)
                        val subSubjectNotesJson = subSubjectJson.optJSONArray("notes") ?: JSONArray()
                        val subSubjectNotes = buildList {
                            for (noteIndex in 0 until subSubjectNotesJson.length()) {
                                add(subSubjectNotesJson.getJSONObject(noteIndex).toStoredNote())
                            }
                        }
                        add(
                            StoredSubSubject(
                                id = subSubjectJson.getString("id"),
                                name = subSubjectJson.getString("name"),
                                notes = subSubjectNotes
                            )
                        )
                    }
                }

                add(
                    StoredSubject(
                        id = subjectJson.getString("id"),
                        name = subjectJson.getString("name"),
                        isInBasket = subjectJson.optBoolean("isInBasket", false),
                        isOptionSubject = subjectJson.optBoolean("isOptionSubject", false),
                        optionChoice = subjectJson.optString("optionChoice")
                            .takeIf { it.isNotBlank() }
                            ?.let(InitialOptionChoice::valueOf),
                        subjectColor = subjectJson.optString("subjectColor")
                            .takeIf { it.isNotBlank() }
                            ?.let(SubjectColorChoice::valueOf)
                            ?: SubjectColorChoice.BLUE,
                        subjectIcon = subjectJson.optString("subjectIcon")
                            .takeIf { it.isNotBlank() }
                            ?.let(SubjectIconChoice::valueOf)
                            ?: SubjectIconChoice.BOOK,
                        notes = notes,
                        subSubjects = subSubjects
                    )
                )
            }
        }

        return GradeTrackerAppState(
            selectedOption = root.optString("selectedOption")
                .takeIf { it.isNotBlank() }
                ?.let(InitialOptionChoice::valueOf),
            subjects = subjects,
            nextSubjectSequence = root.optInt("nextSubjectSequence", subjects.size + 1),
            nextNoteSequence = root.optInt(
                "nextNoteSequence",
                subjects.sumOf { it.notes.size + it.subSubjects.sumOf { sub -> sub.notes.size } } + 1
            )
        )
    }
}

object InMemoryGradeTrackerRepository : GradeTrackerRepository {
    private var state: GradeTrackerAppState? = null

    override fun load(): GradeTrackerAppState? = state

    override fun save(state: GradeTrackerAppState) {
        this.state = state
    }
}

private fun StoredNote.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("value", value)
        .put("weight", weight.name)
        .put("description", description)
        .put("createdAtEpochMillis", createdAtEpochMillis)
}

private fun JSONObject.toStoredNote(): StoredNote {
    return StoredNote(
        id = getString("id"),
        value = getDouble("value"),
        weight = AssessmentWeight.valueOf(getString("weight")),
        description = optString("description"),
        createdAtEpochMillis = optLong("createdAtEpochMillis", 0L)
    )
}
