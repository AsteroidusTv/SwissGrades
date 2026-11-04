package me.asteroidus.swissgrades.ui.app

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.asteroidus.swissgrades.domain.model.AssessmentWeight
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CarnetInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun pageTransitionsKeepHeaderAndContentAnchored() {
        launchCarnet()
        val headerBounds = compose.onNodeWithTag("carnet-app-header").fetchSemanticsNode().boundsInRoot
        val contentTop = compose.onNodeWithTag("carnet-page-content").fetchSemanticsNode().boundsInRoot.top
        fun assertAnchored() {
            assertEquals(headerBounds, compose.onNodeWithTag("carnet-app-header").fetchSemanticsNode().boundsInRoot)
            assertEquals(contentTop, compose.onNodeWithTag("carnet-page-content").fetchSemanticsNode().boundsInRoot.top)
        }
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithTag("open-settings").performClick()
            compose.mainClock.advanceTimeBy(80)
            assertAnchored()
            compose.mainClock.advanceTimeBy(240)
            compose.onNodeWithTag("carnet-nav-SUBJECTS").performClick()
            compose.mainClock.advanceTimeBy(240)
            mainNode("subject-card-math").performClick()
            compose.mainClock.advanceTimeBy(80)
            assertAnchored()
            compose.mainClock.advanceTimeBy(240)
            detailNode("show-add-note-sheet").performClick()
            compose.mainClock.advanceTimeBy(80)
            assertAnchored()
            compose.mainClock.advanceTimeBy(240)
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun subjectSwitchesRemainVisibleWhenOffAndUnavailable() = verifySubjectSwitches(dark = false)

    @Test
    fun darkSubjectSwitchesRemainVisibleWhenOffAndUnavailable() = verifySubjectSwitches(dark = true)

    private fun verifySubjectSwitches(dark: Boolean) {
        launchCarnet(dark = dark)
        compose.onNodeWithTag("show-add-subject-header").performClick()
        compose.onNodeWithTag("add-subject-counted").performScrollTo().performClick().assertIsOff()
        compose.onNodeWithTag("add-subject-basket").assertIsOff().assertIsNotEnabled()
        screenshot(if (dark) "carnet-switches-dark-disabled" else "carnet-switches-disabled")
        compose.onNodeWithTag("add-subject-counted").performClick()
        compose.onNodeWithTag("add-subject-basket").assertIsOff().assertIsEnabled()
        screenshot(if (dark) "carnet-switches-dark-off" else "carnet-switches-off")
        compose.onNodeWithTag("toggle-subject-personalization").performScrollTo().performClick()
        compose.onNodeWithTag("subject-icon-SPORT").performScrollTo().assertIsDisplayed()
        screenshot(if (dark) "carnet-icons-dark" else "carnet-icons")
    }

    @Test
    fun popupsKeepTheirActionsReachable() {
        launchCarnet()
        compose.onNodeWithTag("open-period-picker").performClick()
        screenshot("carnet-period-picker")
        compose.onNodeWithTag("back-from-period-picker").performClick()
        mainNode("carnet-add-grade").performClick()
        compose.onNodeWithTag("grade-subject-math").assertIsDisplayed()
        screenshot("carnet-subject-picker")
        compose.onNodeWithText("Annuler").performClick()
        compose.onNodeWithTag("open-settings").performClick()
        listOf(
            "settings-appearance" to "carnet-appearance-dialog",
            "settings-language" to "carnet-language-dialog",
            "settings-backup-and-restore" to "carnet-backup-dialog",
            "export-grade-report-button" to "carnet-report-dialog",
            "toggle-option-choices" to "carnet-option-dialog",
            "reset-app-button" to "carnet-reset-dialog"
        ).forEach { (tag, capture) ->
            compose.onNodeWithTag(tag).performScrollTo().performClick()
            compose.onNodeWithText("Annuler").assertIsDisplayed()
            screenshot(capture)
            compose.onNodeWithText("Annuler").performClick()
        }
        compose.onNodeWithTag("carnet-nav-SUBJECTS").performClick()
        mainNode("carnet-add-grade").performClick()
        compose.onNodeWithTag("grade-subject-math").performClick()
        compose.onNodeWithTag("show-add-photo-sheet").performScrollTo().performClick()
        screenshot("carnet-attachment-dialog")
        compose.onNode(hasText("Annuler") and hasAnyAncestor(isDialog())).assertIsDisplayed().performClick()
    }

    @Test
    fun carnetStartsOnSubjectsAndTabsRemainReachable() {
        launchCarnet()
        mainNode("carnet-add-grade").assertIsDisplayed()
        mainNode("subject-card-math").assertIsDisplayed()
        screenshot("carnet-subjects")
        compose.onNodeWithTag("carnet-nav-PROMOTION").performClick().assertIsSelected()
        compose.onNodeWithTag("promotion-setup-card").performScrollTo().assertIsDisplayed()
        screenshot("carnet-overview")
        compose.onNodeWithTag("open-settings").performClick().assertIsSelected()
        screenshot("carnet-settings")
        compose.onNodeWithTag("carnet-nav-SUBJECTS").performClick().assertIsSelected()
        mainNode("carnet-add-grade").assertIsDisplayed()
    }

    @Test
    fun narrowLargeTextKeepsGradeEditorScrollableWithKeyboard() {
        launchCarnet(largeText = true)
        compose.onNodeWithTag("subject-card-math").assertIsDisplayed()
        screenshot("carnet-narrow-large-text")
        mainNode("carnet-add-grade").performClick()
        compose.onNodeWithTag("grade-subject-math").performClick()
        compose.onNodeWithTag("note-value-input").performScrollTo().performClick()
        compose.onNodeWithTag("note-value-input").performTextReplacement("6,25")
        compose.onNodeWithTag("add-note").assertIsDisplayed().performClick()
        compose.onNodeWithTag("note-draft-error").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("note-value-input").performScrollTo().performTextReplacement("4,75")
        compose.onNodeWithTag("note-description-input").performScrollTo().performTextReplacement("Test Carnet")
        compose.onNodeWithTag("add-note").assertIsDisplayed()
        screenshot("carnet-grade-editor-large-text")
        compose.onNodeWithTag("add-note").performClick()
        detailNode("show-add-note-sheet").assertIsDisplayed()
    }


    @Test
    fun swipeDeletesNotesAndSubjectsOnlyAfterConfirmation() {
        launchCarnet()
        mainNode("subject-card-math").performClick()
        screenshot("carnet-detail")
        detailNode("swipe-note-note-1").performTouchInput { swipeRight() }
        screenshot("carnet-delete-dialog")
        compose.onNodeWithText("Annuler").performClick()
        compose.onNodeWithTag("note-card-note-1").assertIsDisplayed()
        compose.onNodeWithTag("swipe-note-note-1").performTouchInput { swipeRight() }
        compose.onNodeWithText("Supprimer", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("note-card-note-1").assertDoesNotExist()
        detailNode("back-from-detail").performClick()
        mainNode("swipe-subject-math").performTouchInput { swipeRight() }
        compose.onNodeWithText("Annuler").performClick()
        compose.onNodeWithTag("subject-card-math").assertIsDisplayed()
        compose.onNodeWithTag("swipe-subject-math").performTouchInput { swipeRight() }
        compose.onNodeWithText("Supprimer", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("subject-card-math").assertDoesNotExist()
        mainNode("subject-card-option").assertIsDisplayed()
    }

    @Test
    fun targetAndDarkPaletteUseCarnetSurfaces() {
        launchCarnet(dark = true)
        screenshot("carnet-dark")
        compose.onNodeWithTag("open-settings").performClick()
        compose.onNodeWithTag("settings-appearance").performScrollTo().performClick()
        screenshot("carnet-appearance-dialog-dark")
        compose.onNodeWithText("Annuler").performClick()
        compose.onNodeWithTag("carnet-nav-SUBJECTS").performClick()
        mainNode("subject-card-math").performClick()
        detailNode("show-target-simulation-card").performClick()
        detailNode("target-count-picker").performClick()
        screenshot("carnet-simulation-menu-dark")
        compose.onNodeWithTag("target-planned-grade-count-2").performClick()
        detailNode("target-simulation-required-value").assertIsDisplayed()
        screenshot("carnet-target-dark")
    }

    @Test
    fun editingAnOldGradeReturnsToTheSameHistoryPosition() {
        launchCarnet(longHistory = true)
        mainNode("subject-card-math").performClick()
        detailNode("note-card-note-1").performClick()
        screenshot("carnet-edit-grade")
        compose.onNodeWithTag("back-from-grade-editor").performClick()
        compose.onNodeWithTag("note-card-note-1").assertIsDisplayed()
    }

    @Test
    fun targetDraftSurvivesScrollingOutOfView() {
        launchCarnet(longHistory = true)
        mainNode("subject-card-math").performClick()
        detailNode("edit-branch-target-average").performClick()
        detailNode("branch-target-average-input").performTextReplacement("5")
        detailNode("note-card-note-30").assertIsDisplayed()
        detailNode("branch-target-average-input").assertTextEquals("5")
        screenshot("carnet-target-draft")
    }

    @Test
    fun onboardingChoicesStayReadableWithLargeTextInDarkTheme() {
        launchCarnet(largeText = true, initialState = GradeTrackerAppState(themeMode = AppThemeMode.DARK))
        compose.onNodeWithTag("onboarding-import-year-YEAR_3").performScrollTo()
        screenshot("carnet-onboarding-years-large")
        compose.onNodeWithTag("onboarding-option-PHYSICS_AND_APPLICATIONS_OF_MATH").performScrollTo().performClick()
        screenshot("carnet-onboarding-large-dark")
        compose.onNodeWithTag("onboarding-option-PHYSICS_AND_APPLICATIONS_OF_MATH").assertIsSelected()
        compose.onNodeWithTag("onboarding-continue").performScrollTo().performClick()
        compose.onNodeWithTag("confirm-period-selection").assertIsDisplayed()
    }

    @Test
    fun compositeSelectionShowsOnlyTheSelectedSubjectsNotes() {
        launchCarnet(initialState = GradeTrackerAppState(
            selectedOption = InitialOptionChoice.BIOLOGY_CHEMISTRY,
            subjects = listOf(StoredSubject("option", "Biologie et chimie", isInBasket = true, isOptionSubject = true,
                optionChoice = InitialOptionChoice.BIOLOGY_CHEMISTRY, subSubjects = listOf(
                    StoredSubSubject("biology", "Biology", notes = listOf(StoredNote("bio-note", 5.0, AssessmentWeight.FULL, "Cellules", 1L))),
                    StoredSubSubject("chemistry", "Chemistry", notes = listOf(StoredNote("chem-note", 4.0, AssessmentWeight.FULL, "Réactions", 2L)))
                )))
        ))
        mainNode("subject-card-option").performClick()
        detailNode("select-sub-subject-biology").assertIsSelected()
        screenshot("carnet-composite-subjects")
        detailNode("note-card-bio-note").assertIsDisplayed()
        detailNode("select-sub-subject-chemistry").performClick().assertIsSelected()
        detailNode("note-card-chem-note").assertIsDisplayed()
        compose.onNodeWithTag("note-card-bio-note").assertDoesNotExist()
        detailNode("show-add-note-sheet").performClick()
        compose.onNodeWithTag("note-value-input").performScrollTo().assertIsDisplayed()
    }

    private fun launchCarnet(largeText: Boolean = false, dark: Boolean = false, longHistory: Boolean = false, initialState: GradeTrackerAppState? = null) {
        val repository = object : GradeTrackerRepository {
            private var state = initialState ?: GradeTrackerAppState(
                selectedOption = InitialOptionChoice.SPANISH,
                subjects = listOf(
                    StoredSubject("option", "Espagnol", isInBasket = true, isOptionSubject = true, optionChoice = InitialOptionChoice.SPANISH),
                    StoredSubject("math", if (largeText) "Mathématiques et applications des mathématiques" else "Mathématiques", isInBasket = true,
                        notes = if (longHistory) (1..30).map { StoredNote("note-$it", 4.75, AssessmentWeight.FULL, "Évaluation $it", it.toLong()) } else listOf(StoredNote("note-1", 4.75, AssessmentWeight.FULL, "Fonctions", 1L)))
                ),
                nextSubjectSequence = 3,
                nextNoteSequence = 2,
                themeMode = if (dark) AppThemeMode.DARK else AppThemeMode.LIGHT
            )
            override fun load() = state
            override fun save(state: GradeTrackerAppState) { this.state = state }
        }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, if (largeText) 1.8f else 1f)) {
                Box(Modifier.width(if (largeText) 320.dp else 412.dp).fillMaxHeight()) {
                    GradeTrackerApp(repository = repository)
                }
            }
        }
    }

    private fun mainNode(tag: String): SemanticsNodeInteraction {
        compose.onNodeWithTag("main-screen-list").performScrollToNode(hasTestTag(tag))
        return compose.onNodeWithTag(tag, useUnmergedTree = true)
    }

    private fun detailNode(tag: String): SemanticsNodeInteraction {
        compose.onNodeWithTag("branch-detail-list").performScrollToNode(hasTestTag(tag))
        return compose.onNodeWithTag(tag, useUnmergedTree = true)
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
            ?.let(::File) ?: instrumentation.targetContext.getExternalFilesDir(null)!!
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use { output ->
            instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}
