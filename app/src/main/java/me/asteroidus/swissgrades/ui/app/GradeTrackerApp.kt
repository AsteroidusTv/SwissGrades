package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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

    when (val screen = uiState.screen) {
        is ScreenUiState.Onboarding -> OnboardingScreen(
            selectedOption = screen.selectedOption,
            onOptionSelected = viewModel::selectInitialOption,
            onContinue = viewModel::completeOnboarding,
            modifier = modifier
        )

        is ScreenUiState.Main -> MainScreen(
            state = screen,
            onOpenSubject = viewModel::openSubject,
            onShowAddSubjectForm = viewModel::showAddSubjectForm,
            onHideAddSubjectForm = viewModel::hideAddSubjectForm,
            onAddSubjectNameChanged = viewModel::updateAddSubjectName,
            onAddSubjectBasketChanged = viewModel::updateAddSubjectBasketFlag,
            onAddSubject = viewModel::addSubject,
            onDeleteSubject = viewModel::deleteSubject,
            onOpenSettings = viewModel::openSettings,
            modifier = modifier
        )

        is ScreenUiState.BranchDetail -> BranchDetailScreen(
            detail = screen.detail,
            onBack = viewModel::backFromDetail,
            onDraftValueChanged = viewModel::updateDraftValue,
            onDraftTypeChanged = viewModel::updateDraftType,
            onDraftDescriptionChanged = viewModel::updateDraftDescription,
            onSelectedSubSubjectChanged = viewModel::selectCompositeSubSubject,
            onAddNote = viewModel::addNote,
            modifier = modifier
        )

        is ScreenUiState.Settings -> SettingsScreen(
            settings = screen.settings,
            onSelectOption = viewModel::changeOption,
            onBack = viewModel::closeSettings,
            modifier = modifier
        )
    }
}

