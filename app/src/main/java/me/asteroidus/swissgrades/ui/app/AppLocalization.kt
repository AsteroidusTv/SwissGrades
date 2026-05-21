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
    val exportBackupLabel: String,
    val importBackupLabel: String,
    val backupExportSuccess: String,
    val backupExportFailure: String,
    val backupImportTitle: String,
    val backupImportMessageTemplate: String,
    val backupImportConfirm: String,
    val backupImportInvalid: String,
    val backupImportFailure: String,
    val backupImportSuccess: String,
    val optionSectionTitle: String,
    val optionSectionDescriptionPrefix: String,
    val optionSectionDescriptionSuffix: String,
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
    val evaluationDefaultTitle: String
) {
    fun optionDescription(optionLabel: String): String {
        return "$optionSectionDescriptionPrefix$optionLabel$optionSectionDescriptionSuffix"
    }

    fun backupImportMessage(fileName: String): String {
        return backupImportMessageTemplate.replace("{file}", fileName)
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
            backupSectionDescription = "Export your full SwissGrades data to one backup file, or import a backup that replaces the current app data.",
            exportBackupLabel = "Export backup",
            importBackupLabel = "Import backup",
            backupExportSuccess = "Backup exported successfully.",
            backupExportFailure = "Could not export this backup.",
            backupImportTitle = "Import backup?",
            backupImportMessageTemplate = "Import {file} and replace all current app data? This action cannot be undone.",
            backupImportConfirm = "Import backup",
            backupImportInvalid = "This backup file is invalid or incomplete.",
            backupImportFailure = "Could not import this backup.",
            backupImportSuccess = "Backup imported successfully.",
            optionSectionTitle = "Option",
            optionSectionDescriptionPrefix = "Current option: ",
            optionSectionDescriptionSuffix = ". Changing it updates your Option subject directly.",
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
            evaluationDefaultTitle = "Evaluation"
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
            backupSectionDescription = "Exporte toutes tes données SwissGrades dans un fichier de sauvegarde, ou importe une sauvegarde qui remplace toutes les données actuelles.",
            exportBackupLabel = "Exporter la sauvegarde",
            importBackupLabel = "Importer une sauvegarde",
            backupExportSuccess = "Sauvegarde exportée avec succès.",
            backupExportFailure = "Impossible d'exporter cette sauvegarde.",
            backupImportTitle = "Importer la sauvegarde ?",
            backupImportMessageTemplate = "Importer {file} et remplacer toutes les données actuelles ? Cette action est irréversible.",
            backupImportConfirm = "Importer la sauvegarde",
            backupImportInvalid = "Ce fichier de sauvegarde est invalide ou incomplet.",
            backupImportFailure = "Impossible d'importer cette sauvegarde.",
            backupImportSuccess = "Sauvegarde importée avec succès.",
            optionSectionTitle = "Option",
            optionSectionDescriptionPrefix = "Option actuelle : ",
            optionSectionDescriptionSuffix = ". La changer met à jour directement ta branche Option.",
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
            gradeValuePlaceholder = "Ex : 5.5",
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
            invalidGradeValue = "Entre une note de 1.0 à 6.0 par pas de 0.25.",
            emptySubjectName = "Entre un nom de branche.",
            duplicateSubjectName = "Cette branche existe déjà.",
            rawAverage = "Moyenne brute",
            compositeAverage = "Moyenne composite",
            notCalculableYet = "Pas encore calculable",
            notEnoughGrades = "Pas assez de notes",
            unlockPromotionTooMany = "Garde exactement trois branches hors option dans le panier pour débloquer le statut de promotion.",
            unlockPromotionMissingGrades = "Ajoute des notes à chaque branche du panier et à la branche Option pour débloquer le statut de promotion.",
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
            evaluationDefaultTitle = "Évaluation"
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
