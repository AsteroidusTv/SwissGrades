package me.asteroidus.swissgrades.domain

import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.SubSubject

data class GradeImpact(
    val withGradeAverage: Double,
    val withoutGradeAverage: Double?
) {
    val officialAverageDelta: Double?
        get() = withoutGradeAverage?.let { withGradeAverage - it }
}

object GradeImpactCalculator {

    fun calculateSimple(grades: List<Grade>, targetIndex: Int): GradeImpact {
        require(targetIndex in grades.indices) { "Target grade index is out of bounds." }

        val withGradeAverage = requireNotNull(
            GradeCalculator.computeBranchAverage(
                Branch.Simple.create(name = "Impact", grades = grades)
            )
        )
        val withoutGradeAverage = GradeCalculator.computeBranchAverage(
            Branch.Simple.create(
                name = "Impact",
                grades = grades.filterIndexed { index, _ -> index != targetIndex }
            )
        )
        return GradeImpact(withGradeAverage, withoutGradeAverage)
    }

    fun calculateComposite(
        branch: Branch.Composite,
        targetSubSubjectIndex: Int,
        targetGradeIndex: Int
    ): GradeImpact? {
        require(targetSubSubjectIndex in branch.subSubjects.indices) {
            "Target sub-subject index is out of bounds."
        }
        require(targetGradeIndex in branch.subSubjects[targetSubSubjectIndex].grades.indices) {
            "Target grade index is out of bounds."
        }

        val withGradeAverage = GradeCalculator.computeBranchAverage(branch) ?: return null
        val branchWithoutGrade = Branch.Composite.create(
            name = branch.name,
            optionType = branch.optionType,
            subSubjects = branch.subSubjects.mapIndexed { subSubjectIndex, subSubject ->
                SubSubject(
                    name = subSubject.name,
                    grades = if (subSubjectIndex == targetSubSubjectIndex) {
                        subSubject.grades.filterIndexed { gradeIndex, _ ->
                            gradeIndex != targetGradeIndex
                        }
                    } else {
                        subSubject.grades
                    }
                )
            }
        )
        return GradeImpact(
            withGradeAverage = withGradeAverage,
            withoutGradeAverage = GradeCalculator.computeBranchAverage(branchWithoutGrade)
        )
    }
}
