package me.asteroidus.swissgrades.ui.app

import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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
import android.app.Activity
import android.content.ContextWrapper
import androidx.core.view.WindowCompat
import me.asteroidus.swissgrades.ui.theme.SwissGradesTheme

private val DashboardCardShape = RoundedCornerShape(24.dp)
private const val MaxGradeAttachments = 5
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
private fun appAccentBlue(): Color = MaterialTheme.colorScheme.primary

@Composable
private fun isDarkPalette(): Boolean = MaterialTheme.colorScheme.background == AppBackgroundDark

@Composable
private fun appCardSurface(): Color = MaterialTheme.colorScheme.surface

@Composable
private fun appCardBorderColor(): Color = MaterialTheme.colorScheme.outlineVariant

@Composable
private fun appCardBorder(): BorderStroke = BorderStroke(1.dp, appCardBorderColor())

@Composable
private fun appSoftAccentContainer(): Color = MaterialTheme.colorScheme.secondaryContainer

@Composable
private fun appProgressTrack(): Color = if (isDarkPalette()) Color(0xFF304053) else Color(0xFFDCE4F2)

@Composable
private fun appSelectedOptionContainer(): Color =
    if (isDarkPalette()) Color(0xFF16263B) else Color(0xFFF7FAFF)

@Composable
private fun appSelectedOptionBorder(): Color = MaterialTheme.colorScheme.primary

@Composable
private fun appIdleOptionBorder(): Color = appCardBorderColor()

@Composable
private fun appIdleBadgeBackground(): Color =
    if (isDarkPalette()) Color(0xFF22344B) else Color(0xFFE1ECFF)

@Composable
private fun appSelectedBadgeBackground(): Color = MaterialTheme.colorScheme.primary

@Composable
private fun appIdleBadgeTint(): Color =
    if (isDarkPalette()) SwissBlueDark else Color(0xFF1459B2)

@Composable
private fun appPositiveColor(): Color = if (isDarkPalette()) AppPositiveDark else AppPositiveLight

@Composable
private fun appPositiveOnBlue(): Color = if (isDarkPalette()) AppPositiveOnBlueDark else AppPositiveOnBlueLight

@Composable
private fun appPositiveBackground(): Color =
    if (isDarkPalette()) AppPositiveContainerDark else AppPositiveContainerLight

@Composable
private fun appWarningColor(): Color = if (isDarkPalette()) AppWarningDark else AppWarningLight

@Composable
private fun appWarningBackground(): Color =
    if (isDarkPalette()) AppWarningContainerDark else AppWarningContainerLight

@Composable
private fun appNeutralBackground(): Color =
    if (isDarkPalette()) Color(0xFF223046) else Color(0xFFF1F5FB)

