package me.asteroidus.swissgrades.domain.model

sealed interface PromotionRoleAssignment {
    val branch: Branch

    data class German(
        override val branch: Branch
    ) : PromotionRoleAssignment

    data class French(
        override val branch: Branch
    ) : PromotionRoleAssignment

    data class Math(
        override val branch: Branch
    ) : PromotionRoleAssignment

    data class Option(
        override val branch: Branch
    ) : PromotionRoleAssignment

    data class Additional(
        override val branch: Branch,
        val isExplicitlyEmpty: Boolean = false
    ) : PromotionRoleAssignment
}

class PromotionEvaluationInput private constructor(
    val german: PromotionRoleAssignment.German,
    val french: PromotionRoleAssignment.French,
    val math: PromotionRoleAssignment.Math,
    val option: PromotionRoleAssignment.Option,
    val additionalBranches: List<PromotionRoleAssignment.Additional>
) {
    companion object {
        fun create(
            assignments: List<PromotionRoleAssignment>
        ): PromotionEvaluationInput {
            val germanAssignments = assignments.filterIsInstance<PromotionRoleAssignment.German>()
            val frenchAssignments = assignments.filterIsInstance<PromotionRoleAssignment.French>()
            val mathAssignments = assignments.filterIsInstance<PromotionRoleAssignment.Math>()
            val optionAssignments = assignments.filterIsInstance<PromotionRoleAssignment.Option>()
            val additionalAssignments = assignments.filterIsInstance<PromotionRoleAssignment.Additional>()

            require(germanAssignments.size == 1) {
                "Promotion evaluation requires exactly one German branch."
            }
            require(frenchAssignments.size == 1) {
                "Promotion evaluation requires exactly one French branch."
            }
            require(mathAssignments.size == 1) {
                "Promotion evaluation requires exactly one Math branch."
            }
            require(optionAssignments.size == 1) {
                "Promotion evaluation requires exactly one Option branch."
            }

            val input = PromotionEvaluationInput(
                german = germanAssignments.single(),
                french = frenchAssignments.single(),
                math = mathAssignments.single(),
                option = optionAssignments.single(),
                additionalBranches = additionalAssignments
            )

            input.validate()
            return input
        }
    }

    fun allBranches(): List<Branch> {
        return listOf(
            german.branch,
            french.branch,
            math.branch,
            option.branch
        ) + additionalBranches.map { it.branch }
    }

    private fun validate() {
        require(!isOptionBranch(german.branch)) {
            "German role must receive a non-option branch."
        }
        require(!isOptionBranch(french.branch)) {
            "French role must receive a non-option branch."
        }
        require(!isOptionBranch(math.branch)) {
            "Math role must receive a non-option branch."
        }
        require(isOptionBranch(option.branch)) {
            "Option role must receive an option branch."
        }

        val branchNames = allBranches().map { it.name }
        require(branchNames.size == branchNames.toSet().size) {
            "Promotion evaluation requires unique branch names."
        }
    }

    private fun isOptionBranch(branch: Branch): Boolean {
        return when (branch) {
            is Branch.Simple -> branch.optionType != null
            is Branch.Composite -> true
        }
    }
}

enum class PromotionStatus {
    PROMOTED,
    BLOCKED,
    INCOMPLETE
}

enum class BranchAverageStatus {
    COMPUTED,
    MISSING_OR_NON_CALCULABLE,
    EMPTY_OPTIONAL_ADDITIONAL
}

data class BranchAverageResult(
    val branchName: String,
    val average: Double?,
    val status: BranchAverageStatus
)

sealed interface PromotionBlockingReason {
    data class BranchAverageBelowThree(
        val branchName: String,
        val average: Double
    ) : PromotionBlockingReason

    data class MoreThanFourBranchesBelowFour(
        val branchNames: List<String>
    ) : PromotionBlockingReason

    data class BasketBelowThreshold(
        val basketTotal: Double,
        val threshold: Double = 16.0
    ) : PromotionBlockingReason
}

sealed interface PromotionMissingDataReason {
    data class MissingBranchAverage(
        val branchName: String
    ) : PromotionMissingDataReason
}

data class PromotionEvaluationResult(
    val status: PromotionStatus,
    val branchAverages: List<BranchAverageResult>,
    val basketTotal: Double?,
    val promotionPointsTotal: Double?,
    val blockingReasons: List<PromotionBlockingReason>,
    val missingDataReasons: List<PromotionMissingDataReason>
)
