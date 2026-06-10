package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeTrackerRepositorySerializationTest {

    @Test
    fun appStateSerialization_roundTripsSubjectsGradesAndAttachments() {
        val state = GradeTrackerAppState(
            selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
            subjects = listOf(
                StoredSubject(
                    id = "subject-1",
                    name = "History",
                    schoolYear = SchoolYear.YEAR_2,
                    isCounted = true,
                    isInBasket = false,
                    targetAverage = 5.25,
                    notes = listOf(
                        StoredNote(
                            id = "note-1",
                            value = 5.25,
                            weight = AssessmentWeight.HALF,
                            description = "Essay",
                            createdAtEpochMillis = 1_800_000_000_000L,
                            semester = SchoolSemester.SEMESTER_2,
                            attachments = listOf(
                                StoredAttachment(
                                    id = "attachment-1",
                                    filePath = "/files/attachments/attachment-1.jpg"
                                )
                            )
                        )
                    )
                )
            ),
            nextSubjectSequence = 3,
            nextNoteSequence = 4,
            selectedYear = SchoolYear.YEAR_2,
            selectedSemester = SchoolSemester.SEMESTER_2,
            language = AppLanguage.ENGLISH,
            themeMode = AppThemeMode.DARK
        )

        val decodedState = decodeGradeTrackerAppState(state.encodeToJsonString())

        assertEquals(state, decodedState)
    }

    @Test
    fun appStateSerialization_writesSchemaVersion() {
        val json = JSONObject(GradeTrackerAppState().encodeToJsonString())

        assertEquals(1, json.getInt("schemaVersion"))
    }

    @Test
    fun appStateSerialization_readsLegacyStateWithoutSchemaVersion() {
        val legacyJson = JSONObject()
            .put("selectedOption", InitialOptionChoice.MUSIC.name)
            .put("nextSubjectSequence", 2)
            .put("nextNoteSequence", 1)
            .put("subjects", org.json.JSONArray())
            .toString()

        val decodedState = decodeGradeTrackerAppState(legacyJson)

        assertEquals(InitialOptionChoice.MUSIC, decodedState.selectedOption)
        assertEquals(AppLanguage.FRENCH, decodedState.language)
        assertEquals(AppThemeMode.SYSTEM, decodedState.themeMode)
        assertEquals(SchoolYear.YEAR_1, decodedState.selectedYear)
        assertEquals(SchoolSemester.SEMESTER_1, decodedState.selectedSemester)
    }

    @Test
    fun appStateSerialization_fallsBackWhenStoredEnumsAreUnknown() {
        val json = JSONObject(GradeTrackerAppState().encodeToJsonString())
            .put("selectedOption", "UNKNOWN_OPTION")
            .put("selectedYear", "UNKNOWN_YEAR")
            .put("selectedSemester", "UNKNOWN_SEMESTER")
            .put("language", "UNKNOWN_LANGUAGE")
            .put("themeMode", "UNKNOWN_THEME")
            .toString()

        val decodedState = decodeGradeTrackerAppState(json)

        assertEquals(null, decodedState.selectedOption)
        assertEquals(SchoolYear.YEAR_1, decodedState.selectedYear)
        assertEquals(SchoolSemester.SEMESTER_1, decodedState.selectedSemester)
        assertEquals(AppLanguage.FRENCH, decodedState.language)
        assertEquals(AppThemeMode.SYSTEM, decodedState.themeMode)
        assertTrue(decodedState.subjects.isEmpty())
    }
}
