package me.asteroidus.swissgrades.ui.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun GradeTrackerApp(
    modifier: Modifier = Modifier,
    persistence: SimulationEditorPersistence? = null
) {
    val context = LocalContext.current
    val resolvedPersistence = remember(context, persistence) {
        persistence ?: SharedPreferencesSimulationEditorPersistence(context.applicationContext)
    }
    val stateHolder = remember(resolvedPersistence) {
        SimulationEditorStateHolder(persistence = resolvedPersistence)
    }

    SimulationEditorRoute(
        uiState = stateHolder.uiState,
        onGradeInputChanged = stateHolder::onGradeInputChanged,
        onWeightChanged = stateHolder::onWeightChanged,
        onAddGradeEntry = stateHolder::addGradeEntry,
        onRemoveGradeEntry = stateHolder::removeGradeEntry,
        onOptionModeChanged = stateHolder::onOptionModeChanged,
        onSimpleOptionChanged = stateHolder::onSimpleOptionChanged,
        onCompositeOptionChanged = stateHolder::onCompositeOptionChanged,
        onCompositeOptionGradeInputChanged = stateHolder::onCompositeOptionGradeInputChanged,
        onCompositeOptionWeightChanged = stateHolder::onCompositeOptionWeightChanged,
        onAddCompositeOptionGradeEntry = stateHolder::addCompositeOptionGradeEntry,
        onRemoveCompositeOptionGradeEntry = stateHolder::removeCompositeOptionGradeEntry,
        onCustomSubjectNameInputChanged = stateHolder::onCustomSubjectNameInputChanged,
        onAddCustomSubject = stateHolder::addCustomSubject,
        onRemoveCustomSubject = stateHolder::removeCustomSubject,
        modifier = modifier
    )
}

@Composable
fun SimulationEditorRoute(
    uiState: SimulationEditorUiState,
    onGradeInputChanged: (BranchIdentifier, String, String) -> Unit,
    onWeightChanged: (BranchIdentifier, String, GradeWeightUi) -> Unit,
    onAddGradeEntry: (BranchIdentifier) -> Unit,
    onRemoveGradeEntry: (BranchIdentifier, String) -> Unit,
    onOptionModeChanged: (OptionModeUi) -> Unit,
    onSimpleOptionChanged: (SimpleOptionChoice) -> Unit,
    onCompositeOptionChanged: (CompositeOptionChoice) -> Unit,
    onCompositeOptionGradeInputChanged: (OptionSubSubjectKey, String, String) -> Unit,
    onCompositeOptionWeightChanged: (OptionSubSubjectKey, String, GradeWeightUi) -> Unit,
    onAddCompositeOptionGradeEntry: (OptionSubSubjectKey) -> Unit,
    onRemoveCompositeOptionGradeEntry: (OptionSubSubjectKey, String) -> Unit,
    onCustomSubjectNameInputChanged: (String) -> Unit,
    onAddCustomSubject: () -> Unit,
    onRemoveCustomSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "GradeTracker",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Edit your subjects and grades. Your promotion summary updates automatically. Use a dot for decimals, for example 4.25.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("editor-intro")
        )

        SimulationEditorSection(
            branchInputs = uiState.branchInputs,
            optionEditor = uiState.optionEditor,
            customSubjectNameInput = uiState.customSubjectNameInput,
            customSubjectNameErrorMessage = uiState.customSubjectNameErrorMessage,
            onGradeInputChanged = onGradeInputChanged,
            onWeightChanged = onWeightChanged,
            onAddGradeEntry = onAddGradeEntry,
            onRemoveGradeEntry = onRemoveGradeEntry,
            onOptionModeChanged = onOptionModeChanged,
            onSimpleOptionChanged = onSimpleOptionChanged,
            onCompositeOptionChanged = onCompositeOptionChanged,
            onCompositeOptionGradeInputChanged = onCompositeOptionGradeInputChanged,
            onCompositeOptionWeightChanged = onCompositeOptionWeightChanged,
            onAddCompositeOptionGradeEntry = onAddCompositeOptionGradeEntry,
            onRemoveCompositeOptionGradeEntry = onRemoveCompositeOptionGradeEntry,
            onCustomSubjectNameInputChanged = onCustomSubjectNameInputChanged,
            onAddCustomSubject = onAddCustomSubject,
            onRemoveCustomSubject = onRemoveCustomSubject
        )

        uiState.inputNoticeMessage?.let { noticeMessage ->
            Text(
                text = noticeMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("input-notice")
            )
        }

        PromotionSummaryScreen(presentation = uiState.summary)
    }
}

