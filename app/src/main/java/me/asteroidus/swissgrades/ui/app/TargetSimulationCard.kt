package me.asteroidus.swissgrades.ui.app

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.asteroidus.swissgrades.domain.TargetSimulationCalculator
import me.asteroidus.swissgrades.domain.TargetSimulationGrade
import me.asteroidus.swissgrades.domain.TargetSimulationResult

private const val DefaultTargetSimulationInput = "5.0"

@Composable
internal fun TargetSimulationCard(
    notes: List<NoteUiState>,
    accentBlue: Color,
    initialTargetInput: String?,
    targetKey: String
) {
    val strings = currentAppStrings()
    val focusManager = LocalFocusManager.current
    val warningRed = appWarningColor()
    val positiveGreen = appPositiveColor()
    var isExpanded by remember { mutableStateOf(false) }
    val openInteractionSource = remember { MutableInteractionSource() }
    val closeInteractionSource = remember { MutableInteractionSource() }
    var targetInput by remember(targetKey) { mutableStateOf(initialTargetInput ?: DefaultTargetSimulationInput) }
    var lastSyncedTargetInput by remember(targetKey) {
        mutableStateOf(initialTargetInput ?: DefaultTargetSimulationInput)
    }
    var plannedGradeCount by remember(targetKey) { mutableStateOf(1) }
    var plannedGradeType by remember(targetKey) { mutableStateOf(NoteTypeUi.FULL) }
    LaunchedEffect(targetKey, initialTargetInput, isExpanded) {
        val nextSyncedInput = initialTargetInput ?: DefaultTargetSimulationInput
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

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(
                if (isExpanded) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = openInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = { isExpanded = true }
                    )
                }
            )
            .testTag(if (isExpanded) "target-simulation-card" else "show-target-simulation-card"),
        shape = DashboardCardShape,
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.targetSimulationTitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (isExpanded) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = closeInteractionSource,
                                indication = null,
                                role = Role.Button,
                                onClick = { isExpanded = false }
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.closeLabel,
                            color = accentBlue,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accentBlue
                        )
                    }
                }
            }

            if (!isExpanded) return@Column

            Text(
                text = strings.targetSimulationSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.temporaryScenarioTargetLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .testTag("target-average-input"),
                    placeholder = { Text("5.0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = result == TargetSimulationResult.Invalid,
                    shape = RoundedCornerShape(20.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appCardBorderColor(),
                        unfocusedBorderColor = appCardBorderColor(),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = appNeutralBackground(),
                        unfocusedContainerColor = appNeutralBackground(),
                        errorContainerColor = appNeutralBackground(),
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = strings.temporaryScenarioTargetHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("temporary-target-hint")
                )
                if (initialTargetInput != null) {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            targetInput = initialTargetInput
                        },
                        modifier = Modifier.testTag("use-saved-target")
                    ) {
                        Text(
                            text = strings.useSavedTargetLabel,
                            color = accentBlue,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.plannedGradeCountTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    (1..3).forEach { count ->
                        val isSelected = plannedGradeCount == count
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .selectable(
                                    selected = isSelected,
                                    role = Role.RadioButton,
                                    onClick = { plannedGradeCount = count }
                                )
                                .semantics {
                                    contentDescription = strings.plannedGradeCount(count)
                                }
                                .testTag("target-planned-grade-count-$count"),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) accentBlue else appCardBorderColor()
                            ),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) {
                                    appSoftAccentContainer()
                                } else {
                                    appNeutralBackground()
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) accentBlue else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.plannedGradeWeightTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NoteTypeUi.entries.forEach { type ->
                        val isSelected = plannedGradeType == type
                        OutlinedCard(
                            onClick = { plannedGradeType = type },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    selected = isSelected
                                    role = Role.RadioButton
                                    contentDescription = strings.noteTypeLabel(type.weight)
                                }
                                .testTag("target-note-type-${type.name}"),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) accentBlue else appCardBorderColor()
                            ),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) {
                                    appSoftAccentContainer()
                                } else {
                                    appNeutralBackground()
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.noteTypeLabel(type.weight),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) accentBlue else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Text(
                    text = strings.plannedGradeWeightHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target-simulation-result"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = resultTone.copy(alpha = 0.14f)),
                border = BorderStroke(1.dp, resultTone.copy(alpha = 0.36f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.requiredSimulationTitle(plannedGradeCount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    when (result) {
                        TargetSimulationResult.Invalid -> Text(
                            text = strings.targetInvalid,
                            style = MaterialTheme.typography.titleMedium,
                            color = resultTone,
                            fontWeight = FontWeight.SemiBold
                        )
                        TargetSimulationResult.AlreadyReached -> Text(
                            text = strings.targetAlreadyReached,
                            style = MaterialTheme.typography.titleMedium,
                            color = resultTone,
                            fontWeight = FontWeight.SemiBold
                        )
                        TargetSimulationResult.Impossible -> Text(
                            text = strings.targetImpossible,
                            style = MaterialTheme.typography.titleMedium,
                            color = resultTone,
                            fontWeight = FontWeight.SemiBold
                        )
                        is TargetSimulationResult.Required -> {
                            Text(
                                text = TargetSimulationCalculator.formatGrade(result.requiredAverage),
                                modifier = Modifier.testTag("target-simulation-required-value"),
                                style = MaterialTheme.typography.displaySmall,
                                color = resultTone,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.targetProjectedAverage(
                                    TargetSimulationCalculator.formatGrade(result.projectedOfficialAverage)
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
