package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CarnetDetailHeader(detail: SubjectDetailUiState, onBack: () -> Unit, onEditSubject: (String) -> Unit) {
    val strings = currentAppStrings()
    Column {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp), modifier = Modifier.testTag("back-from-detail")) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(20.dp))
            Text(strings.subjectsTabLabel, Modifier.padding(start = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(detail.title, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
            if (!detail.isOptionSubject) IconButton(onClick = { onEditSubject(detail.subjectId) }, modifier = Modifier.testTag("edit-subject")) {
                Icon(Icons.Outlined.Edit, strings.editSubjectAction, Modifier.size(20.dp))
            }
        }
        Text(if (detail.isOptionSubject) strings.optionSectionTitle else strings.carnetSubjectsIntro,
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
internal fun CarnetDetailAverage(detail: SubjectDetailUiState, noteCount: Int) {
    val strings = currentAppStrings()
    Surface(color = MaterialTheme.colorScheme.surface, shape = CarnetSurfaceShape, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(strings.carnetOfficialAverage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(detail.officialAverageLabel, fontSize = if (detail.officialAverageValue == null) 22.sp else 56.sp,
                lineHeight = if (detail.officialAverageValue == null) 28.sp else 62.sp,
                fontWeight = FontWeight.Medium, letterSpacing = (-2).sp, modifier = Modifier.testTag("detail-official-average"))
            Text("${detail.secondaryAverageTitle} ${detail.secondaryAverageLabel} · $noteCount ${if (noteCount == 1) strings.carnetNoteSingular else strings.carnetNotePlural}",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("detail-raw-average"))
        }
    }
}

@Composable
internal fun CarnetNoteRow(note: NoteUiState, onEdit: () -> Unit, onPreviewAttachments: (Int) -> Unit, modifier: Modifier = Modifier, onDelete: (() -> Unit)? = null) {
    val strings = currentAppStrings()
    val language = LocalAppLanguage.current
    Column(modifier.fillMaxWidth()) {
        Surface(onClick = onEdit, color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth().testTag("note-card-${note.id}").semantics {
                if (onDelete != null) customActions = listOf(CustomAccessibilityAction(strings.deleteGradeLabel) { onDelete(); true })
            }) {
            Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Text(note.displayValue, Modifier.widthIn(min = 47.dp).testTag("note-value-${note.id}"), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(note.description.ifBlank { strings.evaluationDefaultTitle }, fontWeight = FontWeight.Medium)
                    Text("${strings.coefficientLabel} ${language.formatOneOrTwoDecimals(note.weightCoefficient)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.semesterShortLabel(note.semester), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("note-semester-${note.id}"))
                        Text(note.dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (note.attachments.isNotEmpty()) {
                        TextButton(onClick = { onPreviewAttachments(0) }, modifier = Modifier.testTag("open-note-attachments-${note.id}"), contentPadding = PaddingValues(0.dp)) {
                            Icon(Icons.Outlined.PhotoLibrary, null, Modifier.size(18.dp))
                            Text(strings.photoAttachmentCount(note.attachments.size), Modifier.padding(start = 6.dp).testTag("note-attachment-count-${note.id}"))
                        }
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.testTag("visible-edit-note-${note.id}")) {
                    Icon(Icons.Outlined.Edit, strings.editGrade, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
