package me.asteroidus.swissgrades.domain.model

data class PromotionPresentation(
    val statusLabel: String,
    val headline: String,
    val branchAverages: List<BranchAveragePresentation>,
    val basketTotal: MetricPresentation,
    val promotionPointsTotal: MetricPresentation,
    val blockingMessages: List<String>,
    val missingDataMessages: List<String>
)

data class BranchAveragePresentation(
    val branchName: String,
    val valueLabel: String,
    val detailLabel: String? = null
)

data class MetricPresentation(
    val label: String,
    val valueLabel: String
)
