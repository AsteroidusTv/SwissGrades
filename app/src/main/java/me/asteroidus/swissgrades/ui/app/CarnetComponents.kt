package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.asteroidus.swissgrades.R

internal val CarnetButtonShape = RoundedCornerShape(16.dp)
internal val CarnetSurfaceShape = RoundedCornerShape(22.dp)

@Composable
internal fun CarnetLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_carnet_logo),
        contentDescription = null, // The adjacent app name supplies the accessible label.
        modifier = modifier.size(32.dp).clip(RoundedCornerShape(9.dp))
    )
}

@Composable
internal fun CarnetAppHeader(
    year: SchoolYear,
    semester: SchoolSemester,
    enabled: Boolean,
    onPeriod: () -> Unit,
    onAddSubject: (() -> Unit)? = null
) {
    val strings = currentAppStrings()
    Column(Modifier.fillMaxWidth().testTag("carnet-app-header").padding(start = 22.dp, end = 22.dp, top = 12.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            CarnetLogo()
            Text(strings.appName, Modifier.weight(1f).padding(start = 8.dp), fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
            if (onAddSubject != null) {
                IconButton(onClick = onAddSubject, modifier = Modifier.testTag("show-add-subject-header")) {
                    Icon(Icons.Outlined.Add, strings.addSubjectTitle, Modifier.size(20.dp))
                }
            }
        }
        TextButton(onClick = onPeriod, enabled = enabled, contentPadding = PaddingValues(0.dp), modifier = Modifier.testTag("open-period-picker")) {
            Text("${strings.schoolYearLabel(year)} · ${strings.semesterLabel(semester)}", fontSize = 13.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun CarnetNavigation(
    selectedSection: MainSection?, settingsSelected: Boolean, enabled: Boolean,
    onSelectSection: (MainSection) -> Unit, onSettings: () -> Unit
) {
    val strings = currentAppStrings()
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val destinations = listOf(
                    Triple(strings.subjectsTabLabel, Icons.Outlined.Layers, MainSection.SUBJECTS),
                    Triple(strings.promotionTabLabel, Icons.Outlined.BarChart, MainSection.PROMOTION),
                    Triple(strings.optionSettingsTitle, Icons.Outlined.Settings, null)
                )
                destinations.forEach { (label, icon, section) ->
                    val selected = if (section == null) settingsSelected else !settingsSelected && selectedSection == section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f).selectable(selected, enabled = enabled, role = Role.Tab,
                            onClick = { if (section == null) onSettings() else onSelectSection(section) })
                            .testTag(if (section == null) "open-settings" else "carnet-nav-${section.name}")
                    ) {
                        Column(Modifier.heightIn(min = 56.dp).padding(horizontal = 3.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(icon, null, Modifier.size(20.dp), tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CarnetMainScreen(
    state: ScreenUiState.Main, onOpenSubject: (String) -> Unit, onAddGrade: (String) -> Unit,
    onAddSubject: () -> Unit, onDeleteSubject: (String) -> Unit, onOpenPeriodPicker: () -> Unit,
    onOpenPromotion: () -> Unit, promotionContent: @Composable () -> Unit, modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    val subjects = state.userSubjects + state.optionSubject
    var choosingGradeSubject by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = rememberLazyListState(), modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().testTag("main-screen-list"),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item("heading") {
                Text(if (state.selectedSection == MainSection.SUBJECTS) strings.mySubjects else strings.carnetOverviewTitle,
                    style = MaterialTheme.typography.headlineLarge)
                Text(if (state.selectedSection == MainSection.SUBJECTS) strings.carnetIntro else strings.carnetOverviewIntro,
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 15.dp))
            }
            if (state.selectedSection == MainSection.SUBJECTS) {
                items(subjects, key = { it.id }) { subject ->
                    if (subject.isOptionSubject) CarnetSubjectRow(subject, onOpen = { onOpenSubject(subject.id) })
                    else SwipeableSubjectCard(subject, onOpenSubject, onRequestDeleteSubject = { pendingDeleteId = subject.id })
                }
                item("add-note") {
                    Button(onClick = { choosingGradeSubject = true }, shape = CarnetButtonShape,
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 50.dp).testTag("carnet-add-grade"),
                        contentPadding = PaddingValues(15.dp)) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(20.dp))
                        Text(strings.addGrade, Modifier.padding(start = 9.dp), fontSize = 14.sp)
                    }
                }
                item("promotion-link") {
                    Surface(onClick = onOpenPromotion, color = MaterialTheme.colorScheme.secondaryContainer, shape = CarnetSurfaceShape,
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp).testTag("carnet-promotion-link")) {
                        Row(Modifier.padding(16.dp).heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(strings.carnetOverviewLink, fontWeight = FontWeight.SemiBold)
                                Text("${strings.basketTitle} ${state.summary.basketLabel} · ${state.summary.insufficienciesLabel} ${strings.insufficienciesTitle.lowercase()}",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.padding(start = 8.dp).size(20.dp))
                        }
                    }
                }
            } else {
                item("promotion") { promotionContent() }
                item("projection-disclaimer") { Text(strings.projectionDisclaimer, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 22.dp)) }
            }
        }
    }
    if (choosingGradeSubject) {
        CarnetDialog(onDismissRequest = { choosingGradeSubject = false }, title = { Text(strings.chooseGradeSubjectTitle) },
            text = { LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                items(subjects, key = { it.id }) { subject ->
                    CarnetSubjectRow(subject, onOpen = { choosingGradeSubject = false; onAddGrade(subject.id) }, modifier = Modifier.testTag("grade-subject-${subject.id}"), containerColor = MaterialTheme.colorScheme.surface)
                }
            } }, confirmButton = {}, dismissButton = { TextButton(onClick = { choosingGradeSubject = false }) { Text(strings.cancelLabel) } })
    }
    subjects.firstOrNull { it.id == pendingDeleteId }?.let { subject ->
        DeleteConfirmationDialog(strings.deleteSubjectTitle, strings.deleteSubjectMessage(subject.title),
            onDismiss = { pendingDeleteId = null }, onConfirm = { pendingDeleteId = null; onDeleteSubject(subject.id) })
    }
}

