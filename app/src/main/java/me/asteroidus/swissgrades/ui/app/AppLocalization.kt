package me.asteroidus.swissgrades.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import java.util.Locale

@Immutable
data class AppStrings(
    val appName: String,
    val chooseOption: String,
    val onboardingBody: String,
    val continueLabel: String,
    val mySubjects: String,
    val addLabel: String,
    val optionSettingsTitle: String,
    val languageSectionTitle: String,
    val languageSectionDescription: String,
    val themeSectionTitle: String,
    val themeSectionDescription: String,
    val backupSectionTitle: String,
    val backupSectionDescription: String,
    val plusPointsSectionTitle: String,
    val plusPointsSectionDescription: String,
    val exportBackupLabel: String,
    val importBackupLabel: String,
    val importPlusPointsLabel: String,
    val plusPointsImportTitle: String,
    val plusPointsImportMessageTemplate: String,
    val plusPointsImportConfirm: String,
    val plusPointsImportSuccess: String,
    val plusPointsImportFailure: String,
    val backupExportSuccess: String,
    val backupExportFailure: String,
    val backupImportTitle: String,
    val backupImportMessageTemplate: String,
    val backupImportConfirm: String,
    val backupImportInvalid: String,
    val backupImportFailure: String,
    val backupImportSuccess: String,
    val resetSectionTitle: String,
    val resetSectionDescription: String,
    val resetAppLabel: String,
    val resetAppTitle: String,
    val resetAppMessage: String,
    val resetAppConfirm: String,
    val optionSectionTitle: String,
    val optionSectionDescriptionPrefix: String,
    val optionSectionDescriptionSuffix: String,
    val periodTitle: String,
    val choosePeriodTitle: String,
    val schoolYearTitle: String,
    val schoolYear1Label: String,
    val schoolYear2Label: String,
    val schoolYear3Label: String,
    val semesterTitle: String,
    val semester1Label: String,
    val semester2Label: String,
    val plusPointsTargetSemesterTitle: String,
    val darkModeSystem: String,
    val darkModeLight: String,
    val darkModeDark: String,
    val languageEnglish: String,
    val languageFrench: String,
    val deleteSubjectTitle: String,
    val addSubjectTitle: String,
    val editSubjectTitle: String,
    val subjectNameLabel: String,
    val subjectNamePlaceholder: String,
    val countInResultsTitle: String,
    val countInResultsDescription: String,
    val addToBasketTitle: String,
    val addToBasketDescription: String,
    val personalizationTitle: String,
    val saveChanges: String,
    val createSubject: String,
    val promotionPointsTitle: String,
    val promotionPointsUnit: String,
    val promotionStatusPromoted: String,
    val promotionStatusBlocked: String,
    val promotionStatusIncomplete: String,
    val promotionHeadlinePromoted: String,
    val promotionHeadlineBlocked: String,
    val promotionHeadlineIncomplete: String,
    val basketTitle: String,
    val insufficienciesTitle: String,
    val inBasketLabel: String,
    val notCountedLabel: String,
    val insufficientLabel: String,
    val officialAverageLabel: String,
    val pointLabel: String,
    val pointsLabel: String,
    val statusLabel: String,
    val subSubjectsTitle: String,
    val averagePrefix: String,
    val evolutionTitle: String,
    val gradeHistoryTitle: String,
    val evaluationSingular: String,
    val evaluationPlural: String,
    val gradeValueLabel: String,
    val gradeValuePlaceholder: String,
    val descriptionOptional: String,
    val attachmentsTitle: String,
    val addPhotoLabel: String,
    val addMorePhotosLabel: String,
    val takePhotoLabel: String,
    val chooseFromGalleryLabel: String,
    val removePhotoLabel: String,
    val attachedPhotosTitle: String,
    val importAttachmentFailed: String,
    val maxAttachmentsReachedTemplate: String,
    val photoAttachmentCountTemplate: String,
    val deleteLabel: String,
    val cancelLabel: String,
    val closeLabel: String,
    val backLabel: String,
    val openSettingsLabel: String,
    val editSubjectAction: String,
    val selectedColorDescription: String,
    val changeOptionTitle: String,
    val changeOptionMessage: String,
    val changeOptionConfirm: String,
    val deleteGradeTitle: String,
    val deleteGradeMessageTemplate: String,
    val deleteSubjectMessageTemplate: String,
    val deleteGradeLabel: String,
    val deleteSubjectActionTemplate: String,
    val gradeFallbackDescription: String,
    val emptyNotes: String,
    val invalidGradeValue: String,
    val emptySubjectName: String,
    val duplicateSubjectName: String,
    val rawAverage: String,
    val compositeAverage: String,
    val notCalculableYet: String,
    val notEnoughGrades: String,
    val unlockPromotionTooMany: String,
    val unlockPromotionMissingGrades: String,
    val promotionSetupTitle: String,
    val promotionSetupIntro: String,
    val promotionSetupBasketStep: String,
    val promotionSetupOptionStep: String,
    val promotionSetupGradesStep: String,
    val promotionSetupReady: String,
    val promotionSetupNeedsAction: String,
    val promotionSetupWaitingForBasket: String,
    val promotionSetupBasketProgressTemplate: String,
    val promotionSetupMissingBasketOne: String,
    val promotionSetupMissingBasketMany: String,
    val promotionSetupTooManyBasketOne: String,
    val promotionSetupTooManyBasketMany: String,
    val promotionSetupMissingGradesTemplate: String,
    val promotionSetupAddBranchAction: String,
    val promotionSetupReviewBranchesAction: String,
    val promotionSetupAddGradeAction: String,
    val branchPromoted: String,
    val branchInsufficient: String,
    val branchInsufficientShort: String,
    val noteTypeFull: String,
    val noteTypeHalf: String,
    val noteTypeQuarter: String,
    val addGrade: String,
    val addGradeToTemplate: String,
    val editGrade: String,
    val editGradeInTemplate: String,
    val evaluationDefaultTitle: String,
    val branchTargetTitle: String,
    val branchTargetSubtitle: String,
    val branchTargetUnset: String,
    val branchTargetEdit: String,
    val branchTargetPlaceholder: String,
    val branchTargetInvalid: String,
    val targetSimulationTitle: String,
    val targetSimulationSubtitle: String,
    val targetAverageLabel: String,
    val plannedGradeCountTitle: String,
    val plannedGradeCountTemplate: String,
    val plannedGradeWeightTitle: String,
    val plannedGradeWeightHint: String,
    val requiredGradeTitle: String,
    val requiredAverageTitleTemplate: String,
    val targetAlreadyReached: String,
    val targetImpossible: String,
    val targetInvalid: String,
    val targetProjectedAverageTemplate: String
) {
    fun optionDescription(optionLabel: String): String {
        return "$optionSectionDescriptionPrefix$optionLabel$optionSectionDescriptionSuffix"
    }

    fun plannedGradeCount(count: Int): String {
        return plannedGradeCountTemplate.replace("{count}", count.toString())
    }

    fun requiredSimulationTitle(plannedGradeCount: Int): String {
        return if (plannedGradeCount == 1) {
            requiredGradeTitle
        } else {
            requiredAverageTitleTemplate.replace("{count}", plannedGradeCount.toString())
        }
    }

    fun backupImportMessage(fileName: String): String {
        return backupImportMessageTemplate.replace("{file}", fileName)
    }

    fun plusPointsImportMessage(fileName: String): String {
        return plusPointsImportMessageTemplate.replace("{file}", fileName)
    }

    fun deleteSubjectMessage(subjectTitle: String): String {
        return deleteSubjectMessageTemplate.replace("{subject}", subjectTitle)
    }

    fun deleteGradeMessage(noteTitle: String): String {
        return deleteGradeMessageTemplate.replace("{grade}", noteTitle)
    }

    fun addGradeTo(subjectTitle: String): String {
        return addGradeToTemplate.replace("{subject}", subjectTitle)
    }

    fun editGradeIn(subjectTitle: String): String {
        return editGradeInTemplate.replace("{subject}", subjectTitle)
    }

    fun evaluationCount(count: Int): String {
        val unit = if (count == 1) evaluationSingular else evaluationPlural
        return "$count $unit"
    }

    fun noteTypeLabel(weight: AssessmentWeight): String {
        return when (weight) {
            AssessmentWeight.FULL -> noteTypeFull
            AssessmentWeight.HALF -> noteTypeHalf
            AssessmentWeight.QUARTER -> noteTypeQuarter
        }
    }

    fun maxAttachmentsReached(limit: Int): String {
        return maxAttachmentsReachedTemplate.replace("{count}", limit.toString())
    }

    fun photoAttachmentCount(count: Int): String {
        return photoAttachmentCountTemplate.replace("{count}", count.toString())
    }

    fun targetProjectedAverage(average: String): String {
        return targetProjectedAverageTemplate.replace("{average}", average)
    }

    fun promotionSetupBasketProgress(count: Int): String {
        return promotionSetupBasketProgressTemplate.replace("{count}", count.toString())
    }

    fun promotionSetupMissingBasket(count: Int): String {
        return if (count == 1) {
            promotionSetupMissingBasketOne
        } else {
            promotionSetupMissingBasketMany.replace("{count}", count.toString())
        }
    }

    fun promotionSetupTooManyBasket(count: Int): String {
        return if (count == 1) {
            promotionSetupTooManyBasketOne
        } else {
            promotionSetupTooManyBasketMany.replace("{count}", count.toString())
        }
    }

    fun promotionSetupMissingGrades(subjects: String): String {
        return promotionSetupMissingGradesTemplate.replace("{subjects}", subjects)
    }

    fun themeModeLabel(mode: AppThemeMode): String {
        return when (mode) {
            AppThemeMode.SYSTEM -> darkModeSystem
            AppThemeMode.LIGHT -> darkModeLight
            AppThemeMode.DARK -> darkModeDark
        }
    }

    fun languageLabel(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> languageEnglish
            AppLanguage.FRENCH -> languageFrench
        }
    }

    fun semesterLabel(semester: SchoolSemester): String {
        return when (semester) {
            SchoolSemester.SEMESTER_1 -> semester1Label
            SchoolSemester.SEMESTER_2 -> semester2Label
        }
    }

    fun schoolYearLabel(year: SchoolYear): String {
        return when (year) {
            SchoolYear.YEAR_1 -> schoolYear1Label
            SchoolYear.YEAR_2 -> schoolYear2Label
            SchoolYear.YEAR_3 -> schoolYear3Label
        }
    }

    fun periodLabel(year: SchoolYear, semester: SchoolSemester): String {
        return "${schoolYearLabel(year)} · ${semesterLabel(semester)}"
    }

    companion object {
        val English = AppStrings(
            appName = "SwissGrades",
            chooseOption = "Choose your option",
            onboardingBody = "Set up your Option subject now. You can add grades and more subjects progressively later.",
            continueLabel = "Continue",
            mySubjects = "My subjects",
            addLabel = "Add",
            optionSettingsTitle = "Settings",
            languageSectionTitle = "Language",
            languageSectionDescription = "Choose the display language used in the app.",
            themeSectionTitle = "Appearance",
            themeSectionDescription = "Choose whether the app follows the system, stays light, or stays dark.",
            backupSectionTitle = "Backup",
            backupSectionDescription = "Export your SwissGrades data or restore a previous SwissGrades backup.",
            plusPointsSectionTitle = "PlusPoints migration",
            plusPointsSectionDescription = "Import a PlusPoints export and replace the current school data.",
            exportBackupLabel = "Export backup",
            importBackupLabel = "Import backup",
            importPlusPointsLabel = "Import PlusPoints",
            plusPointsImportTitle = "Import PlusPoints data?",
            plusPointsImportMessageTemplate = "Import {file} from PlusPoints and replace the notes in the selected semester? Your language and theme stay unchanged. Photos attached to replaced SwissGrades grades from that semester will be removed.",
            plusPointsImportConfirm = "Import data",
            plusPointsImportSuccess = "PlusPoints data imported successfully.",
            plusPointsImportFailure = "Could not import this PlusPoints file.",
            backupExportSuccess = "Backup exported successfully.",
            backupExportFailure = "Could not export this backup.",
            backupImportTitle = "Import backup?",
            backupImportMessageTemplate = "Import {file} and replace all current app data? This action cannot be undone.",
            backupImportConfirm = "Import backup",
            backupImportInvalid = "This backup file is invalid or incomplete.",
            backupImportFailure = "Could not import this backup.",
            backupImportSuccess = "Backup imported successfully.",
            resetSectionTitle = "Reset app",
            resetSectionDescription = "Delete all subjects, grades, photos, imports, and settings from this device.",
            resetAppLabel = "Reset all app data",
            resetAppTitle = "Reset SwissGrades?",
            resetAppMessage = "This will permanently delete all subjects, grades, photos, settings, imports, and backups stored in the app. This action cannot be undone.",
            resetAppConfirm = "Reset app",
            optionSectionTitle = "Option",
            optionSectionDescriptionPrefix = "Current option: ",
            optionSectionDescriptionSuffix = ". Changing it updates your Option subject directly.",
            periodTitle = "Period",
            choosePeriodTitle = "Choose a period",
            schoolYearTitle = "School year",
            schoolYear1Label = "First year",
            schoolYear2Label = "Second year",
            schoolYear3Label = "Third year",
            semesterTitle = "Semester",
            semester1Label = "Semester 1",
            semester2Label = "Semester 2",
            plusPointsTargetSemesterTitle = "Import into semester",
            darkModeSystem = "Auto",
            darkModeLight = "Light",
            darkModeDark = "Dark",
            languageEnglish = "English",
            languageFrench = "French",
            deleteSubjectTitle = "Delete subject?",
            addSubjectTitle = "Add a subject",
            editSubjectTitle = "Edit subject",
            subjectNameLabel = "SUBJECT NAME",
            subjectNamePlaceholder = "Ex: History",
            countInResultsTitle = "Count in calculations",
            countInResultsDescription = "Turn this off to keep the subject visible without affecting results.",
            addToBasketTitle = "Add to basket",
            addToBasketDescription = "Basket subjects count toward the 16-point rule.",
            personalizationTitle = "PERSONALIZATION",
            saveChanges = "Save changes",
            createSubject = "Create subject",
            promotionPointsTitle = "Promotion points",
            promotionPointsUnit = "advance points",
            promotionStatusPromoted = "Promoted",
            promotionStatusBlocked = "Blocked",
            promotionStatusIncomplete = "Incomplete",
            promotionHeadlinePromoted = "Promotion requirements are currently satisfied.",
            promotionHeadlineBlocked = "Promotion requirements are not satisfied.",
            promotionHeadlineIncomplete = "Promotion cannot be decided yet because some data is missing.",
            basketTitle = "Basket",
            insufficienciesTitle = "Insufficiencies",
            inBasketLabel = "In basket",
            notCountedLabel = "Not counted",
            insufficientLabel = "Insufficient",
            officialAverageLabel = "OFFICIAL AVERAGE",
            pointLabel = "Point",
            pointsLabel = "points",
            statusLabel = "Status",
            subSubjectsTitle = "Sub-subjects",
            averagePrefix = "Average",
            evolutionTitle = "Evolution",
            gradeHistoryTitle = "Notes",
            evaluationSingular = "evaluation",
            evaluationPlural = "evaluations",
            gradeValueLabel = "Grade value",
            gradeValuePlaceholder = "Ex: 5.5",
            descriptionOptional = "Description (optional)",
            attachmentsTitle = "Exam photos",
            addPhotoLabel = "Add photo",
            addMorePhotosLabel = "Add more",
            takePhotoLabel = "Take photo",
            chooseFromGalleryLabel = "Choose from gallery",
            removePhotoLabel = "Remove photo",
            attachedPhotosTitle = "Attached photos",
            importAttachmentFailed = "Could not import this image.",
            maxAttachmentsReachedTemplate = "You can attach up to {count} images to one grade.",
            photoAttachmentCountTemplate = "{count} photos",
            deleteLabel = "Delete",
            cancelLabel = "Cancel",
            closeLabel = "Close",
            backLabel = "Back",
            openSettingsLabel = "Open settings",
            editSubjectAction = "Edit subject",
            selectedColorDescription = "Selected color",
            changeOptionTitle = "Change option?",
            changeOptionMessage = "Changing your option will delete the grades currently saved in the Option subject. This action cannot be undone.",
            changeOptionConfirm = "Change option",
            deleteGradeTitle = "Delete grade?",
            deleteGradeMessageTemplate = "Remove {grade} from this subject? This action cannot be undone.",
            deleteSubjectMessageTemplate = "Remove {subject} and all its grades? This action cannot be undone.",
            deleteGradeLabel = "Delete grade",
            deleteSubjectActionTemplate = "Delete {subject}",
            gradeFallbackDescription = "this grade",
            emptyNotes = "No grades yet",
            invalidGradeValue = "Enter a grade from 1.0 to 6.0 in 0.25 steps.",
            emptySubjectName = "Enter a subject name.",
            duplicateSubjectName = "This subject already exists.",
            rawAverage = "Raw average",
            compositeAverage = "Composite average",
            notCalculableYet = "Not calculable yet",
            notEnoughGrades = "Not enough grades",
            unlockPromotionTooMany = "Keep exactly three non-option subjects in the basket to unlock promotion status.",
            unlockPromotionMissingGrades = "Add grades to every basket subject and the Option branch to unlock promotion status.",
            promotionSetupTitle = "Set up promotion",
            promotionSetupIntro = "Promotion needs exactly 3 basket branches. Add at least one grade in each basket branch and in your Option to unlock the result.",
            promotionSetupBasketStep = "Basket branches",
            promotionSetupOptionStep = "Option grade",
            promotionSetupGradesStep = "Required grades",
            promotionSetupReady = "Ready",
            promotionSetupNeedsAction = "Needs action",
            promotionSetupWaitingForBasket = "Choose basket first",
            promotionSetupBasketProgressTemplate = "{count} of 3 in basket",
            promotionSetupMissingBasketOne = "1 basket branch is missing.",
            promotionSetupMissingBasketMany = "{count} basket branches are missing.",
            promotionSetupTooManyBasketOne = "1 branch must be removed from the basket.",
            promotionSetupTooManyBasketMany = "{count} branches must be removed from the basket.",
            promotionSetupMissingGradesTemplate = "Add a grade to {subjects}.",
            promotionSetupAddBranchAction = "Add a branch",
            promotionSetupReviewBranchesAction = "Review basket",
            promotionSetupAddGradeAction = "Add missing grade",
            branchPromoted = "Promoted",
            branchInsufficient = "Insufficient",
            branchInsufficientShort = "Insuff.",
            noteTypeFull = "Full grade",
            noteTypeHalf = "Half grade",
            noteTypeQuarter = "Quarter grade",
            addGrade = "Add a grade",
            addGradeToTemplate = "Add a grade to {subject}",
            editGrade = "Edit grade",
            editGradeInTemplate = "Edit grade in {subject}",
            evaluationDefaultTitle = "Evaluation",
            branchTargetTitle = "Average target",
            branchTargetSubtitle = "Saved for this subject and used by the grade simulator.",
            branchTargetUnset = "No target set",
            branchTargetEdit = "Edit",
            branchTargetPlaceholder = "Ex: 5.0",
            branchTargetInvalid = "Use a target from 1.0 to 6.0 in 0.5 steps.",
            targetSimulationTitle = "Grade simulator",
            targetSimulationSubtitle = "Choose a target and plan your next grades.",
            targetAverageLabel = "Target average",
            plannedGradeCountTitle = "Future grades",
            plannedGradeCountTemplate = "Future grades: {count}",
            plannedGradeWeightTitle = "Grade weight",
            plannedGradeWeightHint = "All planned grades use this weight.",
            requiredGradeTitle = "Needed next grade",
            requiredAverageTitleTemplate = "Average needed over {count} grades",
            targetAlreadyReached = "Target secured, even with minimum grades.",
            targetImpossible = "Impossible within this plan, even with 6.0 grades.",
            targetInvalid = "Enter a target from 1.0 to 6.0 in 0.5 steps.",
            targetProjectedAverageTemplate = "Projected official average: {average}"
        )

        val French = AppStrings(
            appName = "SwissGrades",
            chooseOption = "Choisis ton option",
            onboardingBody = "Configure maintenant ta branche Option. Tu pourras ajouter des notes et d'autres branches ensuite.",
            continueLabel = "Continuer",
            mySubjects = "Mes branches",
            addLabel = "Ajouter",
            optionSettingsTitle = "Paramètres",
            languageSectionTitle = "Langue",
            languageSectionDescription = "Choisis la langue d'affichage de l'application.",
            themeSectionTitle = "Apparence",
            themeSectionDescription = "Choisis si l'application suit le système, reste claire ou reste sombre.",
            backupSectionTitle = "Sauvegarde",
            backupSectionDescription = "Exporte tes données SwissGrades ou restaure une sauvegarde SwissGrades existante.",
            plusPointsSectionTitle = "Migration PlusPoints",
            plusPointsSectionDescription = "Importe un export PlusPoints et remplace les données scolaires actuelles.",
            exportBackupLabel = "Exporter la sauvegarde",
            importBackupLabel = "Importer une sauvegarde",
            importPlusPointsLabel = "Importer PlusPoints",
            plusPointsImportTitle = "Importer des données PlusPoints ?",
            plusPointsImportMessageTemplate = "Importer {file} depuis PlusPoints et remplacer les notes du semestre choisi ? La langue et le thème restent inchangés. Les photos liées aux notes SwissGrades remplacées de ce semestre seront supprimées.",
            plusPointsImportConfirm = "Importer les données",
            plusPointsImportSuccess = "Données PlusPoints importées avec succès.",
            plusPointsImportFailure = "Impossible d'importer ce fichier PlusPoints.",
            backupExportSuccess = "Sauvegarde exportée avec succès.",
            backupExportFailure = "Impossible d'exporter cette sauvegarde.",
            backupImportTitle = "Importer la sauvegarde ?",
            backupImportMessageTemplate = "Importer {file} et remplacer toutes les données actuelles ? Cette action est irréversible.",
            backupImportConfirm = "Importer la sauvegarde",
            backupImportInvalid = "Ce fichier de sauvegarde est invalide ou incomplet.",
            backupImportFailure = "Impossible d'importer cette sauvegarde.",
            backupImportSuccess = "Sauvegarde importée avec succès.",
            resetSectionTitle = "Réinitialiser l'app",
            resetSectionDescription = "Supprime toutes les branches, notes, photos, imports et préférences de cet appareil.",
            resetAppLabel = "Tout réinitialiser",
            resetAppTitle = "Réinitialiser SwissGrades ?",
            resetAppMessage = "Toutes les branches, notes, photos, préférences, imports et sauvegardes stockés dans l'app seront supprimés définitivement. Cette action est irréversible.",
            resetAppConfirm = "Réinitialiser",
            optionSectionTitle = "Option",
            optionSectionDescriptionPrefix = "Option actuelle : ",
            optionSectionDescriptionSuffix = ". La changer met à jour directement ta branche Option.",
            periodTitle = "Période",
            choosePeriodTitle = "Choisir une période",
            schoolYearTitle = "Année scolaire",
            schoolYear1Label = "Première année",
            schoolYear2Label = "Deuxième année",
            schoolYear3Label = "Troisième année",
            semesterTitle = "Semestre",
            semester1Label = "Semestre 1",
            semester2Label = "Semestre 2",
            plusPointsTargetSemesterTitle = "Importer dans le semestre",
            darkModeSystem = "Auto",
            darkModeLight = "Clair",
            darkModeDark = "Sombre",
            languageEnglish = "Anglais",
            languageFrench = "Français",
            deleteSubjectTitle = "Supprimer la branche ?",
            addSubjectTitle = "Ajouter une branche",
            editSubjectTitle = "Modifier la branche",
            subjectNameLabel = "NOM DE LA BRANCHE",
            subjectNamePlaceholder = "Ex : Histoire",
            countInResultsTitle = "Compter dans les calculs",
            countInResultsDescription = "Désactive pour garder la branche visible sans l'inclure dans les résultats.",
            addToBasketTitle = "Ajouter au panier",
            addToBasketDescription = "Les branches du panier comptent pour la règle des 16 points.",
            personalizationTitle = "PERSONNALISATION",
            saveChanges = "Enregistrer",
            createSubject = "Créer la branche",
            promotionPointsTitle = "Points de promotion",
            promotionPointsUnit = "points d'avance",
            promotionStatusPromoted = "Promu",
            promotionStatusBlocked = "Bloqué",
            promotionStatusIncomplete = "Incomplet",
            promotionHeadlinePromoted = "Les conditions de promotion sont actuellement remplies.",
            promotionHeadlineBlocked = "Les conditions de promotion ne sont pas remplies.",
            promotionHeadlineIncomplete = "La promotion ne peut pas encore être décidée car certaines données manquent.",
            basketTitle = "Panier",
            insufficienciesTitle = "Insuffisances",
            inBasketLabel = "Dans le panier",
            notCountedLabel = "Non comptée",
            insufficientLabel = "Insuffisant",
            officialAverageLabel = "MOYENNE OFFICIELLE",
            pointLabel = "Point",
            pointsLabel = "points",
            statusLabel = "Statut",
            subSubjectsTitle = "Sous-branches",
            averagePrefix = "Moyenne",
            evolutionTitle = "Évolution",
            gradeHistoryTitle = "Notes",
            evaluationSingular = "évaluation",
            evaluationPlural = "évaluations",
            gradeValueLabel = "Valeur de la note",
            gradeValuePlaceholder = "Ex : 5,5",
            descriptionOptional = "Description (optionnelle)",
            attachmentsTitle = "Photos de l'examen",
            addPhotoLabel = "Ajouter une photo",
            addMorePhotosLabel = "Ajouter",
            takePhotoLabel = "Prendre une photo",
            chooseFromGalleryLabel = "Choisir depuis la galerie",
            removePhotoLabel = "Retirer la photo",
            attachedPhotosTitle = "Photos jointes",
            importAttachmentFailed = "Impossible d'importer cette image.",
            maxAttachmentsReachedTemplate = "Tu peux joindre jusqu'à {count} images à une note.",
            photoAttachmentCountTemplate = "{count} photos",
            deleteLabel = "Supprimer",
            cancelLabel = "Annuler",
            closeLabel = "Fermer",
            backLabel = "Retour",
            openSettingsLabel = "Ouvrir les paramètres",
            editSubjectAction = "Modifier la branche",
            selectedColorDescription = "Couleur sélectionnée",
            changeOptionTitle = "Changer d'option ?",
            changeOptionMessage = "Changer d'option supprimera les notes actuellement enregistrées dans la branche Option. Cette action est irréversible.",
            changeOptionConfirm = "Changer l'option",
            deleteGradeTitle = "Supprimer la note ?",
            deleteGradeMessageTemplate = "Supprimer {grade} de cette branche ? Cette action est irréversible.",
            deleteSubjectMessageTemplate = "Supprimer {subject} et toutes ses notes ? Cette action est irréversible.",
            deleteGradeLabel = "Supprimer la note",
            deleteSubjectActionTemplate = "Supprimer {subject}",
            gradeFallbackDescription = "cette note",
            emptyNotes = "Aucune note pour l'instant",
            invalidGradeValue = "Entre une note de 1,0 à 6,0 par pas de 0,25.",
            emptySubjectName = "Entre un nom de branche.",
            duplicateSubjectName = "Cette branche existe déjà.",
            rawAverage = "Moyenne brute",
            compositeAverage = "Moyenne composite",
            notCalculableYet = "Pas encore calculable",
            notEnoughGrades = "Pas assez de notes",
            unlockPromotionTooMany = "Garde exactement trois branches hors option dans le panier pour débloquer le statut de promotion.",
            unlockPromotionMissingGrades = "Ajoute des notes à chaque branche du panier et à la branche Option pour débloquer le statut de promotion.",
            promotionSetupTitle = "Préparer la promotion",
            promotionSetupIntro = "La promotion se calcule avec exactement 3 branches dans le panier. Ajoute au moins une note dans chacune d'elles et dans ton option pour débloquer le résultat.",
            promotionSetupBasketStep = "Branches du panier",
            promotionSetupOptionStep = "Note d'option",
            promotionSetupGradesStep = "Notes requises",
            promotionSetupReady = "Prêt",
            promotionSetupNeedsAction = "À corriger",
            promotionSetupWaitingForBasket = "Choisis d'abord le panier",
            promotionSetupBasketProgressTemplate = "{count} sur 3 dans le panier",
            promotionSetupMissingBasketOne = "Il manque 1 branche dans le panier.",
            promotionSetupMissingBasketMany = "Il manque {count} branches dans le panier.",
            promotionSetupTooManyBasketOne = "1 branche doit être retirée du panier.",
            promotionSetupTooManyBasketMany = "{count} branches doivent être retirées du panier.",
            promotionSetupMissingGradesTemplate = "Ajoute une note dans {subjects}.",
            promotionSetupAddBranchAction = "Ajouter une branche",
            promotionSetupReviewBranchesAction = "Vérifier le panier",
            promotionSetupAddGradeAction = "Ajouter la note manquante",
            branchPromoted = "Promu",
            branchInsufficient = "Insuffisant",
            branchInsufficientShort = "Insuff.",
            noteTypeFull = "Note entière",
            noteTypeHalf = "Demi-note",
            noteTypeQuarter = "Quart de note",
            addGrade = "Ajouter une note",
            addGradeToTemplate = "Ajouter une note à {subject}",
            editGrade = "Modifier la note",
            editGradeInTemplate = "Modifier la note de {subject}",
            evaluationDefaultTitle = "Évaluation",
            branchTargetTitle = "Objectif de moyenne",
            branchTargetSubtitle = "Sauvegardé pour cette branche et utilisé par le simulateur.",
            branchTargetUnset = "Aucun objectif défini",
            branchTargetEdit = "Modifier",
            branchTargetPlaceholder = "Ex : 5,0",
            branchTargetInvalid = "Utilise un objectif de 1,0 à 6,0 par pas de 0,5.",
            targetSimulationTitle = "Simulateur de note",
            targetSimulationSubtitle = "Choisis un objectif et planifie tes prochaines notes.",
            targetAverageLabel = "Moyenne visée",
            plannedGradeCountTitle = "Notes à venir",
            plannedGradeCountTemplate = "Notes à venir : {count}",
            plannedGradeWeightTitle = "Poids des notes",
            plannedGradeWeightHint = "Toutes les notes planifiées utilisent ce poids.",
            requiredGradeTitle = "Note nécessaire",
            requiredAverageTitleTemplate = "Moyenne nécessaire sur {count} notes",
            targetAlreadyReached = "Objectif assuré, même avec les notes minimales.",
            targetImpossible = "Impossible avec ce plan, même avec des notes de 6,0.",
            targetInvalid = "Entre un objectif de 1,0 à 6,0 par pas de 0,5.",
            targetProjectedAverageTemplate = "Moyenne officielle projetée : {average}"
        )
    }
}

