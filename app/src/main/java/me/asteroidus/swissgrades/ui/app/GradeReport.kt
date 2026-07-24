package me.asteroidus.swissgrades.ui.app

import me.asteroidus.swissgrades.domain.GradeCalculator
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import me.asteroidus.swissgrades.domain.model.Branch
import me.asteroidus.swissgrades.domain.model.Grade
import me.asteroidus.swissgrades.domain.model.PromotionStatus
import me.asteroidus.swissgrades.domain.model.SubSubject

data class GradeReport(
    val schoolYear: SchoolYear,
    val semester: SchoolSemester,
    val generatedAtEpochMillis: Long,
    val overallAverage: Double?,
    val promotionStatus: PromotionStatus?,
    val promotionPoints: Double?,
    val basketTotal: Double?,
    val insufficiencyCount: Int,
    val subjects: List<GradeReportSubject>
)

data class GradeReportSubject(
    val sourceName: String,
    val optionChoice: InitialOptionChoice?,
    val isCounted: Boolean,
    val isInBasket: Boolean,
    val isOptionSubject: Boolean,
    val officialAverage: Double?,
    val detailedAverage: Double?,
    val promotionPoints: Double?,
    val notes: List<GradeReportNote>,
    val subSubjects: List<GradeReportSubSubject>
)

data class GradeReportSubSubject(
    val sourceName: String,
    val average: Double?,
    val notes: List<GradeReportNote>
)

data class GradeReportNote(
    val value: Double,
    val weight: AssessmentWeight,
    val description: String,
    val createdAtEpochMillis: Long,
    val semester: SchoolSemester
)

internal object GradeReportBuilder {

    fun build(
        state: GradeTrackerAppState,
        generatedAtEpochMillis: Long = System.currentTimeMillis()
    ): GradeReport {
        val subjects = state.subjects
            .filter { it.schoolYear == state.selectedYear }
            .sortedByDescending { it.isOptionSubject }
            .map { it.toReportSubject(state.selectedSemester) }
        val includedSubjects = subjects.filter { it.isCounted || it.isOptionSubject }
        val calculableAverages = includedSubjects.mapNotNull(GradeReportSubject::officialAverage)
        val basketSubjects = subjects.filter {
            it.isCounted && it.isInBasket && !it.isOptionSubject
        }
        val optionSubject = subjects.firstOrNull { it.isOptionSubject }
        val basketAverages = basketSubjects.map(GradeReportSubject::officialAverage) +
            listOf(optionSubject?.officialAverage)
        val basketTotal = if (
            basketSubjects.size == 3 &&
            optionSubject != null &&
            basketAverages.all { it != null }
        ) {
            basketAverages.filterNotNull().sum()
        } else {
            null
        }

        return GradeReport(
            schoolYear = state.selectedYear,
            semester = state.selectedSemester,
            generatedAtEpochMillis = generatedAtEpochMillis,
            overallAverage = calculableAverages.takeIf { it.isNotEmpty() }?.average(),
            promotionStatus = PromotionEvaluationFactory.evaluate(state)?.status,
            promotionPoints = includedSubjects
                .mapNotNull(GradeReportSubject::promotionPoints)
                .takeIf { it.isNotEmpty() }
                ?.sum(),
            basketTotal = basketTotal,
            insufficiencyCount = includedSubjects.count {
                it.officialAverage?.let { average -> average < 4.0 } == true
            },
            subjects = subjects
        )
    }
}

private fun StoredSubject.toReportSubject(semester: SchoolSemester): GradeReportSubject {
    val reportSubSubjects = subSubjects.map { it.toReportSubSubject(semester) }
    val branch = toReportBranch(semester)
    val officialAverage = GradeCalculator.computeBranchAverage(branch)
    val detailedAverage = when (branch) {
        is Branch.Simple -> GradeCalculator.weightedAverage(branch.grades)
        is Branch.Composite -> {
            val subSubjectAverages = branch.subSubjects.map { subSubject ->
                GradeCalculator.weightedAverage(subSubject.grades)
                    ?.let(GradeCalculator::roundToHundredth)
            }
            if (subSubjectAverages.all { it != null }) {
                subSubjectAverages.filterNotNull().average()
            } else {
                null
            }
        }
    }
    val countsInResults = isCounted || isOptionSubject

    return GradeReportSubject(
        sourceName = name,
        optionChoice = optionChoice,
        isCounted = isCounted,
        isInBasket = isInBasket,
        isOptionSubject = isOptionSubject,
        officialAverage = officialAverage,
        detailedAverage = detailedAverage,
        promotionPoints = if (countsInResults) {
            officialAverage?.let(GradeCalculator::computePromotionPoints)
        } else {
            null
        },
        notes = notes.filter { it.isIncludedIn(semester) }.map(StoredNote::toReportNote),
        subSubjects = reportSubSubjects
    )
}

private fun StoredSubject.toReportBranch(semester: SchoolSemester): Branch {
    return if (subSubjects.isEmpty()) {
        Branch.Simple.create(
            name = name,
            grades = notes.filter { it.isIncludedIn(semester) }.map(StoredNote::toGrade),
            optionType = optionChoice?.optionType
        )
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

private fun StoredSubSubject.toReportSubSubject(
    semester: SchoolSemester
): GradeReportSubSubject {
    val includedNotes = notes.filter { it.isIncludedIn(semester) }
    return GradeReportSubSubject(
        sourceName = name,
        average = GradeCalculator.weightedAverage(includedNotes.map(StoredNote::toGrade))
            ?.let(GradeCalculator::roundToHundredth),
        notes = includedNotes.map(StoredNote::toReportNote)
    )
}

private fun StoredNote.toReportNote(): GradeReportNote {
    return GradeReportNote(
        value = value,
        weight = weight,
        description = description,
        createdAtEpochMillis = createdAtEpochMillis,
        semester = semester
    )
}

private fun StoredNote.toGrade(): Grade = Grade(value = value, weight = weight)

private fun StoredNote.isIncludedIn(selectedSemester: SchoolSemester): Boolean {
    return selectedSemester == SchoolSemester.SEMESTER_2 || semester == SchoolSemester.SEMESTER_1
}
