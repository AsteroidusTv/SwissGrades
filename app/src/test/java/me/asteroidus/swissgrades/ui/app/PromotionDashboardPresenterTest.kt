package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromotionDashboardPresenterTest {

    @Test
    fun present_mapsStructuredStatusesInEnglishAndFrench() {
        val expectations = listOf(
            StatusExpectation(
                status = PromotionStatus.PROMOTED,
                englishLabel = AppStrings.English.promotionStatusPromoted,
                englishHeadline = AppStrings.English.promotionHeadlinePromoted,
                frenchLabel = AppStrings.French.promotionStatusPromoted,
                frenchHeadline = AppStrings.French.promotionHeadlinePromoted,
                tone = DashboardStatusTone.POSITIVE
            ),
            StatusExpectation(
                status = PromotionStatus.BLOCKED,
                englishLabel = AppStrings.English.promotionStatusBlocked,
                englishHeadline = AppStrings.English.promotionHeadlineBlocked,
                frenchLabel = AppStrings.French.promotionStatusBlocked,
                frenchHeadline = AppStrings.French.promotionHeadlineBlocked,
                tone = DashboardStatusTone.NEGATIVE
            ),
            StatusExpectation(
                status = PromotionStatus.INCOMPLETE,
                englishLabel = AppStrings.English.promotionStatusIncomplete,
                englishHeadline = AppStrings.English.promotionHeadlineIncomplete,
                frenchLabel = AppStrings.French.promotionStatusIncomplete,
                frenchHeadline = AppStrings.French.promotionHeadlineIncomplete,
                tone = DashboardStatusTone.NEUTRAL
            )
        )

        expectations.forEach { expectation ->
            val result = promotionResult(status = expectation.status, basketTotal = 16.0)
            val english = PromotionDashboardPresenter.present(result, AppStrings.English)
            val french = PromotionDashboardPresenter.present(result, AppStrings.French)

            assertEquals(expectation.englishLabel, english.statusLabel)
            assertEquals(expectation.englishHeadline, english.headline)
            assertEquals(expectation.frenchLabel, french.statusLabel)
            assertEquals(expectation.frenchHeadline, french.headline)
            assertEquals(expectation.tone, english.statusTone)
            assertEquals(expectation.tone, french.statusTone)
        }
    }

    @Test
    fun present_derivesCalculabilityFromStructuredBasketTotal() {
        val calculable = PromotionDashboardPresenter.present(
            promotionResult(status = PromotionStatus.PROMOTED, basketTotal = 16.0),
            AppStrings.English
        )
        val notCalculable = PromotionDashboardPresenter.present(
            promotionResult(status = PromotionStatus.INCOMPLETE, basketTotal = null),
            AppStrings.English
        )

        assertTrue(calculable.isCalculable)
        assertFalse(notCalculable.isCalculable)
    }

    private fun promotionResult(
        status: PromotionStatus,
        basketTotal: Double?
    ): PromotionEvaluationResult {
        return PromotionEvaluationResult(
            status = status,
            branchAverages = emptyList(),
            basketTotal = basketTotal,
            promotionPointsTotal = null,
            blockingReasons = emptyList(),
            missingDataReasons = emptyList()
        )
    }

    private data class StatusExpectation(
        val status: PromotionStatus,
        val englishLabel: String,
        val englishHeadline: String,
        val frenchLabel: String,
        val frenchHeadline: String,
        val tone: DashboardStatusTone
    )
}