val AppLanguage.locale: Locale
    get() = when (this) {
        AppLanguage.ENGLISH -> Locale.US
        AppLanguage.FRENCH -> Locale.FRENCH
    }

val AppLanguage.strings: AppStrings
    get() = when (this) {
        AppLanguage.ENGLISH -> AppStrings.English
        AppLanguage.FRENCH -> AppStrings.French
    }

val LocalAppLanguage = compositionLocalOf { AppLanguage.FRENCH }
val LocalAppStrings = compositionLocalOf { AppStrings.English }

@Composable
fun currentAppStrings(): AppStrings = LocalAppStrings.current

fun AppLanguage.optionCategoryLabel(choice: InitialOptionChoice): String {
    return when (this) {
        AppLanguage.ENGLISH -> when (choice) {
            InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH,
            InitialOptionChoice.BIOLOGY_CHEMISTRY -> "Experimental sciences"
            InitialOptionChoice.ECONOMICS_LAW -> "Management & society"
            InitialOptionChoice.SPANISH,
            InitialOptionChoice.ITALIAN -> "Modern languages"
            InitialOptionChoice.LATIN -> "Classical languages"
            InitialOptionChoice.MUSIC,
            InitialOptionChoice.VISUAL_ARTS -> "Arts"
            InitialOptionChoice.PHILOSOPHY -> "Humanities"
            InitialOptionChoice.OTHER -> "Custom option"
        }

        AppLanguage.FRENCH -> when (choice) {
            InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH,
            InitialOptionChoice.BIOLOGY_CHEMISTRY -> "Sciences expérimentales"
            InitialOptionChoice.ECONOMICS_LAW -> "Gestion et société"
            InitialOptionChoice.SPANISH,
            InitialOptionChoice.ITALIAN -> "Langues modernes"
            InitialOptionChoice.LATIN -> "Langues classiques"
            InitialOptionChoice.MUSIC,
            InitialOptionChoice.VISUAL_ARTS -> "Arts"
            InitialOptionChoice.PHILOSOPHY -> "Sciences humaines"
            InitialOptionChoice.OTHER -> "Option personnalisée"
        }
    }
}

