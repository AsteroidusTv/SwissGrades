package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val language = LocalAppLanguage.current
    var pendingOptionChange by remember { mutableStateOf<InitialOptionChoice?>(null) }
    var isResetConfirmationVisible by remember { mutableStateOf(false) }
    var isGradeReportConfirmationVisible by remember { mutableStateOf(false) }
    var isOptionChooserExpanded by remember { mutableStateOf(false) }
    val isFileOperationInProgress =
        settings.isBackupInProgress || settings.isGradeReportExportInProgress
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = AppScreenHorizontalPadding,
                top = AppScreenTopPadding,
                end = AppScreenHorizontalPadding,
                bottom = AppScreenBottomPadding
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderBackButton(
                onClick = onBack,
                modifier = Modifier.testTag("back-from-settings")
            )
            Text(
                text = strings.optionSettingsTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SettingsGroupHeading(strings.appPreferencesGroupTitle)

        SettingsChoiceSection(
            title = strings.languageSectionTitle,
            description = strings.languageSectionDescription
        ) {
            AppLanguage.entries.forEach { language ->
                SettingsChoiceCard(
                    title = strings.languageLabel(language),
                    selected = settings.selectedLanguage == language,
                    onClick = { onSelectLanguage(language) }
                )
            }
        }

        SettingsChoiceSection(
            title = strings.themeSectionTitle,
            description = strings.themeSectionDescription
        ) {
            AppThemeMode.entries.forEach { themeMode ->
                SettingsChoiceCard(
                    title = strings.themeModeLabel(themeMode),
                    selected = settings.selectedThemeMode == themeMode,
                    onClick = { onSelectThemeMode(themeMode) }
                )
            }
        }

        SettingsGroupHeading(strings.schoolSetupGroupTitle)

        SettingsChoiceSection(
            title = strings.optionSectionTitle,
            description = strings.optionDescription(language.optionChoiceLabel(settings.selectedOption))
        ) {
            Button(
                onClick = { isOptionChooserExpanded = !isOptionChooserExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle-option-choices"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appSelectedOptionContainer(),
                    contentColor = appAccentBlue()
                )
            ) {
                Text(
                    if (isOptionChooserExpanded) {
                        strings.hideOptionChoicesLabel
                    } else {
                        strings.changeOptionLabel
                    }
                )
            }

            if (isOptionChooserExpanded) {
                InitialOptionChoice.entries.forEach { choice ->
                    OnboardingOptionCard(
                        choice = choice,
                        isSelected = settings.selectedOption == choice,
                        onClick = {
                            if (settings.selectedOption != choice) {
                                pendingOptionChange = choice
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings-option-${choice.name}")
                    )
                }
            }
        }

        SettingsGroupHeading(strings.dataManagementGroupTitle)

        if (isFileOperationInProgress) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-file-operation-progress"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    text = strings.fileOperationInProgress,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SettingsChoiceSection(
            title = strings.backupSectionTitle,
            description = strings.backupSectionDescription
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportBackup,
                    enabled = !isFileOperationInProgress,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appSoftAccentContainer(),
                        contentColor = appAccentBlue()
                    )
                ) {
                    Text(strings.exportBackupLabel, textAlign = TextAlign.Center)
                }
                Button(
                    onClick = onImportBackup,
                    enabled = !isFileOperationInProgress,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appAccentBlue())
                ) {
                    Text(strings.importBackupLabel, textAlign = TextAlign.Center)
                }
            }

            settings.backupMessage?.let { message ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = when (settings.backupMessageTone) {
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
                        color = when (settings.backupMessageTone) {
                            DashboardStatusTone.POSITIVE -> appPositiveColor()
                            DashboardStatusTone.NEGATIVE -> appWarningColor()
                            DashboardStatusTone.NEUTRAL -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        SettingsChoiceSection(
            title = strings.gradeReportSectionTitle,
            description = strings.gradeReportSectionDescription
        ) {
            Button(
                onClick = { isGradeReportConfirmationVisible = true },
                enabled = !isFileOperationInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export-grade-report-button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appSelectedOptionContainer(),
                    contentColor = appAccentBlue()
                )
            ) {
                Text(strings.exportGradeReportLabel, textAlign = TextAlign.Center)
            }

            settings.gradeReportMessage?.let { message ->
                SettingsStatusMessage(
                    message = message,
                    tone = settings.gradeReportMessageTone
                )
            }
        }

        SettingsChoiceSection(
            title = strings.plusPointsSectionTitle,
            description = strings.plusPointsSectionDescription
        ) {
            Button(
                onClick = onImportPlusPoints,
                enabled = !isFileOperationInProgress,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appSelectedOptionContainer(),
                    contentColor = appAccentBlue()
                )
            ) {
                Text(strings.importPlusPointsLabel, textAlign = TextAlign.Center)
            }
        }

        SettingsChoiceSection(
            title = strings.resetSectionTitle,
            description = strings.resetSectionDescription
        ) {
            Button(
                onClick = { isResetConfirmationVisible = true },
                enabled = !isFileOperationInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset-app-button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appWarningBackground(),
                    contentColor = appWarningColor()
                )
            ) {
                Text(strings.resetAppLabel, textAlign = TextAlign.Center)
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
        AlertDialog(
            onDismissRequest = onDismissPendingPlusPointsImport,
            title = {
                Text(
                    text = strings.plusPointsImportTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
            shape = RoundedCornerShape(24.dp),
            containerColor = appCardSurface()
        )
    }
}

@Composable
private fun SettingsGroupHeading(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = appAccentBlue()
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
private fun SettingsChoiceSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsChoiceCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                this.selected = selected
                role = Role.RadioButton
                contentDescription = title
            },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) appSelectedOptionBorder() else appCardBorderColor()
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) appSelectedOptionContainer() else appCardSurface()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (selected) appSelectedOptionBorder() else appCardBorderColor())
            )
        }
    }
}
