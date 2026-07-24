package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.PromotionEvaluator
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationInput
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionRoleAssignment
import me.asteroidus.swissgrades.domain.model.SubSubject

internal object PromotionEvaluationFactory {

    fun evaluate(state: GradeTrackerAppState): PromotionEvaluationResult? {
        val currentYearSubjects = state.subjects.filter { it.schoolYear == state.selectedYear }
        val option = currentYearSubjects.firstOrNull { it.isOptionSubject } ?: return null
        val basketSubjects = currentYearSubjects.filter {
            it.isCounted && it.isInBasket && !it.isOptionSubject
        }
        if (basketSubjects.size != 3) return null

        val basketSubjectIds = basketSubjects.mapTo(mutableSetOf()) { it.id }
        basketSubjectIds += option.id

        val assignments = buildList {
            add(PromotionRoleAssignment.German(basketSubjects[0].toSimpleBranch(state.selectedSemester)))
            add(PromotionRoleAssignment.French(basketSubjects[1].toSimpleBranch(state.selectedSemester)))
            add(PromotionRoleAssignment.Math(basketSubjects[2].toSimpleBranch(state.selectedSemester)))
            add(PromotionRoleAssignment.Option(option.toBranch(state.selectedSemester)))
            currentYearSubjects
                .filter { it.isCounted && !it.isOptionSubject && it.id !in basketSubjectIds }
                .forEach { subject ->
                    add(
                        PromotionRoleAssignment.Additional(
                            branch = subject.toSimpleBranch(state.selectedSemester),
                            isExplicitlyEmpty = subject.notes.none {
                                it.isIncludedIn(state.selectedSemester)
                            }
                        )
                    )
                }
        }
        return PromotionEvaluator.evaluate(PromotionEvaluationInput.create(assignments))
    }
}

private fun StoredSubject.toBranch(semester: SchoolSemester): Branch {
    return if (subSubjects.isEmpty()) {
        toSimpleBranch(semester)
    } else {
        Branch.Composite.create(
            name = name,
            optionType = requireNotNull(optionChoice?.optionType),
            subSubjects = subSubjects.map { subSubject ->
                SubSubject(
                    name = subSubject.name,
                    grades = subSubject.notes
                        .filter { it.isIncludedIn(semester) }
                        .map(StoredNote::toGrade)
                )
            }
        )
    }
}

private fun StoredSubject.toSimpleBranch(semester: SchoolSemester): Branch.Simple {
    return Branch.Simple.create(
        name = name,
        grades = notes.filter { it.isIncludedIn(semester) }.map(StoredNote::toGrade),
        optionType = optionChoice?.optionType
    )
}

private fun StoredNote.toGrade(): Grade = Grade(value = value, weight = weight)

private fun StoredNote.isIncludedIn(selectedSemester: SchoolSemester): Boolean {
    return selectedSemester == SchoolSemester.SEMESTER_2 || semester == SchoolSemester.SEMESTER_1
}
