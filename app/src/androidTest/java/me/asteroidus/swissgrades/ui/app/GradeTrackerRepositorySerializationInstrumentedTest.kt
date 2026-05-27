package me.asteroidus.swissgrades.ui.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradeTrackerRepositorySerializationInstrumentedTest {

    @Test
    fun encodeAndDecode_preservesNoteAttachments() {
        val state = GradeTrackerAppState(
            selectedOption = InitialOptionChoice.SPANISH,
            subjects = listOf(
                StoredSubject(
                    id = "subject-1",
                    name = "Spanish",
                    isInBasket = true,
                    isOptionSubject = true,
                    optionChoice = InitialOptionChoice.SPANISH,
                    notes = listOf(
                        StoredNote(
                            id = "note-1",
                            value = 5.0,
                            weight = AssessmentWeight.FULL,
                            description = "Essay",
                            createdAtEpochMillis = 1L,
                            attachments = listOf(
                                StoredAttachment("attachment-1", "/tmp/attachment-1.jpg"),
                                StoredAttachment("attachment-2", "/tmp/attachment-2.jpg")
                            )
                        )
                    )
                )
            )
        )

        val restored = decodeGradeTrackerAppState(state.encodeToJsonString())
        val note = restored.subjects.single().notes.single()

        assertEquals(2, note.attachments.size)
        assertEquals("/tmp/attachment-1.jpg", note.attachments.first().filePath)
    }

    @Test
    fun decode_withoutAttachments_remainsBackwardCompatible() {
        val serialized = """
            {
              "selectedOption":"SPANISH",
              "nextSubjectSequence":2,
              "nextNoteSequence":2,
              "language":"ENGLISH",
              "themeMode":"SYSTEM",
              "subjects":[
                {
                  "id":"subject-1",
                  "name":"Spanish",
                  "isInBasket":true,
                  "isOptionSubject":true,
                  "optionChoice":"SPANISH",
                  "subjectColor":"BLUE",
                  "subjectIcon":"BOOK",
                  "notes":[
                    {
                      "id":"note-1",
                      "value":5.0,
                      "weight":"FULL",
                      "description":"Essay",
                      "createdAtEpochMillis":1
                    }
                  ],
                  "subSubjects":[]
                }
              ]
            }
        """.trimIndent()

        val restored = decodeGradeTrackerAppState(serialized)

        assertTrue(restored.subjects.single().notes.single().attachments.isEmpty())
    }

    @Test
    fun decode_withoutSemester_defaultsToSemester1() {
        val serialized = """
            {
              "selectedOption":"SPANISH",
              "nextSubjectSequence":2,
              "nextNoteSequence":2,
              "language":"FRENCH",
              "themeMode":"SYSTEM",
              "subjects":[
                {
                  "id":"subject-1",
                  "name":"Spanish",
                  "isCounted":true,
                  "isInBasket":true,
                  "isOptionSubject":true,
                  "optionChoice":"SPANISH",
                  "subjectColor":"BLUE",
                  "subjectIcon":"BOOK",
                  "notes":[
                    {
                      "id":"note-1",
                      "value":5.0,
                      "weight":"FULL",
                      "description":"Legacy",
                      "createdAtEpochMillis":1000,
                      "attachments":[]
                    }
                  ],
                  "subSubjects":[]
                }
              ]
            }
        """.trimIndent()

        val restored = decodeGradeTrackerAppState(serialized)

        assertEquals(SchoolSemester.SEMESTER_1, restored.selectedSemester)
        assertEquals(SchoolSemester.SEMESTER_1, restored.subjects.single().notes.single().semester)
    }
}
