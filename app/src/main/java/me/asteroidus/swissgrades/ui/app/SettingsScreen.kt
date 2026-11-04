package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingsScreen(
    settings: SettingsUiState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onSelectOption: (InitialOptionChoice) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onImportPlusPoints: () -> Unit,
    onExportGradeReport: () -> Unit,
    onDismissPendingImport: () -> Unit,
    onConfirmPendingImport: () -> Unit,
    onDismissPendingPlusPointsImport: () -> Unit,
    onSelectPendingPlusPointsSemester: (SchoolSemester) -> Unit,
    onConfirmPendingPlusPointsImport: () -> Unit,
    onResetApp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true
) {
    val strings = currentAppStrings()
    val language = LocalAppLanguage.current
    var pendingOptionChange by remember { mutableStateOf<InitialOptionChoice?>(null) }
    var isResetConfirmationVisible by remember { mutableStateOf(false) }
    var isGradeReportConfirmationVisible by remember { mutableStateOf(false) }
    var isOptionChooserExpanded by remember { mutableStateOf(false) }
    val isFileOperationInProgress =
        settings.isBackupInProgress || settings.isGradeReportExportInProgress
    var isAppearanceChooserVisible by remember { mutableStateOf(false) }
    var isLanguageChooserVisible by remember { mutableStateOf(false) }
    var isBackupChooserVisible by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showBackButton) {
                    HeaderBackButton(onClick = onBack, modifier = Modifier.testTag("back-from-settings"))
                }
                Text(
                    text = strings.optionSettingsTitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = strings.settingsIntro,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            CarnetSettingsRow(
                icon = Icons.Outlined.Palette,
                title = strings.themeSectionTitle,
                supportingText = strings.themeModeLabel(settings.selectedThemeMode),
                enabled = !isFileOperationInProgress,
                modifier = Modifier.testTag("settings-appearance"),
                onClick = { isAppearanceChooserVisible = true }
            )
            CarnetSettingsRow(
                icon = Icons.Outlined.Language,
                title = strings.languageSectionTitle,
                supportingText = strings.languageLabel(settings.selectedLanguage),
                enabled = !isFileOperationInProgress,
                modifier = Modifier.testTag("settings-language"),
                onClick = { isLanguageChooserVisible = true }
            )

            SettingsGroupHeading(strings.myDataTitle)
            if (isFileOperationInProgress) {
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("settings-file-operation-progress").padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(strings.fileOperationInProgress, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            CarnetSettingsRow(
                icon = Icons.Outlined.Archive,
                title = strings.backupAndRestoreLabel,
                supportingText = strings.backupFilesCaption,
                enabled = !isFileOperationInProgress,
                modifier = Modifier.testTag("settings-backup-and-restore"),
                onClick = { isBackupChooserVisible = true }
            )
            settings.backupMessage?.let { SettingsStatusMessage(it, settings.backupMessageTone) }
            CarnetSettingsRow(
                icon = Icons.Outlined.FileUpload,
                title = strings.importPlusPointsLabel,
                enabled = !isFileOperationInProgress,
                modifier = Modifier.testTag("import-pluspoints-button"),
                onClick = onImportPlusPoints
            )
            CarnetSettingsRow(
                icon = Icons.Outlined.Description,
                title = strings.exportGradeReportLabel,
                enabled = !isFileOperationInProgress,
                modifier = Modifier.testTag("export-grade-report-button"),
                onClick = { isGradeReportConfirmationVisible = true }
            )
            settings.gradeReportMessage?.let { SettingsStatusMessage(it, settings.gradeReportMessageTone) }

            SettingsGroupHeading(strings.schoolSetupGroupTitle)
            CarnetSettingsRow(
                icon = Icons.Outlined.School,
                title = strings.optionSectionTitle,
                supportingText = language.optionChoiceLabel(settings.selectedOption),
                enabled = !isFileOperationInProgress,
                modifier = Modifier.testTag("toggle-option-choices"),
                onClick = { isOptionChooserExpanded = true }
            )
            Text(
                text = "${strings.schoolYearLabel(settings.selectedYear)} · ${strings.semesterLabel(settings.selectedSemester)}",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            CarnetSettingsRow(
                icon = Icons.Outlined.RestartAlt,
                title = strings.resetAppLabel,
                supportingText = strings.resetSectionDescription,
                enabled = !isFileOperationInProgress,
                destructive = true,
                modifier = Modifier.padding(top = 8.dp).testTag("reset-app-button"),
                onClick = { isResetConfirmationVisible = true }
            )
        }
    }

    if (isAppearanceChooserVisible) {
        SettingsSelectionDialog(
            title = strings.themeSectionTitle,
            description = strings.themeSectionDescription,
            onDismiss = { isAppearanceChooserVisible = false }
        ) {
            AppThemeMode.entries.forEach { mode ->
                SettingsChoiceCard(strings.themeModeLabel(mode), settings.selectedThemeMode == mode) {
                    isAppearanceChooserVisible = false
                    onSelectThemeMode(mode)
                }
            }
        }
    }
    if (isLanguageChooserVisible) {
        SettingsSelectionDialog(
            title = strings.languageSectionTitle,
            description = strings.languageSectionDescription,
            onDismiss = { isLanguageChooserVisible = false }
        ) {
            AppLanguage.entries.forEach { choice ->
                SettingsChoiceCard(strings.languageLabel(choice), settings.selectedLanguage == choice) {
                    isLanguageChooserVisible = false
                    onSelectLanguage(choice)
                }
            }
        }
    }
    if (isBackupChooserVisible) {
        SettingsSelectionDialog(
            title = strings.backupAndRestoreLabel,
            description = strings.backupSectionDescription,
            onDismiss = { isBackupChooserVisible = false }
        ) {
            CarnetSettingsRow(Icons.Outlined.FileDownload, strings.exportBackupLabel,
                modifier = Modifier.testTag("export-backup-button"), onClick = {
                    isBackupChooserVisible = false
                    onExportBackup()
                })
            CarnetSettingsRow(Icons.Outlined.FileUpload, strings.importBackupLabel,
                modifier = Modifier.testTag("import-backup-button"), onClick = {
                    isBackupChooserVisible = false
                    onImportBackup()
                })
        }
    }
    if (isOptionChooserExpanded) {
        SettingsSelectionDialog(
            title = strings.changeOptionLabel,
            description = strings.optionDescription(language.optionChoiceLabel(settings.selectedOption)),
            onDismiss = { isOptionChooserExpanded = false }
        ) {
            InitialOptionChoice.entries.forEach { choice ->
                SettingsChoiceCard(
                    title = language.optionChoiceLabel(choice),
                    selected = settings.selectedOption == choice,
                    modifier = Modifier.testTag("settings-option-${choice.name}"),
                    onClick = {
                        isOptionChooserExpanded = false
                        if (settings.selectedOption != choice) pendingOptionChange = choice
                    }
                )
            }
        }
    }

    pendingOptionChange?.let { choice ->
        ConfirmationDialog(
            title = strings.changeOptionTitle,
            message = strings.changeOptionMessage,
            confirmLabel = strings.changeOptionConfirm,
            onDismiss = { pendingOptionChange = null },
            onConfirm = {
                pendingOptionChange = null
                isOptionChooserExpanded = false
                onSelectOption(choice)
            }
        )
    }

    if (isResetConfirmationVisible) {
        ConfirmationDialog(
            title = strings.resetAppTitle,
            message = strings.resetAppMessage,
            confirmLabel = strings.resetAppConfirm,
            onDismiss = { isResetConfirmationVisible = false },
            onConfirm = {
                isResetConfirmationVisible = false
                onResetApp()
            }
        )
    }

    if (isGradeReportConfirmationVisible) {
        ConfirmationDialog(
            title = strings.gradeReportExportTitle,
            message = strings.gradeReportExportMessage(
                strings.gradeReportPeriodLabel(
                    settings.selectedYear,
                    settings.selectedSemester
                )
            ),
            confirmLabel = strings.gradeReportExportConfirm,
            onDismiss = { isGradeReportConfirmationVisible = false },
            onConfirm = {
                isGradeReportConfirmationVisible = false
                onExportGradeReport()
            }
        )
    }

    settings.pendingImportDisplayName?.let { displayName ->
        ConfirmationDialog(
            title = strings.backupImportTitle,
            message = strings.backupImportMessage(displayName),
            confirmLabel = strings.backupImportConfirm,
            onDismiss = onDismissPendingImport,
            onConfirm = onConfirmPendingImport
        )
    }

    settings.pendingPlusPointsImportDisplayName?.let { displayName ->
        CarnetDialog(
            onDismissRequest = onDismissPendingPlusPointsImport,
            title = {
                Text(
                    text = strings.plusPointsImportTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        strings.plusPointsImportMessage(
                            fileName = displayName,
                            year = settings.selectedYear,
                            semester = settings.pendingPlusPointsTargetSemester
                                ?: settings.selectedSemester
                        )
                    )
                    Text(
                        text = strings.plusPointsTargetSemesterTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SemesterSwitcher(
                        selectedSemester = settings.pendingPlusPointsTargetSemester
                            ?: settings.selectedSemester,
                        onSelectSemester = onSelectPendingPlusPointsSemester
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmPendingPlusPointsImport) {
                    Text(strings.plusPointsImportConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPendingPlusPointsImport) {
                    Text(strings.cancelLabel)
                }
            },
        )
    }
}

@Composable
private fun SettingsGroupHeading(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 18.dp, bottom = 2.dp),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun CarnetSettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            color = MaterialTheme.colorScheme.background,
            contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.heightIn(min = 56.dp).padding(vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(title, fontSize = 14.sp, lineHeight = 20.sp)
                    supportingText?.let {
                        Text(it, fontSize = 13.sp, lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun SettingsSelectionDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val strings = currentAppStrings()
    CarnetDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancelLabel) } },
    )
}

@Composable
private fun SettingsStatusMessage(
    message: String,
    tone: DashboardStatusTone
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = when (tone) {
            DashboardStatusTone.POSITIVE -> appPositiveBackground()
            DashboardStatusTone.NEGATIVE -> appWarningBackground()
            DashboardStatusTone.NEUTRAL -> appNeutralBackground()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = when (tone) {
                DashboardStatusTone.POSITIVE -> appPositiveColor()
                DashboardStatusTone.NEGATIVE -> appWarningColor()
                DashboardStatusTone.NEUTRAL -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun SettingsChoiceCard(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics {
            this.selected = selected
            role = Role.RadioButton
            contentDescription = title
        },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
            if (selected) Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
