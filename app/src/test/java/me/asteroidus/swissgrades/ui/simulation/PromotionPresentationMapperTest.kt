package me.asteroidus.swissgrades.ui.simulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import me.asteroidus.swissgrades.domain.model.BranchAverageResult
import me.asteroidus.swissgrades.domain.model.BranchAverageStatus
import me.asteroidus.swissgrades.domain.model.PromotionBlockingReason
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionMissingDataReason
import me.asteroidus.swissgrades.domain.model.PromotionStatus

class PromotionPresentationMapperTest {

    @Test
    fun map_returnsStablePresentationForPromotedResult() {
        val presentation = PromotionPresentationMapper.map(
            PromotionEvaluationResult(
                status = PromotionStatus.PROMOTED,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.5, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("French", 4.0, BranchAverageStatus.COMPUTED)
                ),
                basketTotal = 17.0,
                promotionPointsTotal = 1.5,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        assertEquals("Promoted", presentation.statusLabel)
        assertEquals("Promotion requirements are currently satisfied.", presentation.headline)
        assertEquals("17.00", presentation.basketTotal.valueLabel)
        assertEquals("+1.50", presentation.promotionPointsTotal.valueLabel)
        assertEquals("4.50", presentation.branchAverages.first().valueLabel)
        assertEquals(null, presentation.branchAverages.first().detailLabel)
    }

    @Test
    fun map_returnsStablePresentationForBlockedResultWithMultipleReasons() {
        val presentation = PromotionPresentationMapper.map(
            PromotionEvaluationResult(
                status = PromotionStatus.BLOCKED,
                branchAverages = listOf(BranchAverageResult("History", 2.5, BranchAverageStatus.COMPUTED)),
                basketTotal = 15.0,
                promotionPointsTotal = -2.0,
                blockingReasons = listOf(
                    PromotionBlockingReason.BranchAverageBelowThree("History", 2.5),
                    PromotionBlockingReason.BasketBelowThreshold(15.0),
                    PromotionBlockingReason.MoreThanFourBranchesBelowFour(
                        listOf("History", "English", "Physics", "Biology", "Geography")
                    )
                ),
                missingDataReasons = emptyList()
            )
        )

        assertEquals("Blocked", presentation.statusLabel)
        assertEquals("Promotion requirements are not satisfied.", presentation.headline)
        assertEquals(3, presentation.blockingMessages.size)
        assertTrue(presentation.blockingMessages.contains("History is below 3.0 with an average of 2.50."))
        assertTrue(presentation.blockingMessages.contains("The basket total is 15.00, below the required 16.00."))
        assertTrue(
            presentation.blockingMessages.contains(
                "More than 4 branches are below 4.0: History, English, Physics, Biology, Geography."
            )
        )
    }

    @Test
    fun map_returnsStablePresentationForIncompleteResultWithMissingData() {
        val presentation = PromotionPresentationMapper.map(
            PromotionEvaluationResult(
                status = PromotionStatus.INCOMPLETE,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.0, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("English", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
                ),
                basketTotal = 15.0,
                promotionPointsTotal = null,
                blockingReasons = emptyList(),
                missingDataReasons = listOf(
                    PromotionMissingDataReason.MissingBranchAverage("English")
                )
            )
        )

        assertEquals("Incomplete", presentation.statusLabel)
        assertEquals(
            "Promotion cannot be decided yet because some data is missing.",
            presentation.headline
        )
        assertEquals("No average available", presentation.branchAverages[1].valueLabel)
        assertEquals("This branch cannot be evaluated yet.", presentation.branchAverages[1].detailLabel)
        assertEquals("Not available", presentation.promotionPointsTotal.valueLabel)
        assertEquals(listOf("No average is available for English."), presentation.missingDataMessages)
    }

    @Test
    fun map_keepsKnownBlockersWhenStatusIsIncomplete() {
        val presentation = PromotionPresentationMapper.map(
            PromotionEvaluationResult(
                status = PromotionStatus.INCOMPLETE,
                branchAverages = listOf(
                    BranchAverageResult("Option", 3.0, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("History", 2.5, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("English", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
                ),
                basketTotal = 15.0,
                promotionPointsTotal = null,
                blockingReasons = listOf(
                    PromotionBlockingReason.BranchAverageBelowThree("History", 2.5),
                    PromotionBlockingReason.BasketBelowThreshold(15.0)
                ),
                missingDataReasons = listOf(
                    PromotionMissingDataReason.MissingBranchAverage("English")
                )
            )
        )

        assertEquals("Incomplete", presentation.statusLabel)
        assertTrue(presentation.blockingMessages.contains("History is below 3.0 with an average of 2.50."))
        assertTrue(presentation.blockingMessages.contains("The basket total is 15.00, below the required 16.00."))
        assertEquals(listOf("No average is available for English."), presentation.missingDataMessages)
    }

    @Test
    fun map_formatsBasketAndPromotionPointsConsistently() {
        val presentation = PromotionPresentationMapper.map(
            PromotionEvaluationResult(
                status = PromotionStatus.BLOCKED,
                branchAverages = emptyList(),
                basketTotal = 16.5,
                promotionPointsTotal = -1.0,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        assertEquals("Basket total", presentation.basketTotal.label)
        assertEquals("16.50", presentation.basketTotal.valueLabel)
        assertEquals("Promotion points total", presentation.promotionPointsTotal.label)
        assertEquals("-1.00", presentation.promotionPointsTotal.valueLabel)
    }

    @Test
    fun map_usesDedicatedPresentationForEmptyOptionalAdditionalSubject() {
        val presentation = PromotionPresentationMapper.map(
            PromotionEvaluationResult(
                status = PromotionStatus.PROMOTED,
                branchAverages = listOf(
                    BranchAverageResult("History", null, BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL)
                ),
                basketTotal = 16.0,
                promotionPointsTotal = 0.0,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        assertEquals("Not evaluated", presentation.branchAverages.single().valueLabel)
        assertEquals("Optional subject left empty.", presentation.branchAverages.single().detailLabel)
    }

    @Test
    fun eachTypedReasonMapsToStableEnglishMessage() {
        assertEquals(
            "Math is below 3.0 with an average of 2.75.",
            PromotionPresentationMapper.mapBlockingReason(
                PromotionBlockingReason.BranchAverageBelowThree(
                    branchName = "Math",
                    average = 2.75
                )
            )
        )
        assertEquals(
            "More than 4 branches are below 4.0: A, B, C, D, E.",
            PromotionPresentationMapper.mapBlockingReason(
                PromotionBlockingReason.MoreThanFourBranchesBelowFour(
                    branchNames = listOf("A", "B", "C", "D", "E")
                )
            )
        )
        assertEquals(
            "The basket total is 15.25, below the required 16.00.",
            PromotionPresentationMapper.mapBlockingReason(
                PromotionBlockingReason.BasketBelowThreshold(basketTotal = 15.25)
            )
        )
        assertEquals(
            "No average is available for German.",
            PromotionPresentationMapper.mapMissingDataReason(
                PromotionMissingDataReason.MissingBranchAverage("German")
            )
        )
        assertEquals(
            "Not evaluated",
            PromotionPresentationMapper.mapBranchAverageValueLabel(
                BranchAverageResult("History", null, BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL)
            )
        )
        assertEquals(
            "Optional subject left empty.",
            PromotionPresentationMapper.mapBranchAverageDetailLabel(
                BranchAverageResult("History", null, BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL)
            )
        )
        assertEquals(
            "This branch cannot be evaluated yet.",
            PromotionPresentationMapper.mapBranchAverageDetailLabel(
                BranchAverageResult("German", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
            )
        )
    }
}
