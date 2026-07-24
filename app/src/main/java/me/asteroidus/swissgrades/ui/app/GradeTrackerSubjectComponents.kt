package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.asteroidus.swissgrades.ui.theme.AppPositiveDark
import me.asteroidus.swissgrades.ui.theme.AppWarningDark
import me.asteroidus.swissgrades.ui.theme.SwissBlue
import me.asteroidus.swissgrades.ui.theme.SwissBlueDark

private val AddSubjectAvailableColors = listOf(
    SubjectColorChoice.BLUE,
    SubjectColorChoice.TEAL,
    SubjectColorChoice.SLATE,
    SubjectColorChoice.PURPLE,
    SubjectColorChoice.PINK,
    SubjectColorChoice.GREEN,
    SubjectColorChoice.AMBER,
    SubjectColorChoice.ORANGE
)

private val AddSubjectAvailableIcons = listOf(
    SubjectIconChoice.BOOK,
    SubjectIconChoice.SCIENCE,
    SubjectIconChoice.LANGUAGE,
    SubjectIconChoice.MUSIC,
    SubjectIconChoice.ART,
    SubjectIconChoice.MIND,
    SubjectIconChoice.BALANCE,
    SubjectIconChoice.CATEGORY,
    SubjectIconChoice.HISTORY,
    SubjectIconChoice.MATH,
    SubjectIconChoice.WORLD,
    SubjectIconChoice.SPORT
)

