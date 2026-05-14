package me.asteroidus.swissgrades.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

private val DashboardCardShape = RoundedCornerShape(24.dp)
private val DashboardCardBorder = BorderStroke(1.dp, Color(0xFFD5DCEA))
private val DashboardCardSurface = Color.White
private val DashboardSoftBlue = Color(0xFFEAF2FF)
private val DashboardBorderBlue = Color(0xFFD5DCEA)
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
fun GradeTrackerApp(
    modifier: Modifier = Modifier,
    repository: GradeTrackerRepository? = null
) {
    val context = LocalContext.current
    val resolvedRepository = remember(context, repository) {
        repository ?: SharedPreferencesGradeTrackerRepository(context.applicationContext)
    }
    val viewModel: GradeTrackerViewModel = viewModel(
        factory = GradeTrackerViewModel.factory(resolvedRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = screenSupportsInAppBack(uiState.screen)) {
        when (uiState.screen) {
            is ScreenUiState.AddSubject -> viewModel.hideAddSubjectForm()
            is ScreenUiState.BranchDetail -> viewModel.backFromDetail()
            is ScreenUiState.Settings -> viewModel.closeSettings()
            else -> Unit
        }
    }

    AnimatedContent(
        targetState = uiState.screen,
        modifier = modifier.fillMaxSize(),
        contentKey = { screenAnimationKey(it) },
        transitionSpec = {
            screenTransition(initialState, targetState)
        },
        label = "grade-tracker-screen-transition"
    ) { screen ->
        when (screen) {
            is ScreenUiState.Onboarding -> OnboardingScreen(
                selectedOption = screen.selectedOption,
                onOptionSelected = viewModel::selectInitialOption,
                onContinue = viewModel::completeOnboarding,
                modifier = Modifier
            )

            is ScreenUiState.Main -> MainScreen(
                state = screen,
                onOpenSubject = viewModel::openSubject,
                onShowAddSubjectForm = viewModel::showAddSubjectForm,
                onDeleteSubject = viewModel::deleteSubject,
                onOpenSettings = viewModel::openSettings,
                modifier = Modifier
            )

            is ScreenUiState.AddSubject -> AddSubjectScreen(
                state = screen.form,
                onBack = viewModel::hideAddSubjectForm,
                onNameChanged = viewModel::updateAddSubjectName,
                onBasketChanged = viewModel::updateAddSubjectBasketFlag,
                onColorSelected = viewModel::updateAddSubjectColor,
                onIconSelected = viewModel::updateAddSubjectIcon,
                onCreate = viewModel::addSubject,
                modifier = Modifier
            )

            is ScreenUiState.BranchDetail -> BranchDetailScreen(
                detail = screen.detail,
                onBack = viewModel::backFromDetail,
                onEditSubject = viewModel::showEditSubjectForm,
                onShowAddNoteSheet = viewModel::showAddGradeSheet,
                onDismissAddNoteSheet = viewModel::hideAddGradeSheet,
                onRequestEditNote = viewModel::requestEditNote,
                onRequestDeleteNote = viewModel::requestDeleteNote,
                onDismissDeleteNoteDialog = viewModel::dismissDeleteNoteDialog,
                onConfirmDeleteNote = viewModel::confirmDeleteNote,
                onDraftValueChanged = viewModel::updateDraftValue,
                onDraftTypeChanged = viewModel::updateDraftType,
                onDraftDescriptionChanged = viewModel::updateDraftDescription,
                onSelectedSubSubjectChanged = viewModel::selectCompositeSubSubject,
                onAddNote = viewModel::addNote,
                modifier = Modifier
            )

            is ScreenUiState.Settings -> SettingsScreen(
                settings = screen.settings,
                onSelectOption = viewModel::changeOption,
                onBack = viewModel::closeSettings,
                modifier = Modifier
            )
        }
    }
}

private fun screenAnimationKey(screen: ScreenUiState): String {
    return when (screen) {
        is ScreenUiState.Onboarding -> "onboarding"
        is ScreenUiState.Main -> "main"
        is ScreenUiState.AddSubject -> "add-subject"
        is ScreenUiState.Settings -> "settings"
        is ScreenUiState.BranchDetail -> "branch-detail-${screen.detail.subjectId}"
    }
}

private fun screenTransition(
    initialScreen: ScreenUiState,
    targetScreen: ScreenUiState
): ContentTransform {
    val initialDepth = screenDepth(initialScreen)
    val targetDepth = screenDepth(targetScreen)

    return if (targetDepth > initialDepth) {
        slideInHorizontally(initialOffsetX = { it / 5 }) + fadeIn() togetherWith
            slideOutHorizontally(targetOffsetX = { -it / 10 }) + fadeOut()
    } else if (targetDepth < initialDepth) {
        slideInHorizontally(initialOffsetX = { -it / 5 }) + fadeIn() togetherWith
            slideOutHorizontally(targetOffsetX = { it / 10 }) + fadeOut()
    } else {
        fadeIn() togetherWith fadeOut()
    }
}

private fun screenDepth(screen: ScreenUiState): Int {
    return when (screen) {
        is ScreenUiState.Onboarding -> 0
        is ScreenUiState.Main -> 1
        is ScreenUiState.AddSubject,
        is ScreenUiState.BranchDetail,
        is ScreenUiState.Settings -> 2
    }
}

private fun screenSupportsInAppBack(screen: ScreenUiState): Boolean {
    return when (screen) {
        is ScreenUiState.AddSubject,
        is ScreenUiState.BranchDetail,
        is ScreenUiState.Settings -> true
        is ScreenUiState.Main,
        is ScreenUiState.Onboarding -> false
    }
}

@Composable
private fun OnboardingScreen(
    selectedOption: InitialOptionChoice?,
    onOptionSelected: (InitialOptionChoice) -> Unit,
    onContinue: (InitialOptionChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = Color(0xFF1F74E7)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "SwissGrades",
                style = MaterialTheme.typography.displaySmall,
                color = accentBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Choose your option",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Set up your Option subject now. You can add grades and more subjects progressively later.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        InitialOptionChoice.entries.forEach { choice ->
            OnboardingOptionCard(
                choice = choice,
                isSelected = selectedOption == choice,
                onClick = { onOptionSelected(choice) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding-option-${choice.name}")
            )
        }

        Button(
            onClick = { selectedOption?.let(onContinue) },
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("onboarding-continue"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentBlue,
                disabledContainerColor = accentBlue.copy(alpha = 0.35f),
                disabledContentColor = Color.White.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingOptionCard(
    choice: InitialOptionChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedContainer = Color(0xFFF7FAFF)
    val selectedBorder = Color(0xFF1F74E7)
    val idleBorder = Color(0xFFD5DCEA)
    val idleBadgeBackground = Color(0xFFE1ECFF)
    val selectedBadgeBackground = Color(0xFF1F74E7)
    val idleBadgeTint = Color(0xFF1459B2)
    val selectedBadgeTint = Color.White
    val selectedSecondaryText = Color(0xFF1F74E7)

    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) selectedContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isSelected) selectedBorder else idleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            selectedBadgeBackground
                        } else {
                            idleBadgeBackground
                        }
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = choice.onboardingIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (isSelected) selectedBadgeTint else idleBadgeTint
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = choice.label,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = choice.categoryLabel.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) {
                        selectedSecondaryText
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun InitialOptionChoice.onboardingIcon(): ImageVector {
    return when (this) {
        InitialOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATH -> Icons.Filled.Science
        InitialOptionChoice.BIOLOGY_CHEMISTRY -> Icons.Filled.Biotech
        InitialOptionChoice.ECONOMICS_LAW -> Icons.Filled.AccountBalance
        InitialOptionChoice.SPANISH -> Icons.Filled.Language
        InitialOptionChoice.ITALIAN -> Icons.Filled.Language
        InitialOptionChoice.LATIN -> Icons.Filled.AutoStories
        InitialOptionChoice.MUSIC -> Icons.Filled.MusicNote
        InitialOptionChoice.PHILOSOPHY -> Icons.Filled.Psychology
        InitialOptionChoice.VISUAL_ARTS -> Icons.Filled.Palette
        InitialOptionChoice.OTHER -> Icons.Filled.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    state: ScreenUiState.Main,
    onOpenSubject: (String) -> Unit,
    onShowAddSubjectForm: () -> Unit,
    onDeleteSubject: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = Color(0xFF1F74E7)
    var pendingDeleteSubjectId by remember { mutableStateOf<String?>(null) }
    val pendingDeleteSubject = pendingDeleteSubjectId?.let { pendingId ->
        state.userSubjects.firstOrNull { it.id == pendingId }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SwissGrades",
                    style = MaterialTheme.typography.headlineMedium,
                    color = accentBlue,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("open-settings")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Open settings",
                        tint = accentBlue
                    )
                }
            }
        }
        item {
            SummaryCard(state.summary)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My subjects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = onShowAddSubjectForm,
                    modifier = Modifier.testTag("show-add-subject-header"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DashboardSoftBlue,
                        contentColor = accentBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
        item {
            SubjectCard(subject = state.optionSubject, onOpenSubject = onOpenSubject)
        }
        items(state.userSubjects, key = { it.id }) { subject ->
            SwipeableSubjectCard(
                subject = subject,
                onOpenSubject = onOpenSubject,
                onRequestDeleteSubject = { pendingDeleteSubjectId = subject.id }
            )
        }
    }

    pendingDeleteSubject?.let { subject ->
        DeleteConfirmationDialog(
            title = "Delete subject?",
            message = "Remove ${subject.title} and all its grades? This action cannot be undone.",
            onDismiss = { pendingDeleteSubjectId = null },
            onConfirm = {
                pendingDeleteSubjectId = null
                onDeleteSubject(subject.id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableSubjectCard(
    subject: SubjectListItemUiState,
    onOpenSubject: (String) -> Unit,
    onRequestDeleteSubject: () -> Unit
) {
    var shouldResetSwipe by remember(subject.id) { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.6f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onRequestDeleteSubject()
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
        modifier = Modifier.testTag("swipe-subject-${subject.id}"),
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("delete-subject-${subject.id}"),
                shape = DashboardCardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE85A7A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete ${subject.title}",
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        SubjectCard(subject = subject, onOpenSubject = onOpenSubject)
    }
}

@Composable
private fun AddSubjectScreen(
    state: AddSubjectFormUiState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onBasketChanged: (Boolean) -> Unit,
    onColorSelected: (SubjectColorChoice) -> Unit,
    onIconSelected: (SubjectIconChoice) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = Color(0xFF1F74E7)
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
                .padding(horizontal = 12.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back-from-add-subject")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = if (isEditing) "Edit subject" else "Add a subject",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentBlue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SUBJECT NAME",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = onNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .testTag("add-subject-name"),
                    placeholder = { Text("Ex: History") },
                    singleLine = true,
                    isError = state.errorMessage != null,
                    shape = RoundedCornerShape(20.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DashboardBorderBlue,
                        unfocusedBorderColor = DashboardBorderBlue,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorContainerColor = Color.White,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = DashboardCardShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFF)),
                border = DashboardCardBorder
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
                        Text("Add to basket", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Basket subjects count toward the 16-point rule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isInBasket,
                        onCheckedChange = onBasketChanged,
                        modifier = Modifier.testTag("add-subject-basket")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "PERSONALIZATION",
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
                                    accentColor = activeColor.toColor(),
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
                Text(if (isEditing) "Save changes" else "Create subject", style = MaterialTheme.typography.titleMedium)
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
private fun SummaryCard(summary: DashboardSummaryUiState) {
    val accentBlue = Color(0xFF1F74E7)
    val positiveGreen = Color(0xFF2E7D32)
    val positiveBackground = Color(0xFFE8F5E9)
    val warningRed = Color(0xFFC62828)
    val warningBackground = Color(0xFFFFEBEE)
    val neutralBackground = Color(0xFFF1F5FB)
    val statusColor = when (summary.statusTone) {
        DashboardStatusTone.POSITIVE -> positiveGreen
        DashboardStatusTone.NEGATIVE -> warningRed
        DashboardStatusTone.NEUTRAL -> accentBlue
    }
    val statusBackground = when (summary.statusTone) {
        DashboardStatusTone.POSITIVE -> positiveBackground
        DashboardStatusTone.NEGATIVE -> warningBackground
        DashboardStatusTone.NEUTRAL -> neutralBackground
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard-summary"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (summary.overallAverageValue != null) {
                Text(
                    text = summary.overallAverageLabel,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("overall-average")
                )
            } else {
                Text(
                    text = EMPTY_NOTES_MESSAGE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("overall-average")
                )
            }
            if (summary.isPromotionCalculable) {
                Card(
                    shape = RoundedCornerShape(999.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBackground)
                ) {
                    Text(
                        text = summary.promotionStatusLabel.uppercase(),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("promotion-status"),
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (summary.promotionHeadline.isNotBlank()) {
            Text(
                text = summary.promotionHeadline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("promotion-headline")
            )
        }

        HighlightMetricCard(
            title = "Promotion points",
            value = summary.promotionPointsLabel,
            supportingText = if (summary.promotionPointsValue != null) {
                "advance points"
            } else {
                EMPTY_NOTES_MESSAGE
            },
            icon = Icons.Filled.BarChart,
            accentColor = accentBlue,
            progress = summary.promotionPointsValue?.let { ((it + 4.0) / 8.0).coerceIn(0.0, 1.0).toFloat() }
        )

        CompactMetricCard(
            title = "Basket",
            value = summary.basketLabel,
            icon = Icons.Filled.ShoppingBasket,
            accentColor = accentBlue,
            testTag = "basket-total"
        )

        CompactMetricCard(
            title = "Insufficiencies",
            value = summary.insufficienciesLabel,
            icon = Icons.Filled.WarningAmber,
            accentColor = if (summary.insufficiencyCount > 0) warningRed else accentBlue,
            testTag = "insufficiency-count"
        )
    }
}

@Composable
private fun HighlightMetricCard(
    title: String,
    value: String,
    supportingText: String,
    icon: ImageVector,
    accentColor: Color,
    progress: Float?
) {
    val hasNumericValue = progress != null && !value.equals(EMPTY_NOTES_MESSAGE, ignoreCase = true)
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = DashboardCardShape,
        border = DashboardCardBorder,
        colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DashboardSoftBlue)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor
                        )
                    }
                }
            }
            if (hasNumericValue) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            } else {
                Text(
                    text = EMPTY_NOTES_MESSAGE,
                    style = MaterialTheme.typography.headlineMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
            }
            LinearProgressIndicator(
                progress = { progress ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = accentColor,
                trackColor = Color(0xFFDCE4F2)
            )
        }
    }
}

@Composable
private fun CompactMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = DashboardCardShape,
        border = DashboardCardBorder,
        colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag(testTag)
                )
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DashboardSoftBlue)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectListItemUiState,
    onOpenSubject: (String) -> Unit
) {
    val accentBlue = Color(0xFF1F74E7)
    val warningRed = Color(0xFFC62828)
    val valueColor = when {
        subject.averageValue == null -> MaterialTheme.colorScheme.onSurfaceVariant
        subject.averageValue < 4.0 -> warningRed
        else -> subject.colorChoice.toColor()
    }
    val secondaryText = when {
        subject.averageValue == null && subject.subtitle != null -> subject.subtitle
        subject.averageValue == null -> EMPTY_NOTES_MESSAGE
        subject.isOptionSubject && subject.subtitle != null -> subject.subtitle
        subject.averageValue < 4.0 -> "${subject.pointsLabel} point • Insufficient"
        subject.isInBasket -> "In basket"
        else -> "${subject.pointsLabel} points"
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSubject(subject.id) }
            .testTag("subject-card-${subject.id}"),
        shape = DashboardCardShape,
        border = if (subject.averageValue != null && subject.averageValue < 4.0) {
            BorderStroke(1.5.dp, warningRed.copy(alpha = 0.45f))
        } else {
            DashboardCardBorder
        },
        colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
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
                    colors = CardDefaults.cardColors(containerColor = subject.colorChoice.toSoftBackgroundColor())
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = subject.iconChoice.toImageVector(),
                            contentDescription = null,
                            tint = if (subject.averageValue != null && subject.averageValue < 4.0) {
                                warningRed
                            } else {
                                subject.colorChoice.toColor()
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        subject.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (subject.averageValue != null && subject.averageValue < 4.0) {
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

private fun SubjectListItemUiState.icon(): ImageVector {
    return when {
        isOptionSubject && isCompositeOption -> Icons.Filled.Science
        isOptionSubject -> iconChoice.toImageVector()
        else -> iconChoice.toImageVector()
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

private fun SubjectColorChoice.toColor(): Color {
    return when (this) {
        SubjectColorChoice.BLUE -> Color(0xFF1F74E7)
        SubjectColorChoice.RED -> Color(0xFFD11F1F)
        SubjectColorChoice.TEAL -> Color(0xFF0E7C90)
        SubjectColorChoice.SLATE -> Color(0xFF546E7A)
        SubjectColorChoice.PURPLE -> Color(0xFF8E44AD)
        SubjectColorChoice.PINK -> Color(0xFFD85AA3)
        SubjectColorChoice.GREEN -> Color(0xFF27AE60)
        SubjectColorChoice.AMBER -> Color(0xFFF39C12)
        SubjectColorChoice.ORANGE -> Color(0xFFD35400)
    }
}

private fun SubjectColorChoice.toSoftBackgroundColor(): Color {
    return when (this) {
        SubjectColorChoice.BLUE -> Color(0xFFEAF2FF)
        SubjectColorChoice.RED -> Color(0xFFFFECEC)
        SubjectColorChoice.TEAL -> Color(0xFFE7F7FA)
        SubjectColorChoice.SLATE -> Color(0xFFEDF3F6)
        SubjectColorChoice.PURPLE -> Color(0xFFF3EAFB)
        SubjectColorChoice.PINK -> Color(0xFFFCEAF4)
        SubjectColorChoice.GREEN -> Color(0xFFEAF8F0)
        SubjectColorChoice.AMBER -> Color(0xFFFFF4DD)
        SubjectColorChoice.ORANGE -> Color(0xFFFFEEE4)
    }
}

@Composable
private fun ColorChoiceChip(
    colorChoice: SubjectColorChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .testTag("subject-color-${colorChoice.name}"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            shape = CircleShape,
            border = BorderStroke(if (isSelected) 3.dp else 0.dp, Color.White),
            colors = CardDefaults.cardColors(containerColor = colorChoice.toColor())
        ) {}
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
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick)
            .testTag("subject-icon-${iconChoice.name}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else Color(0xFFDCE4F2)
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconChoice.toImageVector(),
                contentDescription = null,
                tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchDetailScreen(
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
    onDraftDescriptionChanged: (String) -> Unit,
    onSelectedSubSubjectChanged: (String) -> Unit,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = Color(0xFF1F74E7)
    val positiveGreen = Color(0xFFCCF5D5)
    val warningRed = Color(0xFFC62828)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeSubSubject = detail.subSubjects.firstOrNull { it.id == detail.selectedSubSubjectId }
        ?: detail.subSubjects.firstOrNull()
    val visibleNotes = if (detail.isCompositeOption) activeSubSubject?.notes.orEmpty() else detail.notes
    val hasVisibleNotes = visibleNotes.isNotEmpty()
    val evolutionNotes = visibleNotes.takeLast(5)
    val hasAverage = detail.officialAverageLabel != EMPTY_NOTES_MESSAGE
    val statusColor = when (detail.statusTone) {
        DashboardStatusTone.POSITIVE -> positiveGreen
        DashboardStatusTone.NEGATIVE -> warningRed
        DashboardStatusTone.NEUTRAL -> Color.White
    }
    val statusLabelColor = when (detail.statusTone) {
        DashboardStatusTone.NEUTRAL -> Color(0xFFE8F1FF)
        else -> statusColor
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back-from-detail")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                detail.subtitle?.takeIf { it != detail.title }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!detail.isOptionSubject) {
                IconButton(
                    onClick = { onEditSubject(detail.subjectId) },
                    modifier = Modifier.testTag("edit-subject")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit subject"
                    )
                }
            }
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
                                    text = "OFFICIAL AVERAGE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE8F1FF),
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
                                        color = Color.White
                                    )
                                    Text(
                                        text = "/ 6.0",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFFE8F1FF),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x1FFFFFFF))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = detail.pointsLabel,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Point",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFFE8F1FF)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detail.secondaryAverageTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE8F1FF)
                                )
                                Text(
                                    text = detail.secondaryAverageLabel,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE8F1FF)
                                )
                                Text(
                                    text = detail.statusLabel.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = statusLabelColor,
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
                            text = "OFFICIAL AVERAGE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE8F1FF),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = EMPTY_NOTES_MESSAGE,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                    }
                }
            }

        if (detail.isCompositeOption) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = DashboardCardShape,
                border = DashboardCardBorder,
                colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sub-subjects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    detail.subSubjects.forEach { subSubject ->
                        val isSelected = subSubject.id == activeSubSubject?.id
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectedSubSubjectChanged(subSubject.id) }
                                .testTag("select-sub-subject-${subSubject.id}"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) accentBlue else DashboardBorderBlue
                            ),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = subSubject.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.testTag("sub-subject-name-${subSubject.id}")
                                    )
                                    val subtitle = if (subSubject.internalAverageLabel == EMPTY_NOTES_MESSAGE) {
                                        EMPTY_NOTES_MESSAGE
                                    } else {
                                        "Average ${subSubject.internalAverageLabel}"
                                    }
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.testTag("sub-subject-average-${subSubject.id}")
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) accentBlue else Color(0xFFDCE4F2))
                                )
                            }
                        }
                    }
                }
            }
        }

        if (hasVisibleNotes) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = DashboardCardShape,
                border = DashboardCardBorder,
                colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
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
                            text = "Evolution",
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
                                        .height((76.dp * (note.numericValue / maxValue).toFloat()).coerceAtLeast(18.dp))
                                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                        .background(
                                            if (index == evolutionNotes.lastIndex) accentBlue else Color(0xFFDDE4F2)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Grade history",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
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
                text = "${visibleNotes.size} evaluations",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }

        if (visibleNotes.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = DashboardCardShape,
                border = DashboardCardBorder,
                colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
            ) {
                Text(
                    text = EMPTY_NOTES_MESSAGE,
                    modifier = Modifier
                        .padding(20.dp)
                        .testTag("empty-notes"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            visibleNotes.forEach { note ->
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("show-add-note-sheet"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Add a grade", style = MaterialTheme.typography.titleMedium)
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
                containerColor = Color.White
            ) {
                AddGradeSheetContent(
                    detail = detail,
                    activeSubSubjectName = activeSubSubject?.name,
                    accentBlue = accentBlue,
                    onDraftValueChanged = onDraftValueChanged,
                    onDraftTypeChanged = onDraftTypeChanged,
                    onDraftDescriptionChanged = onDraftDescriptionChanged,
                    onAddNote = onAddNote
                )
            }
        }

        detail.pendingDeleteNoteTitle?.let { noteTitle ->
            DeleteConfirmationDialog(
                title = "Delete grade?",
                message = "Remove $noteTitle from this subject? This action cannot be undone.",
                onDismiss = onDismissDeleteNoteDialog,
                onConfirm = onConfirmDeleteNote
            )
        }
    }
}

@Composable
private fun DetailMiniMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFE8F1FF)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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
    onDraftDescriptionChanged: (String) -> Unit,
    onAddNote: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = when {
                detail.draft.editingNoteId != null && detail.isCompositeOption ->
                    "Edit grade in ${activeSubSubjectName ?: detail.title}"
                detail.draft.editingNoteId != null -> "Edit grade"
                detail.isCompositeOption -> "Add a grade to ${activeSubSubjectName ?: detail.title}"
                else -> "Add a grade"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = detail.draft.valueInput,
            onValueChange = onDraftValueChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note-value-input"),
            label = { Text("Grade value") },
            placeholder = { Text("Ex: 5.5") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = detail.draft.errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DashboardBorderBlue,
                unfocusedBorderColor = DashboardBorderBlue
            ),
            shape = RoundedCornerShape(20.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NoteTypeUi.entries.forEach { type ->
                val isSelected = detail.draft.selectedType == type
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDraftTypeChanged(type) }
                        .testTag("note-type-${type.name}"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) accentBlue else DashboardBorderBlue
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) DashboardSoftBlue else Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.label,
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
        OutlinedTextField(
            value = detail.draft.descriptionInput,
            onValueChange = onDraftDescriptionChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note-description-input"),
            label = { Text("Description (optional)") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DashboardBorderBlue,
                unfocusedBorderColor = DashboardBorderBlue
            ),
            shape = RoundedCornerShape(20.dp)
        )
        detail.draft.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("note-draft-error")
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
                    if (detail.draft.editingNoteId != null) "Save changes" else "Add a grade",
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
private fun SwipeableNoteHistoryCard(
    note: NoteUiState,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit
) {
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
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("delete-note-${note.id}"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE85A7A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete grade",
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        NoteHistoryCard(
            note = note,
            onClick = onRequestEdit
        )
    }
}

@Composable
private fun NoteHistoryCard(
    note: NoteUiState,
    onClick: () -> Unit
) {
    val accentBlue = Color(0xFF1F74E7)
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("note-card-${note.id}"),
        shape = RoundedCornerShape(22.dp),
        border = DashboardCardBorder,
        colors = CardDefaults.outlinedCardColors(containerColor = DashboardCardSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DashboardSoftBlue),
                border = BorderStroke(1.dp, DashboardBorderBlue)
            ) {
                Box(
                    modifier = Modifier.size(width = 68.dp, height = 76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = note.displayValue,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (note.numericValue < 4.0) Color(0xFFC62828) else accentBlue
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = note.description.ifBlank { "Evaluation" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = DashboardSoftBlue)
                    ) {
                        Text(
                            text = note.noteTypeLabel.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = accentBlue,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
                note.dateLabel.takeIf { it.isNotBlank() }?.let { dateLabel ->
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmationDialog(
        title = title,
        message = message,
        confirmLabel = "Delete",
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val accentBlue = Color(0xFF1F74E7)
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accentBlue)
            }
        }
    )
}

@Composable
private fun SettingsScreen(
    settings: SettingsUiState,
    onSelectOption: (InitialOptionChoice) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = Color(0xFF1F74E7)
    var pendingOptionChange by remember { mutableStateOf<InitialOptionChoice?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back-from-settings")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Option settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentBlue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Choose your option",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Current option: ${settings.selectedOption.label}. Changing it updates your Option subject directly.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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

    pendingOptionChange?.let { choice ->
        ConfirmationDialog(
            title = "Change option?",
            message = "Changing your option will delete the grades currently saved in the Option subject. This action cannot be undone.",
            confirmLabel = "Change option",
            onDismiss = { pendingOptionChange = null },
            onConfirm = {
                pendingOptionChange = null
                onSelectOption(choice)
            }
        )
    }
}
