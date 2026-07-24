package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.PromotionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class GradeReportLocalizationTest {

    @Test
    fun frenchReportCopyUsesCumulativeSemesterMeaning() {
        val text = GradeReportText.forLanguage(AppLanguage.FRENCH)

        assertEquals(
            "Troisième année · Situation cumulative S1 + S2",
            text.period(year = SchoolYear.YEAR_3, semester = SchoolSemester.SEMESTER_2)
        )
        assertEquals(
            "Première année · Semestre 1",
            text.period(year = SchoolYear.YEAR_1, semester = SchoolSemester.SEMESTER_1)
        )
        assertEquals("Conditions de promotion remplies", text.status(PromotionStatus.PROMOTED))
        assertEquals("Pas encore calculable", text.status(null))
    }

    @Test
    fun englishReportCopyUsesCumulativeSemesterMeaning() {
        val text = GradeReportText.forLanguage(AppLanguage.ENGLISH)

        assertEquals(
            "Second year · Cumulative situation S1 + S2",
            text.period(year = SchoolYear.YEAR_2, semester = SchoolSemester.SEMESTER_2)
        )
        assertEquals("Promotion blocked", text.status(PromotionStatus.BLOCKED))
    }
}
