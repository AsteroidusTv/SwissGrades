package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.asteroidus.swissgrades.domain.TargetSimulationCalculator
import me.asteroidus.swissgrades.domain.TargetSimulationGrade
import me.asteroidus.swissgrades.domain.TargetSimulationResult

@Composable
internal fun TargetSimulationCard(
    notes: List<NoteUiState>,
    accentBlue: Color,
    initialTargetInput: String?,
    targetKey: String
) {
    val strings = currentAppStrings()
    val language = LocalAppLanguage.current
    val defaultTargetInput = language.formatOneOrTwoDecimals(5.0)
    val focusManager = LocalFocusManager.current
    val warningRed = appWarningColor()
    val positiveGreen = appPositiveColor()
    var isExpanded by rememberSaveable(targetKey) { mutableStateOf(false) }
    var targetInput by rememberSaveable(targetKey, language) {
        mutableStateOf(initialTargetInput ?: defaultTargetInput)
    }
    var lastSyncedTargetInput by rememberSaveable(targetKey, language) {
        mutableStateOf(initialTargetInput ?: defaultTargetInput)
    }
    var plannedGradeCount by rememberSaveable(targetKey) { mutableStateOf(1) }
    var plannedGradeType by rememberSaveable(targetKey) { mutableStateOf(NoteTypeUi.FULL) }
    LaunchedEffect(targetKey, initialTargetInput, isExpanded) {
        val nextSyncedInput = initialTargetInput ?: defaultTargetInput
        targetInput = synchronizedTargetSimulationInput(
            currentInput = targetInput,
            lastSyncedInput = lastSyncedTargetInput,
            nextSyncedInput = nextSyncedInput,
            isExpanded = isExpanded
        )
        lastSyncedTargetInput = nextSyncedInput
    }
    val result = remember(notes, targetInput, plannedGradeType, plannedGradeCount) {
        TargetSimulationCalculator.compute(
            grades = notes.map { note ->
                TargetSimulationGrade(
                    value = note.numericValue,
                    weightCoefficient = note.weightCoefficient
                )
            },
            targetAverageInput = targetInput,
            plannedGradeWeightCoefficient = plannedGradeType.weight.coefficient,
            plannedGradeCount = plannedGradeCount
        )
    }
    val resultTone = when (result) {
        TargetSimulationResult.Invalid,
        TargetSimulationResult.Impossible -> warningRed
        TargetSimulationResult.AlreadyReached -> positiveGreen
        is TargetSimulationResult.Required -> accentBlue
    }

    if (!isExpanded) {
        androidx.compose.material3.OutlinedButton(
            onClick = { isExpanded = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("show-target-simulation-card"),
            shape = RoundedCornerShape(14.dp),
            border = appCardBorder(),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = appCardSurface(), contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(strings.carnetSimulateAction, Modifier.padding(start = 8.dp))
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().testTag("target-simulation-card"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = strings.carnetTargetTitle,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { focusManager.clearFocus(); isExpanded = false }) {
                Text(strings.closeLabel)
            }
        }
        Text(strings.carnetSimulationCaption, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(strings.temporaryScenarioTargetLabel, style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = targetInput,
                onValueChange = { targetInput = it },
                modifier = Modifier.fillMaxWidth().testTag("target-average-input"),
                placeholder = { Text(defaultTargetInput) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = result == TargetSimulationResult.Invalid,
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentBlue,
                    unfocusedBorderColor = appCardBorderColor(),
                    focusedContainerColor = appCardSurface(),
                    unfocusedContainerColor = appCardSurface()
                )
            )
            Text(strings.temporaryScenarioTargetHint,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("temporary-target-hint"))
            if (initialTargetInput != null) {
                TextButton(onClick = { focusManager.clearFocus(); targetInput = initialTargetInput },
                    modifier = Modifier.testTag("use-saved-target")) {
                    Text(strings.useSavedTargetLabel)
                }
            }
        }
        SimulationChoiceField(
            label = strings.plannedGradeCountTitle,
            selected = plannedGradeCount,
            options = (1..3).map { it to strings.plannedGradeCount(it) },
            pickerTag = "target-count-picker",
            optionTag = { "target-planned-grade-count-$it" },
            onSelect = { focusManager.clearFocus(); plannedGradeCount = it }
        )
        SimulationChoiceField(
            label = strings.plannedGradeWeightTitle,
            selected = plannedGradeType,
            options = NoteTypeUi.entries.map { it to language.formatOneOrTwoDecimals(it.weight.coefficient) },
            pickerTag = "target-weight-picker",
            optionTag = { "target-note-type-${it.name}" },
            onSelect = { focusManager.clearFocus(); plannedGradeType = it }
        )
        Text(strings.plannedGradeWeightHint, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("target-simulation-result"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.requiredSimulationTitle(plannedGradeCount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                when (result) {
                    TargetSimulationResult.Invalid -> Text(strings.targetInvalid, color = resultTone)
                    TargetSimulationResult.AlreadyReached -> Text(strings.targetAlreadyReached, color = resultTone)
                    TargetSimulationResult.Impossible -> Text(strings.targetImpossible, color = resultTone)
                    is TargetSimulationResult.Required -> {
                        Text(language.formatOneOrTwoDecimals(result.requiredAverage),
                            modifier = Modifier.testTag("target-simulation-required-value"),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            fontWeight = FontWeight.Medium)
                        Text(strings.targetProjectedAverage(language.formatOneOrTwoDecimals(result.projectedOfficialAverage)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SimulationChoiceField(
    label: String,
    selected: T,
    options: List<Pair<T, String>>,
    pickerTag: String,
    optionTag: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            androidx.compose.material3.OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                    .semantics { contentDescription = "$label, ${options.first { it.first == selected }.second}" }
                    .testTag(pickerTag),
                shape = RoundedCornerShape(12.dp),
                border = appCardBorder(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(13.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = appCardSurface(), contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(options.first { it.first == selected }.second, Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(20.dp))
            }
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                border = appCardBorder()
            ) {
                options.forEach { (value, text) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text) },
                        onClick = { expanded = false; onSelect(value) },
                        modifier = Modifier.semantics { this.selected = value == selected; role = Role.RadioButton }
                            .testTag(optionTag(value)),
                        trailingIcon = if (value == selected) ({ Icon(Icons.Filled.Check, null) }) else null
                    )
                }
            }
        }
    }
}

internal fun synchronizedTargetSimulationInput(
    currentInput: String,
    lastSyncedInput: String,
    nextSyncedInput: String,
    isExpanded: Boolean
): String {
    return if (!isExpanded || currentInput == lastSyncedInput) {
        nextSyncedInput
    } else {
        currentInput
    }
}
