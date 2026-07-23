package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class PlusPointsImportCoordinatorTest {

    @Test
    fun parsePlusPointsExport_rejectsDocumentTypeDeclarations() {
        assertThrows(Exception::class.java) {
            parsePlusPointsExport(
                """
                <!DOCTYPE plist [
                  <!ENTITY injected SYSTEM "file:///etc/passwd">
                ]>
                <plist version="1.0">
                  <dict>
                    <key>data</key>
                    <dict>
                      <key>class</key>
                      <string>Semester</string>
                      <key>name</key>
                      <string>&injected;</string>
                      <key>subjects</key>
                      <array/>
                    </dict>
                  </dict>
                </plist>
                """.trimIndent()
            )
        }
    }

    @Test
    fun readPlusPointsImportText_rejectsOversizedInput() {
        val input = ByteArrayInputStream(ByteArray(17) { 'x'.code.toByte() })

        assertThrows(IllegalStateException::class.java) {
            input.readPlusPointsImportText(maxBytes = 16)
        }
    }

    @Test
    fun parsePlusPointsExport_importsCountedZeroExamsToo() {
        val importedState = parsePlusPointsExport(
            """
            <plist version="1.0">
              <dict>
                <key>data</key>
                <dict>
                  <key>class</key>
                  <string>Semester</string>
                  <key>subjects</key>
                  <array>
                    <dict>
                      <key>class</key>
                      <string>Subject</string>
                      <key>name</key>
                      <string>Allemand</string>
                      <key>counted</key>
                      <integer>1</integer>
                      <key>exams</key>
                      <array>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>Counted exam</string>
                          <key>counted</key>
                          <integer>1</integer>
                          <key>mark</key>
                          <real>4.5</real>
                          <key>weight</key>
                          <string>1</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.9e+08</real>
                          <key>exams</key>
                          <array/>
                        </dict>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>Uncounted in PlusPoints</string>
                          <key>counted</key>
                          <integer>0</integer>
                          <key>mark</key>
                          <real>5.0</real>
                          <key>weight</key>
                          <string>0.5</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.91e+08</real>
                          <key>exams</key>
                          <array/>
                        </dict>
                      </array>
                    </dict>
                  </array>
                </dict>
              </dict>
            </plist>
            """.trimIndent()
        )

        val importedSubject = importedState.subjects.first { !it.isOptionSubject }
        assertEquals(2, importedSubject.notes.size)
        assertEquals("Counted exam", importedSubject.notes[0].description)
        assertEquals("Uncounted in PlusPoints", importedSubject.notes[1].description)
        assertEquals(me.asteroidus.swissgrades.domain.model.AssessmentWeight.HALF, importedSubject.notes[1].weight)
    }

    @Test
    fun parsePlusPointsExport_marksUncountedSubjectsAsNotCounted() {
        val importedState = parsePlusPointsExport(
            """
            <plist version="1.0">
              <dict>
                <key>data</key>
                <dict>
                  <key>class</key>
                  <string>Semester</string>
                  <key>subjects</key>
                  <array>
                    <dict>
                      <key>class</key>
                      <string>Subject</string>
                      <key>name</key>
                      <string>Projet libre</string>
                      <key>counted</key>
                      <integer>0</integer>
                      <key>exams</key>
                      <array>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>Projet final</string>
                          <key>counted</key>
                          <integer>1</integer>
                          <key>mark</key>
                          <real>6.0</real>
                          <key>weight</key>
                          <string>1</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.9e+08</real>
                          <key>exams</key>
                          <array/>
                        </dict>
                      </array>
                    </dict>
                  </array>
                </dict>
              </dict>
            </plist>
            """.trimIndent()
        )

        val importedSubject = importedState.subjects.first { !it.isOptionSubject }
        assertFalse(importedSubject.isCounted)
        assertFalse(importedSubject.isInBasket)
        assertEquals("Projet libre", importedSubject.name)
    }

    @Test
    fun parsePlusPointsExport_mapsCompositePyamIntoOptionSubSubjects() {
        val importedState = parsePlusPointsExport(
            """
            <plist version="1.0">
              <dict>
                <key>data</key>
                <dict>
                  <key>class</key>
                  <string>Semester</string>
                  <key>subjects</key>
                  <array>
                    <dict>
                      <key>class</key>
                      <string>Subject</string>
                      <key>name</key>
                      <string>OS PYAM</string>
                      <key>counted</key>
                      <integer>1</integer>
                      <key>exams</key>
                      <array>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>AM</string>
                          <key>counted</key>
                          <integer>1</integer>
                          <key>mark</key>
                          <real>0</real>
                          <key>weight</key>
                          <string>1</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.9e+08</real>
                          <key>exams</key>
                          <array>
                            <dict>
                              <key>class</key>
                              <string>Exam</string>
                              <key>name</key>
                              <string>Subgrade 1</string>
                              <key>counted</key>
                              <integer>1</integer>
                              <key>mark</key>
                              <real>5.75</real>
                              <key>weight</key>
                              <string>1</string>
                              <key>dAtEaTtr:date</key>
                              <real>7.9e+08</real>
                              <key>exams</key>
                              <array/>
                            </dict>
                          </array>
                        </dict>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>Physique</string>
                          <key>counted</key>
                          <integer>1</integer>
                          <key>mark</key>
                          <real>0</real>
                          <key>weight</key>
                          <string>1</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.9e+08</real>
                          <key>exams</key>
                          <array>
                            <dict>
                              <key>class</key>
                              <string>Exam</string>
                              <key>name</key>
                              <string>Subgrade 1</string>
                              <key>counted</key>
                              <integer>1</integer>
                              <key>mark</key>
                              <real>4.5</real>
                              <key>weight</key>
                              <string>1</string>
                              <key>dAtEaTtr:date</key>
                              <real>7.9e+08</real>
                              <key>exams</key>
                              <array/>
                            </dict>
                          </array>
                        </dict>
                      </array>
                    </dict>
                  </array>
                </dict>
              </dict>
            </plist>
            """.trimIndent()
        )

        val option = importedState.subjects.first { it.isOptionSubject }
        assertEquals(InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH, importedState.selectedOption)
        assertEquals("PYAM", option.name)
        assertEquals(listOf("Physics", "Applications of Mathematics"), option.subSubjects.map { it.name })
        assertEquals(1, option.subSubjects[0].notes.size)
        assertEquals(1, option.subSubjects[1].notes.size)
        assertEquals(4.5, option.subSubjects[0].notes.single().value, 0.0001)
        assertEquals(5.75, option.subSubjects[1].notes.single().value, 0.0001)
        assertNotNull(option.optionChoice)
    }

    @Test
    fun parsePlusPointsExport_mapsSimpleOptionAndKeepsItsNotes() {
        val importedState = parsePlusPointsExport(
            """
            <plist version="1.0">
              <dict>
                <key>data</key>
                <dict>
                  <key>class</key>
                  <string>Semester</string>
                  <key>subjects</key>
                  <array>
                    <dict>
                      <key>class</key>
                      <string>Subject</string>
                      <key>name</key>
                      <string>OS Espagnol</string>
                      <key>counted</key>
                      <integer>1</integer>
                      <key>exams</key>
                      <array>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>Vocabulaire</string>
                          <key>counted</key>
                          <integer>1</integer>
                          <key>mark</key>
                          <real>5.25</real>
                          <key>weight</key>
                          <string>1</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.9e+08</real>
                          <key>exams</key>
                          <array/>
                        </dict>
                      </array>
                    </dict>
                  </array>
                </dict>
              </dict>
            </plist>
            """.trimIndent()
        )

        val option = importedState.subjects.first { it.isOptionSubject }
        assertEquals(InitialOptionChoice.SPANISH, importedState.selectedOption)
        assertEquals("Spanish", option.name)
        assertTrue(option.subSubjects.isEmpty())
        assertEquals(1, option.notes.size)
        assertEquals("Vocabulaire", option.notes.single().description)
        assertEquals(5.25, option.notes.single().value, 0.0001)
    }

    @Test
    fun parsePlusPointsExport_detectsSemesterFromRootName() {
        val importedState = parsePlusPointsExport(
            """
            <plist version="1.0">
              <dict>
                <key>data</key>
                <dict>
                  <key>class</key>
                  <string>Semester</string>
                  <key>name</key>
                  <string>Semestre 2</string>
                  <key>subjects</key>
                  <array>
                    <dict>
                      <key>class</key>
                      <string>Subject</string>
                      <key>name</key>
                      <string>Allemand</string>
                      <key>counted</key>
                      <integer>1</integer>
                      <key>exams</key>
                      <array>
                        <dict>
                          <key>class</key>
                          <string>Exam</string>
                          <key>name</key>
                          <string>Vocabulaire</string>
                          <key>counted</key>
                          <integer>1</integer>
                          <key>mark</key>
                          <real>5.25</real>
                          <key>weight</key>
                          <string>1</string>
                          <key>dAtEaTtr:date</key>
                          <real>7.9e+08</real>
                          <key>exams</key>
                          <array/>
                        </dict>
                      </array>
                    </dict>
                  </array>
                </dict>
              </dict>
            </plist>
            """.trimIndent()
        )

        val note = importedState.subjects.first { !it.isOptionSubject }.notes.single()
        assertEquals(SchoolSemester.SEMESTER_2, importedState.selectedSemester)
        assertEquals(SchoolSemester.SEMESTER_2, note.semester)
    }
}
