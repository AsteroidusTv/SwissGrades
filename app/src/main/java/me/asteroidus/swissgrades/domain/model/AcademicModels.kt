package me.asteroidus.swissgrades.domain.model

import kotlin.math.round

enum class AssessmentWeight(val coefficient: Double) {
    FULL(1.0),
    HALF(0.5),
    QUARTER(0.25)
}

enum class OptionType(val isComposite: Boolean) {
    SPANISH(false),
    ITALIAN(false),
    LATIN(false),
    MUSIC(false),
    PHILOSOPHY(false),
    VISUAL_ARTS(false),
    ECONOMICS_LAW(false),
    OTHER(false),
    BIOLOGY_CHEMISTRY(true),
    PHYSICS_AND_MATH_APPLICATIONS(true),
    ECONOMICS_AND_LAW(true)
}

data class Grade(
    val value: Double,
    val weight: AssessmentWeight
) {
    init {
        require(value in 1.0..6.0) { "Grade value must be between 1 and 6." }
        require(round(value * 4) == value * 4) {
            "Grade value must use quarter steps."
        }
    }
}

data class SubSubject(
    val name: String,
    val grades: List<Grade>
)

sealed interface Branch {
    val name: String

    class Simple private constructor(
        override val name: String,
        val grades: List<Grade>,
        val optionType: OptionType?
    ) : Branch {
        companion object {
            fun create(
                name: String,
                grades: List<Grade>,
                optionType: OptionType? = null,
                subSubjects: List<SubSubject> = emptyList()
            ): Simple {
                require(subSubjects.isEmpty()) {
                    "A simple branch cannot contain sub-subjects."
                }
                require(optionType?.isComposite != true) {
                    "A simple branch cannot use a composite option type."
                }

                return Simple(
                    name = name,
                    grades = grades,
                    optionType = optionType
                )
            }
        }
    }

    class Composite private constructor(
        override val name: String,
        val optionType: OptionType,
        val subSubjects: List<SubSubject>
    ) : Branch {
        companion object {
            fun create(
                name: String,
                optionType: OptionType,
                subSubjects: List<SubSubject>,
                grades: List<Grade> = emptyList()
            ): Composite {
                require(optionType.isComposite) {
                    "A composite branch must use a composite option type."
                }
                require(grades.isEmpty()) {
                    "A composite branch cannot contain direct grades."
                }
                require(subSubjects.size == 2) {
                    "A composite branch must contain exactly two sub-subjects."
                }

                return Composite(
                    name = name,
                    optionType = optionType,
                    subSubjects = subSubjects
                )
            }
        }
    }
}