@Composable
fun AddSubjectScreen(
    state: AddSubjectFormUiState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onCountedChanged: (Boolean) -> Unit,
    onBasketChanged: (Boolean) -> Unit,
    onColorSelected: (SubjectColorChoice) -> Unit,
    onIconSelected: (SubjectIconChoice) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = appAccentBlue()
    val strings = currentAppStrings()
    val activeColor = state.selectedColor
        .takeIf { it in AddSubjectAvailableColors }
        ?: SubjectColorChoice.BLUE
    val isEditing = state.editingSubjectId != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppScreenHorizontalPadding,
                    top = AppScreenTopPadding,
                    end = AppScreenHorizontalPadding,
                    bottom = AppScreenBottomPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderBackButton(
                onClick = onBack,
                modifier = Modifier.testTag("back-from-add-subject")
            )
            Text(
                text = if (isEditing) strings.editSubjectTitle else strings.addSubjectTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.subjectNameLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = onNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add-subject-name"),
                    placeholder = { Text(strings.subjectNamePlaceholder) },
                    singleLine = true,
                    isError = state.errorMessage != null,
                    shape = RoundedCornerShape(20.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appCardBorderColor(),
                        unfocusedBorderColor = appCardBorderColor(),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = appCardSurface(),
                        unfocusedContainerColor = appCardSurface(),
                        errorContainerColor = appCardSurface(),
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = DashboardCardShape,
                colors = CardDefaults.cardColors(containerColor = appSelectedOptionContainer()),
                border = appCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(strings.addToBasketTitle, style = MaterialTheme.typography.titleLarge)
                        Text(
                            strings.addToBasketDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isInBasket,
                        onCheckedChange = onBasketChanged,
                        enabled = state.isCounted,
                        modifier = Modifier.testTag("add-subject-basket")
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = DashboardCardShape,
                colors = CardDefaults.cardColors(containerColor = appSelectedOptionContainer()),
                border = appCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(strings.countInResultsTitle, style = MaterialTheme.typography.titleLarge)
                        Text(
                            strings.countInResultsDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isCounted,
                        onCheckedChange = onCountedChanged,
                        modifier = Modifier.testTag("add-subject-counted")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = strings.personalizationTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                AddSubjectAvailableColors.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        row.forEach { colorChoice ->
                            ColorChoiceChip(
                                colorChoice = colorChoice,
                                isSelected = activeColor == colorChoice,
                                onClick = { onColorSelected(colorChoice) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AddSubjectAvailableIcons.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { iconChoice ->
                                IconChoiceChip(
                                    iconChoice = iconChoice,
                                    accentColor = activeColor.toColor(isDarkPalette()),
                                    isSelected = state.selectedIcon == iconChoice,
                                    onClick = { onIconSelected(iconChoice) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("add-subject-error")
                )
            }
        }

        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("confirm-add-subject"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isEditing) strings.saveChanges else strings.createSubject,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (isEditing) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

@Composable
fun SubjectCard(
    subject: SubjectListItemUiState,
    onOpenSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val isDarkTheme = isDarkPalette()
    val warningRed = appWarningColor()
    val isExcludedFromResults = !subject.isCounted && !subject.isOptionSubject
    val valueColor = when {
        subject.averageValue == null -> MaterialTheme.colorScheme.onSurfaceVariant
        !isExcludedFromResults && subject.averageValue < 4.0 -> warningRed
        else -> subject.colorChoice.toColor(isDarkTheme)
    }
    val secondaryText = when {
        isExcludedFromResults -> strings.notCountedLabel
        subject.averageValue == null && subject.subtitle != null -> subject.subtitle
        subject.averageValue == null -> strings.emptyNotes
        subject.isOptionSubject && subject.subtitle != null -> subject.subtitle
        !isExcludedFromResults && subject.averageValue < 4.0 ->
            "${subject.pointsLabel} ${strings.pointLabel.lowercase()} • ${strings.insufficientLabel}"
        subject.isInBasket -> strings.inBasketLabel
        else -> "${subject.pointsLabel} ${strings.pointsLabel}"
    }

    OutlinedCard(
        onClick = { onOpenSubject(subject.id) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject-card-${subject.id}"),
        shape = DashboardCardShape,
        border = if (!isExcludedFromResults && subject.averageValue != null && subject.averageValue < 4.0) {
            BorderStroke(1.5.dp, warningRed.copy(alpha = 0.45f))
        } else {
            appCardBorder()
        },
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = subject.colorChoice.toSoftBackgroundColor(isDarkTheme)
                    )
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = subject.iconChoice.toImageVector(),
                            contentDescription = null,
                            tint = if (!isExcludedFromResults && subject.averageValue != null && subject.averageValue < 4.0) {
                                warningRed
                            } else {
                                subject.colorChoice.toColor(isDarkTheme)
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = subject.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (!isExcludedFromResults && subject.averageValue != null && subject.averageValue < 4.0) {
                            warningRed
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (subject.averageValue != null) {
                    Text(
                        text = subject.averageLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = valueColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("subject-average-${subject.id}")
                    )
                }
            }
        }
    }
}

private fun SubjectIconChoice.toImageVector(): ImageVector {
    return when (this) {
        SubjectIconChoice.BOOK -> Icons.Filled.AutoStories
        SubjectIconChoice.SCIENCE -> Icons.Filled.Science
        SubjectIconChoice.LANGUAGE -> Icons.Filled.Language
        SubjectIconChoice.MUSIC -> Icons.Filled.MusicNote
        SubjectIconChoice.ART -> Icons.Filled.Palette
        SubjectIconChoice.MIND -> Icons.Filled.Psychology
        SubjectIconChoice.BALANCE -> Icons.Filled.AccountBalance
        SubjectIconChoice.CATEGORY -> Icons.Filled.Category
        SubjectIconChoice.HISTORY -> Icons.Filled.HistoryEdu
        SubjectIconChoice.MATH -> Icons.Filled.Calculate
        SubjectIconChoice.WORLD -> Icons.Filled.Public
        SubjectIconChoice.SPORT -> Icons.Filled.SportsBasketball
    }
}

private fun SubjectColorChoice.toColor(darkTheme: Boolean = false): Color {
    return when (this) {
        SubjectColorChoice.BLUE -> if (darkTheme) SwissBlueDark else SwissBlue
        SubjectColorChoice.RED -> if (darkTheme) AppWarningDark else Color(0xFFD11F1F)
        SubjectColorChoice.TEAL -> if (darkTheme) Color(0xFF4CB9CB) else Color(0xFF0E7C90)
        SubjectColorChoice.SLATE -> if (darkTheme) Color(0xFF9BB0BA) else Color(0xFF546E7A)
        SubjectColorChoice.PURPLE -> if (darkTheme) Color(0xFFC48AEB) else Color(0xFF8E44AD)
        SubjectColorChoice.PINK -> if (darkTheme) Color(0xFFFF8BCF) else Color(0xFFD85AA3)
        SubjectColorChoice.GREEN -> if (darkTheme) AppPositiveDark else Color(0xFF27AE60)
        SubjectColorChoice.AMBER -> if (darkTheme) Color(0xFFFFC34D) else Color(0xFFF39C12)
        SubjectColorChoice.ORANGE -> if (darkTheme) Color(0xFFFF9D57) else Color(0xFFD35400)
    }
}

private fun SubjectColorChoice.toSoftBackgroundColor(darkTheme: Boolean): Color {
    return when (this) {
        SubjectColorChoice.BLUE -> if (darkTheme) Color(0xFF1B2A40) else Color(0xFFEAF2FF)
        SubjectColorChoice.RED -> if (darkTheme) Color(0xFF41232A) else Color(0xFFFFECEC)
        SubjectColorChoice.TEAL -> if (darkTheme) Color(0xFF173741) else Color(0xFFE7F7FA)
        SubjectColorChoice.SLATE -> if (darkTheme) Color(0xFF26343C) else Color(0xFFEDF3F6)
        SubjectColorChoice.PURPLE -> if (darkTheme) Color(0xFF332145) else Color(0xFFF3EAFB)
        SubjectColorChoice.PINK -> if (darkTheme) Color(0xFF46253B) else Color(0xFFFCEAF4)
        SubjectColorChoice.GREEN -> if (darkTheme) Color(0xFF183827) else Color(0xFFEAF8F0)
        SubjectColorChoice.AMBER -> if (darkTheme) Color(0xFF403115) else Color(0xFFFFF4DD)
        SubjectColorChoice.ORANGE -> if (darkTheme) Color(0xFF432819) else Color(0xFFFFEEE4)
    }
}

@Composable
private fun ColorChoiceChip(
    colorChoice: SubjectColorChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val chipColor = colorChoice.toColor(isDarkPalette())
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics {
                selected = isSelected
                role = Role.RadioButton
                contentDescription = language.colorChoiceLabel(colorChoice)
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(onClick = onClick, role = Role.RadioButton)
                .testTag("subject-color-${colorChoice.name}"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                shape = CircleShape,
                border = BorderStroke(
                    if (isSelected) 2.dp else 0.dp,
                    MaterialTheme.colorScheme.surface
                ),
                colors = CardDefaults.cardColors(containerColor = chipColor)
            ) {}
        }

        if (isSelected) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .zIndex(1f),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = chipColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IconChoiceChip(
    iconChoice: SubjectIconChoice,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    Card(
        modifier = modifier
            .height(72.dp)
            .semantics {
                selected = isSelected
                role = Role.RadioButton
                contentDescription = language.iconChoiceLabel(iconChoice)
            }
            .clickable(onClick = onClick, role = Role.RadioButton)
            .testTag("subject-icon-${iconChoice.name}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else appCardBorderColor()
        ),
        colors = CardDefaults.cardColors(containerColor = appCardSurface())
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconChoice.toImageVector(),
                contentDescription = null,
                tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
