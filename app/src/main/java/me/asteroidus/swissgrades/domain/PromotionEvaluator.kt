package me.asteroidus.swissgrades.domain

import me.asteroidus.swissgrades.domain.model.BranchAverageResult
import me.asteroidus.swissgrades.domain.model.BranchAverageStatus
import me.asteroidus.swissgrades.domain.model.PromotionBlockingReason
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationInput
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionMissingDataReason
import me.asteroidus.swissgrades.domain.model.PromotionStatus

object PromotionEvaluator {

    fun evaluate(input: PromotionEvaluationInput): PromotionEvaluationResult {
        val germanAverage = GradeCalculator.computeBranchAverage(input.german.branch)
        val frenchAverage = GradeCalculator.computeBranchAverage(input.french.branch)
        val mathAverage = GradeCalculator.computeBranchAverage(input.math.branch)
        val optionAverage = GradeCalculator.computeBranchAverage(input.option.branch)
        val additionalBranchAverages = input.additionalBranches.map { additionalBranch ->
            additionalBranch to GradeCalculator.computeBranchAverage(additionalBranch.branch)
        }

        val branchAverages = listOf(
            BranchAverageResult(
                branchName = input.german.branch.name,
                average = germanAverage,
                status = branchAverageStatus(isExplicitlyEmptyAdditional = false, average = germanAverage)
            ),
            BranchAverageResult(
                branchName = input.french.branch.name,
                average = frenchAverage,
                status = branchAverageStatus(isExplicitlyEmptyAdditional = false, average = frenchAverage)
            ),
            BranchAverageResult(
                branchName = input.math.branch.name,
                average = mathAverage,
                status = branchAverageStatus(isExplicitlyEmptyAdditional = false, average = mathAverage)
            ),
            BranchAverageResult(
                branchName = input.option.branch.name,
                average = optionAverage,
                status = branchAverageStatus(isExplicitlyEmptyAdditional = false, average = optionAverage)
            )
        ) + additionalBranchAverages.map { (additionalBranch, average) ->
            BranchAverageResult(
                branchName = additionalBranch.branch.name,
                average = average,
                status = branchAverageStatus(
                    isExplicitlyEmptyAdditional = additionalBranch.isExplicitlyEmpty,
                    average = average
                )
            )
        }

        val missingDataReasons = buildList {
            listOf(
                input.german.branch.name to germanAverage,
                input.french.branch.name to frenchAverage,
                input.math.branch.name to mathAverage,
                input.option.branch.name to optionAverage
            ).filter { it.second == null }
                .forEach { add(PromotionMissingDataReason.MissingBranchAverage(branchName = it.first)) }

            additionalBranchAverages
                .filter { (additionalBranch, average) -> average == null && !additionalBranch.isExplicitlyEmpty }
                .forEach { (additionalBranch, _) ->
                    add(PromotionMissingDataReason.MissingBranchAverage(branchName = additionalBranch.branch.name))
                }
        }

        val availableBranchAverages = branchAverages.filter { it.average != null }
        val blockingReasons = buildList {
            addAll(
                availableBranchAverages
                    .filter { it.average!! < 3.0 }
                    .map {
                        PromotionBlockingReason.BranchAverageBelowThree(
                            branchName = it.branchName,
                            average = it.average!!
                        )
                    }
            )

            val branchesBelowFour = availableBranchAverages
                .filter { it.average!! < 4.0 }
                .map { it.branchName }

            if (branchesBelowFour.size > 4) {
                add(
                    PromotionBlockingReason.MoreThanFourBranchesBelowFour(
                        branchNames = branchesBelowFour
                    )
                )
            }

            val basketTotal = computeBasketTotal(
                germanAverage = germanAverage,
                frenchAverage = frenchAverage,
                mathAverage = mathAverage,
                optionAverage = optionAverage
            )
            if (basketTotal != null && basketTotal < 16.0) {
                add(
                    PromotionBlockingReason.BasketBelowThreshold(
                        basketTotal = basketTotal
                    )
                )
            }
        }

        val basketTotal = computeBasketTotal(
            germanAverage = germanAverage,
            frenchAverage = frenchAverage,
            mathAverage = mathAverage,
            optionAverage = optionAverage
        )
        val promotionPointsTotal = if (missingDataReasons.isEmpty()) {
            val requiredPoints = listOf(germanAverage, frenchAverage, mathAverage, optionAverage)
                .sumOf { GradeCalculator.computePromotionPoints(it!!) }
            val additionalPoints = additionalBranchAverages.sumOf { (_, average) ->
                average?.let(GradeCalculator::computePromotionPoints) ?: 0.0
            }
            requiredPoints + additionalPoints
        } else {
            null
        }

        val status = when {
            missingDataReasons.isNotEmpty() -> PromotionStatus.INCOMPLETE
            blockingReasons.isNotEmpty() -> PromotionStatus.BLOCKED
            else -> PromotionStatus.PROMOTED
        }

        return PromotionEvaluationResult(
            status = status,
            branchAverages = branchAverages,
            basketTotal = basketTotal,
            promotionPointsTotal = promotionPointsTotal,
            blockingReasons = blockingReasons,
            missingDataReasons = missingDataReasons
        )
    }

    private fun computeBasketTotal(
        germanAverage: Double?,
        frenchAverage: Double?,
        mathAverage: Double?,
        optionAverage: Double?
    ): Double? {
        if (listOf(germanAverage, frenchAverage, mathAverage, optionAverage).any { it == null }) {
            return null
        }

        return GradeCalculator.computeBasketSum(
            germanAverage = germanAverage!!,
            frenchAverage = frenchAverage!!,
            mathAverage = mathAverage!!,
            optionAverage = optionAverage!!
        )
    }

    private fun branchAverageStatus(
        isExplicitlyEmptyAdditional: Boolean,
        average: Double?
    ): BranchAverageStatus {
        return when {
            average != null -> BranchAverageStatus.COMPUTED
            isExplicitlyEmptyAdditional -> BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL
            else -> BranchAverageStatus.MISSING_OR_NON_CALCULABLE
        }
    }
}