fun AppLanguage.optionChoiceLabel(choice: InitialOptionChoice): String {
    return when (this) {
        AppLanguage.ENGLISH -> when (choice) {
            InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH -> "PYAM"
            InitialOptionChoice.BIOLOGY_CHEMISTRY -> "BICH"
            InitialOptionChoice.ECONOMICS_LAW -> "Economics-Law"
            InitialOptionChoice.SPANISH -> "Spanish"
            InitialOptionChoice.ITALIAN -> "Italian"
            InitialOptionChoice.LATIN -> "Latin"
            InitialOptionChoice.MUSIC -> "Music"
            InitialOptionChoice.PHILOSOPHY -> "Philosophy"
            InitialOptionChoice.VISUAL_ARTS -> "Visual Arts"
            InitialOptionChoice.OTHER -> "Other"
        }

        AppLanguage.FRENCH -> when (choice) {
            InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH -> "PYAM"
            InitialOptionChoice.BIOLOGY_CHEMISTRY -> "BICH"
            InitialOptionChoice.ECONOMICS_LAW -> "Économie-droit"
            InitialOptionChoice.SPANISH -> "Espagnol"
            InitialOptionChoice.ITALIAN -> "Italien"
            InitialOptionChoice.LATIN -> "Latin"
            InitialOptionChoice.MUSIC -> "Musique"
            InitialOptionChoice.PHILOSOPHY -> "Philosophie"
            InitialOptionChoice.VISUAL_ARTS -> "Arts visuels"
            InitialOptionChoice.OTHER -> "Autre"
        }
    }
}

