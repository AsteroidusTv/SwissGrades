package me.asteroidus.swissgrades.ui.app

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import me.asteroidus.swissgrades.domain.OfficialAverageTarget
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchDetailScreen(
    detail: SubjectDetailUiState,
    onBack: () -> Unit,
    onEditSubject: (String) -> Unit,
    onShowAddNoteSheet: () -> Unit,
    onDismissAddNoteSheet: () -> Unit,
    onRequestEditNote: (String) -> Unit,
    onRequestDeleteNote: (String) -> Unit,
    onDismissDeleteNoteDialog: () -> Unit,
    onConfirmDeleteNote: () -> Unit,
    onDraftValueChanged: (String) -> Unit,
    onDraftTypeChanged: (NoteTypeUi) -> Unit,
    onDraftSemesterChanged: (SchoolSemester) -> Unit,
    onDraftDescriptionChanged: (String) -> Unit,
    onImportDraftAttachments: (List<String>) -> Unit,
    onPrepareCameraCapture: () -> PendingCameraCaptureRequest?,
    onCompleteCameraCapture: (PendingCameraCaptureRequest, Boolean) -> Unit,
    onRemoveDraftAttachment: (String) -> Unit,
    onSelectedSubSubjectChanged: (String) -> Unit,
    onTargetAverageChanged: (String, String) -> Unit,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val context = LocalContext.current
    val cameraAvailable = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    val accentBlue = appAccentBlue()
    val positiveGreen = appPositiveOnBlue()
    val warningRed = appWarningColor()
    val onBlueSupport = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.84f)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingCameraRequest by remember(detail.subjectId, detail.draft.editingNoteId) {
        mutableStateOf<PendingCameraCaptureRequest?>(null)
    }
    var showAttachmentSourceDialog by remember(detail.subjectId, detail.draft.editingNoteId) {
        mutableStateOf(false)
    }
    var attachmentViewer by remember(detail.subjectId, detail.draft.editingNoteId) {
        mutableStateOf<AttachmentViewerState?>(null)
    }
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MaxGradeAttachments)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportDraftAttachments(uris.map(Uri::toString))
        }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        pendingCameraRequest?.let { request ->
            onCompleteCameraCapture(request, success)
        }
        pendingCameraRequest = null
    }
    val activeSubSubject = detail.subSubjects.firstOrNull { it.id == detail.selectedSubSubjectId }
        ?: detail.subSubjects.firstOrNull()
    val visibleNotes = if (detail.isCompositeOption) activeSubSubject?.notes.orEmpty() else detail.notes
    val evolutionNotes = visibleNotes.takeLast(5)
    val hasAverage = detail.officialAverageLabel != strings.emptyNotes
    val statusColor = when (detail.statusTone) {
        DashboardStatusTone.POSITIVE -> positiveGreen
        DashboardStatusTone.NEGATIVE -> warningRed
        DashboardStatusTone.NEUTRAL -> MaterialTheme.colorScheme.onPrimary
    }
    val statusLabelColor = when (detail.statusTone) {
        DashboardStatusTone.NEUTRAL -> onBlueSupport
        else -> statusColor
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("branch-detail-list"),
            contentPadding = PaddingValues(
                start = AppScreenHorizontalPadding,
                top = AppScreenTopPadding,
                end = AppScreenHorizontalPadding,
                bottom = 84.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BranchDetailHeader(
                    detail = detail,
                    onBack = onBack,
                    onEditSubject = onEditSubject
                )
            }

            item {
                BranchDetailSummaryCard(
                    detail = detail,
                    hasAverage = hasAverage,
                    accentBlue = accentBlue,
                    onBlueSupport = onBlueSupport,
                    statusLabelColor = statusLabelColor
                )
            }

            item {
                BranchTargetAverageCard(
                    detail = detail,
                    accentBlue = accentBlue,
                    onTargetAverageChanged = onTargetAverageChanged
                )
            }

            if (!detail.isCompositeOption) {
                item {
                    TargetSimulationCard(
                        notes = visibleNotes,
                        accentBlue = accentBlue,
                        initialTargetInput = detail.targetAverageInput,
                        targetKey = detail.subjectId
                    )
                }
            }

            if (detail.isCompositeOption) {
                item {
                    CompositeSubSubjectSelectorCard(
                        detail = detail,
                        activeSubSubject = activeSubSubject,
                        accentBlue = accentBlue,
                        onSelectedSubSubjectChanged = onSelectedSubSubjectChanged
                    )
                }
            }

            if (visibleNotes.isNotEmpty()) {
                item {
                    EvolutionCard(
                        evolutionNotes = evolutionNotes,
                        accentBlue = accentBlue
                    )
                }
            }

            item {
                GradeHistoryHeader(
                    detail = detail,
                    activeSubSubject = activeSubSubject,
                    visibleNotes = visibleNotes
                )
            }

            if (visibleNotes.isEmpty()) {
                item {
                    OutlinedCard(
                        onClick = onShowAddNoteSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty-notes-card"),
                        shape = DashboardCardShape,
                        border = appCardBorder(),
                        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
                    ) {
                        Text(
                            text = strings.emptyNotes,
                            modifier = Modifier
                                .padding(20.dp)
                                .testTag("empty-notes"),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(visibleNotes, key = { note -> note.id }) { note ->
                    SwipeableNoteHistoryCard(
                        note = note,
                        onRequestEdit = { onRequestEditNote(note.id) },
                        onRequestDelete = { onRequestDeleteNote(note.id) }
                    )
                }
            }
        }

        Button(
            onClick = onShowAddNoteSheet,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = AppScreenHorizontalPadding, end = AppScreenHorizontalPadding, bottom = 12.dp)
                .fillMaxWidth()
                .testTag("show-add-note-sheet"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(strings.addGrade, style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        if (detail.isAddGradeSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = onDismissAddNoteSheet,
                sheetState = sheetState,
                containerColor = appCardSurface()
            ) {
                AddGradeSheetContent(
                    detail = detail,
                    activeSubSubjectName = activeSubSubject?.name,
                    accentBlue = accentBlue,
                    onDraftValueChanged = onDraftValueChanged,
                    onDraftTypeChanged = onDraftTypeChanged,
                    onDraftSemesterChanged = onDraftSemesterChanged,
                    onDraftDescriptionChanged = onDraftDescriptionChanged,
                    onShowAttachmentSourceDialog = { showAttachmentSourceDialog = true },
                    onRemoveDraftAttachment = onRemoveDraftAttachment,
                    onPreviewAttachment = { attachments, index ->
                        attachmentViewer = AttachmentViewerState(
                            attachments = attachments,
                            selectedIndex = index
                        )
                    },
                    onAddNote = onAddNote
                )
            }
        }

        if (showAttachmentSourceDialog) {
            AttachmentSourceDialog(
                cameraAvailable = cameraAvailable,
                onDismiss = { showAttachmentSourceDialog = false },
                onChooseFromGallery = {
                    showAttachmentSourceDialog = false
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTakePhoto = {
                    val request = onPrepareCameraCapture() ?: run {
                        showAttachmentSourceDialog = false
                        return@AttachmentSourceDialog
                    }
                    pendingCameraRequest = request
                    showAttachmentSourceDialog = false
                    takePictureLauncher.launch(request.outputUriString.toUri())
                }
            )
        }

        attachmentViewer?.let { viewerState ->
            AttachmentViewerDialog(
                attachments = viewerState.attachments,
                initialIndex = viewerState.selectedIndex,
                onDismiss = { attachmentViewer = null }
            )
        }

        detail.pendingDeleteNoteTitle?.let { noteTitle ->
            DeleteConfirmationDialog(
                title = strings.deleteGradeTitle,
                message = strings.deleteGradeMessage(noteTitle),
                onDismiss = onDismissDeleteNoteDialog,
                onConfirm = onConfirmDeleteNote
            )
        }
    }
}

@Composable
private fun BranchDetailHeader(
    detail: SubjectDetailUiState,
    onBack: () -> Unit,
    onEditSubject: (String) -> Unit
) {
    val strings = currentAppStrings()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HeaderBackButton(
            onClick = onBack,
            modifier = Modifier.testTag("back-from-detail")
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = detail.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            detail.subtitle?.takeIf { it != detail.title }?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!detail.isOptionSubject) {
            HeaderActionButton(
                onClick = { onEditSubject(detail.subjectId) },
                modifier = Modifier.testTag("edit-subject")
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = strings.editSubjectAction
                )
            }
        }
    }
}

@Composable
private fun BranchDetailSummaryCard(
    detail: SubjectDetailUiState,
    hasAverage: Boolean,
    accentBlue: Color,
    onBlueSupport: Color,
    statusLabelColor: Color
) {
    val strings = currentAppStrings()
    val isExcludedFromResults = !detail.isCounted && !detail.isOptionSubject
    val compactStatusLabel = if (detail.statusTone == DashboardStatusTone.NEGATIVE) {
        strings.branchInsufficientShort
    } else {
        detail.statusLabel
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = accentBlue)
    ) {
        if (hasAverage) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.officialAverageLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = onBlueSupport,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = detail.officialAverageLabel,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "/ 6.0",
                                style = MaterialTheme.typography.titleLarge,
                                color = onBlueSupport,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    if (!isExcludedFromResults) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = detail.pointsLabel,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.pointLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = onBlueSupport
                                )
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                            )
                        ) {
                            Text(
                                text = strings.notCountedLabel,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (!isExcludedFromResults) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detail.secondaryAverageTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = onBlueSupport
                            )
                            Text(
                                text = detail.secondaryAverageLabel,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.statusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = onBlueSupport
                            )
                            Text(
                                text = compactStatusLabel.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = statusLabelColor,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Column {
                        Text(
                            text = detail.secondaryAverageTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = onBlueSupport
                        )
                        Text(
                            text = detail.secondaryAverageLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = strings.officialAverageLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = onBlueSupport,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.emptyNotes,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun BranchTargetAverageCard(
    detail: SubjectDetailUiState,
    accentBlue: Color,
    onTargetAverageChanged: (String, String) -> Unit
) {
    val strings = currentAppStrings()
    var isEditing by remember(detail.subjectId) {
        mutableStateOf(false)
    }
    var targetInput by remember(detail.subjectId) {
        mutableStateOf(detail.targetAverageInput.orEmpty())
    }
    val isInvalid = targetInput.isNotBlank() &&
        OfficialAverageTarget.parse(targetInput) == null
    val canSave = !isInvalid

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("branch-target-average-card"),
        shape = DashboardCardShape,
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                top = 18.dp,
                end = 18.dp,
                bottom = if (isEditing) 18.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isEditing) 12.dp else 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.branchTargetTitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isEditing) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                onClick = {
                                    targetInput = detail.targetAverageInput.orEmpty()
                                    isEditing = true
                                },
                                role = Role.Button
                            )
                            .testTag("edit-branch-target-average"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = strings.branchTargetEdit,
                            tint = accentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = strings.branchTargetSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { input ->
                            targetInput = input
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .testTag("branch-target-average-input"),
                        placeholder = { Text(strings.branchTargetPlaceholder) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = isInvalid,
                        supportingText = if (isInvalid) {
                            { Text(strings.branchTargetInvalid) }
                        } else {
                            null
                        },
                        shape = RoundedCornerShape(20.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = appCardBorderColor(),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = appNeutralBackground(),
                            unfocusedContainerColor = appNeutralBackground(),
                            errorContainerColor = appNeutralBackground(),
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                targetInput = detail.targetAverageInput.orEmpty()
                                isEditing = false
                            }
                        ) {
                            Text(strings.cancelLabel, color = accentBlue)
                        }
                        Button(
                            onClick = {
                                onTargetAverageChanged(detail.subjectId, targetInput)
                                isEditing = false
                            },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                        ) {
                            Text(strings.saveChanges)
                        }
                    }
                }
            } else {
                val hasTargetAverage = detail.targetAverageInput != null
                Text(
                    text = detail.targetAverageInput ?: strings.branchTargetUnset,
                    style = if (hasTargetAverage) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = if (hasTargetAverage) {
                        accentBlue
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (hasTargetAverage) FontWeight.SemiBold else FontWeight.Medium
                )
                Text(
                    text = strings.branchTargetScope,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("branch-target-scope")
                )
            }
        }
    }
}

