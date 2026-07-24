package me.asteroidus.swissgrades.ui.app

import android.graphics.Bitmap
import android.graphics.Color
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

    @Test
    fun firstPageLayoutKeepsHeaderSummaryAndSubjectContentSeparated() {
        val file = File.createTempFile("grade-report-layout", ".pdf")
        try {
            FileOutputStream(file).use { outputStream ->
                writeGradeReportPdf(sampleReport(noteCount = 2), AppLanguage.FRENCH, outputStream)
            }

            val bitmap = renderFirstPage(file)
            val summaryRows = (0 until bitmap.height).filter { y ->
                (0 until bitmap.width).count { x ->
                    isSummaryBackground(bitmap.getPixel(x, y))
                } > 300
            }
            val summaryTop = summaryRows.first()
            val summaryBottom = summaryRows.last()

            val logoBottom = (0 until summaryTop)
                .filter { y -> rowContains(bitmap, y, ::isAccentBlue) }
                .max()
            val titleTop = (0 until summaryTop)
                .first { y -> rowContains(bitmap, y, ::isDarkText) }
            assertTrue("The header logo must not overlap the title.", logoBottom < titleTop)

            val summaryTextBands = contiguousBands(
                (summaryTop..summaryBottom).filter { y ->
                    (42..280).count { x -> isDarkText(bitmap.getPixel(x, y)) } > 2
                }
            )
            assertTrue(
                "The three summary labels and values must form six separate rows.",
                summaryTextBands.size >= 6
            )

            val dividerY = ((summaryBottom + 1) until bitmap.height)
                .first { y ->
                    (42..552).count { x -> isDivider(bitmap.getPixel(x, y)) } > 400
                }
            val firstSubjectPixelY = ((dividerY + 1) until bitmap.height)
                .first { y ->
                    (42..400).count { x -> isDarkText(bitmap.getPixel(x, y)) } > 2
                }
            assertTrue(
                "Subject titles need visible padding above them.",
                firstSubjectPixelY - dividerY >= 8
            )
        } finally {
            file.delete()
        }
    }

    private fun renderFirstPage(file: File): Bitmap {
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                renderer.openPage(0).use { page ->
                    Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }
    }

    private fun rowContains(bitmap: Bitmap, y: Int, predicate: (Int) -> Boolean): Boolean {
        return (30 until bitmap.width - 30).count { x -> predicate(bitmap.getPixel(x, y)) } > 2
    }

    private fun contiguousBands(rows: List<Int>): List<IntRange> {
        if (rows.isEmpty()) return emptyList()
        val bands = mutableListOf<IntRange>()
        var start = rows.first()
        var previous = start
        rows.drop(1).forEach { row ->
            if (row > previous + 1) {
                bands += start..previous
                start = row
            }
            previous = row
        }
        bands += start..previous
        return bands
    }

    private fun isAccentBlue(pixel: Int): Boolean {
        return Color.red(pixel) in 30..100 &&
            Color.green(pixel) in 80..170 &&
            Color.blue(pixel) in 150..255
    }

    private fun isDarkText(pixel: Int): Boolean {
        return Color.red(pixel) < 90 &&
            Color.green(pixel) < 100 &&
            Color.blue(pixel) < 120
    }

    private fun isSummaryBackground(pixel: Int): Boolean {
        return Color.red(pixel) in 225..245 &&
            Color.green(pixel) in 235..250 &&
            Color.blue(pixel) in 248..255
    }

    private fun isDivider(pixel: Int): Boolean {
        return Color.red(pixel) in 205..230 &&
            Color.green(pixel) in 215..238 &&
            Color.blue(pixel) in 225..248
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