@Composable
private fun OnboardingScreen(
    selectedOption: InitialOptionChoice?,
    onOptionSelected: (InitialOptionChoice) -> Unit,
    onContinue: (InitialOptionChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Welcome to SwissGrades", style = MaterialTheme.typography.headlineMedium)
        Text("Choose your option to set up your Option branch.")

        InitialOptionChoice.entries.forEach { choice ->
            Button(
                onClick = { onOptionSelected(choice) },
                enabled = selectedOption != choice,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding-option-${choice.name}")
            ) {
                Text(choice.label)
            }
        }

        Button(
            onClick = { selectedOption?.let(onContinue) },
            enabled = selectedOption != null,
            modifier = Modifier.testTag("onboarding-continue")
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun MainScreen(
    state: ScreenUiState.Main,
    onOpenSubject: (String) -> Unit,
    onShowAddSubjectForm: () -> Unit,
    onHideAddSubjectForm: () -> Unit,
    onAddSubjectNameChanged: (String) -> Unit,
    onAddSubjectBasketChanged: (Boolean) -> Unit,
    onAddSubject: () -> Unit,
    onDeleteSubject: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SwissGrades", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Button(onClick = onOpenSettings, modifier = Modifier.testTag("open-settings")) {
                Text("Change option")
            }
        }
        item {
            SummaryCard(state.summary)
        }
        item {
            Text("Option", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            SubjectCard(subject = state.optionSubject, onOpenSubject = onOpenSubject)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subjects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Button(onClick = onShowAddSubjectForm, modifier = Modifier.testTag("show-add-subject")) {
                    Text("Add subject")
                }
            }
        }
        if (state.addSubjectForm.isVisible) {
            item {
                AddSubjectForm(
                    state = state.addSubjectForm,
                    onNameChanged = onAddSubjectNameChanged,
                    onBasketChanged = onAddSubjectBasketChanged,
                    onAdd = onAddSubject,
                    onCancel = onHideAddSubjectForm
                )
            }
        }
        items(state.userSubjects, key = { it.id }) { subject ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubjectCard(subject = subject, onOpenSubject = onOpenSubject)
                Button(
                    onClick = { onDeleteSubject(subject.id) },
                    modifier = Modifier.testTag("delete-subject-${subject.id}")
                ) {
                    Text("Delete ${subject.title}")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: DashboardSummaryUiState) {
    Card(modifier = Modifier.fillMaxWidth().testTag("dashboard-summary")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Overall average: ${summary.overallAverageLabel}", modifier = Modifier.testTag("overall-average"))
            Text("Promotion status: ${summary.promotionStatusLabel}", modifier = Modifier.testTag("promotion-status"))
            Text(summary.promotionHeadline, modifier = Modifier.testTag("promotion-headline"))
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectListItemUiState,
    onOpenSubject: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSubject(subject.id) }
            .testTag("subject-card-${subject.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(subject.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            subject.subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            if (subject.isInBasket) {
                Text("In basket", style = MaterialTheme.typography.bodySmall)
            }
            Text("Average: ${subject.averageLabel}")
            Text("Points: ${subject.pointsLabel}")
        }
    }
}

@Composable
private fun AddSubjectForm(
    state: AddSubjectFormUiState,
    onNameChanged: (String) -> Unit,
    onBasketChanged: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("add-subject-form")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.nameInput,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth().testTag("add-subject-name"),
                label = { Text("Subject name") },
                singleLine = true,
                isError = state.errorMessage != null
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.isInBasket,
                    onCheckedChange = onBasketChanged,
                    modifier = Modifier.testTag("add-subject-basket")
                )
                Text("Include this subject in the basket")
            }
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("add-subject-error"))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd, modifier = Modifier.testTag("confirm-add-subject")) {
                    Text("Create subject")
                }
                Button(onClick = onCancel, modifier = Modifier.testTag("cancel-add-subject")) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun BranchDetailScreen(
    detail: SubjectDetailUiState,
    onBack: () -> Unit,
    onDraftValueChanged: (String) -> Unit,
    onDraftTypeChanged: (NoteTypeUi) -> Unit,
    onDraftDescriptionChanged: (String) -> Unit,
    onSelectedSubSubjectChanged: (String) -> Unit,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack, modifier = Modifier.testTag("back-from-detail")) {
            Text("Back")
        }
        Text(detail.title, style = MaterialTheme.typography.headlineMedium)
        detail.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

        detail.metrics.forEach { metric ->
            Text("${metric.label}: ${metric.value}")
        }

        if (detail.isCompositeOption) {
            detail.subSubjects.forEach { subSubject ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(subSubject.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Internal average: ${subSubject.internalAverageLabel}")
                        Button(
                            onClick = { onSelectedSubSubjectChanged(subSubject.id) },
                            modifier = Modifier.testTag("select-sub-subject-${subSubject.id}")
                        ) {
                            Text("Add notes to ${subSubject.name}")
                        }
                        if (subSubject.notes.isEmpty()) {
                            Text("No grades yet")
                        } else {
                            subSubject.notes.forEach { note ->
                                NoteRow(note)
                            }
                        }
                    }
                }
            }
        } else {
            Text("Grades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (detail.notes.isEmpty()) {
                Text("No grades yet", modifier = Modifier.testTag("empty-notes"))
            } else {
                detail.notes.forEach { note ->
                    NoteRow(note)
                }
            }
        }

        Text("Add a grade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = detail.draft.valueInput,
            onValueChange = onDraftValueChanged,
            modifier = Modifier.fillMaxWidth().testTag("note-value-input"),
            label = { Text("Grade value") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = detail.draft.errorMessage != null
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NoteTypeUi.entries.forEach { type ->
                Button(
                    onClick = { onDraftTypeChanged(type) },
                    enabled = detail.draft.selectedType != type,
                    modifier = Modifier.weight(1f).testTag("note-type-${type.name}")
                ) {
                    Text(type.label)
                }
            }
        }
        OutlinedTextField(
            value = detail.draft.descriptionInput,
            onValueChange = onDraftDescriptionChanged,
            modifier = Modifier.fillMaxWidth().testTag("note-description-input"),
            label = { Text("Description (optional)") }
        )
        detail.draft.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("note-draft-error"))
        }
        Button(onClick = onAddNote, modifier = Modifier.testTag("add-note")) {
            Text("Add grade")
        }
    }
}

@Composable
private fun NoteRow(note: NoteUiState) {
    Text(
        buildString {
            append(note.displayValue)
            append(" • ")
            append(note.noteTypeLabel)
            if (note.description.isNotBlank()) {
                append(" • ")
                append(note.description)
            }
            if (note.dateLabel.isNotBlank()) {
                append(" • ")
                append(note.dateLabel)
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack, modifier = Modifier.testTag("back-from-settings")) {
            Text("Back")
        }
        Text("Option settings", style = MaterialTheme.typography.headlineMedium)
        Text("Current option: ${settings.selectedOption.label}")
        InitialOptionChoice.entries.forEach { choice ->
            Button(
                onClick = { onSelectOption(choice) },
                enabled = settings.selectedOption != choice,
                modifier = Modifier.fillMaxWidth().testTag("settings-option-${choice.name}")
            ) {
                Text(choice.label)
            }
        }
    }
}
