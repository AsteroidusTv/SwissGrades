package me.asteroidus.swissgrades.ui.simulation

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.asteroidus.swissgrades.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistedEditorFlowInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var appContext: Context
    private lateinit var persistence: SharedPreferencesSimulationEditorPersistence
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        persistence = SharedPreferencesSimulationEditorPersistence(appContext)
        clearPersistedState()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        clearPersistedState()
    }

    @Test
    fun enterData_thenRecreate_restoresTheSameVisibleEditorState() {
        launchApp()

        inputText("grade-input-GERMAN-german-entry-1", "4.5")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        inputText("grade-input-OPTION-option-entry-1", "4.0")
        inputText("custom-subject-name-input", "History")
        clickTag("add-custom-subject")
        assertTagText("branch-average-value-history", "History: Not evaluated")

        scenario!!.recreate()
        composeRule.waitForIdle()

        assertTagText("grade-input-GERMAN-german-entry-1", "4.5")
        assertTagText("grade-input-FRENCH-french-entry-1", "4.0")
        assertTagText("grade-input-MATH-math-entry-1", "4.0")
        assertTagText("grade-input-OPTION-option-entry-1", "4.0")
        assertTagDisplayed("remove-subject-custom-subject-1")
        assertTagText("required-subjects-heading", "Required subjects")
        assertTagText("promotion-summary-heading", "Promotion summary")
        assertTagText("branch-average-value-history", "History: Not evaluated")
        assertTagText("summary-basket-total", "Basket total: 16.50")
    }

    @Test
    fun launchWithPersistedCompositeOption_restoresModeTypeAndSubSubjectInputs() {
        persistence.save(
            PersistedSimulationEditorState(
                branchInputs = baseBasketState(),
                optionMode = OptionModeUi.COMPOSITE,
                simpleOption = SimpleOptionChoice.SPANISH,
                compositeOption = CompositeOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATHEMATICS,
                optionSubSubjects = listOf(
                    PersistedOptionSubSubjectInput(
                        key = OptionSubSubjectKey.PHYSICS,
                        name = "Physics",
                        gradeEntries = listOf(PersistedGradeEntry("option-physics-entry-3", "5.0", GradeWeightUi.FULL))
                    ),
                    PersistedOptionSubSubjectInput(
                        key = OptionSubSubjectKey.APPLICATIONS_OF_MATHEMATICS,
                        name = "Applications of Mathematics",
                        gradeEntries = listOf(
                            PersistedGradeEntry("option-applications-of-mathematics-entry-4", "4.0", GradeWeightUi.FULL)
                        )
                    )
                )
            )
        )

        launchApp()

        assertTextDisplayed("Composite option type")
        assertTextDisplayed("Physics")
        assertTextDisplayed("Applications of Mathematics")
        assertTagText("grade-input-PHYSICS-option-physics-entry-3", "5.0")
        assertTagText("grade-input-APPLICATIONS_OF_MATHEMATICS-option-applications-of-mathematics-entry-4", "4.0")
        composeRule.onAllNodesWithTag("grade-input-OPTION-option-entry-1").assertCountEquals(0)
    }

    @Test
    fun relaunchWithPersistedCompositeOption_restoresVisibleSummary() {
        persistence.save(
            PersistedSimulationEditorState(
                branchInputs = baseBasketState(),
                optionMode = OptionModeUi.COMPOSITE,
                simpleOption = SimpleOptionChoice.SPANISH,
                compositeOption = CompositeOptionChoice.ECONOMICS_AND_LAW,
                optionSubSubjects = listOf(
                    PersistedOptionSubSubjectInput(
                        key = OptionSubSubjectKey.ECONOMICS,
                        name = "Economics",
                        gradeEntries = listOf(PersistedGradeEntry("option-economics-entry-2", "5.0", GradeWeightUi.FULL))
                    ),
                    PersistedOptionSubSubjectInput(
                        key = OptionSubSubjectKey.LAW,
                        name = "Law",
                        gradeEntries = listOf(PersistedGradeEntry("option-law-entry-4", "4.0", GradeWeightUi.FULL))
                    )
                )
            )
        )

        launchApp()
        scenario!!.close()
        scenario = null

        launchApp()

        assertTagText("summary-status", "Status: Promoted")
        assertTagText("branch-average-value-option", "Option: 4.50")
        assertTagText("summary-basket-total", "Basket total: 16.50")
    }

    @Test
    fun launchWithPersistedAdditionalSubjects_restoresVisibleAdditionalBranchesIncludingEmptyOptional() {
        persistence.save(
            PersistedSimulationEditorState(
                branchInputs = baseBasketState() + listOf(
                    PersistedBranchInput(
                        branchId = "custom-subject-1",
                        branchName = "History",
                        gradeEntries = listOf(PersistedGradeEntry("custom-subject-1-entry-1", "", GradeWeightUi.FULL))
                    ),
                    PersistedBranchInput(
                        branchId = "custom-subject-2",
                        branchName = "Biology",
                        gradeEntries = listOf(PersistedGradeEntry("custom-subject-2-entry-1", "5.0", GradeWeightUi.FULL))
                    )
                )
            )
        )

        launchApp()

        assertTagDisplayed("remove-subject-custom-subject-1")
        assertTagDisplayed("remove-subject-custom-subject-2")
        assertTagText("branch-average-value-history", "History: Not evaluated")
        assertTagText("branch-average-detail-history", "Optional subject left empty.")
        assertTagText("branch-average-value-biology", "Biology: 5.00")
        assertTagText("summary-promotion-points-total", "Promotion points total: +1.00")
    }

    @Test
    fun launchWithInvalidPersistedInput_restoresFieldAndGlobalValidationState() {
        persistence.save(
            PersistedSimulationEditorState(
                branchInputs = listOf(
                    PersistedBranchInput(
                        branchId = "german",
                        branchName = "German",
                        branchKey = EditorBranchKey.GERMAN,
                        gradeEntries = listOf(PersistedGradeEntry("german-entry-1", "4.3", GradeWeightUi.FULL))
                    ),
                    PersistedBranchInput(
                        branchId = "french",
                        branchName = "French",
                        branchKey = EditorBranchKey.FRENCH,
                        gradeEntries = listOf(PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL))
                    ),
                    PersistedBranchInput(
                        branchId = "math",
                        branchName = "Math",
                        branchKey = EditorBranchKey.MATH,
                        gradeEntries = listOf(PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL))
                    ),
                    PersistedBranchInput(
                        branchId = "option",
                        branchName = "Option",
                        branchKey = EditorBranchKey.OPTION,
                        gradeEntries = listOf(PersistedGradeEntry("option-entry-1", "4.0", GradeWeightUi.FULL))
                    )
                )
            )
        )

        launchApp()

        assertTagText("grade-input-GERMAN-german-entry-1", "4.3")
        assertTagText("field-error-GERMAN-german-entry-1", INVALID_GRADE_MESSAGE)
        assertTagDisplayed("input-notice")
        assertTagText("summary-status", "Status: Incomplete")
        assertTextDisplayed("No average is available for German.")
    }

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
        assertTagDisplayed("promotion-summary")
    }

    private fun clearPersistedState() {
        appContext.getSharedPreferences("simulation_editor_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private fun baseBasketState(): List<PersistedBranchInput> {
        return listOf(
            PersistedBranchInput(
                branchId = "german",
                branchName = "German",
                branchKey = EditorBranchKey.GERMAN,
                gradeEntries = listOf(PersistedGradeEntry("german-entry-1", "4.0", GradeWeightUi.FULL))
            ),
            PersistedBranchInput(
                branchId = "french",
                branchName = "French",
                branchKey = EditorBranchKey.FRENCH,
                gradeEntries = listOf(PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL))
            ),
            PersistedBranchInput(
                branchId = "math",
                branchName = "Math",
                branchKey = EditorBranchKey.MATH,
                gradeEntries = listOf(PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL))
            ),
            PersistedBranchInput(
                branchId = "option",
                branchName = "Option",
                branchKey = EditorBranchKey.OPTION,
                gradeEntries = listOf(PersistedGradeEntry("option-entry-1", "4.0", GradeWeightUi.FULL))
            )
        )
    }

    private fun inputText(tag: String, text: String) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .performTextInput(text)
    }

    private fun clickTag(tag: String) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    private fun assertTagDisplayed(tag: String) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun assertTagText(tag: String, text: String) {
        composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .assertTextContains(text)
    }

    private fun assertTextDisplayed(text: String) {
        composeRule.onNodeWithText(text, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
