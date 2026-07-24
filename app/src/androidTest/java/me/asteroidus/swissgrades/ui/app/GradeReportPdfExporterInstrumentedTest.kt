package me.asteroidus.swissgrades.ui.app

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.io.FileOutputStream
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import me.asteroidus.swissgrades.domain.model.PromotionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradeReportPdfExporterInstrumentedTest {

    @Test
    fun generatedPdfOpensWithAndroidPdfRenderer() {
        val file = File.createTempFile("grade-report", ".pdf")
        try {
            val pageCount = FileOutputStream(file).use { outputStream ->
                writeGradeReportPdf(sampleReport(noteCount = 2), AppLanguage.FRENCH, outputStream)
            }

            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    assertEquals(pageCount, renderer.pageCount)
                    assertEquals(1, renderer.pageCount)
                }
            }
            assertTrue(file.length() > 1_000L)
        } finally {
            file.delete()
        }
    }

    @Test
    fun longReportCreatesMultipleValidPages() {
        val file = File.createTempFile("long-grade-report", ".pdf")
        try {
            val pageCount = FileOutputStream(file).use { outputStream ->
                writeGradeReportPdf(sampleReport(noteCount = 80), AppLanguage.ENGLISH, outputStream)
            }

            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    assertEquals(pageCount, renderer.pageCount)
                    assertTrue(renderer.pageCount > 1)
                }
            }
        } finally {
            file.delete()
        }
    }

    private fun sampleReport(noteCount: Int): GradeReport {
        return GradeReport(
            schoolYear = SchoolYear.YEAR_3,
            semester = SchoolSemester.SEMESTER_2,
            generatedAtEpochMillis = 1_700_000_000_000L,
            overallAverage = 4.83,
            promotionStatus = PromotionStatus.PROMOTED,
            promotionPoints = 3.5,
            basketTotal = 18.5,
            insufficiencyCount = 1,
            subjects = listOf(
                GradeReportSubject(
                    sourceName = "German",
                    optionChoice = null,
                    isCounted = true,
                    isInBasket = true,
                    isOptionSubject = false,
                    officialAverage = 5.0,
                    detailedAverage = 4.83,
                    promotionPoints = 1.0,
                    notes = List(noteCount) { index ->
                        GradeReportNote(
                            value = 4.0 + ((index % 5) * 0.25),
                            weight = AssessmentWeight.entries[index % AssessmentWeight.entries.size],
                            description = "Assessment ${index + 1} with a readable description",
                            createdAtEpochMillis = 1_700_000_000_000L + index,
                            semester = if (index % 2 == 0) {
                                SchoolSemester.SEMESTER_1
                            } else {
                                SchoolSemester.SEMESTER_2
                            }
                        )
                    },
                    subSubjects = emptyList()
                ),
                GradeReportSubject(
                    sourceName = "BICH",
                    optionChoice = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                    isCounted = true,
                    isInBasket = true,
                    isOptionSubject = true,
                    officialAverage = 5.0,
                    detailedAverage = 4.75,
                    promotionPoints = 1.0,
                    notes = emptyList(),
                    subSubjects = listOf(
                        GradeReportSubSubject(
                            sourceName = "Biology",
                            average = 5.0,
                            notes = listOf(
                                GradeReportNote(
                                    value = 5.0,
                                    weight = AssessmentWeight.FULL,
                                    description = "Biology exam",
                                    createdAtEpochMillis = 1_700_000_000_000L,
                                    semester = SchoolSemester.SEMESTER_1
                                )
                            )
                        ),
                        GradeReportSubSubject(
                            sourceName = "Chemistry",
                            average = 4.5,
                            notes = listOf(
                                GradeReportNote(
                                    value = 4.5,
                                    weight = AssessmentWeight.HALF,
                                    description = "Chemistry exam",
                                    createdAtEpochMillis = 1_700_000_000_000L,
                                    semester = SchoolSemester.SEMESTER_2
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