@Composable
private fun CompositeSubSubjectSelectorCard(
    detail: SubjectDetailUiState,
    activeSubSubject: CompositeSubSubjectDetailUiState?,
    accentBlue: Color,
    onSelectedSubSubjectChanged: (String) -> Unit
) {
    val strings = currentAppStrings()
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = DashboardCardShape,
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.subSubjectsTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            detail.subSubjects.forEachIndexed { index, subSubject ->
                val isSelected = subSubject.id == activeSubSubject?.id
                val subtitle = if (subSubject.internalAverageLabel == strings.emptyNotes) {
                    strings.emptyNotes
                } else {
                    "${strings.averagePrefix} ${subSubject.internalAverageLabel}"
                }

                OutlinedCard(
                    onClick = { onSelectedSubSubjectChanged(subSubject.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            selected = isSelected
                            role = Role.RadioButton
                            contentDescription = subSubject.name
                        }
                        .testTag("select-sub-subject-${subSubject.id}"),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) accentBlue else appCardBorderColor()
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) accentBlue.copy(alpha = 0.14f) else appCardSurface()
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) accentBlue else appNeutralBackground()
                                ) {
                                    Box(
                                        modifier = Modifier.size(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = subSubject.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.testTag("sub-subject-name-${subSubject.id}"),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.testTag("sub-subject-average-${subSubject.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvolutionCard(
    evolutionNotes: List<NoteUiState>,
    accentBlue: Color
) {
    val strings = currentAppStrings()
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = DashboardCardShape,
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.evolutionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accentBlue
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxValue = 6.0
                evolutionNotes.forEachIndexed { index, note ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    (76.dp * (note.numericValue / maxValue).toFloat())
                                        .coerceAtLeast(18.dp)
                                )
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .background(
                                    if (index == evolutionNotes.lastIndex) accentBlue else appProgressTrack()
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeHistoryHeader(
    detail: SubjectDetailUiState,
    activeSubSubject: CompositeSubSubjectDetailUiState?,
    visibleNotes: List<NoteUiState>
) {
    val strings = currentAppStrings()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.gradeHistoryTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (detail.isCompositeOption) {
                Text(
                    text = activeSubSubject?.name ?: detail.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = strings.evaluationCount(visibleNotes.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGradeSheetContent(
    detail: SubjectDetailUiState,
    activeSubSubjectName: String?,
    accentBlue: Color,
    onDraftValueChanged: (String) -> Unit,
    onDraftTypeChanged: (NoteTypeUi) -> Unit,
    onDraftSemesterChanged: (SchoolSemester) -> Unit,
    onDraftDescriptionChanged: (String) -> Unit,
    onShowAttachmentSourceDialog: () -> Unit,
    onRemoveDraftAttachment: (String) -> Unit,
    onPreviewAttachment: (List<AttachmentUiState>, Int) -> Unit,
    onAddNote: () -> Unit
) {
    val strings = currentAppStrings()
    val sectionSpacing = 16.dp
    val inlineSpacing = 10.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        Text(
            text = when {
                detail.draft.editingNoteId != null && detail.isCompositeOption ->
                    strings.editGradeIn(activeSubSubjectName ?: detail.title)
                detail.draft.editingNoteId != null -> strings.editGrade
                detail.isCompositeOption -> strings.addGradeTo(activeSubSubjectName ?: detail.title)
                else -> strings.addGrade
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = strings.savingToPeriod(detail.schoolYear, detail.draft.selectedSemester),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("grade-destination-period")
            )
            Text(
                text = strings.gradeSemesterTitle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(inlineSpacing)
            ) {
                SchoolSemester.entries.forEach { semester ->
                    val isSelected = detail.draft.selectedSemester == semester
                    OutlinedCard(
                        onClick = { onDraftSemesterChanged(semester) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                selected = isSelected
                                role = Role.RadioButton
                                contentDescription = strings.semesterLabel(semester)
                            }
                            .testTag("grade-semester-${semester.name}"),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) accentBlue else appCardBorderColor()
                        ),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) appSoftAccentContainer() else appCardSurface()
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.semesterShortLabel(semester),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) accentBlue else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        OutlinedTextField(
            value = detail.draft.valueInput,
            onValueChange = onDraftValueChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note-value-input"),
            label = { Text(strings.gradeValueLabel) },
            placeholder = { Text(strings.gradeValuePlaceholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = detail.draft.errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appCardBorderColor(),
                unfocusedBorderColor = appCardBorderColor(),
                focusedContainerColor = appCardSurface(),
                unfocusedContainerColor = appCardSurface()
            ),
            shape = RoundedCornerShape(20.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(inlineSpacing)
        ) {
            NoteTypeUi.entries.forEach { type ->
                val isSelected = detail.draft.selectedType == type
                OutlinedCard(
                    onClick = { onDraftTypeChanged(type) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            selected = isSelected
                            role = Role.RadioButton
                            contentDescription = strings.noteTypeLabel(type.weight)
                        }
                        .testTag("note-type-${type.name}"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) accentBlue else appCardBorderColor()
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) appSoftAccentContainer() else appCardSurface()
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 8.dp),
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
        detail.draft.savedGradeImpact?.let { impact ->
            GradeImpactCard(impact = impact)
        }
        OutlinedTextField(
            value = detail.draft.descriptionInput,
            onValueChange = onDraftDescriptionChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note-description-input"),
            label = { Text(strings.descriptionOptional) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appCardBorderColor(),
                unfocusedBorderColor = appCardBorderColor(),
                focusedContainerColor = appCardSurface(),
                unfocusedContainerColor = appCardSurface()
            ),
            shape = RoundedCornerShape(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(sectionSpacing)) {
            Button(
                onClick = onShowAttachmentSourceDialog,
                enabled = detail.draft.attachments.size < MaxGradeAttachments,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("show-add-photo-sheet"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appSoftAccentContainer(),
                    contentColor = appAccentBlue()
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (detail.draft.attachments.isEmpty()) {
                            Icons.Filled.PhotoLibrary
                        } else {
                            Icons.Filled.Add
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = strings.addPhotoLabel,
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            if (detail.draft.attachments.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("draft-attachment-strip"),
                    horizontalArrangement = Arrangement.spacedBy(inlineSpacing),
                    contentPadding = PaddingValues(top = 4.dp, end = 4.dp)
                ) {
                    items(detail.draft.attachments, key = { it.id }) { attachment ->
                        DraftAttachmentChip(
                            attachment = attachment,
                            onRemove = { onRemoveDraftAttachment(attachment.id) },
                            onPreview = {
                                onPreviewAttachment(
                                    detail.draft.attachments.map {
                                        AttachmentUiState(id = it.id, filePath = it.filePath)
                                    },
                                    detail.draft.attachments.indexOfFirst { it.id == attachment.id }
                                )
                            }
                        )
                    }
                }
            }
        }
        detail.draft.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("note-draft-error")
            )
        }
        detail.draft.attachmentErrorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("note-attachment-error")
            )
        }
        Button(
            onClick = onAddNote,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add-note"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (detail.draft.editingNoteId != null) strings.saveChanges else strings.addGrade,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (detail.draft.editingNoteId != null) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun DraftAttachmentChip(
    attachment: DraftAttachmentUiState,
    onRemove: () -> Unit,
    onPreview: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(128.dp)
            .height(128.dp)
            .testTag("draft-attachment-${attachment.id}")
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 10.dp),
            shape = RoundedCornerShape(20.dp),
            border = appCardBorder(),
            colors = CardDefaults.cardColors(containerColor = appCardSurface())
        ) {
            AsyncImage(
                model = File(attachment.filePath),
                contentDescription = currentAppStrings().attachedPhotosTitle,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onPreview),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .clickable(onClick = onRemove, role = Role.Button)
                .testTag("remove-draft-attachment-${attachment.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = currentAppStrings().removePhotoLabel,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AttachmentSourceDialog(
    cameraAvailable: Boolean,
    onDismiss: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    val strings = currentAppStrings()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = appCardSurface(),
        title = {
            Text(
                text = strings.addPhotoLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onChooseFromGallery) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text(strings.chooseFromGalleryLabel, modifier = Modifier.padding(start = 8.dp))
                }
                if (cameraAvailable) {
                    TextButton(onClick = onTakePhoto) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Text(strings.takePhotoLabel, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelLabel, color = appAccentBlue())
            }
        }
    )
}

private data class AttachmentViewerState(
    val attachments: List<AttachmentUiState>,
    val selectedIndex: Int
)

@Composable
private fun AttachmentViewerDialog(
    attachments: List<AttachmentUiState>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val strings = currentAppStrings()
    val safeInitialIndex = initialIndex.coerceIn(0, (attachments.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = safeInitialIndex,
        pageCount = { attachments.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
        ) {
            val maxImageWidth = maxWidth * 0.96f
            val maxImageHeight = maxHeight * 0.86f
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("attachment-viewer-pager")
            ) { page ->
                val imageFile = File(attachments[page].filePath)
                val painter = rememberAsyncImagePainter(model = imageFile)
                val intrinsicSize = painter.intrinsicSize
                val aspectRatio = if (
                    intrinsicSize != Size.Unspecified &&
                    intrinsicSize.width > 0f &&
                    intrinsicSize.height > 0f
                ) {
                    intrinsicSize.width / intrinsicSize.height
                } else {
                    3f / 4f
                }
                val widthFromHeight = maxImageHeight * aspectRatio
                val finalWidth = minOf(maxImageWidth, widthFromHeight)
                val finalHeight = finalWidth / aspectRatio

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(finalWidth)
                            .height(finalHeight)
                            .shadow(
                                elevation = 26.dp,
                                shape = RoundedCornerShape(28.dp),
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.36f),
                                spotColor = Color.Black.copy(alpha = 0.52f)
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF0D1016))
                            .border(
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(28.dp)
                            )
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("attachment-viewer-image-$page"),
                            contentScale = ContentScale.Fit
                        )

                        if (attachments.size > 1) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${attachments.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 18.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.Black.copy(alpha = 0.42f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            HeaderActionButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = AppScreenTopPadding, end = AppScreenHorizontalPadding)
                    .testTag("attachment-viewer-close")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = strings.closeLabel,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun SwipeableNoteHistoryCard(
    note: NoteUiState,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val strings = currentAppStrings()
    var shouldResetSwipe by remember(note.id) { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.6f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onRequestDelete()
                shouldResetSwipe = true
                false
            } else {
                false
            }
        }
    )
    LaunchedEffect(shouldResetSwipe) {
        if (shouldResetSwipe) {
            dismissState.reset()
            shouldResetSwipe = false
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.testTag("swipe-note-${note.id}"),
        enableDismissFromEndToStart = false,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("delete-note-${note.id}"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = appSwipeDeleteBackground())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = strings.deleteGradeLabel,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) {
        NoteHistoryCard(
            note = note,
            onClick = onRequestEdit,
            modifier = Modifier.blockEndToStartSwipeMotion(dismissState)
        )
    }
}

@Composable
private fun NoteHistoryCard(
    note: NoteUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val accentBlue = appAccentBlue()
    val warningRed = appWarningColor()
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("note-card-${note.id}"),
        shape = RoundedCornerShape(22.dp),
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = note.description.ifBlank { strings.evaluationDefaultTitle },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer()),
                    border = BorderStroke(1.dp, appCardBorderColor())
                ) {
                    Box(
                        modifier = Modifier.size(78.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = note.displayValue,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (note.numericValue < 4.0) warningRed else accentBlue
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
                    ) {
                        Text(
                            text = note.noteTypeLabel.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = accentBlue,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
                    ) {
                        Text(
                            text = strings.semesterShortLabel(note.semester),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("note-semester-${note.id}"),
                            style = MaterialTheme.typography.labelLarge,
                            color = accentBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (note.attachments.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(999.dp),
                            colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    tint = accentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.photoAttachmentCount(note.attachments.size),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = accentBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.testTag("note-attachment-count-${note.id}"),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    note.dateLabel.takeIf { it.isNotBlank() }?.let { dateLabel ->
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
