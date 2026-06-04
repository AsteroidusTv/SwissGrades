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
            strings.resetSectionTitle,
            strings.periodTitle,
            strings.choosePeriodTitle,
            strings.schoolYearTitle,
            strings.semesterTitle,
            strings.addSubjectTitle,
            strings.editSubjectTitle,
            strings.gradeHistoryTitle,
            strings.addGrade,
            strings.targetSimulationTitle
        )

        assertEquals("Choisis ton option", strings.chooseOption)
        assertEquals("Mes branches", strings.mySubjects)
        assertEquals("Paramètres", strings.optionSettingsTitle)
        assertEquals("Simulateur de note", strings.targetSimulationTitle)

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
}
