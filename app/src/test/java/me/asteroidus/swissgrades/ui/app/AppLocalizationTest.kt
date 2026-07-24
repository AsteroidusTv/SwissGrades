package me.asteroidus.swissgrades.ui.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppLocalizationTest {

    @Test
    fun frenchMainLabels_doNotFallbackToEnglish() {
        val strings = AppStrings.French
        val mainLabels = listOf(
            strings.chooseOption,
            strings.continueLabel,
            strings.mySubjects,
            strings.addLabel,
            strings.optionSettingsTitle,
            strings.languageSectionTitle,
            strings.themeSectionTitle,
            strings.backupSectionTitle,
            strings.gradeReportSectionTitle,
            strings.exportGradeReportLabel,
            strings.gradeReportExportTitle,
            strings.gradeReportExportConfirm,
            strings.gradeReportExportSuccess,
            strings.gradeReportExportFailure,
            strings.resetSectionTitle,
            strings.periodTitle,
            strings.choosePeriodTitle,
            strings.schoolYearTitle,
            strings.semesterTitle,
            strings.addSubjectTitle,
            strings.editSubjectTitle,
            strings.gradeHistoryTitle,
            strings.addGrade,
            strings.targetSimulationTitle,
            strings.gradeImpactTitle,
            strings.gradeImpactWithGrade,
            strings.gradeImpactWithoutGrade,
            strings.gradeImpactDelta,
            strings.gradeImpactUnavailable
        )

        assertEquals("Choisis ton option", strings.chooseOption)
        assertEquals("Mes branches", strings.mySubjects)
        assertEquals("Paramètres", strings.optionSettingsTitle)
        assertEquals("Simulateur de note", strings.targetSimulationTitle)
        assertEquals("Moyenne générale", strings.overallAverageTitle)
        assertEquals("4 branches notées", strings.contributingSubjects(4))
        assertEquals("Impact actuel", strings.gradeImpactTitle)
        assertEquals("Relevé de résultats PDF", strings.gradeReportSectionTitle)
        assertEquals(
            "Troisième année · Situation cumulative S1 + S2",
            strings.gradeReportPeriodLabel(SchoolYear.YEAR_3, SchoolSemester.SEMESTER_2)
        )

        val forbiddenEnglishTerms = listOf(
            "Choose",
            "My subjects",
            "Settings",
            "Language",
            "Appearance",
            "Reset app",
            "Period",
            "School year",
            "Add a subject",
            "Edit subject",
            "Grade simulator"
        )

        mainLabels.forEach { label ->
            forbiddenEnglishTerms.forEach { forbidden ->
                assertFalse(
                    "French label should not contain English fallback '$forbidden': $label",
                    label.contains(forbidden, ignoreCase = false)
                )
            }
        }
    }

    @Test
    fun simulationResultTitles_distinguishSingleAndMultipleGrades() {
        assertEquals("Needed next grade", AppStrings.English.requiredSimulationTitle(1))
        assertEquals("Average needed over 3 grades", AppStrings.English.requiredSimulationTitle(3))
        assertEquals("Note nécessaire", AppStrings.French.requiredSimulationTitle(1))
        assertEquals("Moyenne nécessaire sur 3 notes", AppStrings.French.requiredSimulationTitle(3))
    }
}
