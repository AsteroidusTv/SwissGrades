package me.asteroidus.swissgrades.ui.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlusPointsParserInstrumentedTest {

    @Test
    fun validExportParsesOnAndroidRuntime() {
        val state = parsePlusPointsExport(
            """
            <plist version="1.0">
              <dict>
                <key>data</key>
                <dict>
                  <key>class</key>
                  <string>Semester</string>
                  <key>name</key>
                  <string>Semestre 1</string>
                  <key>subjects</key>
                  <array/>
                </dict>
              </dict>
            </plist>
            """.trimIndent()
        )

        assertEquals(SchoolSemester.SEMESTER_1, state.selectedSemester)
    }

    @Test
    fun documentTypeIsRejectedOnAndroidRuntime() {
        assertThrows(IllegalStateException::class.java) {
            parsePlusPointsExport(
                """
                <!DOCTYPE plist [
                  <!ENTITY injected SYSTEM "file:///data/data/private">
                ]>
                <plist version="1.0">
                  <dict>
                    <key>data</key>
                    <dict>
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
}
