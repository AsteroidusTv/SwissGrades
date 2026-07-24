package me.asteroidus.swissgrades.ui.app

import android.content.Context
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.asteroidus.swissgrades.MainActivity
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradeTrackerAppInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var appContext: Context
    private lateinit var repository: SharedPreferencesGradeTrackerRepository
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        repository = SharedPreferencesGradeTrackerRepository(appContext)
        clearPersistedState()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        clearPersistedState()
    }

    @Test
    fun manualBasketSubjectsDriveVisiblePromotionSummary() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = true,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH,
                        notes = listOf(storedNote(id = "note-1", value = 4.0))
                    ),
                    StoredSubject(
                        id = "subject-2",
                        name = "Literature",
                        isInBasket = true,
                        notes = listOf(storedNote(id = "note-2", value = 4.0))
                    ),
                    StoredSubject(
                        id = "subject-3",
                        name = "Science",
                        isInBasket = true,
                        notes = listOf(storedNote(id = "note-3", value = 4.0))
                    ),
                    StoredSubject(
                        id = "subject-4",
                        name = "Projects",
                        isInBasket = true,
                        notes = listOf(storedNote(id = "note-4", value = 4.0))
                    )
                ),
                nextSubjectSequence = 5,
                nextNoteSequence = 5
            )
        )

        launchApp()

        assertTagText("overall-average-title", "Moyenne générale")
        assertTagText("overall-average-contributors", "4 branches notées")
        assertTagText("promotion-status", "PROMU")
    }

    @Test
    fun restoredCompositeOptionShowsSubSubjectsAndComputedAverages() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = true,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.BIOLOGY_CHEMISTRY,
                        subSubjects = listOf(
                            StoredSubSubject(
                                id = "option-subject-1",
                                name = "Biology",
                                notes = listOf(storedNote(id = "note-1", value = 5.0))
                            ),
                            StoredSubSubject(
                                id = "option-subject-2",
                                name = "Chemistry",
                                notes = listOf(storedNote(id = "note-2", value = 4.0))
                            )
                        )
                    )
                ),
                nextSubjectSequence = 2,
                nextNoteSequence = 3
            )
        )

        launchApp()
        scrollMainScreenUntilTag("subject-card-subject-1")
        composeRule.onNodeWithTag("subject-card-subject-1", useUnmergedTree = true)
            .performClick()
        scrollBranchDetailUntilTag("select-sub-subject-option-subject-1")

        assertTagDisplayed("select-sub-subject-option-subject-1")
        assertTagAbsent("show-target-simulation-card")
        assertTagAbsent("target-simulation-card")
    }

    @Test
    fun incompletePromotionSetupShowsAssistantAndPrimaryAction() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = true,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    )
                ),
                nextSubjectSequence = 2,
                nextNoteSequence = 1
            )
        )

        launchApp()

        assertTagDisplayed("promotion-setup-card")
        assertTagAbsent("dashboard-summary")
        composeRule.onNodeWithTag("promotion-setup-action", useUnmergedTree = true)
            .performClick()
        assertTagDisplayed("add-subject-name")
    }

    @Test
    fun mainScreenRestoresScrollPositionAfterReturningFromSubject() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = buildList {
                    add(
                        StoredSubject(
                            id = "option",
                            name = "Option",
                            isInBasket = false,
                            isOptionSubject = true,
                            optionChoice = InitialOptionChoice.SPANISH
                        )
                    )
                    repeat(8) { index ->
                        add(
                            StoredSubject(
                                id = "subject-${index + 1}",
                                name = "Subject ${index + 1}",
                                isInBasket = false
                            )
                        )
                    }
                },
                nextSubjectSequence = 9,
                nextNoteSequence = 1
            )
        )

        launchApp()

        scrollMainScreenUntilTag("subject-card-subject-8")
        composeRule.onNodeWithTag("subject-card-subject-8", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTag("back-from-detail", useUnmergedTree = true)
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("subject-card-subject-8", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun gradeSimulatorPlansTwoFutureGrades() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = false,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    ),
                    StoredSubject(
                        id = "subject-2",
                        name = "Mathematics",
                        isInBasket = false,
                        targetAverage = 5.0,
                        notes = listOf(storedNote(id = "note-1", value = 4.0))
                    )
                ),
                nextSubjectSequence = 3,
                nextNoteSequence = 2
            )
        )

        launchApp()
        scrollMainScreenUntilTag("subject-card-subject-2")
        composeRule.onNodeWithTag("subject-card-subject-2", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTag("show-target-simulation-card", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        assertTagText(
            "temporary-target-hint",
            "Les modifications ici ne changent pas l’objectif sauvegardé."
        )
        composeRule.onNodeWithTag("target-average-input", useUnmergedTree = true)
            .performTextReplacement("6.0")
        composeRule.onNodeWithTag("use-saved-target", useUnmergedTree = true)
            .performScrollTo()
            .performTouchInput { click() }
        assertTagText("target-average-input", "5,0")
        composeRule.onNodeWithTag("target-planned-grade-count-2", useUnmergedTree = true)
            .performScrollTo()
        composeRule.onNodeWithTag("branch-detail-list", useUnmergedTree = true)
            .performTouchInput { swipeUp(startY = 1_500f, endY = 1_100f) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("target-planned-grade-count-2", useUnmergedTree = true)
            .performTouchInput { click() }
            .assertIsSelected()

        composeRule.onNodeWithTag("target-simulation-required-value", useUnmergedTree = true)
            .performScrollTo()
            .assertTextEquals("5,13")
    }

    @Test
    fun editingGradeShowsItsCurrentOfficialImpact() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = false,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    ),
                    StoredSubject(
                        id = "subject-2",
                        name = "Mathematics",
                        isInBasket = false,
                        notes = listOf(
                            storedNote(id = "note-1", value = 4.0),
                            storedNote(id = "note-2", value = 6.0)
                        )
                    )
                ),
                nextSubjectSequence = 3,
                nextNoteSequence = 3
            )
        )

        launchApp()
        scrollMainScreenUntilTag("subject-card-subject-2")
        composeRule.onNodeWithTag("subject-card-subject-2", useUnmergedTree = true)
            .performClick()
        scrollBranchDetailUntilTag("note-card-note-2")
        composeRule.onNodeWithTag("note-card-note-2", useUnmergedTree = true)
            .performClick()

        composeRule.onNodeWithTag("grade-impact-card", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("grade-impact-with", useUnmergedTree = true)
            .assertTextEquals("5,0")
        composeRule.onNodeWithTag("grade-impact-without", useUnmergedTree = true)
            .assertTextEquals("4,0")
        composeRule.onNodeWithTag("grade-impact-delta", useUnmergedTree = true)
            .assertTextEquals("+1,0")
    }

    @Test
    fun gradeHistoryAndEditorExposeTheStoredSemester() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                selectedSemester = SchoolSemester.SEMESTER_2,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = false,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    ),
                    StoredSubject(
                        id = "subject-2",
                        name = "Mathematics",
                        isInBasket = false,
                        notes = listOf(
                            storedNote(id = "note-1", value = 4.0),
                            storedNote(id = "note-2", value = 5.0).copy(
                                semester = SchoolSemester.SEMESTER_2
                            )
                        )
                    )
                ),
                nextSubjectSequence = 3,
                nextNoteSequence = 3
            )
        )

        launchApp()
        scrollMainScreenUntilTag("subject-card-subject-2")
        composeRule.onNodeWithTag("subject-card-subject-2", useUnmergedTree = true)
            .performClick()
        scrollBranchDetailUntilTag("note-semester-note-2")

        assertTagText("note-semester-note-1", "S1")
        assertTagText("note-semester-note-2", "S2")
        composeRule.onNodeWithTag("note-card-note-2", useUnmergedTree = true)
            .performClick()
        assertTagText(
            "grade-destination-period",
            "Enregistrée dans : Première année · Semestre 2 · Cumul S1 + S2"
        )
        composeRule.onNodeWithTag(
            "grade-semester-${SchoolSemester.SEMESTER_1.name}",
            useUnmergedTree = true
        ).performClick()
        assertTagText(
            "grade-destination-period",
            "Enregistrée dans : Première année · Semestre 1"
        )
    }

    @Test
    fun swipeDeletionAndVisibleCorrectionActionsRemainReachable() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = false,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    ),
                    StoredSubject(
                        id = "subject-2",
                        name = "Mathematics",
                        isInBasket = false,
                        notes = listOf(
                            storedNote(id = "note-1", value = 5.0).copy(
                                description = "Geometry",
                                attachments = listOf(
                                    StoredAttachment(
                                        id = "attachment-1",
                                        filePath = appContext.filesDir.resolve("attachment-1.jpg").path
                                    )
                                )
                            )
                        )
                    )
                ),
                nextSubjectSequence = 3,
                nextNoteSequence = 2
            )
        )

        launchApp()
        scrollMainScreenUntilTag("swipe-subject-subject-2")
        assertTagAbsent("visible-delete-subject-subject-2")
        composeRule.onNodeWithTag("swipe-subject-subject-2", useUnmergedTree = true)
            .performTouchInput { swipeRight() }
        assertTextDisplayed("Supprimer la branche ?")
        composeRule.onNodeWithText("Annuler", useUnmergedTree = true).performClick()

        composeRule.onNodeWithTag("subject-card-subject-2", useUnmergedTree = true)
            .performClick()
        scrollBranchDetailUntilTag("swipe-note-note-1")
        composeRule.onNodeWithTag("visible-edit-note-note-1", useUnmergedTree = true)
            .assertIsDisplayed()
        assertTagAbsent("visible-delete-note-note-1")
        composeRule.onNodeWithTag("swipe-note-note-1", useUnmergedTree = true)
            .performTouchInput { swipeRight() }
        assertTextDisplayed("Supprimer la note ?")
        composeRule.onNodeWithText("Annuler", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("open-note-attachments-note-1", useUnmergedTree = true)
            .performClick()

        composeRule.onNodeWithTag("attachment-viewer-image-0", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Photo 1 sur 1 pour Geometry")
    }

    @Test
    fun pdfReportExportExplainsCumulativeScopeBeforeOpeningFilePicker() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        schoolYear = SchoolYear.YEAR_3,
                        isInBasket = true,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    )
                ),
                selectedYear = SchoolYear.YEAR_3,
                selectedSemester = SchoolSemester.SEMESTER_2,
                language = AppLanguage.FRENCH
            )
        )

        launchApp()
        composeRule.onNodeWithTag("open-settings", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTag("export-grade-report-button", useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        assertTextDisplayed("Créer un relevé personnel ?")
        composeRule.onNodeWithText(
            text = "Situation cumulative S1 + S2",
            substring = true,
            useUnmergedTree = true
        ).fetchSemanticsNode()
        composeRule.onNodeWithText(
            text = "n’est pas un bulletin scolaire officiel",
            substring = true,
            useUnmergedTree = true
        ).fetchSemanticsNode()
        assertTextDisplayed("Choisir l’emplacement")
    }

    @Test
    fun settingsGroupsTasksAndKeepsOptionChoicesCollapsedUntilRequested() {
        repository.save(
            GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject(
                        id = "subject-1",
                        name = "Option",
                        isInBasket = true,
                        isOptionSubject = true,
                        optionChoice = InitialOptionChoice.SPANISH
                    )
                ),
                language = AppLanguage.FRENCH
            )
        )

        launchApp()
        composeRule.onNodeWithTag("open-settings", useUnmergedTree = true)
            .performClick()

        assertTextDisplayed("Préférences de l'app")
        assertTextDisplayed("Configuration scolaire")
        composeRule.onNodeWithText("Données et exports", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        assertTagAbsent("settings-option-BIOLOGY_CHEMISTRY")

        composeRule.onNodeWithTag("toggle-option-choices", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag(
            "settings-option-BIOLOGY_CHEMISTRY",
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText(
            text = "trois années scolaires",
            substring = true,
            useUnmergedTree = true
        ).fetchSemanticsNode()
        composeRule.onNodeWithText(
            text = "photos",
            substring = true,
            useUnmergedTree = true
        ).fetchSemanticsNode()
    }

    @Test
    fun firstUsePeriodConfirmationRemainsDirectlyClickable() {
        launchApp()

        composeRule.onNodeWithTag(
            "onboarding-option-${InitialOptionChoice.SPANISH.name}",
            useUnmergedTree = true
        )
            .performScrollTo()
            .performTouchInput { click() }
            .assertIsSelected()
        composeRule.onNodeWithTag("onboarding-continue", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
            .performTouchInput { click() }
        waitForTag("confirm-period-selection")
        composeRule.onNodeWithTag(
            "semester-${SchoolSemester.SEMESTER_2.name}",
            useUnmergedTree = true
        ).performTouchInput { click() }
        composeRule.onNodeWithText("Semestre 2", useUnmergedTree = true)
            .assertIsDisplayed()
        assertTagText("period-cumulative-hint", "Cumul S1 + S2")
        composeRule.onNodeWithText(
            "Première année · Semestre 2",
            useUnmergedTree = true
        ).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithTag(
                    "confirm-period-selection",
                    useUnmergedTree = true
                ).assertIsDisplayed()
            }.isSuccess
        }
        composeRule.onNodeWithTag("confirm-period-selection", useUnmergedTree = true)
            .performTouchInput { click() }

        waitForTag("main-screen-list")
        composeRule.onNodeWithTag("main-screen-list", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun scrollBranchDetailUntilTag(tag: String) {
        repeat(12) {
            val targetExists = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
            if (targetExists) return

            composeRule.onNodeWithTag("branch-detail-list", useUnmergedTree = true)
                .performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        waitForTag(tag)
    }

    private fun scrollMainScreenUntilTag(tag: String) {
        repeat(12) {
            val targetExists = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
            if (targetExists) return

            composeRule.onNodeWithTag("main-screen-list", useUnmergedTree = true)
                .performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        waitForTag(tag)
    }

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
    }

    private fun clearPersistedState() {
        appContext.getSharedPreferences("grade_tracker_app_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private fun storedNote(id: String, value: Double): StoredNote {
        return StoredNote(
            id = id,
            value = value,
            weight = AssessmentWeight.FULL,
            description = "",
            createdAtEpochMillis = 0L
        )
    }

    private fun assertTagDisplayed(tag: String) {
        waitForTag(tag)
        composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun assertTagAbsent(tag: String) {
        check(composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNodes().isEmpty()) {
            "Expected no node with tag '$tag'."
        }
    }

    private fun assertTextDisplayed(text: String) {
        composeRule.onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun assertTagText(tag: String, text: String) {
        waitForTag(tag)
        composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .assertTextEquals(text)
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

}
