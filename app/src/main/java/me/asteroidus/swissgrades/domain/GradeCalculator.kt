package me.asteroidus.swissgrades.domain

import kotlin.math.floor
import kotlin.math.round
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade

object GradeCalculator {

    fun weightedAverage(grades: List<Grade>): Double? {
        if (grades.isEmpty()) {
            return null
        }

        val weightedSum = grades.sumOf { it.value * it.weight.coefficient }
        val totalWeight = grades.sumOf { it.weight.coefficient }
        return weightedSum / totalWeight
    }

    fun roundToHalf(value: Double): Double = floor((value * 2.0) + 0.5 + 1e-9) / 2.0

    fun roundToHundredth(value: Double): Double = round(value * 100.0) / 100.0

    fun computeBranchAverage(branch: Branch): Double? {
        return when (branch) {
            is Branch.Simple -> weightedAverage(branch.grades)?.let(::roundToHalf)
            is Branch.Composite -> computeCompositeOptionAverage(branch)
        }
    }

    fun computeCompositeOptionAverage(branch: Branch.Composite): Double? {
        val roundedSubSubjectAverages = branch.subSubjects.map { subSubject ->
            weightedAverage(subSubject.grades)?.let(::roundToHundredth)
        }

        if (roundedSubSubjectAverages.any { it == null }) {
            return null
        }

        return roundToHalf(roundedSubSubjectAverages.filterNotNull().average())
    }

    fun computePromotionPoints(branchAverage: Double): Double {
        return if (branchAverage >= 4.0) {
            branchAverage - 4.0
        } else {
            -2.0 * (4.0 - branchAverage)
        }
    }

    fun checkPromotionBlockingRules(branchAverages: List<Double>): Boolean {
        val hasAverageBelowThree = branchAverages.any { it < 3.0 }
        val branchesBelowFour = branchAverages.count { it < 4.0 }

        return !hasAverageBelowThree && branchesBelowFour <= 4
    }

    fun computeBasketSum(
        germanAverage: Double,
        frenchAverage: Double,
        mathAverage: Double,
        optionAverage: Double
    ): Double = germanAverage + frenchAverage + mathAverage + optionAverage
}
