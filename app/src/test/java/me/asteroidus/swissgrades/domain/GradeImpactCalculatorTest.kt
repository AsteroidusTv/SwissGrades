package me.asteroidus.swissgrades.domain

import me.asteroidus.swissgrades.domain.model.AssessmentWeight.FULL
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.HALF
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.OptionType
import me.asteroidus.swissgrades.domain.model.SubSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeImpactCalculatorTest {

    @Test
    fun simpleImpact_comparesOfficialAveragesWithAndWithoutTargetGrade() {
        val impact = GradeImpactCalculator.calculateSimple(
            grades = listOf(
                Grade(value = 4.0, weight = FULL),
                Grade(value = 6.0, weight = FULL)
            ),
            targetIndex = 1
        )

        assertEquals(5.0, impact.withGradeAverage, 0.0)
        assertEquals(4.0, impact.withoutGradeAverage!!, 0.0)
        assertEquals(1.0, impact.officialAverageDelta!!, 0.0)
    }

    @Test
    fun simpleImpact_usesWeightAndCanHaveNoOfficialDelta() {
        val impact = GradeImpactCalculator.calculateSimple(
            grades = listOf(
                Grade(value = 5.0, weight = FULL),
                Grade(value = 5.5, weight = HALF)
            ),
            targetIndex = 1
        )

        assertEquals(5.0, impact.withGradeAverage, 0.0)
        assertEquals(5.0, impact.withoutGradeAverage!!, 0.0)
        assertEquals(0.0, impact.officialAverageDelta!!, 0.0)
    }

    @Test
    fun simpleImpact_withoutOnlyGradeIsNotCalculable() {
        val impact = GradeImpactCalculator.calculateSimple(
            grades = listOf(Grade(value = 5.0, weight = FULL)),
            targetIndex = 0
        )

        assertEquals(5.0, impact.withGradeAverage, 0.0)
        assertNull(impact.withoutGradeAverage)
        assertNull(impact.officialAverageDelta)
    }

    @Test
    fun compositeImpact_usesCompositeRoundingRules() {
        val branch = Branch.Composite.create(
            name = "BICH",
            optionType = OptionType.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                SubSubject(
                    name = "Biology",
                    grades = listOf(
                        Grade(value = 5.0, weight = FULL),
                        Grade(value = 4.0, weight = FULL)
                    )
                ),
                SubSubject(
                    name = "Chemistry",
                    grades = listOf(Grade(value = 4.0, weight = FULL))
                )
            )
        )

        val impact = GradeImpactCalculator.calculateComposite(
            branch = branch,
            targetSubSubjectIndex = 0,
            targetGradeIndex = 0
        )!!

        assertEquals(4.5, impact.withGradeAverage, 0.0)
        assertEquals(4.0, impact.withoutGradeAverage!!, 0.0)
        assertEquals(0.5, impact.officialAverageDelta!!, 0.0)
    }

    @Test
    fun compositeImpact_withoutRequiredSubSubjectGradeIsNotCalculable() {
        val branch = Branch.Composite.create(
            name = "BICH",
            optionType = OptionType.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                SubSubject(
                    name = "Biology",
                    grades = listOf(Grade(value = 5.0, weight = FULL))
                ),
                SubSubject(
                    name = "Chemistry",
                    grades = listOf(Grade(value = 4.0, weight = FULL))
                )
            )
        )

        val impact = GradeImpactCalculator.calculateComposite(
            branch = branch,
            targetSubSubjectIndex = 0,
            targetGradeIndex = 0
        )!!

        assertEquals(4.5, impact.withGradeAverage, 0.0)
        assertNull(impact.withoutGradeAverage)
        assertNull(impact.officialAverageDelta)
    }

    @Test
    fun compositeImpact_isUnavailableWhenCurrentBranchIsNotCalculable() {
        val branch = Branch.Composite.create(
            name = "BICH",
            optionType = OptionType.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                SubSubject(
                    name = "Biology",
                    grades = listOf(Grade(value = 5.0, weight = FULL))
                ),
                SubSubject(name = "Chemistry", grades = emptyList())
            )
        )

        val impact = GradeImpactCalculator.calculateComposite(
            branch = branch,
            targetSubSubjectIndex = 0,
            targetGradeIndex = 0
        )

        assertNull(impact)
    }
}
