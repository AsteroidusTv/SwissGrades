package me.asteroidus.swissgrades.ui.simulation

import java.util.Locale
import me.asteroidus.swissgrades.domain.model.BranchAverageResult
import me.asteroidus.swissgrades.domain.model.BranchAverageStatus
import me.asteroidus.swissgrades.domain.model.PromotionBlockingReason
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionMissingDataReason
import me.asteroidus.swissgrades.domain.model.PromotionStatus

object PromotionPresentationMapper {

    fun map(result: PromotionEvaluationResult): PromotionPresentation {
        return PromotionPresentation(
            statusLabel = mapStatusLabel(result.status),
            headline = mapHeadline(result.status),
            branchAverages = result.branchAverages.map { branchAverage ->
                BranchAveragePresentation(
                    branchName = branchAverage.branchName,
                    valueLabel = mapBranchAverageValueLabel(branchAverage),
                    detailLabel = mapBranchAverageDetailLabel(branchAverage)
                )
            },
            basketTotal = MetricPresentation(
                label = "Basket total",
                valueLabel = result.basketTotal?.let(::formatNumber) ?: "Not available"
            ),
            promotionPointsTotal = MetricPresentation(
                label = "Promotion points total",
                valueLabel = result.promotionPointsTotal?.let(::formatSignedNumber) ?: "Not available"
            ),
            blockingMessages = result.blockingReasons.map(::mapBlockingReason),
            missingDataMessages = result.missingDataReasons.map(::mapMissingDataReason)
        )
    }

    fun mapBlockingReason(reason: PromotionBlockingReason): String {
        return when (reason) {
            is PromotionBlockingReason.BranchAverageBelowThree ->
                "${reason.branchName} is below 3.0 with an average of ${formatNumber(reason.average)}."

            is PromotionBlockingReason.MoreThanFourBranchesBelowFour ->
                "More than 4 branches are below 4.0: ${reason.branchNames.joinToString(", ")}."

            is PromotionBlockingReason.BasketBelowThreshold ->
                "The basket total is ${formatNumber(reason.basketTotal)}, below the required ${formatNumber(reason.threshold)}."
        }
    }

    fun mapMissingDataReason(reason: PromotionMissingDataReason): String {
        return when (reason) {
            is PromotionMissingDataReason.MissingBranchAverage ->
                "No average is available for ${reason.branchName}."
        }
    }

    fun mapBranchAverageValueLabel(branchAverage: BranchAverageResult): String {
        return when (branchAverage.status) {
            BranchAverageStatus.COMPUTED -> formatNumber(branchAverage.average!!)
            BranchAverageStatus.MISSING_OR_NON_CALCULABLE -> "No average available"
            BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL -> "Not evaluated"
        }
    }

    fun mapBranchAverageDetailLabel(branchAverage: BranchAverageResult): String? {
        return when (branchAverage.status) {
            BranchAverageStatus.COMPUTED -> null
            BranchAverageStatus.MISSING_OR_NON_CALCULABLE -> "This branch cannot be evaluated yet."
            BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL -> "Optional subject left empty."
        }
    }

    private fun mapStatusLabel(status: PromotionStatus): String {
        return when (status) {
            PromotionStatus.PROMOTED -> "Promoted"
            PromotionStatus.BLOCKED -> "Blocked"
            PromotionStatus.INCOMPLETE -> "Incomplete"
        }
    }

    private fun mapHeadline(status: PromotionStatus): String {
        return when (status) {
            PromotionStatus.PROMOTED -> "Promotion requirements are currently satisfied."
            PromotionStatus.BLOCKED -> "Promotion requirements are not satisfied."
            PromotionStatus.INCOMPLETE -> "Promotion cannot be decided yet because some data is missing."
        }
    }

    private fun formatNumber(value: Double): String = String.format(Locale.US, "%.2f", value)

    private fun formatSignedNumber(value: Double): String {
        val prefix = if (value > 0.0) "+" else ""
        return prefix + formatNumber(value)
    }
}