@Composable
private fun SimulationEditorSection(
    branchInputs: List<BranchInputUiState>,
    optionEditor: OptionEditorUiState,
    customSubjectNameInput: String,
    customSubjectNameErrorMessage: String?,
    onGradeInputChanged: (BranchIdentifier, String, String) -> Unit,
    onWeightChanged: (BranchIdentifier, String, GradeWeightUi) -> Unit,
    onAddGradeEntry: (BranchIdentifier) -> Unit,
    onRemoveGradeEntry: (BranchIdentifier, String) -> Unit,
    onOptionModeChanged: (OptionModeUi) -> Unit,
    onSimpleOptionChanged: (SimpleOptionChoice) -> Unit,
    onCompositeOptionChanged: (CompositeOptionChoice) -> Unit,
    onCompositeOptionGradeInputChanged: (OptionSubSubjectKey, String, String) -> Unit,
    onCompositeOptionWeightChanged: (OptionSubSubjectKey, String, GradeWeightUi) -> Unit,
    onAddCompositeOptionGradeEntry: (OptionSubSubjectKey) -> Unit,
    onRemoveCompositeOptionGradeEntry: (OptionSubSubjectKey, String) -> Unit,
    onCustomSubjectNameInputChanged: (String) -> Unit,
    onAddCustomSubject: () -> Unit,
    onRemoveCustomSubject: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Required subjects",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("required-subjects-heading")
        )

        branchInputs.filter { it.branch.isBasket && it.branch.basketRole != BasketBranchRole.OPTION }.forEach { branchInput ->
            BranchEditor(
                branchInput = branchInput,
                onGradeInputChanged = onGradeInputChanged,
                onWeightChanged = onWeightChanged,
                onAddGradeEntry = onAddGradeEntry,
                onRemoveGradeEntry = onRemoveGradeEntry
            )
        }

        OptionEditorSection(
            simpleBranchInput = branchInputs.first { it.branch.basketRole == BasketBranchRole.OPTION },
            optionEditor = optionEditor,
            onGradeInputChanged = onGradeInputChanged,
            onWeightChanged = onWeightChanged,
            onAddGradeEntry = onAddGradeEntry,
            onRemoveGradeEntry = onRemoveGradeEntry,
            onOptionModeChanged = onOptionModeChanged,
            onSimpleOptionChanged = onSimpleOptionChanged,
            onCompositeOptionChanged = onCompositeOptionChanged,
            onCompositeOptionGradeInputChanged = onCompositeOptionGradeInputChanged,
            onCompositeOptionWeightChanged = onCompositeOptionWeightChanged,
            onAddCompositeOptionGradeEntry = onAddCompositeOptionGradeEntry,
            onRemoveCompositeOptionGradeEntry = onRemoveCompositeOptionGradeEntry
        )

        Text(
            text = "Additional subjects",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = customSubjectNameInput,
            onValueChange = onCustomSubjectNameInputChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom-subject-name-input"),
            label = { Text("Subject name") },
            singleLine = true,
            isError = customSubjectNameErrorMessage != null
        )

        customSubjectNameErrorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("custom-subject-name-error")
            )
        }

        Button(
            onClick = onAddCustomSubject,
            modifier = Modifier.testTag("add-custom-subject")
        ) {
            Text(text = "Add subject")
        }

        branchInputs.filterNot { it.branch.isBasket }.forEach { branchInput ->
            BranchEditor(
                branchInput = branchInput,
                onGradeInputChanged = onGradeInputChanged,
                onWeightChanged = onWeightChanged,
                onAddGradeEntry = onAddGradeEntry,
                onRemoveGradeEntry = onRemoveGradeEntry
            )

            Button(
                onClick = { onRemoveCustomSubject(branchInput.branch.branchId) },
                modifier = Modifier.testTag("remove-subject-${branchInput.branch.branchId}")
            ) {
                Text(text = "Remove ${branchInput.branch.branchName}")
            }
        }
    }
}