fun AppLanguage.optionSubSubjectLabel(name: String): String {
    return when (this) {
        AppLanguage.ENGLISH -> when (name) {
            "Physics" -> "Physics"
            "Applications of Mathematics" -> "Applications of Mathematics"
            "Biology" -> "Biology"
            "Chemistry" -> "Chemistry"
            "Economics" -> "Economics"
            "Law" -> "Law"
            else -> name
        }

        AppLanguage.FRENCH -> when (name) {
            "Physics" -> "Physique"
            "Applications of Mathematics" -> "Applications des mathématiques"
            "Biology" -> "Biologie"
            "Chemistry" -> "Chimie"
            "Economics" -> "Économie"
            "Law" -> "Droit"
            else -> name
        }
    }
}

fun AppLanguage.colorChoiceLabel(choice: SubjectColorChoice): String {
    return when (this) {
        AppLanguage.ENGLISH -> when (choice) {
            SubjectColorChoice.BLUE -> "Blue"
            SubjectColorChoice.RED -> "Red"
            SubjectColorChoice.TEAL -> "Teal"
            SubjectColorChoice.SLATE -> "Slate"
            SubjectColorChoice.PURPLE -> "Purple"
            SubjectColorChoice.PINK -> "Pink"
            SubjectColorChoice.GREEN -> "Green"
            SubjectColorChoice.AMBER -> "Amber"
            SubjectColorChoice.ORANGE -> "Orange"
        }

        AppLanguage.FRENCH -> when (choice) {
            SubjectColorChoice.BLUE -> "Bleu"
            SubjectColorChoice.RED -> "Rouge"
            SubjectColorChoice.TEAL -> "Turquoise"
            SubjectColorChoice.SLATE -> "Ardoise"
            SubjectColorChoice.PURPLE -> "Violet"
            SubjectColorChoice.PINK -> "Rose"
            SubjectColorChoice.GREEN -> "Vert"
            SubjectColorChoice.AMBER -> "Ambre"
            SubjectColorChoice.ORANGE -> "Orange"
        }
    }
}

