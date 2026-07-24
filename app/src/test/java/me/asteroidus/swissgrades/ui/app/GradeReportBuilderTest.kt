package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import me.asteroidus.swissgrades.domain.model.PromotionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeReportBuilderTest {

    @Test
    fun semesterOneReportExcludesSemesterTwoGrades() {
        val state = completeSimpleState(semester = SchoolSemester.SEMESTER_1)

        val report = GradeReportBuilder.build(state, generatedAtEpochMillis = 123L)

        val german = report.subjects.single { it.sourceName == "German" }
        assertEquals(listOf(4.0), german.notes.map { it.value })
        assertEquals(4.0, german.officialAverage)
        assertEquals(4.5, report.overallAverage)
        assertEquals(18.0, report.basketTotal)
        assertEquals(PromotionStatus.PROMOTED, report.promotionStatus)
        assertEquals(123L, report.generatedAtEpochMillis)
    }

    @Test
    fun semesterTwoReportIsCumulativeAndKeepsGradeOrigins() {
        val state = completeSimpleState(semester = SchoolSemester.SEMESTER_2)

        val report = GradeReportBuilder.build(state)

        val german = report.subjects.single { it.sourceName == "German" }
        assertEquals(listOf(4.0, 6.0), german.notes.map { it.value })
        assertEquals(
            listOf(SchoolSemester.SEMESTER_1, SchoolSemester.SEMESTER_2),
            german.notes.map { it.semester }
        )
        assertEquals(5.0, german.officialAverage)
        assertEquals(4.75, report.overallAverage)
        assertEquals(19.0, report.basketTotal)
    }

    @Test
    fun compositeSubjectContainsStructuredSubSubjects() {
        val option = StoredSubject(
            id = "option",
            name = "BICH",
            schoolYear = SchoolYear.YEAR_2,
            isCounted = true,
            isInBasket = true,
            isOptionSubject = true,
            optionChoice = InitialOptionChoice.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                StoredSubSubject(
                    id = "biology",
                    name = "Biology",
                    notes = listOf(note("biology-1", 5.0, SchoolSemester.SEMESTER_1))
                ),
                StoredSubSubject(
                    id = "chemistry",
                    name = "Chemistry",
                    notes = listOf(note("chemistry-1", 4.5, SchoolSemester.SEMESTER_1))
                )
            )
        )
        val report = GradeReportBuilder.build(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                selectedYear = SchoolYear.YEAR_2,
                selectedSemester = SchoolSemester.SEMESTER_1,
                subjects = listOf(option)
            )
        )

        val subject = report.subjects.single()
        assertEquals(5.0, subject.officialAverage)
        assertEquals(4.75, subject.detailedAverage)
        assertEquals(listOf("Biology", "Chemistry"), subject.subSubjects.map { it.sourceName })
        assertEquals(5.0, subject.subSubjects.first().average)
        assertEquals(0, subject.notes.size)
        assertNull(report.basketTotal)
        assertNull(report.promotionStatus)
    }

    @Test
    fun excludedSubjectHasNoPromotionPoints() {
        val excluded = StoredSubject(
            id = "sport",
            name = "Sport",
            schoolYear = SchoolYear.YEAR_1,
            isCounted = false,
            isInBasket = false,
            notes = listOf(note("sport-1", 3.0, SchoolSemester.SEMESTER_1))
        )

        val report = GradeReportBuilder.build(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.OTHER,
                subjects = listOf(excluded)
            )
        )

        assertNull(report.subjects.single().promotionPoints)
        assertNull(report.overallAverage)
        assertEquals(0, report.insufficiencyCount)
    }

    private fun completeSimpleState(semester: SchoolSemester): GradeTrackerAppState {
        return GradeTrackerAppState(
            selectedOption = InitialOptionChoice.OTHER,
            selectedSemester = semester,
            subjects = listOf(
                simpleSubject(
                    id = "option",
                    name = "Other",
                    value = 5.0,
                    isOption = true,
                    isInBasket = true
                ),
                simpleSubject("german", "German", 4.0, isInBasket = true, secondValue = 6.0),
                simpleSubject("french", "French", 4.5, isInBasket = true),
                simpleSubject("math", "Math", 4.5, isInBasket = true)
            )
        )
    }

    private fun simpleSubject(
        id: String,
        name: String,
        value: Double,
        isOption: Boolean = false,
        isInBasket: Boolean,
        secondValue: Double? = null
    ): StoredSubject {
        return StoredSubject(
            id = id,
            name = name,
            isCounted = true,
            isInBasket = isInBasket,
            isOptionSubject = isOption,
            optionChoice = if (isOption) InitialOptionChoice.OTHER else null,
            notes = buildList {
                add(note("$id-s1", value, SchoolSemester.SEMESTER_1))
                secondValue?.let {
                    add(note("$id-s2", it, SchoolSemester.SEMESTER_2))
                }
            }
        )
    }

    private fun note(
        id: String,
        value: Double,
        semester: SchoolSemester
    ): StoredNote {
        return StoredNote(
            id = id,
            value = value,
            weight = AssessmentWeight.FULL,
            description = "Exam",
            createdAtEpochMillis = 1_700_000_000_000L,
            semester = semester
        )
    }
}
