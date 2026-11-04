package me.asteroidus.swissgrades.ui.app

import androidx.compose.runtime.Immutable

/** Copy for the Carnet UI, grouped to keep AppStrings below the JVM signature limit. */
@Immutable
data class CarnetStrings(
    val carnetNoteSingular: String,
    val carnetNotePlural: String,
    val carnetWatchLabel: String,
    val settingsIntro: String,
    val myDataTitle: String,
    val backupAndRestoreLabel: String,
    val backupFilesCaption: String,
    val carnetTargetTitle: String,
    val carnetSimulationCaption: String,
    val carnetSimulateAction: String,
    val carnetOverviewTitle: String,
    val carnetOverviewIntro: String,
    val carnetConditionsTitle: String,
    val carnetMinimumAverage: String,
    val carnetConditionsMet: String,
    val carnetConditionsUnmet: String,
    val carnetConditionsUnknown: String,
    val carnetMinimum16: String,
    val carnetMinimumZero: String,
    val carnetMaximumFour: String,
    val carnetMinimumThree: String,
    val carnetOfficialAverage: String,
    val carnetNewGrade: String,
    val carnetNotesTitle: String,
    val carnetSubjectsIntro: String,
    val carnetGradeHint: String,
    val carnetGradeSave: String,
    val carnetOverviewLink: String
) {
    companion object {
        val English = CarnetStrings(
            carnetNoteSingular = "grade",
            carnetNotePlural = "grades",
            carnetWatchLabel = "Needs attention",
            settingsIntro = "Your preferences and your data.",
            myDataTitle = "My data",
            backupAndRestoreLabel = "Back up and restore",
            backupFilesCaption = "SwissGrades backups",
            carnetTargetTitle = "My target",
            carnetSimulationCaption = "Simulation · no grade saved",
            carnetSimulateAction = "Simulate a target",
            carnetOverviewTitle = "My overview",
            carnetOverviewIntro = "Projection based on saved grades.",
            carnetConditionsTitle = "The four conditions",
            carnetMinimumAverage = "Lowest average",
            carnetConditionsMet = "Conditions met so far",
            carnetConditionsUnmet = "Some conditions remain unmet",
            carnetConditionsUnknown = "Some data is missing",
            carnetMinimum16 = "At least 16 points",
            carnetMinimumZero = "At least 0 points",
            carnetMaximumFour = "At most 4 subjects below 4",
            carnetMinimumThree = "No subject below 3",
            carnetOfficialAverage = "Estimated rounded average",
            carnetNewGrade = "New grade",
            carnetNotesTitle = "My grades",
            carnetSubjectsIntro = "Subject",
            carnetGradeHint = "From 1 to 6, in steps of 0.25.",
            carnetGradeSave = "Save grade",
            carnetOverviewLink = "Promotion overview"
        )
        val French = CarnetStrings(
            carnetNoteSingular = "note",
            carnetNotePlural = "notes",
            carnetWatchLabel = "À surveiller",
            settingsIntro = "Tes préférences et tes données.",
            myDataTitle = "Mes données",
            backupAndRestoreLabel = "Sauvegarder et restaurer",
            backupFilesCaption = "Sauvegardes SwissGrades",
            carnetTargetTitle = "Mon objectif",
            carnetSimulationCaption = "Simulation · aucune note enregistrée",
            carnetSimulateAction = "Simuler un objectif",
            carnetOverviewTitle = "Mon bilan",
            carnetOverviewIntro = "Projection sur les notes saisies.",
            carnetConditionsTitle = "Les quatre conditions",
            carnetMinimumAverage = "Moyenne minimale",
            carnetConditionsMet = "Conditions remplies à ce stade",
            carnetConditionsUnmet = "Des conditions restent à remplir",
            carnetConditionsUnknown = "Des données manquent au bilan",
            carnetMinimum16 = "Minimum 16 points",
            carnetMinimumZero = "Minimum 0 point",
            carnetMaximumFour = "Maximum 4 matières sous 4",
            carnetMinimumThree = "Aucune matière sous 3",
            carnetOfficialAverage = "Moyenne arrondie estimée",
            carnetNewGrade = "Nouvelle note",
            carnetNotesTitle = "Mes notes",
            carnetSubjectsIntro = "Matière",
            carnetGradeHint = "De 1 à 6, par pas de 0,25.",
            carnetGradeSave = "Enregistrer la note",
            carnetOverviewLink = "Bilan de promotion"
        )
    }
}

internal val AppStrings.carnetNoteSingular: String get() = carnet.carnetNoteSingular
internal val AppStrings.carnetNotePlural: String get() = carnet.carnetNotePlural
internal val AppStrings.carnetWatchLabel: String get() = carnet.carnetWatchLabel
internal val AppStrings.settingsIntro: String get() = carnet.settingsIntro
internal val AppStrings.myDataTitle: String get() = carnet.myDataTitle
internal val AppStrings.backupAndRestoreLabel: String get() = carnet.backupAndRestoreLabel
internal val AppStrings.backupFilesCaption: String get() = carnet.backupFilesCaption
internal val AppStrings.carnetTargetTitle: String get() = carnet.carnetTargetTitle
internal val AppStrings.carnetSimulationCaption: String get() = carnet.carnetSimulationCaption
internal val AppStrings.carnetSimulateAction: String get() = carnet.carnetSimulateAction
internal val AppStrings.carnetOverviewTitle: String get() = carnet.carnetOverviewTitle
internal val AppStrings.carnetOverviewIntro: String get() = carnet.carnetOverviewIntro
internal val AppStrings.carnetConditionsTitle: String get() = carnet.carnetConditionsTitle
internal val AppStrings.carnetMinimumAverage: String get() = carnet.carnetMinimumAverage
internal val AppStrings.carnetConditionsMet: String get() = carnet.carnetConditionsMet
internal val AppStrings.carnetConditionsUnmet: String get() = carnet.carnetConditionsUnmet
internal val AppStrings.carnetConditionsUnknown: String get() = carnet.carnetConditionsUnknown
internal val AppStrings.carnetMinimum16: String get() = carnet.carnetMinimum16
internal val AppStrings.carnetMinimumZero: String get() = carnet.carnetMinimumZero
internal val AppStrings.carnetMaximumFour: String get() = carnet.carnetMaximumFour
internal val AppStrings.carnetMinimumThree: String get() = carnet.carnetMinimumThree
internal val AppStrings.carnetOfficialAverage: String get() = carnet.carnetOfficialAverage
internal val AppStrings.carnetNewGrade: String get() = carnet.carnetNewGrade
internal val AppStrings.carnetNotesTitle: String get() = carnet.carnetNotesTitle
internal val AppStrings.carnetSubjectsIntro: String get() = carnet.carnetSubjectsIntro
internal val AppStrings.carnetGradeHint: String get() = carnet.carnetGradeHint
internal val AppStrings.carnetGradeSave: String get() = carnet.carnetGradeSave
internal val AppStrings.carnetOverviewLink: String get() = carnet.carnetOverviewLink
