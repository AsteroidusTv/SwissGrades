package me.asteroidus.swissgrades.ui.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
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
        assertTagDisplayed("open-settings")
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

    private fun openSubject(subjectId: String) {
        waitForTag("subject-card-$subjectId")
        composeRule.onNodeWithTag("subject-card-$subjectId", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
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
