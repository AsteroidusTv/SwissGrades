package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionStatus

internal data class PromotionDashboardPresentation(
    val statusLabel: String,
    val headline: String,
    val isCalculable: Boolean,
    val statusTone: DashboardStatusTone
)

internal object PromotionDashboardPresenter {

    fun present(
        result: PromotionEvaluationResult,
        strings: AppStrings
    ): PromotionDashboardPresentation {
        val statusPresentation = when (result.status) {
            PromotionStatus.PROMOTED -> PromotionStatusPresentation(
                label = strings.promotionStatusPromoted,
                headline = strings.promotionHeadlinePromoted,
                tone = DashboardStatusTone.POSITIVE
            )

            PromotionStatus.BLOCKED -> PromotionStatusPresentation(
                label = strings.promotionStatusBlocked,
                headline = strings.promotionHeadlineBlocked,
                tone = DashboardStatusTone.NEGATIVE
            )

            PromotionStatus.INCOMPLETE -> PromotionStatusPresentation(
                label = strings.promotionStatusIncomplete,
                headline = strings.promotionHeadlineIncomplete,
                tone = DashboardStatusTone.NEUTRAL
            )
        }

        return PromotionDashboardPresentation(
            statusLabel = statusPresentation.label,
            headline = statusPresentation.headline,
            isCalculable = result.basketTotal != null,
            statusTone = statusPresentation.tone
        )
    }
}

private data class PromotionStatusPresentation(
    val label: String,
    val headline: String,
    val tone: DashboardStatusTone
)
