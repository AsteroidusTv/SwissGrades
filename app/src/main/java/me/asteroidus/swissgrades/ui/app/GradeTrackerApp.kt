package me.asteroidus.swissgrades.ui.app

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import java.io.File
import me.asteroidus.swissgrades.ui.theme.AppBackgroundDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveContainerDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveContainerLight
import me.asteroidus.swissgrades.ui.theme.AppPositiveDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveLight
import me.asteroidus.swissgrades.ui.theme.AppPositiveOnBlueDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveOnBlueLight
import me.asteroidus.swissgrades.ui.theme.AppWarningContainerDark
import me.asteroidus.swissgrades.ui.theme.AppWarningContainerLight
import me.asteroidus.swissgrades.ui.theme.AppWarningDark
import me.asteroidus.swissgrades.ui.theme.AppWarningLight
import me.asteroidus.swissgrades.ui.theme.SwissBlue
import me.asteroidus.swissgrades.ui.theme.SwissBlueDark
import android.content.ContextWrapper
import me.asteroidus.swissgrades.ui.theme.SwissGradesTheme

@Composable
fun GradeTrackerApp(
    modifier: Modifier = Modifier,
    repository: GradeTrackerRepository? = null
) {
    val context = LocalContext.current
    val resolvedRepository = remember(context, repository) {
        repository ?: SharedPreferencesGradeTrackerRepository(context.applicationContext)
    }
    val attachmentStorage = remember(context) {
        LocalGradeAttachmentStorage(context.applicationContext)
    }
    val backupCoordinator = remember(context) {
        LocalAppBackupCoordinator(context.applicationContext)
    }
    val plusPointsImportCoordinator = remember(context) {
        LocalPlusPointsImportCoordinator(context.applicationContext)
    }
    val gradeReportExporter = remember(context) {
        LocalGradeReportPdfExporter(context.applicationContext)
    }
    val viewModel: GradeTrackerViewModel = viewModel(
        factory = GradeTrackerViewModel.factory(
            resolvedRepository,
            attachmentStorage,
            backupCoordinator,
            plusPointsImportCoordinator,
            gradeReportExporter = gradeReportExporter
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { destinationUri ->
        destinationUri?.let { viewModel.exportBackup(it.toString()) }
    }
    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { sourceUri ->
        sourceUri?.let { viewModel.prepareBackupImport(it.toString()) }
    }
    val plusPointsImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { sourceUri ->
        sourceUri?.let { viewModel.preparePlusPointsImport(it.toString()) }
    }
    val gradeReportExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        destinationUri?.let { viewModel.exportGradeReport(it.toString()) }
    }
    val useDarkTheme = when (uiState.themeMode) {
        AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val screenStateHolder = rememberSaveableStateHolder()

    BackHandler(enabled = screenSupportsInAppBack(uiState.screen)) {
        when (uiState.screen) {
            is ScreenUiState.AddSubject -> viewModel.hideAddSubjectForm()
            is ScreenUiState.BranchDetail -> viewModel.backFromDetail()
            is ScreenUiState.PeriodPicker -> viewModel.closePeriodPicker()
            is ScreenUiState.Settings -> viewModel.closeSettings()
            else -> Unit
        }
    }

    SwissGradesTheme(darkTheme = useDarkTheme) {
        ApplyEdgeToEdgeSystemBars(darkTheme = useDarkTheme)
        ProvideAppStrings(uiState.language) {
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Bottom
                            )
                        )
                ) {
                    AnimatedContent(
                        targetState = uiState.screen,
                        modifier = Modifier.fillMaxSize(),
                        contentKey = { screenAnimationKey(it) },
                        transitionSpec = {
                            screenTransition(initialState, targetState)
                        },
                        label = "grade-tracker-screen-transition"
                    ) { screen ->
                        screenStateHolder.SaveableStateProvider(screenSaveableStateKey(screen)) {
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
                                    onOpenPeriodPicker = viewModel::openPeriodPicker,
                                    onOpenSettings = viewModel::openSettings,
                                    onPromotionSetupAction = { action, subjectId ->
                                        when (action) {
                                            PromotionSetupAction.ADD_SUBJECT -> viewModel.showAddSubjectForm()
                                            PromotionSetupAction.OPEN_SUBJECT -> subjectId?.let(viewModel::openSubject)
                                        }
                                    },
                                    modifier = Modifier
                                )

                                is ScreenUiState.AddSubject -> AddSubjectScreen(
                                    state = screen.form,
                                    onBack = viewModel::hideAddSubjectForm,
                                    onNameChanged = viewModel::updateAddSubjectName,
                                    onCountedChanged = viewModel::updateAddSubjectCountedFlag,
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
                                    onDraftSemesterChanged = viewModel::updateDraftSemester,
                                    onDraftDescriptionChanged = viewModel::updateDraftDescription,
                                    onImportDraftAttachments = viewModel::importDraftAttachments,
                                    onPrepareCameraCapture = viewModel::prepareCameraCapture,
                                    onCompleteCameraCapture = viewModel::completeCameraCapture,
                                    onRemoveDraftAttachment = viewModel::removeDraftAttachment,
                                    onSelectedSubSubjectChanged = viewModel::selectCompositeSubSubject,
                                    onTargetAverageChanged = viewModel::updateSubjectTargetAverage,
                                    onAddNote = viewModel::addNote,
                                    modifier = Modifier
                                )

                                is ScreenUiState.PeriodPicker -> PeriodPickerScreen(
                                    selectedYear = screen.selectedYear,
                                    selectedSemester = screen.selectedSemester,
                                    onBack = viewModel::closePeriodPicker,
                                    onSelectYear = viewModel::updatePendingYear,
                                    onSelectSemester = viewModel::updatePendingSemester,
                                    onConfirm = viewModel::confirmPeriodSelection,
                                    modifier = Modifier
                                )

                                is ScreenUiState.Settings -> SettingsScreen(
                                    settings = screen.settings,
                                    onSelectLanguage = viewModel::changeLanguage,
                                    onSelectThemeMode = viewModel::changeThemeMode,
                                    onSelectOption = viewModel::changeOption,
                                    onExportBackup = {
                                        backupExportLauncher.launch(screen.settings.backupFileNameSuggestion)
                                    },
                                    onImportBackup = {
                                        backupImportLauncher.launch(arrayOf("*/*", "application/octet-stream"))
                                    },
                                    onImportPlusPoints = {
                                        plusPointsImportLauncher.launch(arrayOf("*/*", "text/xml", "application/xml"))
                                    },
                                    onExportGradeReport = {
                                        gradeReportExportLauncher.launch(
                                            screen.settings.gradeReportFileNameSuggestion
                                        )
                                    },
                                    onDismissPendingImport = viewModel::dismissPendingBackupImport,
                                    onConfirmPendingImport = viewModel::confirmBackupImport,
                                    onDismissPendingPlusPointsImport = viewModel::dismissPendingPlusPointsImport,
                                    onSelectPendingPlusPointsSemester = viewModel::updatePendingPlusPointsTargetSemester,
                                    onConfirmPendingPlusPointsImport = viewModel::confirmPlusPointsImport,
                                    onResetApp = viewModel::resetApp,
                                    onBack = viewModel::closeSettings,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplyEdgeToEdgeSystemBars(darkTheme: Boolean) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return

    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
                detectDarkMode = { darkTheme }
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
                detectDarkMode = { darkTheme }
            )
        )
    }
}

private fun android.content.Context.findActivity(): ComponentActivity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}