@Composable
private fun appSwipeDeleteBackground(): Color =
    if (isDarkPalette()) Color(0xFF8A3F55) else Color(0xFFE85A7A)

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
    val viewModel: GradeTrackerViewModel = viewModel(
        factory = GradeTrackerViewModel.factory(
            resolvedRepository,
            attachmentStorage,
            backupCoordinator,
            plusPointsImportCoordinator
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
    val useDarkTheme = when (uiState.themeMode) {
        AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

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
        ApplySystemBars(darkTheme = useDarkTheme)
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
                                onDraftDescriptionChanged = viewModel::updateDraftDescription,
                                onImportDraftAttachments = viewModel::importDraftAttachments,
                                onPrepareCameraCapture = viewModel::prepareCameraCapture,
                                onCompleteCameraCapture = viewModel::completeCameraCapture,
                                onRemoveDraftAttachment = viewModel::removeDraftAttachment,
                                onSelectedSubSubjectChanged = viewModel::selectCompositeSubSubject,
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
                                onDismissPendingImport = viewModel::dismissPendingBackupImport,
                                onConfirmPendingImport = viewModel::confirmBackupImport,
                                onDismissPendingPlusPointsImport = viewModel::dismissPendingPlusPointsImport,
                                onSelectPendingPlusPointsSemester = viewModel::updatePendingPlusPointsTargetSemester,
                                onConfirmPendingPlusPointsImport = viewModel::confirmPlusPointsImport,
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

@Composable
@Suppress("DEPRECATION")
private fun ApplySystemBars(darkTheme: Boolean) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    SideEffect {
        activity.window.statusBarColor = backgroundColor
        activity.window.navigationBarColor = backgroundColor
        val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
}

private fun android.content.Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
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
private fun OnboardingOptionCard(
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
    modifier: Modifier = Modifier
) {
    val accentBlue = appAccentBlue()
    val strings = currentAppStrings()
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
        item {
            SummaryCard(state.summary)
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
private fun SemesterSwitcher(
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
                        maxLines = 1,
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
                }
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
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
                        textAlign = TextAlign.Center
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
    ) {
        SubjectCard(subject = subject, onOpenSubject = onOpenSubject)
    }
}

@Composable
private fun AddSubjectScreen(
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
                .padding(horizontal = 16.dp, vertical = 18.dp),
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
                .padding(horizontal = 16.dp),
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
                        .height(76.dp)
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
                Text(if (isEditing) strings.saveChanges else strings.createSubject, style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun SubjectCard(
    subject: SubjectListItemUiState,
    onOpenSubject: (String) -> Unit
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
        !isExcludedFromResults && subject.averageValue < 4.0 -> "${subject.pointsLabel} ${strings.pointLabel.lowercase()} • ${strings.insufficientLabel}"
        subject.isInBasket -> strings.inBasketLabel
        else -> "${subject.pointsLabel} ${strings.pointsLabel}"
    }

    OutlinedCard(
        onClick = { onOpenSubject(subject.id) },
        modifier = Modifier
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
                        modifier = Modifier
                            .size(52.dp),
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
                        subject.title,
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
    val strings = currentAppStrings()
    val chipColor = colorChoice.toColor(isDarkPalette())
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("subject-color-${colorChoice.name}"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            shape = CircleShape,
            border = BorderStroke(
                if (isSelected) 2.dp else 0.dp,
                MaterialTheme.colorScheme.surface
            ),
            colors = CardDefaults.cardColors(
                containerColor = chipColor
            )
        ) {}

        if (isSelected) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = strings.selectedColorDescription,
                        tint = chipColor,
                        modifier = Modifier.size(16.dp)
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
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick)
            .testTag("subject-icon-${iconChoice.name}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else appCardBorderColor()
        ),
        colors = CardDefaults.cardColors(containerColor = appCardSurface())
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
    onImportDraftAttachments: (List<String>) -> Unit,
    onPrepareCameraCapture: () -> PendingCameraCaptureRequest?,
    onCompleteCameraCapture: (PendingCameraCaptureRequest, Boolean) -> Unit,
    onRemoveDraftAttachment: (String) -> Unit,
    onSelectedSubSubjectChanged: (String) -> Unit,
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
                .testTag("branch-detail-list")
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 84.dp),
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
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
                    onDraftDescriptionChanged = onDraftDescriptionChanged,
                    onShowAttachmentSourceDialog = { showAttachmentSourceDialog = true },
                    onRemoveDraftAttachment = onRemoveDraftAttachment,
                    onPreviewAttachment = { attachments, index ->
                        attachmentViewer = AttachmentViewerState(attachments = attachments, selectedIndex = index)
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
    val compactStatusLabel = if (detail.statusLabel == strings.branchInsufficient) {
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
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
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) accentBlue else appNeutralBackground(),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(appCardBorderColor())
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                .height((76.dp * (note.numericValue / maxValue).toFloat()).coerceAtLeast(18.dp))
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
                                    detail.draft.attachments.map { AttachmentUiState(id = it.id, filePath = it.filePath) },
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
                    if (detail.draft.editingNoteId != null) strings.saveChanges else strings.addGrade,
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
                contentDescription = null,
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
                .size(34.dp)
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
                    .padding(top = 16.dp, end = 16.dp)
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
    val strings = currentAppStrings()
    val accentBlue = appAccentBlue()
    val warningRed = appWarningColor()
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
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
                        modifier = Modifier.size(width = 74.dp, height = 82.dp),
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

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = currentAppStrings()
    ConfirmationDialog(
        title = title,
        message = message,
        confirmLabel = strings.deleteLabel,
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
    val strings = currentAppStrings()
    val accentBlue = appAccentBlue()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = appCardSurface(),
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
                Text(strings.cancelLabel, color = accentBlue)
            }
        }
    )
}

@Composable
private fun SettingsScreen(
    settings: SettingsUiState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onSelectOption: (InitialOptionChoice) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onImportPlusPoints: () -> Unit,
    onDismissPendingImport: () -> Unit,
    onConfirmPendingImport: () -> Unit,
    onDismissPendingPlusPointsImport: () -> Unit,
    onSelectPendingPlusPointsSemester: (SchoolSemester) -> Unit,
    onConfirmPendingPlusPointsImport: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
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
                    enabled = !settings.isBackupInProgress,
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
                    enabled = !settings.isBackupInProgress,
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
            title = strings.plusPointsSectionTitle,
            description = strings.plusPointsSectionDescription
        ) {
            Button(
                onClick = onImportPlusPoints,
                enabled = !settings.isBackupInProgress,
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = strings.optionSectionTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.optionDescription(settings.selectedOption.label),
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
            title = strings.changeOptionTitle,
            message = strings.changeOptionMessage,
            confirmLabel = strings.changeOptionConfirm,
            onDismiss = { pendingOptionChange = null },
            onConfirm = {
                pendingOptionChange = null
                onSelectOption(choice)
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
                    Text(strings.plusPointsImportMessage(displayName))
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
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun HeaderBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                role = Role.Button
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = strings.backLabel
        )
    }
}

@Composable
private fun HeaderActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                role = Role.Button
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
