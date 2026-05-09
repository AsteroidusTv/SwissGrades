package me.asteroidus.swissgrades.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.FULL
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.HALF
import me.asteroidus.swissgrades.domain.model.AssessmentWeight.QUARTER
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.BranchAverageStatus
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.OptionType
import me.asteroidus.swissgrades.domain.model.PromotionBlockingReason
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationInput
import me.asteroidus.swissgrades.domain.model.PromotionMissingDataReason
import me.asteroidus.swissgrades.domain.model.PromotionRoleAssignment
import me.asteroidus.swissgrades.domain.model.PromotionStatus
import me.asteroidus.swissgrades.domain.model.SubSubject

class PromotionEvaluatorTest {

    @Test
    fun evaluate_returnsPromotedForValidCompleteDataWithSimpleOption() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 4.0)),
                PromotionRoleAssignment.Additional(simpleBranch("English", 4.5))
            )
        )

        assertEquals(PromotionStatus.PROMOTED, result.status)
        assertTrue(result.blockingReasons.isEmpty())
        assertTrue(result.missingDataReasons.isEmpty())
        assertEquals(17.0, result.basketTotal!!, 0.0)
        assertEquals(1.5, result.promotionPointsTotal!!, 0.0)
    }

    @Test
    fun evaluate_returnsPromotedForValidCompleteDataWithCompositeOption() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(compositeOptionBranch("Biology-Chemistry")),
                PromotionRoleAssignment.Additional(simpleBranch("History", 4.0))
            )
        )

        assertEquals(PromotionStatus.PROMOTED, result.status)
        assertTrue(result.blockingReasons.isEmpty())
        assertTrue(result.missingDataReasons.isEmpty())
        assertEquals(17.5, result.basketTotal!!, 0.0)
    }

    @Test
    fun promotionInput_rejectsNonOptionBranchInOptionRole() {
        assertThrows(IllegalArgumentException::class.java) {
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("History", 4.0))
            )
        }
    }

    @Test
    fun promotionInput_rejectsMissingOrDuplicateRequiredRoles() {
        assertThrows(IllegalArgumentException::class.java) {
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH))
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.German(simpleBranch("German 2", 4.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH))
            )
        }
    }

    @Test
    fun promotionInput_rejectsDuplicateBranchNamesAcrossRoles() {
        assertThrows(IllegalArgumentException::class.java) {
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("German", 4.0))
            )
        }
    }

    @Test
    fun evaluate_returnsIncompleteAndKeepsKnownBlockersWhenDataIsMissing() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.0)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 3.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 2.5)),
                PromotionRoleAssignment.Additional(Branch.Simple.create(name = "English", grades = emptyList()))
            )
        )

        assertEquals(PromotionStatus.INCOMPLETE, result.status)
        assertNull(result.promotionPointsTotal)
        assertEquals(15.0, result.basketTotal!!, 0.0)
        assertTrue(
            result.blockingReasons.contains(
                PromotionBlockingReason.BasketBelowThreshold(basketTotal = 15.0)
            )
        )
        assertTrue(
            result.blockingReasons.contains(
                PromotionBlockingReason.BranchAverageBelowThree(
                    branchName = "History",
                    average = 2.5
                )
            )
        )
        assertTrue(
            result.missingDataReasons.contains(
                PromotionMissingDataReason.MissingBranchAverage(branchName = "English")
            )
        )
    }

    @Test
    fun evaluate_reportsBranchBelowThreeIndividually() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.0)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 2.5))
            )
        )

        assertEquals(PromotionStatus.BLOCKED, result.status)
        assertTrue(
            result.blockingReasons.contains(
                PromotionBlockingReason.BranchAverageBelowThree(
                    branchName = "History",
                    average = 2.5
                )
            )
        )
    }

    @Test
    fun evaluate_reportsMoreThanFourBranchesBelowFour() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.0)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("English", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("Physics", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("Biology", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("Geography", 3.5))
            )
        )

        assertEquals(PromotionStatus.BLOCKED, result.status)
        assertTrue(
            result.blockingReasons.any {
                it is PromotionBlockingReason.MoreThanFourBranchesBelowFour &&
                    it.branchNames == listOf("History", "English", "Physics", "Biology", "Geography")
            }
        )
    }

    @Test
    fun evaluate_reportsBasketBelowSixteen() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.0)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 3.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 4.0))
            )
        )

        assertEquals(PromotionStatus.BLOCKED, result.status)
        assertEquals(15.0, result.basketTotal!!, 0.0)
        assertTrue(
            result.blockingReasons.contains(
                PromotionBlockingReason.BasketBelowThreshold(basketTotal = 15.0)
            )
        )
    }

    @Test
    fun evaluate_returnsIncompleteWhenABranchAverageIsMissing() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(Branch.Simple.create(name = "History", grades = emptyList()))
            )
        )

        assertEquals(PromotionStatus.INCOMPLETE, result.status)
        assertNull(result.promotionPointsTotal)
        assertEquals(17.0, result.basketTotal!!, 0.0)
        assertTrue(
            result.missingDataReasons.contains(
                PromotionMissingDataReason.MissingBranchAverage(branchName = "History")
            )
        )
        assertTrue(
            result.branchAverages.containsBranchAverage(
                branchName = "History",
                average = null,
                status = BranchAverageStatus.MISSING_OR_NON_CALCULABLE
            )
        )
    }

    @Test
    fun evaluate_marksExplicitlyEmptyAdditionalSubjectWithoutTreatingItAsMissing() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(
                    branch = Branch.Simple.create(name = "History", grades = emptyList()),
                    isExplicitlyEmpty = true
                )
            )
        )

        assertEquals(PromotionStatus.PROMOTED, result.status)
        assertTrue(result.missingDataReasons.isEmpty())
        assertEquals(17.0, result.basketTotal!!, 0.0)
        assertEquals(1.0, result.promotionPointsTotal!!, 0.0)
        assertTrue(
            result.branchAverages.containsBranchAverage(
                branchName = "History",
                average = null,
                status = BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL
            )
        )
    }

    @Test
    fun evaluate_treatsInvalidAdditionalSubjectAsNonCalculableInsteadOfEmptyOptional() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.5)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(
                    branch = Branch.Simple.create(name = "History", grades = emptyList()),
                    isExplicitlyEmpty = false
                )
            )
        )

        assertEquals(PromotionStatus.INCOMPLETE, result.status)
        assertNull(result.promotionPointsTotal)
        assertTrue(
            result.missingDataReasons.contains(
                PromotionMissingDataReason.MissingBranchAverage(branchName = "History")
            )
        )
        assertTrue(
            result.branchAverages.containsBranchAverage(
                branchName = "History",
                average = null,
                status = BranchAverageStatus.MISSING_OR_NON_CALCULABLE
            )
        )
    }

    @Test
    fun evaluate_doesNotTriggerBelowThreeBlockerAtExactlyThree() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 3.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.5)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.5)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 4.0))
            )
        )

        assertFalse(result.blockingReasons.any { it is PromotionBlockingReason.BranchAverageBelowThree })
    }

    @Test
    fun evaluate_doesNotTriggerBelowFourCountBlockerAtExactlyFourBranches() {
        val result = PromotionEvaluator.evaluate(
            promotionInput(
                PromotionRoleAssignment.German(simpleBranch("German", 4.0)),
                PromotionRoleAssignment.French(simpleBranch("French", 4.0)),
                PromotionRoleAssignment.Math(simpleBranch("Math", 4.0)),
                PromotionRoleAssignment.Option(simpleBranch("Option", 4.0, OptionType.SPANISH)),
                PromotionRoleAssignment.Additional(simpleBranch("History", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("English", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("Physics", 3.5)),
                PromotionRoleAssignment.Additional(simpleBranch("Biology", 3.5))
            )
        )

        assertEquals(PromotionStatus.PROMOTED, result.status)
        assertFalse(result.blockingReasons.any { it is PromotionBlockingReason.MoreThanFourBranchesBelowFour })
    }

    private fun simpleBranch(
        name: String,
        average: Double,
        optionType: OptionType? = null
    ): Branch.Simple {
        return Branch.Simple.create(
            name = name,
            grades = listOf(Grade(value = average, weight = FULL)),
            optionType = optionType
        )
    }

    private fun compositeOptionBranch(name: String): Branch.Composite {
        return Branch.Composite.create(
            name = name,
            optionType = OptionType.BIOLOGY_CHEMISTRY,
            subSubjects = listOf(
                SubSubject(
                    name = "Biology",
                    grades = listOf(
                        Grade(value = 2.25, weight = QUARTER),
                        Grade(value = 6.0, weight = FULL),
                        Grade(value = 6.0, weight = HALF)
                    )
                ),
                SubSubject(
                    name = "Chemistry",
                    grades = listOf(
                        Grade(value = 1.0, weight = HALF),
                        Grade(value = 1.25, weight = QUARTER),
                        Grade(value = 6.0, weight = FULL)
                    )
                )
            )
        )
    }

    private fun promotionInput(
        vararg assignments: PromotionRoleAssignment
    ): PromotionEvaluationInput {
        return PromotionEvaluationInput.create(assignments.toList())
    }

    private fun List<me.asteroidus.swissgrades.domain.model.BranchAverageResult>.containsBranchAverage(
        branchName: String,
        average: Double?,
        status: BranchAverageStatus
    ): Boolean {
        return any {
            it.branchName == branchName &&
                it.average == average &&
                it.status == status
        }
    }
}