private fun screenAnimationKey(screen: ScreenUiState): String {
    return when (screen) {
        is ScreenUiState.Onboarding -> "onboarding"
        is ScreenUiState.Main -> "main"
        is ScreenUiState.PeriodPicker -> "period-picker"
        is ScreenUiState.AddSubject -> "add-subject"
        is ScreenUiState.Settings -> "settings"
        is ScreenUiState.BranchDetail -> "branch-detail-${screen.detail.subjectId}"
    }
}

private fun screenSaveableStateKey(screen: ScreenUiState): String {
    return when (screen) {
        is ScreenUiState.Main -> {
            "main-${screen.selectedYear.name}-${screen.selectedSemester.name}"
        }
        else -> screenAnimationKey(screen)
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
        is ScreenUiState.PeriodPicker -> 2
        is ScreenUiState.AddSubject,
        is ScreenUiState.BranchDetail,
        is ScreenUiState.Settings -> 2
    }
}

private fun screenSupportsInAppBack(screen: ScreenUiState): Boolean {
    return when (screen) {
        is ScreenUiState.PeriodPicker,
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
    val accentBlue = appAccentBlue()
    val strings = currentAppStrings()

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppScreenBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.displaySmall,
                color = accentBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = strings.chooseOption,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = strings.onboardingBody,
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
                    text = strings.continueLabel,
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
internal fun OnboardingOptionCard(
    choice: InitialOptionChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val selectedContainer = appSelectedOptionContainer()
    val selectedBorder = appSelectedOptionBorder()
    val idleBorder = appIdleOptionBorder()
    val idleBadgeBackground = appIdleBadgeBackground()
    val selectedBadgeBackground = appSelectedBadgeBackground()
    val idleBadgeTint = appIdleBadgeTint()
    val selectedBadgeTint = Color.White
    val selectedSecondaryText = appAccentBlue()
    val optionLabel = language.optionChoiceLabel(choice)

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.semantics {
            selected = isSelected
            role = Role.RadioButton
            contentDescription = optionLabel
        },
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
                    text = optionLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = language.optionCategoryLabel(choice).uppercase(),
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
    onOpenPeriodPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onPromotionSetupAction: (PromotionSetupAction, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentBlue = appAccentBlue()
    val strings = currentAppStrings()
    var pendingDeleteSubjectId by remember { mutableStateOf<String?>(null) }
    val pendingDeleteSubject = pendingDeleteSubjectId?.let { pendingId ->
        state.userSubjects.firstOrNull { it.id == pendingId }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("main-screen-list"),
        contentPadding = PaddingValues(
            start = AppScreenHorizontalPadding,
            top = AppScreenTopPadding,
            end = AppScreenHorizontalPadding,
            bottom = AppScreenListBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.appName,
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
                        contentDescription = strings.openSettingsLabel,
                        tint = accentBlue
                    )
                }
            }
        }
        item {
            PeriodSummaryButton(
                selectedYear = state.selectedYear,
                selectedSemester = state.selectedSemester,
                onClick = onOpenPeriodPicker
            )
        }
        if (state.promotionSetup == null) {
            item {
                SummaryCard(state.summary)
            }
        } else {
            val promotionSetup = state.promotionSetup
            item {
                PromotionSetupCard(
                    setup = promotionSetup,
                    onAction = { onPromotionSetupAction(promotionSetup.action, promotionSetup.actionSubjectId) }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.mySubjects, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = onShowAddSubjectForm,
                    modifier = Modifier.testTag("show-add-subject-header"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appSoftAccentContainer(),
                        contentColor = accentBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.addLabel,
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
            title = strings.deleteSubjectTitle,
            message = strings.deleteSubjectMessage(subject.title),
            onDismiss = { pendingDeleteSubjectId = null },
            onConfirm = {
                pendingDeleteSubjectId = null
                onDeleteSubject(subject.id)
            }
        )
    }
}

@Composable
private fun PeriodSummaryButton(
    selectedYear: SchoolYear,
    selectedSemester: SchoolSemester,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val accentBlue = appAccentBlue()
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("open-period-picker"),
        shape = RoundedCornerShape(24.dp),
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = appCardSurface()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = accentBlue
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.schoolYearLabel(selectedYear),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = appSoftAccentContainer()
                ) {
                    Text(
                        text = strings.semesterLabel(selectedSemester),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = accentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (selectedSemester == SchoolSemester.SEMESTER_2) {
                    Text(
                        text = strings.semester2CumulativeHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("period-summary-cumulative-hint")
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = appSoftAccentContainer()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = strings.periodTitle,
                    tint = accentBlue,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun SemesterSwitcher(
    selectedSemester: SchoolSemester,
    onSelectSemester: (SchoolSemester) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val accentBlue = appAccentBlue()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(SchoolSemester.SEMESTER_1, SchoolSemester.SEMESTER_2).forEach { semester ->
            val isSelected = semester == selectedSemester
            OutlinedCard(
                onClick = { onSelectSemester(semester) },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        selected = isSelected
                        role = Role.RadioButton
                        contentDescription = strings.semesterAccessibilityLabel(semester)
                    }
                    .testTag("semester-${semester.name}"),
                shape = RoundedCornerShape(20.dp),
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
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.semesterLabel(semester),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) accentBlue else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodPickerScreen(
    selectedYear: SchoolYear,
    selectedSemester: SchoolSemester,
    onBack: () -> Unit,
    onSelectYear: (SchoolYear) -> Unit,
    onSelectSemester: (SchoolSemester) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = AppScreenHorizontalPadding,
                    top = AppScreenTopPadding,
                    end = AppScreenHorizontalPadding,
                    bottom = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderBackButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back-from-period-picker")
                )
                Text(
                    text = strings.periodTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.choosePeriodTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = appSoftAccentContainer()
                ) {
                    Text(
                        text = strings.periodLabel(selectedYear, selectedSemester),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appAccentBlue(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = appCardSurface(),
                border = appCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = strings.schoolYearTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        PeriodYearSelector(
                            selectedYear = selectedYear,
                            onSelectYear = onSelectYear
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(appCardBorderColor())
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = strings.semesterTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        PeriodSemesterSelector(
                            selectedSemester = selectedSemester,
                            onSelectSemester = onSelectSemester
                        )
                        if (selectedSemester == SchoolSemester.SEMESTER_2) {
                            Text(
                                text = strings.semester2CumulativeHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .testTag("period-cumulative-hint")
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppScreenHorizontalPadding,
                    end = AppScreenHorizontalPadding,
                    bottom = 12.dp
                )
                .testTag("confirm-period-selection"),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = appAccentBlue())
        ) {
            Text(
                text = strings.continueLabel,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun PeriodYearSelector(
    selectedYear: SchoolYear,
    onSelectYear: (SchoolYear) -> Unit
) {
    val strings = currentAppStrings()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SchoolYear.entries.forEachIndexed { index, year ->
            val selected = selectedYear == year
            Surface(
                onClick = { onSelectYear(year) },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        this.selected = selected
                        role = Role.RadioButton
                        contentDescription = strings.schoolYearLabel(year)
                    }
                    .testTag("period-year-${year.name}"),
                shape = RoundedCornerShape(20.dp),
                color = if (selected) appSelectedOptionContainer() else Color.Transparent,
                border = BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) appSelectedOptionBorder() else appCardBorderColor()
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) appAccentBlue() else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSemesterSelector(
    selectedSemester: SchoolSemester,
    onSelectSemester: (SchoolSemester) -> Unit
) {
    val strings = currentAppStrings()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = appCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SchoolSemester.entries.forEach { semester ->
                val selected = selectedSemester == semester
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            this.selected = selected
                            role = Role.RadioButton
                            contentDescription = strings.semesterAccessibilityLabel(semester)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) appAccentBlue() else Color.Transparent)
                        .clickable(role = Role.Button) { onSelectSemester(semester) }
                        .testTag("semester-${semester.name}")
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.semesterLabel(semester),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableSubjectCard(
    subject: SubjectListItemUiState,
    onOpenSubject: (String) -> Unit,
    onRequestDeleteSubject: () -> Unit
) {
    val strings = currentAppStrings()
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
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("delete-subject-${subject.id}"),
                    shape = DashboardCardShape,
                    colors = CardDefaults.cardColors(containerColor = appSwipeDeleteBackground())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = strings.deleteSubjectActionTemplate.replace("{subject}", subject.title),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) {
        SubjectCard(
            subject = subject,
            onOpenSubject = onOpenSubject,
            modifier = Modifier.blockEndToStartSwipeMotion(dismissState)
        )
    }
}

@Composable
private fun PromotionSetupCard(
    setup: PromotionSetupUiState,
    onAction: () -> Unit
) {
    val accentBlue = appAccentBlue()
    val strings = currentAppStrings()
    val warningRed = appWarningColor()
    val neutralBackground = appNeutralBackground()
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("promotion-setup-card"),
        shape = DashboardCardShape,
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = setup.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = strings.promotionSetupIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = setup.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = warningRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                setup.items.forEachIndexed { index, item ->
                    PromotionSetupChecklistRow(
                        item = item,
                        accentColor = if (item.isComplete) accentBlue else warningRed,
                        backgroundColor = if (item.isComplete) {
                            appSoftAccentContainer()
                        } else {
                            neutralBackground
                        },
                        modifier = Modifier.testTag("promotion-setup-item-$index")
                    )
                }
            }

            Button(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("promotion-setup-action"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appSoftAccentContainer(),
                    contentColor = accentBlue
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = setup.actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PromotionSetupChecklistRow(
    item: PromotionSetupChecklistItemUiState,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isComplete) Icons.Filled.Check else Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .size(19.dp)
                        .offset(y = (-1).dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SummaryCard(summary: DashboardSummaryUiState) {
    val accentBlue = appAccentBlue()
    val strings = currentAppStrings()
    val positiveGreen = appPositiveColor()
    val positiveBackground = appPositiveBackground()
    val warningRed = appWarningColor()
    val warningBackground = appWarningBackground()
    val neutralBackground = appNeutralBackground()
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
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = strings.overallAverageTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("overall-average-title")
            )
            Text(
                text = strings.contributingSubjects(summary.contributingSubjectCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("overall-average-contributors")
            )
        }

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
                    text = strings.emptyNotes,
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
            title = strings.promotionPointsTitle,
            value = summary.promotionPointsLabel,
            supportingText = if (summary.promotionPointsValue != null) {
                strings.promotionPointsUnit
            } else {
                strings.emptyNotes
            },
            icon = Icons.Filled.BarChart,
            accentColor = accentBlue,
            progress = summary.promotionPointsValue?.let { ((it + 4.0) / 8.0).coerceIn(0.0, 1.0).toFloat() }
        )

        CompactMetricCard(
            title = strings.basketTitle,
            value = summary.basketLabel,
            icon = Icons.Filled.ShoppingBasket,
            accentColor = accentBlue,
            testTag = "basket-total"
        )

        CompactMetricCard(
            title = strings.insufficienciesTitle,
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
    val strings = currentAppStrings()
    val progressValue = progress ?: 0f
    val hasNumericValue = progress != null && !value.equals(strings.emptyNotes, ignoreCase = true)
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = DashboardCardShape,
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
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
                    colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
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
                    text = strings.notEnoughGrades,
                    style = MaterialTheme.typography.headlineMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
            }
            if (hasNumericValue) {
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = accentColor,
                    trackColor = appProgressTrack()
                )
            }
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
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
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
                colors = CardDefaults.cardColors(containerColor = appSoftAccentContainer())
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