@Composable
private fun BranchEditor(
    branchInput: BranchInputUiState,
    onGradeInputChanged: (BranchIdentifier, String, String) -> Unit,
    onWeightChanged: (BranchIdentifier, String, GradeWeightUi) -> Unit,
    onAddGradeEntry: (BranchIdentifier) -> Unit,
    onRemoveGradeEntry: (BranchIdentifier, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = branchInput.branch.branchName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        branchInput.gradeEntries.forEach { gradeEntry ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val stableTagPrefix = "${branchInput.branch.testTagPrefix}-${gradeEntry.entryId}"

                OutlinedTextField(
                    value = gradeEntry.gradeInput,
                    onValueChange = {
                        onGradeInputChanged(branchInput.branch, gradeEntry.entryId, it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grade-input-$stableTagPrefix"),
                    label = { Text("Grade") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = gradeEntry.errorMessage != null
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradeWeightUi.entries.forEach { weight ->
                        Button(
                            onClick = { onWeightChanged(branchInput.branch, gradeEntry.entryId, weight) },
                            enabled = gradeEntry.weight != weight,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("weight-$stableTagPrefix-${weight.name}")
                        ) {
                            Text(text = weight.label)
                        }
                    }
                }

                Button(
                    onClick = { onRemoveGradeEntry(branchInput.branch, gradeEntry.entryId) },
                    modifier = Modifier.testTag("remove-grade-$stableTagPrefix")
                ) {
                    Text(text = "Remove grade")
                }

                gradeEntry.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("field-error-$stableTagPrefix")
                    )
                }
            }
        }

        Button(
            onClick = { onAddGradeEntry(branchInput.branch) },
            modifier = Modifier.testTag("add-grade-${branchInput.branch.testTagPrefix}")
        ) {
            Text(text = "Add grade")
        }
    }
}

@Composable
private fun OptionEditorSection(
    simpleBranchInput: BranchInputUiState,
    optionEditor: OptionEditorUiState,
    onGradeInputChanged: (BranchIdentifier, String, String) -> Unit,
    onWeightChanged: (BranchIdentifier, String, GradeWeightUi) -> Unit,
    onAddGradeEntry: (BranchIdentifier) -> Unit,
    onRemoveGradeEntry: (BranchIdentifier, String) -> Unit,
    onOptionModeChanged: (OptionModeUi) -> Unit,
    onSimpleOptionChanged: (SimpleOptionChoice) -> Unit,
    onCompositeOptionChanged: (CompositeOptionChoice) -> Unit,
    onCompositeOptionGradeInputChanged: (OptionSubSubjectKey, String, String) -> Unit,
    onCompositeOptionWeightChanged: (OptionSubSubjectKey, String, GradeWeightUi) -> Unit,
    onAddCompositeOptionGradeEntry: (OptionSubSubjectKey) -> Unit,
    onRemoveCompositeOptionGradeEntry: (OptionSubSubjectKey, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Option",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("option-heading")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptionModeUi.entries.forEach { mode ->
                Button(
                    onClick = { onOptionModeChanged(mode) },
                    enabled = optionEditor.mode != mode,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("option-mode-${mode.name}")
                ) {
                    Text(
                        text = if (mode == OptionModeUi.SIMPLE) "Simple option" else "Composite option"
                    )
                }
            }
        }

        when (optionEditor.mode) {
            OptionModeUi.SIMPLE -> {
                Text(
                    text = "Simple option type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                SimpleOptionChoice.entries.forEach { option ->
                    Button(
                        onClick = { onSimpleOptionChanged(option) },
                        enabled = optionEditor.simpleOption != option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("option-simple-${option.name}")
                    ) {
                        Text(text = option.label)
                    }
                }

                BranchEditor(
                    branchInput = simpleBranchInput,
                    onGradeInputChanged = onGradeInputChanged,
                    onWeightChanged = onWeightChanged,
                    onAddGradeEntry = onAddGradeEntry,
                    onRemoveGradeEntry = onRemoveGradeEntry
                )
            }

            OptionModeUi.COMPOSITE -> {
                Text(
                    text = "Composite option type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                CompositeOptionChoice.entries.forEach { option ->
                    Button(
                        onClick = { onCompositeOptionChanged(option) },
                        enabled = optionEditor.compositeOption != option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("option-composite-${option.name}")
                    ) {
                        Text(text = option.label)
                    }
                }

                optionEditor.compositeSubSubjects.forEach { subSubject ->
                    CompositeSubSubjectEditor(
                        subSubject = subSubject,
                        onGradeInputChanged = onCompositeOptionGradeInputChanged,
                        onWeightChanged = onCompositeOptionWeightChanged,
                        onAddGradeEntry = onAddCompositeOptionGradeEntry,
                        onRemoveGradeEntry = onRemoveCompositeOptionGradeEntry
                    )
                }
            }
        }
    }
}