fun AppLanguage.iconChoiceLabel(choice: SubjectIconChoice): String {
    return when (this) {
        AppLanguage.ENGLISH -> when (choice) {
            SubjectIconChoice.BOOK -> "Book"
            SubjectIconChoice.SCIENCE -> "Science"
            SubjectIconChoice.LANGUAGE -> "Language"
            SubjectIconChoice.MUSIC -> "Music"
            SubjectIconChoice.ART -> "Art"
            SubjectIconChoice.MIND -> "Mind"
            SubjectIconChoice.BALANCE -> "Balance"
            SubjectIconChoice.CATEGORY -> "Category"
            SubjectIconChoice.HISTORY -> "History"
            SubjectIconChoice.MATH -> "Math"
            SubjectIconChoice.WORLD -> "World"
            SubjectIconChoice.SPORT -> "Sport"
        }

        AppLanguage.FRENCH -> when (choice) {
            SubjectIconChoice.BOOK -> "Livre"
            SubjectIconChoice.SCIENCE -> "Science"
            SubjectIconChoice.LANGUAGE -> "Langue"
            SubjectIconChoice.MUSIC -> "Musique"
            SubjectIconChoice.ART -> "Art"
            SubjectIconChoice.MIND -> "Esprit"
            SubjectIconChoice.BALANCE -> "Balance"
            SubjectIconChoice.CATEGORY -> "Catégorie"
            SubjectIconChoice.HISTORY -> "Histoire"
            SubjectIconChoice.MATH -> "Mathématiques"
            SubjectIconChoice.WORLD -> "Monde"
            SubjectIconChoice.SPORT -> "Sport"
        }
    }
}

@Composable
fun ProvideAppStrings(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalAppStrings provides language.strings,
        content = content
    )
}
