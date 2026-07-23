package me.asteroidus.swissgrades.ui.simulation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import me.asteroidus.swissgrades.domain.model.BranchAverageResult
import me.asteroidus.swissgrades.domain.model.BranchAverageStatus
import me.asteroidus.swissgrades.domain.model.PromotionBlockingReason
import me.asteroidus.swissgrades.domain.model.PromotionEvaluationResult
import me.asteroidus.swissgrades.domain.model.PromotionMissingDataReason
import me.asteroidus.swissgrades.domain.model.PromotionStatus
import me.asteroidus.swissgrades.ui.theme.SwissGradesTheme

class PromotionSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersPromotedPresentationModel() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.PROMOTED,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.5, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("French", 4.0, BranchAverageStatus.COMPUTED)
                ),
                basketTotal = 17.0,
                promotionPointsTotal = 1.5,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        composeTestRule.onNodeWithText("Status: Promoted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Promotion requirements are currently satisfied.").assertIsDisplayed()
        composeTestRule.onNodeWithText("No blocking reasons.").assertIsDisplayed()
        composeTestRule.onNodeWithText("No missing data.").assertIsDisplayed()
    }

    @Test
    fun rendersBlockedPresentationModelWithMultipleBlockingReasons() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.BLOCKED,
                branchAverages = listOf(BranchAverageResult("History", 2.5, BranchAverageStatus.COMPUTED)),
                basketTotal = 15.0,
                promotionPointsTotal = -2.0,
                blockingReasons = listOf(
                    PromotionBlockingReason.BranchAverageBelowThree("History", 2.5),
                    PromotionBlockingReason.BasketBelowThreshold(15.0),
                    PromotionBlockingReason.MoreThanFourBranchesBelowFour(
                        listOf("History", "English", "Physics", "Biology", "Geography")
                    )
                ),
                missingDataReasons = emptyList()
            )
        )

        composeTestRule.onNodeWithText("Status: Blocked").assertIsDisplayed()
        composeTestRule.onNodeWithText("History is below 3.0 with an average of 2.50.").assertIsDisplayed()
        composeTestRule.onNodeWithText("The basket total is 15.00, below the required 16.00.").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "More than 4 branches are below 4.0: History, English, Physics, Biology, Geography."
        ).assertIsDisplayed()
    }

    @Test
    fun rendersIncompletePresentationModelWithMissingData() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.INCOMPLETE,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.0, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("English", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
                ),
                basketTotal = 15.0,
                promotionPointsTotal = null,
                blockingReasons = emptyList(),
                missingDataReasons = listOf(
                    PromotionMissingDataReason.MissingBranchAverage("English")
                )
            )
        )

        composeTestRule.onNodeWithText("Status: Incomplete").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Promotion cannot be decided yet because some data is missing."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("No average is available for English.").assertIsDisplayed()
    }

    @Test
    fun keepsKnownBlockersVisibleWhenStatusIsIncomplete() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.INCOMPLETE,
                branchAverages = listOf(
                    BranchAverageResult("History", 2.5, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("English", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
                ),
                basketTotal = 15.0,
                promotionPointsTotal = null,
                blockingReasons = listOf(
                    PromotionBlockingReason.BranchAverageBelowThree("History", 2.5),
                    PromotionBlockingReason.BasketBelowThreshold(15.0)
                ),
                missingDataReasons = listOf(
                    PromotionMissingDataReason.MissingBranchAverage("English")
                )
            )
        )

        composeTestRule.onNodeWithText("History is below 3.0 with an average of 2.50.").assertIsDisplayed()
        composeTestRule.onNodeWithText("The basket total is 15.00, below the required 16.00.").assertIsDisplayed()
        composeTestRule.onNodeWithText("No average is available for English.").assertIsDisplayed()
    }

    @Test
    fun showsNotAvailableWhenBasketTotalIsNull() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.INCOMPLETE,
                branchAverages = emptyList(),
                basketTotal = null,
                promotionPointsTotal = null,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        composeTestRule.onNodeWithText("Basket total: Not available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Promotion points total: Not available").assertIsDisplayed()
    }

    @Test
    fun rendersBranchAveragesListStably() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.PROMOTED,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.5, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("French", 4.0, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("English", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
                ),
                basketTotal = 17.0,
                promotionPointsTotal = 1.5,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        composeTestRule.onNodeWithText("German: 4.50").assertIsDisplayed()
        composeTestRule.onNodeWithText("French: 4.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("English: No average available").assertIsDisplayed()
        composeTestRule.onNodeWithText("This branch cannot be evaluated yet.").assertIsDisplayed()
    }

    @Test
    fun rendersMixedBranchStatusesClearly() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.INCOMPLETE,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.5, BranchAverageStatus.COMPUTED),
                    BranchAverageResult("History", null, BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL),
                    BranchAverageResult("Math", null, BranchAverageStatus.MISSING_OR_NON_CALCULABLE)
                ),
                basketTotal = 16.5,
                promotionPointsTotal = null,
                blockingReasons = emptyList(),
                missingDataReasons = listOf(
                    PromotionMissingDataReason.MissingBranchAverage("Math")
                )
            )
        )

        composeTestRule.onNodeWithText("German: 4.50").assertIsDisplayed()
        composeTestRule.onNodeWithText("History: Not evaluated").assertIsDisplayed()
        composeTestRule.onNodeWithText("Optional subject left empty.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Math: No average available").assertIsDisplayed()
        composeTestRule.onNodeWithText("This branch cannot be evaluated yet.").assertIsDisplayed()
    }

    @Test
    fun emptyOptionalAdditionalSubjectDoesNotRenderAsGenericMissingAverage() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.PROMOTED,
                branchAverages = listOf(
                    BranchAverageResult("History", null, BranchAverageStatus.EMPTY_OPTIONAL_ADDITIONAL)
                ),
                basketTotal = 16.0,
                promotionPointsTotal = 0.0,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        composeTestRule.onNodeWithText("History: Not evaluated").assertIsDisplayed()
        composeTestRule.onNodeWithText("Optional subject left empty.").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("History: No average available").assertCountEquals(0)
    }

    @Test
    fun invalidAdditionalSubjectStillRendersThroughMissingPath() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.0")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        inputText("grade-input-OPTION-option-entry-1", "4.0")
        inputText("custom-subject-name-input", "History")
        clickTag("add-custom-subject")
        inputText("grade-input-custom-subject-1-custom-subject-1-entry-1", "4.3")

        assertTagText("branch-average-value-history", "History: No average available")
        assertTagText("branch-average-detail-history", "This branch cannot be evaluated yet.")
        composeTestRule.onAllNodesWithText("History: Not evaluated").assertCountEquals(0)
    }

    @Test
    fun basketTotalAndPromotionPointsDisplayRemainUnchanged() {
        setScreenContent(
            result = PromotionEvaluationResult(
                status = PromotionStatus.PROMOTED,
                branchAverages = listOf(
                    BranchAverageResult("German", 4.5, BranchAverageStatus.COMPUTED)
                ),
                basketTotal = 17.0,
                promotionPointsTotal = 1.5,
                blockingReasons = emptyList(),
                missingDataReasons = emptyList()
            )
        )

        composeTestRule.onNodeWithText("Basket total: 17.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Promotion points total: +1.50").assertIsDisplayed()
    }

    @Test
    fun compositeOptionModeSwitch_showsCompositeEditor() {
        setAppContent()

        clickTag("option-mode-COMPOSITE")

        assertTextDisplayed("Composite option type")
        assertTagDisplayed("option-composite-BIOLOGY_CHEMISTRY")
        assertTagDisplayed("grade-input-BIOLOGY-option-biology-entry-1")
        assertTagDisplayed("grade-input-CHEMISTRY-option-chemistry-entry-1")
        composeTestRule.onAllNodesWithTag("grade-input-OPTION-option-entry-1").assertCountEquals(0)
    }

    @Test
    fun compositeOptionFlow_supportsSelectingEachCompositeType() {
        setAppContent()

        clickTag("option-mode-COMPOSITE")
        assertTextDisplayed("Biology")
        assertTextDisplayed("Chemistry")

        clickTag("option-composite-PHYSICS_AND_APPLICATIONS_OF_MATHEMATICS")
        assertTextDisplayed("Physics")
        assertTextDisplayed("Applications of Mathematics")

        clickTag("option-composite-ECONOMICS_AND_LAW")
        assertTextDisplayed("Economics")
        assertTextDisplayed("Law")
    }

    @Test
    fun compositeBiologyChemistry_updatesSummaryAfterEnteringBothSubSubjects() {
        setAppContent()

        fillRequiredBasketBranchesForCompositeOption()
        inputText("grade-input-BIOLOGY-option-biology-entry-1", "5.5")
        inputText("grade-input-CHEMISTRY-option-chemistry-entry-1", "3.5")

        assertTagText("summary-status", "Status: Promoted")
        assertTagText("branch-average-value-option", "Option: 4.50")
        assertTagText("summary-basket-total", "Basket total: 16.50")
    }

    @Test
    fun compositeOptionWithEmptySubSubject_isIncomplete() {
        setAppContent()

        fillRequiredBasketBranchesForCompositeOption()
        inputText("grade-input-BIOLOGY-option-biology-entry-1", "5.0")

        assertTagText("summary-status", "Status: Incomplete")
        assertTagText("branch-average-value-option", "Option: No average available")
        assertTextDisplayed("No average is available for Option.")
    }

    @Test
    fun compositeOptionWithInvalidSubSubjectInput_isIncomplete() {
        setAppContent()

        fillRequiredBasketBranchesForCompositeOption()
        clickTag("option-composite-PHYSICS_AND_APPLICATIONS_OF_MATHEMATICS")
        inputText("grade-input-PHYSICS-option-physics-entry-1", "4.3")
        inputText("grade-input-APPLICATIONS_OF_MATHEMATICS-option-applications-of-mathematics-entry-1", "5.0")

        assertTagText("summary-status", "Status: Incomplete")
        assertTagText("field-error-PHYSICS-option-physics-entry-1", INVALID_GRADE_MESSAGE)
        assertTagText("branch-average-value-option", "Option: No average available")
    }

    @Test
    fun editableFlow_updatesShownSummaryAfterEditingMultiGradeBranch() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "5.0")
        clickTag("add-grade-GERMAN")
        inputText("grade-input-GERMAN-german-entry-2", "4.5")
        clickTag("weight-GERMAN-german-entry-2-HALF")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        inputText("grade-input-OPTION-option-entry-1", "4.0")

        assertTagText("summary-status", "Status: Promoted")
        assertTagText("summary-basket-total", "Basket total: 17.00")
        assertTagText("branch-average-value-german", "German: 5.00")
    }

    @Test
    fun editableFlow_weightChangeUpdatesSummaryWithoutManualRefresh() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "5.0")
        clickTag("add-grade-GERMAN")
        inputText("grade-input-GERMAN-german-entry-2", "2.0")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        inputText("grade-input-OPTION-option-entry-1", "4.0")
        assertTagText("summary-basket-total", "Basket total: 15.50")

        clickTag("weight-GERMAN-german-entry-2-HALF")

        assertTagText("summary-basket-total", "Basket total: 16.00")
        assertTagText("branch-average-value-german", "German: 4.00")
    }

    @Test
    fun editableFlow_usesStableEntryTagsAcrossAddAndRemoveOperations() {
        setAppContent()

        clickTag("add-grade-GERMAN")
        clickTag("add-grade-GERMAN")
        clickTag("remove-grade-GERMAN-german-entry-2")

        assertTagDisplayed("grade-input-GERMAN-german-entry-1")
        assertTagDisplayed("grade-input-GERMAN-german-entry-3")
        composeTestRule.onAllNodesWithTag("grade-input-GERMAN-german-entry-2").assertCountEquals(0)
    }

    @Test
    fun editableFlow_doesNotComputePartialAverageFromMixedValidAndInvalidBranch() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.0")
        clickTag("add-grade-GERMAN")
        inputText("grade-input-GERMAN-german-entry-2", "4.3")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        inputText("grade-input-OPTION-option-entry-1", "4.0")

        assertTagText("summary-status", "Status: Incomplete")
        assertTagText("field-error-GERMAN-german-entry-2", INVALID_GRADE_MESSAGE)
        assertTagDisplayed("input-notice")
        assertTagText("branch-average-value-german", "German: No average available")
        assertTextDisplayed("No average is available for German.")
        composeTestRule.onAllNodesWithText("German: 4.00").assertCountEquals(0)
    }

    @Test
    fun editableFlow_showsInvalidGradeInputError() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.3")

        assertTagText("field-error-GERMAN-german-entry-1", INVALID_GRADE_MESSAGE)
        assertTagDisplayed("input-notice")
    }

    @Test
    fun editableFlow_keepsLiveErrorWhenInvalidValueChangesToAnotherInvalidValue() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.3")
        clearAndInputText("grade-input-GERMAN-german-entry-1", "4.1")

        assertTagText("field-error-GERMAN-german-entry-1", INVALID_GRADE_MESSAGE)
        assertTagDisplayed("input-notice")
    }

    @Test
    fun editableFlow_showsIncompleteWhenDataIsMissing() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.0")

        assertTagText("summary-status", "Status: Incomplete")
        assertTextDisplayed("No average is available for French.")
        assertTextDisplayed("No average is available for Math.")
        assertTextDisplayed("No average is available for Option.")
        composeTestRule.onAllNodesWithTag("input-notice").assertCountEquals(0)
    }

    @Test
    fun editableFlow_rejectsDecimalCommaExplicitly() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4,5")

        assertTagText("field-error-GERMAN-german-entry-1", DECIMAL_SEPARATOR_MESSAGE)
        assertTagDisplayed("input-notice")
        assertTagText("summary-status", "Status: Incomplete")
        assertTextDisplayed("No average is available for German.")
    }

    @Test
    fun editableFlow_keepsInvalidFieldStateExplicitAlongsideIncompleteSummary() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4,5")

        assertTagText("summary-status", "Status: Incomplete")
        assertTagText("field-error-MATH-math-entry-1", DECIMAL_SEPARATOR_MESSAGE)
        assertTagDisplayed("input-notice")
        assertTextDisplayed("No average is available for Math.")
    }

    @Test
    fun editableFlow_clearsGlobalInvalidNoticeImmediatelyAfterCorrection() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.3")
        assertTagDisplayed("input-notice")

        clearAndInputText("grade-input-GERMAN-german-entry-1", "5.0")

        composeTestRule.onAllNodesWithTag("input-notice").assertCountEquals(0)
    }

    @Test
    fun editableFlow_correctingInvalidInputRestoresCalculableSummaryImmediately() {
        setAppContent()

        inputText("grade-input-GERMAN-german-entry-1", "4.3")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        inputText("grade-input-OPTION-option-entry-1", "4.0")
        assertTagText("summary-status", "Status: Incomplete")
        assertTextDisplayed("No average is available for German.")

        clearAndInputText("grade-input-GERMAN-german-entry-1", "4.5")

        assertTagText("summary-status", "Status: Promoted")
        assertTagText("summary-basket-total", "Basket total: 16.50")
        assertTagText("branch-average-value-german", "German: 4.50")
    }

    @Test
    fun editableFlow_removingTheLastVisibleGradeEntry_keepsAnEmptyPlaceholderEntry() {
        setAppContent()

        clickTag("remove-grade-GERMAN-german-entry-1")

        assertTagDisplayed("grade-input-GERMAN-german-entry-2")
        assertTagText("summary-status", "Status: Incomplete")
        composeTestRule.onAllNodesWithTag("grade-input-GERMAN-german-entry-1").assertCountEquals(0)
    }

    @Test
    fun mainFlow_noLongerShowsHardcodedScenarioButtons() {
        setAppContent()

        composeTestRule.onAllNodesWithText("Promoted").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Blocked").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Incomplete").assertCountEquals(0)
        assertTagDisplayed("grade-input-GERMAN-german-entry-1")
        composeTestRule.onAllNodesWithTag("update-summary").assertCountEquals(0)
    }

    @Test
    fun mainFlow_usesRealEditingWordingInsteadOfSimulationWording() {
        setAppContent()

        assertTagDisplayed("editor-intro")
        assertTagText("required-subjects-heading", "Required subjects")
        assertTagText("promotion-summary-heading", "Promotion summary")
        composeTestRule.onAllNodesWithText("simulation", substring = true).assertCountEquals(0)
    }

    @Test
    fun customSubject_canBeCreatedAndShownInEditor() {
        setAppContent()

        inputText("custom-subject-name-input", "Literature")
        clickTag("add-custom-subject")

        assertTextDisplayed("Literature")
        assertTagDisplayed("grade-input-custom-subject-1-custom-subject-1-entry-1")
    }

    private fun setScreenContent(result: PromotionEvaluationResult) {
        val presentation = PromotionPresentationMapper.map(result)

        composeTestRule.setContent {
            SwissGradesTheme {
                PromotionSummaryScreen(presentation = presentation)
            }
        }
    }

    private fun setAppContent() {
        composeTestRule.setContent {
            SwissGradesTheme {
                GradeTrackerApp(persistence = InMemorySimulationEditorPersistence())
            }
        }
    }

    private fun fillRequiredBasketBranchesForCompositeOption() {
        inputText("grade-input-GERMAN-german-entry-1", "4.0")
        inputText("grade-input-FRENCH-french-entry-1", "4.0")
        inputText("grade-input-MATH-math-entry-1", "4.0")
        clickTag("option-mode-COMPOSITE")
    }

    private fun inputText(tag: String, text: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .performTextInput(text)
    }

    private fun clearAndInputText(tag: String, text: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .performTextClearance()
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .performTextInput(text)
    }

    private fun clickTag(tag: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    private fun assertTagDisplayed(tag: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun assertTagText(tag: String, text: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollTo()
            .assertTextContains(text)
    }

    private fun assertTextDisplayed(text: String) {
        composeTestRule.onNodeWithText(text, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    private class InMemorySimulationEditorPersistence(
        private var state: PersistedSimulationEditorState? = null
    ) : SimulationEditorPersistence {
        override fun load(): PersistedSimulationEditorState? = state

        override fun save(state: PersistedSimulationEditorState) {
            this.state = state
        }
    }
}