@Composable
private fun CompositeSubSubjectEditor(
    subSubject: OptionSubSubjectUiState,
    onGradeInputChanged: (OptionSubSubjectKey, String, String) -> Unit,
    onWeightChanged: (OptionSubSubjectKey, String, GradeWeightUi) -> Unit,
    onAddGradeEntry: (OptionSubSubjectKey) -> Unit,
    onRemoveGradeEntry: (OptionSubSubjectKey, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = subSubject.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        subSubject.gradeEntries.forEach { gradeEntry ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val stableTagPrefix = "${subSubject.key.name}-${gradeEntry.entryId}"

                OutlinedTextField(
                    value = gradeEntry.gradeInput,
                    onValueChange = { onGradeInputChanged(subSubject.key, gradeEntry.entryId, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grade-input-$stableTagPrefix"),
                    label = { Text("Grade") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = gradeEntry.errorMessage != null
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradeWeightUi.entries.forEach { weight ->
                        Button(
                            onClick = { onWeightChanged(subSubject.key, gradeEntry.entryId, weight) },
                            enabled = gradeEntry.weight != weight,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("weight-$stableTagPrefix-${weight.name}")
                        ) {
                            Text(text = weight.label)
                        }
                    }
                }

                Button(
                    onClick = { onRemoveGradeEntry(subSubject.key, gradeEntry.entryId) },
                    modifier = Modifier.testTag("remove-grade-$stableTagPrefix")
                ) {
                    Text(text = "Remove grade")
                }

                gradeEntry.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("field-error-$stableTagPrefix")
                    )
                }
            }
        }

        Button(
            onClick = { onAddGradeEntry(subSubject.key) },
            modifier = Modifier.testTag("add-grade-${subSubject.key.name}")
        ) {
            Text(text = "Add grade")
        }
    }
}

@Composable
fun PromotionSummaryScreen(
    presentation: PromotionPresentation,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("promotion-summary"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Promotion summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("promotion-summary-heading")
        )
        Text(
            text = "Status: ${presentation.statusLabel}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("summary-status")
        )
        Text(
            text = presentation.headline,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag("summary-headline")
        )

        SummaryMetric(
            presentation.basketTotal.label,
            presentation.basketTotal.valueLabel,
            modifier = Modifier.testTag("summary-basket-total")
        )
        SummaryMetric(
            presentation.promotionPointsTotal.label,
            presentation.promotionPointsTotal.valueLabel,
            modifier = Modifier.testTag("summary-promotion-points-total")
        )

        SummarySection(
            title = "Blocking reasons",
            items = presentation.blockingMessages,
            emptyPlaceholder = "No blocking reasons."
        )
        SummarySection(
            title = "Missing data",
            items = presentation.missingDataMessages,
            emptyPlaceholder = "No missing data."
        )
        BranchAverageSection(
            title = "Branch averages",
            branchAverages = presentation.branchAverages,
            emptyPlaceholder = "No branch averages."
        )
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    valueLabel: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$label: $valueLabel",
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

@Composable
private fun SummarySection(
    title: String,
    items: List<String>,
    emptyPlaceholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (items.isEmpty()) {
            Text(
                text = emptyPlaceholder,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BranchAverageSection(
    title: String,
    branchAverages: List<BranchAveragePresentation>,
    emptyPlaceholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (branchAverages.isEmpty()) {
            Text(
                text = emptyPlaceholder,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            branchAverages.forEach { branchAverage ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.testTag("branch-average-${branchAverage.branchName.toTestTagSuffix()}")
                ) {
                    Text(
                        text = "${branchAverage.branchName}: ${branchAverage.valueLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(
                            "branch-average-value-${branchAverage.branchName.toTestTagSuffix()}"
                        )
                    )
                    branchAverage.detailLabel?.let { detailLabel ->
                        Text(
                            text = detailLabel,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(
                                "branch-average-detail-${branchAverage.branchName.toTestTagSuffix()}"
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun String.toTestTagSuffix(): String {
    return lowercase()
        .replace(" ", "-")
        .replace("/", "-")
}