@Composable
internal fun CarnetSubjectRow(subject: SubjectListItemUiState, onOpen: () -> Unit, modifier: Modifier = Modifier, onDelete: (() -> Unit)? = null, containerColor: Color = MaterialTheme.colorScheme.background) {
    val strings = currentAppStrings()
    val excluded = !subject.isCounted && !subject.isOptionSubject
    val warning = !excluded && subject.averageValue != null && subject.averageValue < 4.0
    val countLabel = "${subject.noteCount} ${if (subject.noteCount == 1) strings.carnetNoteSingular else strings.carnetNotePlural}"
    val subtitle = listOfNotNull(countLabel, when {
        excluded -> strings.notCountedLabel
        subject.isOptionSubject -> strings.optionSectionTitle
        warning -> strings.carnetWatchLabel
        else -> null
    }).joinToString(" · ")
    Column(modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val stacked = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.5f
            Surface(onClick = onOpen, color = containerColor,
                modifier = Modifier.fillMaxWidth().testTag("subject-card-${subject.id}").semantics {
                    if (onDelete != null) customActions = listOf(CustomAccessibilityAction(strings.deleteSubjectActionTemplate.replace("{subject}", subject.title)) { onDelete(); true })
                }) {
                Row(Modifier.padding(vertical = 14.dp).heightIn(min = 42.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                        Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                            Icon(subject.iconChoice.toImageVector(), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(subject.title, style = MaterialTheme.typography.titleMedium)
                        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (stacked && subject.averageValue != null) CarnetAverage(subject, warning)
                    }
                    if (!stacked && subject.averageValue != null) CarnetAverage(subject, warning)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun CarnetAverage(subject: SubjectListItemUiState, warning: Boolean) {
    Text(subject.averageLabel, fontSize = 22.sp, fontWeight = FontWeight.Medium,
        color = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("subject-average-${subject.id}"))
}

@Composable
internal fun CarnetPromotionOverview(summary: DashboardSummaryUiState, setup: PromotionSetupUiState?, onSetup: () -> Unit) {
    val strings = currentAppStrings()
    val language = LocalAppLanguage.current
    val unknown = summary.conditions.any { it.status == ConditionStatus.UNKNOWN }
    val failed = summary.conditions.any { it.status == ConditionStatus.UNMET }
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CarnetSurfaceShape, modifier = Modifier.fillMaxWidth().testTag("dashboard-summary")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(when { failed -> strings.carnetConditionsUnmet; unknown -> strings.carnetConditionsUnknown; else -> strings.carnetConditionsMet }, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("promotion-status"))
            Text(strings.contributingSubjects(summary.contributingSubjectCount), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("overall-average-contributors"))
        }
    }
    Column {
        Text(strings.carnetConditionsTitle, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
        summary.conditions.forEach { condition ->
            val (title, hint) = when (condition.kind) {
                PromotionConditionKind.BASKET -> strings.basketTitle to strings.carnetMinimum16
                PromotionConditionKind.COMPENSATION -> strings.promotionPointsTitle to strings.carnetMinimumZero
                PromotionConditionKind.INSUFFICIENCIES -> strings.insufficienciesTitle to strings.carnetMaximumFour
                PromotionConditionKind.MINIMUM_AVERAGE -> strings.carnetMinimumAverage to strings.carnetMinimumThree
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(when (condition.status) { ConditionStatus.MET -> Icons.Outlined.CheckCircle; ConditionStatus.UNMET -> Icons.Outlined.ErrorOutline; ConditionStatus.UNKNOWN -> Icons.Outlined.HelpOutline },
                    null, Modifier.size(20.dp), tint = if (condition.status == ConditionStatus.UNMET) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, fontWeight = FontWeight.Medium)
                    Text("${condition.value?.let(language::formatOneOrTwoDecimals) ?: strings.notEnoughGrades} · $hint", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("condition-${condition.kind.name}"))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        if (setup != null) {
            Column(Modifier.padding(top = 20.dp).testTag("promotion-setup-card"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(setup.description, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onSetup, modifier = Modifier.testTag("promotion-setup-action")) { Text(setup.actionLabel) }
            }
        }
        Text(strings.overallAverageTitle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 22.dp).testTag("overall-average-title"))
        Text(summary.overallAverageLabel, fontSize = 22.sp, fontWeight = FontWeight.Medium, modifier = Modifier.testTag("overall-average"))
    }
}
