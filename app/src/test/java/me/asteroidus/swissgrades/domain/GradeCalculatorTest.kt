package me.asteroidus.swissgrades.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertThrows
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.FULL
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.HALF
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.QUARTER
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.OptionType
import me.asteroidus.swissgrades.domain.model.SubSubject

class GradeCalculatorTest {

    @Test
    fun weightedAverage_usesDeclaredCoefficients() {
        val grades = listOf(
            Grade(value = 5.0, weight = FULL),
            Grade(value = 4.5, weight = HALF)
        )

        assertEquals(
            4.83,
            GradeCalculator.roundToHundredth(GradeCalculator.weightedAverage(grades)!!),
            0.0
        )
    }

    @Test
    fun roundToHalf_roundsToNearestHalf() {
        assertEquals(4.5, GradeCalculator.roundToHalf(4.26), 0.0)
        assertEquals(5.0, GradeCalculator.roundToHalf(4.83), 0.0)
    }

    @Test
    fun weightedAverage_returnsNullForEmptyGrades() {
        assertNull(GradeCalculator.weightedAverage(emptyList()))
    }

    @Test
    fun computePromotionPoints_returnsPositivePointsAboveFour() {
        assertEquals(1.0, GradeCalculator.computePromotionPoints(5.0), 0.0)
    }

    @Test
    fun computePromotionPoints_returnsNegativePointsBelowFour() {
        assertEquals(-1.0, GradeCalculator.computePromotionPoints(3.5), 0.0)
    }

    @Test
    fun checkPromotionBlockingRules_failsWhenABranchIsBelowThree() {
        val branchAverages = listOf(5.0, 4.5, 2.5, 4.0)

        assertFalse(GradeCalculator.checkPromotionBlockingRules(branchAverages))
    }

    @Test
    fun checkPromotionBlockingRules_failsWhenMoreThanFourBranchesAreBelowFour() {
        val branchAverages = listOf(3.5, 3.5, 3.5, 3.5, 3.5, 4.5)

        assertFalse(GradeCalculator.checkPromotionBlockingRules(branchAverages))
    }

    @Test
    fun computeBasketSum_reachesRequiredThresholdAtSixteen() {
        val basketSum = GradeCalculator.computeBasketSum(
            germanAverage = 4.0,
            frenchAverage = 4.0,
            mathAverage = 4.0,
            optionAverage = 4.0
        )

        assertTrue(basketSum >= 16.0)
        assertEquals(16.0, basketSum, 0.0)
    }

    @Test
    fun computeBranchAverage_returnsNullForEmptySimpleBranch() {
        val branch = Branch.Simple.create(
            name = "Math",
            grades = emptyList()
        )

        assertNull(GradeCalculator.computeBranchAverage(branch))
    }

    @Test
    fun computeCompositeOptionAverage_returnsNullWhenOneSubSubjectIsEmpty() {
        val branch = Branch.Composite.create(
            name = "Biologie-Chimie",
            optionType = OptionType.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                SubSubject(
                    name = "Biologie",
                    grades = listOf(Grade(value = 5.0, weight = FULL))
                ),
                SubSubject(
                    name = "Chimie",
                    grades = emptyList()
                )
            )
        )

        assertNull(GradeCalculator.computeCompositeOptionAverage(branch))
        assertNull(GradeCalculator.computeBranchAverage(branch))
    }

    @Test
    fun simpleBranchCreation_rejectsSubSubjects() {
        assertThrows(IllegalArgumentException::class.java) {
            Branch.Simple.create(
                name = "Math",
                grades = listOf(Grade(value = 5.0, weight = FULL)),
                subSubjects = listOf(
                    SubSubject(name = "Ignored", grades = listOf(Grade(value = 4.0, weight = HALF)))
                )
            )
        }
    }

    @Test
    fun compositeBranchCreation_rejectsDirectGrades() {
        assertThrows(IllegalArgumentException::class.java) {
            Branch.Composite.create(
                name = "Biologie-Chimie",
                optionType = OptionType.BIOLOGY_CHEMISTRY,
                subSubjects = listOf(
                    SubSubject(name = "Biologie", grades = listOf(Grade(value = 5.0, weight = FULL))),
                    SubSubject(name = "Chimie", grades = listOf(Grade(value = 4.0, weight = FULL)))
                ),
                grades = listOf(Grade(value = 6.0, weight = FULL))
            )
        }
    }

    @Test
    fun compositeBranchCreation_rejectsWrongStructure() {
        assertThrows(IllegalArgumentException::class.java) {
            Branch.Composite.create(
                name = "Biologie-Chimie",
                optionType = OptionType.BIOLOGY_CHEMISTRY,
                subSubjects = listOf(
                    SubSubject(name = "Biologie", grades = listOf(Grade(value = 5.0, weight = FULL)))
                )
            )
        }
    }

    @Test
    fun computeCompositeOptionAverage_roundsBiologyChemistryToBulletinHalfGrade() {
        val branch = Branch.Composite.create(
            name = "Biologie-Chimie",
            optionType = OptionType.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                SubSubject(
                    name = "Biologie",
                    grades = listOf(
                        Grade(value = 2.25, weight = QUARTER),
                        Grade(value = 6.0, weight = FULL),
                        Grade(value = 6.0, weight = HALF)
                    )
                ),
                SubSubject(
                    name = "Chimie",
                    grades = listOf(
                        Grade(value = 1.0, weight = HALF),
                        Grade(value = 1.25, weight = QUARTER),
                        Grade(value = 6.0, weight = FULL)
                    )
                )
            )
        )

        val biologyAverage = GradeCalculator.roundToHundredth(
            GradeCalculator.weightedAverage(branch.subSubjects[0].grades)!!
        )
        val chemistryAverage = GradeCalculator.roundToHundredth(
            GradeCalculator.weightedAverage(branch.subSubjects[1].grades)!!
        )

        assertEquals(5.46, biologyAverage, 0.0)
        assertEquals(3.89, chemistryAverage, 0.0)
        assertEquals(4.5, GradeCalculator.computeCompositeOptionAverage(branch)!!, 0.0)
    }

    @Test
    fun computeBranchAverage_roundsWeightedAverageToHalfGrade() {
        val branch = Branch.Simple.create(
            name = "Math",
            grades = listOf(
                Grade(value = 5.0, weight = FULL),
                Grade(value = 4.5, weight = HALF)
            )
        )

        assertEquals(5.0, GradeCalculator.computeBranchAverage(branch)!!, 0.0)
    }
}
