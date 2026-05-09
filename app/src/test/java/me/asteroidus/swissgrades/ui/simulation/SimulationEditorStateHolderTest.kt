package me.asteroidus.swissgrades.ui.simulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationEditorStateHolderTest {

    @Test
    fun liveSummary_reflectsEnteredGradesInPresentation() {
        val stateHolder = SimulationEditorStateHolder()

        val germanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)
        val frenchEntryId = stateHolder.entryId(BasketBranchRole.FRENCH)
        val mathEntryId = stateHolder.entryId(BasketBranchRole.MATH)
        val optionEntryId = stateHolder.entryId(BasketBranchRole.OPTION)

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.5")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, frenchEntryId, "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, mathEntryId, "4.5")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, optionEntryId, "4.0")

        assertEquals("Promoted", stateHolder.uiState.summary.statusLabel)
        assertEquals("17.00", stateHolder.uiState.summary.basketTotal.valueLabel)
    }

    @Test
    fun liveSummary_multipleGradesInOneBranch_changeTheComputedSummary() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val secondGermanEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, secondGermanEntryId, "6.0")
        stateHolder.onWeightChanged(BasketBranchRole.GERMAN, secondGermanEntryId, GradeWeightUi.HALF)
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")

        assertEquals("16.50", stateHolder.uiState.summary.basketTotal.valueLabel)
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "German" && it.valueLabel == "4.50"
            }
        )
    }

    @Test
    fun liveSummary_differentCoefficients_affectBranchAverageCorrectly() {
        val stateHolder = SimulationEditorStateHolder()

        val germanFirstEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanFirstEntryId, "5.0")
        stateHolder.onWeightChanged(BasketBranchRole.GERMAN, germanFirstEntryId, GradeWeightUi.FULL)
        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val germanSecondEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanSecondEntryId, "4.5")
        stateHolder.onWeightChanged(BasketBranchRole.GERMAN, germanSecondEntryId, GradeWeightUi.HALF)
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")

        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "German" && it.valueLabel == "5.00"
            }
        )
        assertEquals("17.00", stateHolder.uiState.summary.basketTotal.valueLabel)
    }

    @Test
    fun liveSummary_mixedValidAndInvalidGradesInOneBranch_makeEvaluationIncomplete() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.lastEntryId(BasketBranchRole.GERMAN), "4.3")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")

        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("German") })
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
    }

    @Test
    fun liveSummary_mixedValidAndInvalidGradesInOneBranch_doNotProducePartialAverage() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.lastEntryId(BasketBranchRole.GERMAN), "4.3")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")

        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "German" && it.valueLabel == "No average available"
            }
        )
        assertTrue(
            stateHolder.uiState.summary.branchAverages.none {
                it.branchName == "German" && it.valueLabel == "4.00"
            }
        )
    }

    @Test
    fun addRemoveEditBehavior_updatesGradeEntries() {
        val stateHolder = SimulationEditorStateHolder()
        val initialGermanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val secondGermanEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, secondGermanEntryId, "4.5")
        stateHolder.onWeightChanged(BasketBranchRole.GERMAN, secondGermanEntryId, GradeWeightUi.QUARTER)
        stateHolder.removeGradeEntry(BasketBranchRole.GERMAN, initialGermanEntryId)

        val germanEntries = stateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(1, germanEntries.size)
        assertEquals("4.5", germanEntries.first().gradeInput)
        assertEquals(GradeWeightUi.QUARTER, germanEntries.first().weight)
    }

    @Test
    fun gradeEntryIdentifiers_remainStableAcrossAddAndRemoveOperations() {
        val stateHolder = SimulationEditorStateHolder()
        val initialGermanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val secondGermanEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val thirdGermanEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.removeGradeEntry(BasketBranchRole.GERMAN, secondGermanEntryId)

        val germanEntryIds = stateHolder.branchEntries(BasketBranchRole.GERMAN).map { it.entryId }
        assertEquals(listOf(initialGermanEntryId, thirdGermanEntryId), germanEntryIds)
    }

    @Test
    fun removingTheLastGradeEntry_keepsOneEmptyPlaceholderEntry() {
        val stateHolder = SimulationEditorStateHolder()
        val germanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.removeGradeEntry(BasketBranchRole.GERMAN, germanEntryId)

        val germanEntries = stateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(1, germanEntries.size)
        assertEquals("", germanEntries.first().gradeInput)
        assertEquals(GradeWeightUi.FULL, germanEntries.first().weight)
        assertNull(germanEntries.first().errorMessage)
    }

    @Test
    fun correctingTheLastInvalidField_clearsTheGlobalNoticeImmediately() {
        val stateHolder = SimulationEditorStateHolder()
        val germanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.3")
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.25")

        assertNull(stateHolder.uiState.inputNoticeMessage)
        assertNull(stateHolder.branchEntries(BasketBranchRole.GERMAN).first().errorMessage)
    }

    @Test
    fun liveSummary_incompleteEvaluation_whenRequiredBranchHasNoValidGrades() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "")
        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("Option") })
    }

    @Test
    fun liveSummary_acceptsQuarterHalfAndThreeQuarterGrades() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.25")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.5")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.75")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        assertEquals("17.50", stateHolder.uiState.summary.basketTotal.valueLabel)
        assertTrue(stateHolder.uiState.branchInputs.flatMap { it.gradeEntries }.all { it.errorMessage == null })
        assertNull(stateHolder.uiState.inputNoticeMessage)
    }

    @Test
    fun updateSummary_marksInvalidGradeInputInUiState() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.3")

        val germanInput = stateHolder.branchEntries(BasketBranchRole.GERMAN).first()
        assertEquals(INVALID_GRADE_MESSAGE, germanInput.errorMessage)
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
    }

    @Test
    fun liveValidation_keepsFieldInvalidWhenInvalidValueChangesToAnotherInvalidValue() {
        val stateHolder = SimulationEditorStateHolder()
        val germanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.3")
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.1")

        val germanInput = stateHolder.branchEntries(BasketBranchRole.GERMAN).first()
        assertEquals(INVALID_GRADE_MESSAGE, germanInput.errorMessage)
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
    }

    @Test
    fun liveValidation_keepsGlobalInvalidNoticeVisibleWhileAnyFieldRemainsInvalid() {
        val stateHolder = SimulationEditorStateHolder()
        val germanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)
        val mathEntryId = stateHolder.entryId(BasketBranchRole.MATH)

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.3")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, mathEntryId, "4,5")
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.25")

        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
        assertEquals(DECIMAL_SEPARATOR_MESSAGE, stateHolder.branchEntries(BasketBranchRole.MATH).first().errorMessage)
    }

    @Test
    fun initialState_isIncompleteBecauseDataIsMissingWithoutFieldErrors() {
        val stateHolder = SimulationEditorStateHolder()

        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.isNotEmpty())
        assertTrue(stateHolder.uiState.branchInputs.flatMap { it.gradeEntries }.all { it.errorMessage == null })
        assertNull(stateHolder.uiState.inputNoticeMessage)
    }

    @Test
    fun liveSummary_rejectsDecimalCommaWithExplicitMessage() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4,5")

        val germanInput = stateHolder.branchEntries(BasketBranchRole.GERMAN).first()
        assertEquals(DECIMAL_SEPARATOR_MESSAGE, germanInput.errorMessage)
        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
    }

    @Test
    fun liveSummary_invalidFieldStateAndIncompleteSummaryCoexistExplicitly() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4,5")
        val mathInput = stateHolder.branchEntries(BasketBranchRole.MATH).first()
        assertEquals(DECIMAL_SEPARATOR_MESSAGE, mathInput.errorMessage)
        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("Math") })
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
    }

    @Test
    fun liveSummary_regressionForValidInvalidAndMissingInputsRemainsUnchanged() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.3")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "")
        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("Math") })
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("Option") })
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "German" && it.valueLabel == "4.00"
            }
        )
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "French" && it.valueLabel == "4.00"
            }
        )
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "Math" && it.valueLabel == "No average available"
            }
        )
    }

    @Test
    fun liveSummary_weightChangeUpdatesSummaryWithoutManualRefresh() {
        val stateHolder = SimulationEditorStateHolder()
        val germanFirstEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanFirstEntryId, "5.0")
        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val germanSecondEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanSecondEntryId, "2.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        assertEquals("15.50", stateHolder.uiState.summary.basketTotal.valueLabel)

        stateHolder.onWeightChanged(BasketBranchRole.GERMAN, germanSecondEntryId, GradeWeightUi.HALF)

        assertEquals("16.00", stateHolder.uiState.summary.basketTotal.valueLabel)
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "German" && it.valueLabel == "4.00"
            }
        )
    }

    @Test
    fun liveSummary_correctingInvalidInputRestoresCalculableSummaryImmediately() {
        val stateHolder = SimulationEditorStateHolder()
        val germanEntryId = stateHolder.entryId(BasketBranchRole.GERMAN)

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.3")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")

        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("German") })

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, germanEntryId, "4.5")

        assertEquals("Promoted", stateHolder.uiState.summary.statusLabel)
        assertEquals("16.50", stateHolder.uiState.summary.basketTotal.valueLabel)
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "German" && it.valueLabel == "4.50"
            }
        )
    }

    @Test
    fun stateHolder_initialFlowDoesNotDependOnHardcodedScenarios() {
        val stateHolder = SimulationEditorStateHolder()

        assertEquals(4, stateHolder.uiState.branchInputs.size)
        assertEquals(
            listOf("German", "French", "Math", "Option"),
            stateHolder.uiState.branchInputs.map { it.branch.branchName }
        )
        assertTrue(stateHolder.uiState.branchInputs.all { it.gradeEntries.size == 1 })
        assertTrue(stateHolder.uiState.branchInputs.flatMap { it.gradeEntries }.all { it.gradeInput.isEmpty() })
        assertTrue(stateHolder.uiState.branchInputs.flatMap { it.gradeEntries }.all { it.errorMessage == null })
    }

    @Test
    fun persistedValidEditorState_isRestoredCorrectly() {
        val persistence = FakeSimulationEditorPersistence(
            loadedState = PersistedSimulationEditorState(
                branchInputs = listOf(
                    persistedBranchInput(BasketBranchRole.GERMAN, PersistedGradeEntry("german-entry-7", "4.5", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-3", "4.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-2", "4.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-5", "4.0", GradeWeightUi.FULL))
                )
            )
        )

        val stateHolder = SimulationEditorStateHolder(persistence = persistence)

        assertEquals("Promoted", stateHolder.uiState.summary.statusLabel)
        assertEquals("16.50", stateHolder.uiState.summary.basketTotal.valueLabel)
        assertEquals("german-entry-7", stateHolder.branchEntries(BasketBranchRole.GERMAN).first().entryId)
        assertEquals("4.5", stateHolder.branchEntries(BasketBranchRole.GERMAN).first().gradeInput)
    }

    @Test
    fun persistedMultipleGradeEntries_restoreWithStableIdentities() {
        val persistence = FakeSimulationEditorPersistence(
            loadedState = PersistedSimulationEditorState(
                branchInputs = listOf(
                    persistedBranchInput(
                        BasketBranchRole.GERMAN,
                        PersistedGradeEntry("german-entry-4", "5.0", GradeWeightUi.FULL),
                        PersistedGradeEntry("german-entry-8", "4.5", GradeWeightUi.HALF)
                    ),
                    persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "4.0", GradeWeightUi.FULL))
                )
            )
        )

        val stateHolder = SimulationEditorStateHolder(persistence = persistence)

        val germanEntries = stateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(listOf("german-entry-4", "german-entry-8"), germanEntries.map { it.entryId })
        assertEquals(listOf("5.0", "4.5"), germanEntries.map { it.gradeInput })
        assertEquals(listOf(GradeWeightUi.FULL, GradeWeightUi.HALF), germanEntries.map { it.weight })
    }

    @Test
    fun restoredNonContiguousIds_generateNextIdAboveRestoredMaximum() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(
                            BasketBranchRole.GERMAN,
                            PersistedGradeEntry("german-entry-2", "4.0", GradeWeightUi.FULL),
                            PersistedGradeEntry("german-entry-7", "5.0", GradeWeightUi.HALF)
                        ),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "", GradeWeightUi.FULL))
                    )
                )
            )
        )

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)

        assertEquals("german-entry-8", stateHolder.lastEntryId(BasketBranchRole.GERMAN))
    }

    @Test
    fun restoredIdsWithGap_addNewEntryWithoutCollision() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(
                            BasketBranchRole.GERMAN,
                            PersistedGradeEntry("german-entry-1", "4.0", GradeWeightUi.FULL),
                            PersistedGradeEntry("german-entry-3", "5.0", GradeWeightUi.FULL)
                        ),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "", GradeWeightUi.FULL))
                    )
                )
            )
        )

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)

        val germanEntryIds = stateHolder.branchEntries(BasketBranchRole.GERMAN).map { it.entryId }
        assertEquals(listOf("german-entry-1", "german-entry-3", "german-entry-4"), germanEntryIds)
    }

    @Test
    fun restoredPerBranchMaxima_keepSequencingIndependentPerBranch() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(BasketBranchRole.GERMAN, PersistedGradeEntry("german-entry-5", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-2", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(
                            BasketBranchRole.MATH,
                            PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL),
                            PersistedGradeEntry("math-entry-9", "5.0", GradeWeightUi.HALF)
                        ),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-4", "4.0", GradeWeightUi.FULL))
                    )
                )
            )
        )

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        stateHolder.addGradeEntry(BasketBranchRole.FRENCH)
        stateHolder.addGradeEntry(BasketBranchRole.MATH)
        stateHolder.addGradeEntry(BasketBranchRole.OPTION)

        assertEquals("german-entry-6", stateHolder.lastEntryId(BasketBranchRole.GERMAN))
        assertEquals("french-entry-3", stateHolder.lastEntryId(BasketBranchRole.FRENCH))
        assertEquals("math-entry-10", stateHolder.lastEntryId(BasketBranchRole.MATH))
        assertEquals("option-entry-5", stateHolder.lastEntryId(BasketBranchRole.OPTION))
    }

    @Test
    fun persistedPlaceholderEntry_isRestoredAfterDeletingLastVisibleEntry() {
        val persistence = FakeSimulationEditorPersistence()
        val originalStateHolder = SimulationEditorStateHolder(persistence = persistence)
        val germanEntryId = originalStateHolder.entryId(BasketBranchRole.GERMAN)

        originalStateHolder.removeGradeEntry(BasketBranchRole.GERMAN, germanEntryId)

        val restoredStateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(loadedState = persistence.savedState)
        )

        val germanEntries = restoredStateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(1, germanEntries.size)
        assertTrue(germanEntries.first().entryId.startsWith("german-entry-"))
        assertEquals("", germanEntries.first().gradeInput)
        assertEquals(GradeWeightUi.FULL, germanEntries.first().weight)
    }

    @Test
    fun restoredState_yieldsTheSameSummaryAsBeforePersistence() {
        val persistence = FakeSimulationEditorPersistence()
        val originalStateHolder = SimulationEditorStateHolder(persistence = persistence)

        originalStateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, originalStateHolder.entryId(BasketBranchRole.GERMAN), "5.0")
        originalStateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        originalStateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, originalStateHolder.lastEntryId(BasketBranchRole.GERMAN), "4.5")
        originalStateHolder.onWeightChanged(BasketBranchRole.GERMAN, originalStateHolder.lastEntryId(BasketBranchRole.GERMAN), GradeWeightUi.HALF)
        originalStateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, originalStateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        originalStateHolder.onGradeInputChanged(BasketBranchRole.MATH, originalStateHolder.entryId(BasketBranchRole.MATH), "4.0")
        originalStateHolder.onGradeInputChanged(BasketBranchRole.OPTION, originalStateHolder.entryId(BasketBranchRole.OPTION), "4.0")

        val restoredStateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(loadedState = persistence.savedState)
        )

        assertEquals(originalStateHolder.uiState.summary.statusLabel, restoredStateHolder.uiState.summary.statusLabel)
        assertEquals(originalStateHolder.uiState.summary.basketTotal.valueLabel, restoredStateHolder.uiState.summary.basketTotal.valueLabel)
        assertEquals(originalStateHolder.uiState.summary.branchAverages, restoredStateHolder.uiState.summary.branchAverages)
    }

    @Test
    fun invalidPersistedInput_isRestoredAsIsAndRevalidatedImmediately() {
        val persistence = FakeSimulationEditorPersistence(
            loadedState = PersistedSimulationEditorState(
                branchInputs = listOf(
                    persistedBranchInput(BasketBranchRole.GERMAN, PersistedGradeEntry("german-entry-1", "4.3", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "4.0", GradeWeightUi.FULL))
                )
            )
        )

        val stateHolder = SimulationEditorStateHolder(persistence = persistence)

        assertEquals("4.3", stateHolder.branchEntries(BasketBranchRole.GERMAN).first().gradeInput)
        assertEquals(INVALID_GRADE_MESSAGE, stateHolder.branchEntries(BasketBranchRole.GERMAN).first().errorMessage)
        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("German") })
        assertEquals(
            "Some grades are invalid. Fix the highlighted fields to continue editing.",
            stateHolder.uiState.inputNoticeMessage
        )
    }

    @Test
    fun stateHolder_startsFromPersistedDataInsteadOfHardcodedDefaults_whenAvailable() {
        val persistence = FakeSimulationEditorPersistence(
            loadedState = PersistedSimulationEditorState(
                branchInputs = listOf(
                    persistedBranchInput(BasketBranchRole.GERMAN, PersistedGradeEntry("german-entry-9", "5.0", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-9", "", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-9", "", GradeWeightUi.FULL)),
                    persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-9", "", GradeWeightUi.FULL))
                )
            )
        )

        val stateHolder = SimulationEditorStateHolder(persistence = persistence)

        assertEquals("german-entry-9", stateHolder.entryId(BasketBranchRole.GERMAN))
        assertEquals("5.0", stateHolder.branchEntries(BasketBranchRole.GERMAN).first().gradeInput)
        assertFalse(stateHolder.uiState.branchInputs.flatMap { it.gradeEntries }.all { it.gradeInput.isEmpty() })
    }

    @Test
    fun restoringEmptyPersistedGradeEntries_replacesBranchWithSinglePlaceholder() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(BasketBranchRole.GERMAN),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "4.0", GradeWeightUi.FULL))
                    )
                )
            )
        )

        val germanEntries = stateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(1, germanEntries.size)
        assertEquals("german-entry-1", germanEntries.first().entryId)
        assertEquals("", germanEntries.first().gradeInput)
        assertEquals(GradeWeightUi.FULL, germanEntries.first().weight)
    }

    @Test
    fun restoringMalformedEntryIds_normalizesThemDeterministically() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(
                            BasketBranchRole.GERMAN,
                            PersistedGradeEntry("broken-id", "4.5", GradeWeightUi.HALF),
                            PersistedGradeEntry("german-entry-3", "5.0", GradeWeightUi.FULL)
                        ),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "", GradeWeightUi.FULL))
                    )
                )
            )
        )

        val germanEntries = stateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(listOf("german-entry-4", "german-entry-3"), germanEntries.map { it.entryId })
        assertEquals(listOf("4.5", "5.0"), germanEntries.map { it.gradeInput })
        assertEquals(listOf(GradeWeightUi.HALF, GradeWeightUi.FULL), germanEntries.map { it.weight })
    }

    @Test
    fun restoringPartiallyCorruptedState_keepsOtherBranchesValidAndLoadsSafely() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(BasketBranchRole.GERMAN),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-8", "4.5", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-oops", "4.0", GradeWeightUi.HALF)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-2", "4.0", GradeWeightUi.FULL))
                    )
                )
            )
        )

        assertEquals("french-entry-8", stateHolder.branchEntries(BasketBranchRole.FRENCH).first().entryId)
        assertEquals("4.5", stateHolder.branchEntries(BasketBranchRole.FRENCH).first().gradeInput)
        assertEquals("math-entry-1", stateHolder.branchEntries(BasketBranchRole.MATH).first().entryId)
        assertEquals("4.0", stateHolder.branchEntries(BasketBranchRole.MATH).first().gradeInput)
        assertEquals(1, stateHolder.branchEntries(BasketBranchRole.GERMAN).size)
    }

    @Test
    fun addingAfterMalformedRestore_stillGeneratesUniqueIds() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(
                            BasketBranchRole.GERMAN,
                            PersistedGradeEntry("broken-id", "4.0", GradeWeightUi.FULL),
                            PersistedGradeEntry("german-entry-3", "5.0", GradeWeightUi.FULL)
                        ),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "", GradeWeightUi.FULL))
                    )
                )
            )
        )

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)

        assertEquals(
            listOf("german-entry-4", "german-entry-3", "german-entry-5"),
            stateHolder.branchEntries(BasketBranchRole.GERMAN).map { it.entryId }
        )
    }

    @Test
    fun addEditRemoveBehavior_stillWorksAfterRestore() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(BasketBranchRole.GERMAN, PersistedGradeEntry("german-entry-4", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.FRENCH, PersistedGradeEntry("french-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.MATH, PersistedGradeEntry("math-entry-1", "", GradeWeightUi.FULL)),
                        persistedBranchInput(BasketBranchRole.OPTION, PersistedGradeEntry("option-entry-1", "", GradeWeightUi.FULL))
                    )
                )
            )
        )

        stateHolder.addGradeEntry(BasketBranchRole.GERMAN)
        val addedEntryId = stateHolder.lastEntryId(BasketBranchRole.GERMAN)
        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, addedEntryId, "4.5")
        stateHolder.onWeightChanged(BasketBranchRole.GERMAN, addedEntryId, GradeWeightUi.QUARTER)
        stateHolder.removeGradeEntry(BasketBranchRole.GERMAN, "german-entry-4")

        val germanEntries = stateHolder.branchEntries(BasketBranchRole.GERMAN)
        assertEquals(listOf("german-entry-5"), germanEntries.map { it.entryId })
        assertEquals("4.5", germanEntries.first().gradeInput)
        assertEquals(GradeWeightUi.QUARTER, germanEntries.first().weight)
    }

    @Test
    fun customSubjectWithGrades_affectsPromotionPoints() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        assertEquals("0.00", stateHolder.uiState.summary.promotionPointsTotal.valueLabel)

        stateHolder.onCustomSubjectNameInputChanged("History")
        stateHolder.addCustomSubject()
        stateHolder.onGradeInputChanged(stateHolder.customBranch("History"), stateHolder.entryId("History"), "5.0")

        assertEquals("+1.00", stateHolder.uiState.summary.promotionPointsTotal.valueLabel)
        assertTrue(stateHolder.uiState.summary.branchAverages.any { it.branchName == "History" && it.valueLabel == "5.00" })
    }

    @Test
    fun emptyCustomSubject_doesNotForceIncompleteAndContributesZeroPoints() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        stateHolder.onCustomSubjectNameInputChanged("History")
        stateHolder.addCustomSubject()

        assertEquals("Promoted", stateHolder.uiState.summary.statusLabel)
        assertEquals("0.00", stateHolder.uiState.summary.promotionPointsTotal.valueLabel)
        assertEquals("16.00", stateHolder.uiState.summary.basketTotal.valueLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.none { it.contains("History") })
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "History" && it.valueLabel == "Not evaluated" && it.detailLabel == "Optional subject left empty."
            }
        )
    }

    @Test
    fun invalidCustomSubject_remainsNonCalculableInsteadOfUsingEmptyOptionalPresentation() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        stateHolder.onCustomSubjectNameInputChanged("History")
        stateHolder.addCustomSubject()
        stateHolder.onGradeInputChanged(stateHolder.customBranch("History"), stateHolder.entryId("History"), "4.3")

        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("History") })
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "History" && it.valueLabel == "No average available"
            }
        )
        assertTrue(
            stateHolder.uiState.summary.branchAverages.none {
                it.branchName == "History" && it.valueLabel == "Not evaluated"
            }
        )
    }

    @Test
    fun removingCustomSubject_removesItsImpactFromSummary() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.GERMAN, stateHolder.entryId(BasketBranchRole.GERMAN), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.FRENCH, stateHolder.entryId(BasketBranchRole.FRENCH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.MATH, stateHolder.entryId(BasketBranchRole.MATH), "4.0")
        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        stateHolder.onCustomSubjectNameInputChanged("History")
        stateHolder.addCustomSubject()
        stateHolder.onGradeInputChanged(stateHolder.customBranch("History"), stateHolder.entryId("History"), "5.0")
        assertEquals("+1.00", stateHolder.uiState.summary.promotionPointsTotal.valueLabel)

        stateHolder.removeCustomSubject(stateHolder.customBranch("History").branchId)

        assertEquals("0.00", stateHolder.uiState.summary.promotionPointsTotal.valueLabel)
        assertTrue(stateHolder.uiState.summary.branchAverages.none { it.branchName == "History" })
    }

    @Test
    fun persistedState_withCustomSubjects_restoresThemCorrectly() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(EditorBranchKey.GERMAN, PersistedGradeEntry("german-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(EditorBranchKey.FRENCH, PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(EditorBranchKey.MATH, PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(EditorBranchKey.OPTION, PersistedGradeEntry("option-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedCustomBranchInput("custom-subject-2", "History", PersistedGradeEntry("custom-subject-2-entry-2", "5.0", GradeWeightUi.HALF))
                    )
                )
            )
        )

        assertTrue(stateHolder.uiState.branchInputs.any { it.branch.branchName == "History" && !it.branch.isBasket })
        assertEquals("custom-subject-2-entry-2", stateHolder.entryId("History"))
        assertEquals("+1.00", stateHolder.uiState.summary.promotionPointsTotal.valueLabel)
    }

    @Test
    fun addCustomSubject_addsItToTheEditor() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onCustomSubjectNameInputChanged("Literature")
        stateHolder.addCustomSubject()

        assertTrue(stateHolder.uiState.branchInputs.any { it.branch.branchName == "Literature" && !it.branch.isBasket })
    }

    @Test
    fun emptyCustomSubjectName_isRejected() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onCustomSubjectNameInputChanged("   ")

        assertEquals(INVALID_CUSTOM_SUBJECT_NAME_MESSAGE, stateHolder.uiState.customSubjectNameErrorMessage)
        stateHolder.addCustomSubject()
        assertTrue(stateHolder.uiState.branchInputs.none { !it.branch.isBasket })
    }

    @Test
    fun duplicateCustomSubjectName_isRejectedCaseInsensitively() {
        val stateHolder = SimulationEditorStateHolder()

        stateHolder.onCustomSubjectNameInputChanged("History")
        stateHolder.addCustomSubject()
        stateHolder.onCustomSubjectNameInputChanged("history")

        assertEquals(DUPLICATE_CUSTOM_SUBJECT_NAME_MESSAGE, stateHolder.uiState.customSubjectNameErrorMessage)
        stateHolder.addCustomSubject()
        assertEquals(1, stateHolder.uiState.branchInputs.count { !it.branch.isBasket })
    }

    @Test
    fun compositeBiologyChemistry_computesOptionAverageFromBothSubSubjects() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.BIOLOGY_CHEMISTRY)
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.BIOLOGY, stateHolder.entryId(OptionSubSubjectKey.BIOLOGY), "5.5")
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.CHEMISTRY, stateHolder.entryId(OptionSubSubjectKey.CHEMISTRY), "3.5")

        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "Option" && it.valueLabel == "4.50"
            }
        )
        assertEquals("16.50", stateHolder.uiState.summary.basketTotal.valueLabel)
    }

    @Test
    fun compositePhysicsAndApplicationsOfMathematics_computesOptionAverageFromBothSubSubjects() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATHEMATICS)
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.PHYSICS, stateHolder.entryId(OptionSubSubjectKey.PHYSICS), "5.0")
        stateHolder.onCompositeOptionGradeInputChanged(
            OptionSubSubjectKey.APPLICATIONS_OF_MATHEMATICS,
            stateHolder.entryId(OptionSubSubjectKey.APPLICATIONS_OF_MATHEMATICS),
            "4.0"
        )

        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "Option" && it.valueLabel == "4.50"
            }
        )
        assertEquals("16.50", stateHolder.uiState.summary.basketTotal.valueLabel)
    }

    @Test
    fun compositeEconomicsAndLaw_computesOptionAverageFromBothSubSubjects() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.ECONOMICS_AND_LAW)
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.ECONOMICS, stateHolder.entryId(OptionSubSubjectKey.ECONOMICS), "5.0")
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.LAW, stateHolder.entryId(OptionSubSubjectKey.LAW), "4.0")

        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "Option" && it.valueLabel == "4.50"
            }
        )
        assertEquals("16.50", stateHolder.uiState.summary.basketTotal.valueLabel)
    }

    @Test
    fun compositeOption_withOneEmptySubSubject_isIncomplete() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.BIOLOGY_CHEMISTRY)
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.BIOLOGY, stateHolder.entryId(OptionSubSubjectKey.BIOLOGY), "5.0")

        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("Option") })
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "Option" && it.valueLabel == "No average available"
            }
        )
    }

    @Test
    fun compositeOption_withInvalidSubSubjectEntry_isIncomplete() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.PHYSICS_AND_APPLICATIONS_OF_MATHEMATICS)
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.PHYSICS, stateHolder.entryId(OptionSubSubjectKey.PHYSICS), "4.3")
        stateHolder.onCompositeOptionGradeInputChanged(
            OptionSubSubjectKey.APPLICATIONS_OF_MATHEMATICS,
            stateHolder.entryId(OptionSubSubjectKey.APPLICATIONS_OF_MATHEMATICS),
            "5.0"
        )

        assertEquals("Incomplete", stateHolder.uiState.summary.statusLabel)
        assertEquals(INVALID_GRADE_MESSAGE, stateHolder.subSubjectEntries(OptionSubSubjectKey.PHYSICS).first().errorMessage)
        assertTrue(stateHolder.uiState.summary.missingDataMessages.any { it.contains("Option") })
    }

    @Test
    fun switchingOptionMode_preservesSimpleAndCompositeDataPerMode() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onGradeInputChanged(BasketBranchRole.OPTION, stateHolder.entryId(BasketBranchRole.OPTION), "4.0")
        stateHolder.onOptionModeChanged(OptionModeUi.COMPOSITE)
        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.BIOLOGY_CHEMISTRY)
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.BIOLOGY, stateHolder.entryId(OptionSubSubjectKey.BIOLOGY), "5.0")
        stateHolder.onCompositeOptionGradeInputChanged(OptionSubSubjectKey.CHEMISTRY, stateHolder.entryId(OptionSubSubjectKey.CHEMISTRY), "4.0")

        stateHolder.onOptionModeChanged(OptionModeUi.SIMPLE)
        assertEquals("4.0", stateHolder.branchEntries(BasketBranchRole.OPTION).first().gradeInput)

        stateHolder.onOptionModeChanged(OptionModeUi.COMPOSITE)
        assertEquals("5.0", stateHolder.subSubjectEntries(OptionSubSubjectKey.BIOLOGY).first().gradeInput)
        assertEquals("4.0", stateHolder.subSubjectEntries(OptionSubSubjectKey.CHEMISTRY).first().gradeInput)
    }

    @Test
    fun switchingCompositeOptionType_resetsIncompatibleSubSubjectState() {
        val stateHolder = compositeReadyStateHolder()

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.BIOLOGY_CHEMISTRY)
        stateHolder.onCompositeOptionGradeInputChanged(
            OptionSubSubjectKey.BIOLOGY,
            stateHolder.entryId(OptionSubSubjectKey.BIOLOGY),
            "5.0"
        )
        stateHolder.onCompositeOptionGradeInputChanged(
            OptionSubSubjectKey.CHEMISTRY,
            stateHolder.entryId(OptionSubSubjectKey.CHEMISTRY),
            "4.0"
        )

        stateHolder.onCompositeOptionChanged(CompositeOptionChoice.ECONOMICS_AND_LAW)

        assertEquals(CompositeOptionChoice.ECONOMICS_AND_LAW, stateHolder.uiState.optionEditor.compositeOption)
        assertEquals(listOf(OptionSubSubjectKey.ECONOMICS, OptionSubSubjectKey.LAW), stateHolder.uiState.optionEditor.compositeSubSubjects.map { it.key })
        assertEquals("", stateHolder.subSubjectEntries(OptionSubSubjectKey.ECONOMICS).first().gradeInput)
        assertEquals("", stateHolder.subSubjectEntries(OptionSubSubjectKey.LAW).first().gradeInput)
    }

    @Test
    fun persistedCompositeOption_isRestoredCorrectly() {
        val stateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(
                loadedState = PersistedSimulationEditorState(
                    branchInputs = listOf(
                        persistedBranchInput(EditorBranchKey.GERMAN, PersistedGradeEntry("german-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(EditorBranchKey.FRENCH, PersistedGradeEntry("french-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(EditorBranchKey.MATH, PersistedGradeEntry("math-entry-1", "4.0", GradeWeightUi.FULL)),
                        persistedBranchInput(EditorBranchKey.OPTION, PersistedGradeEntry("option-entry-1", "3.0", GradeWeightUi.FULL))
                    ),
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
        )

        assertEquals(OptionModeUi.COMPOSITE, stateHolder.uiState.optionEditor.mode)
        assertEquals(CompositeOptionChoice.ECONOMICS_AND_LAW, stateHolder.uiState.optionEditor.compositeOption)
        assertEquals("option-economics-entry-2", stateHolder.entryId(OptionSubSubjectKey.ECONOMICS))
        assertEquals("option-law-entry-4", stateHolder.entryId(OptionSubSubjectKey.LAW))
        assertTrue(
            stateHolder.uiState.summary.branchAverages.any {
                it.branchName == "Option" && it.valueLabel == "4.50"
            }
        )
    }

    @Test
    fun persistedHiddenSimpleAndCompositeOptionState_survivesRestart() {
        val persistence = FakeSimulationEditorPersistence()
        val originalStateHolder = SimulationEditorStateHolder(persistence = persistence)

        originalStateHolder.onGradeInputChanged(
            BasketBranchRole.OPTION,
            originalStateHolder.entryId(BasketBranchRole.OPTION),
            "4.0"
        )
        originalStateHolder.onOptionModeChanged(OptionModeUi.COMPOSITE)
        originalStateHolder.onCompositeOptionChanged(CompositeOptionChoice.BIOLOGY_CHEMISTRY)
        originalStateHolder.onCompositeOptionGradeInputChanged(
            OptionSubSubjectKey.BIOLOGY,
            originalStateHolder.entryId(OptionSubSubjectKey.BIOLOGY),
            "5.0"
        )
        originalStateHolder.onCompositeOptionGradeInputChanged(
            OptionSubSubjectKey.CHEMISTRY,
            originalStateHolder.entryId(OptionSubSubjectKey.CHEMISTRY),
            "4.0"
        )
        originalStateHolder.onOptionModeChanged(OptionModeUi.SIMPLE)

        val restoredStateHolder = SimulationEditorStateHolder(
            persistence = FakeSimulationEditorPersistence(loadedState = persistence.savedState)
        )

        assertEquals(OptionModeUi.SIMPLE, restoredStateHolder.uiState.optionEditor.mode)
        assertEquals("4.0", restoredStateHolder.branchEntries(BasketBranchRole.OPTION).first().gradeInput)
        restoredStateHolder.onOptionModeChanged(OptionModeUi.COMPOSITE)
        assertEquals("5.0", restoredStateHolder.subSubjectEntries(OptionSubSubjectKey.BIOLOGY).first().gradeInput)
        assertEquals("4.0", restoredStateHolder.subSubjectEntries(OptionSubSubjectKey.CHEMISTRY).first().gradeInput)
    }

    private fun SimulationEditorStateHolder.entryId(role: BasketBranchRole): String {
        return branchEntries(role).first().entryId
    }

    private fun SimulationEditorStateHolder.lastEntryId(role: BasketBranchRole): String {
        return branchEntries(role).last().entryId
    }

    private fun SimulationEditorStateHolder.branchEntries(role: BasketBranchRole): List<GradeEntryUiState> {
        return uiState.branchInputs.first { it.branch.basketRole == role }.gradeEntries
    }

    private fun SimulationEditorStateHolder.entryId(subjectName: String): String {
        return branchEntries(subjectName).first().entryId
    }

    private fun SimulationEditorStateHolder.branchEntries(subjectName: String): List<GradeEntryUiState> {
        return uiState.branchInputs.first { it.branch.branchName == subjectName && !it.branch.isBasket }.gradeEntries
    }

    private fun SimulationEditorStateHolder.customBranch(subjectName: String): BranchIdentifier {
        return uiState.branchInputs.first { it.branch.branchName == subjectName && !it.branch.isBasket }.branch
    }

    private fun SimulationEditorStateHolder.entryId(key: OptionSubSubjectKey): String {
        return subSubjectEntries(key).first().entryId
    }

    private fun SimulationEditorStateHolder.subSubjectEntries(key: OptionSubSubjectKey): List<GradeEntryUiState> {
        return uiState.optionEditor.compositeSubSubjects.first { it.key == key }.gradeEntries
    }

    private fun compositeReadyStateHolder(): SimulationEditorStateHolder {
        return SimulationEditorStateHolder().apply {
            onGradeInputChanged(BasketBranchRole.GERMAN, entryId(BasketBranchRole.GERMAN), "4.0")
            onGradeInputChanged(BasketBranchRole.FRENCH, entryId(BasketBranchRole.FRENCH), "4.0")
            onGradeInputChanged(BasketBranchRole.MATH, entryId(BasketBranchRole.MATH), "4.0")
            onOptionModeChanged(OptionModeUi.COMPOSITE)
        }
    }

    private fun persistedBranchInput(
        branchKey: EditorBranchKey,
        vararg gradeEntries: PersistedGradeEntry
    ): PersistedBranchInput {
        return PersistedBranchInput(
            branchId = branchKey.name.lowercase(),
            branchName = branchKey.branchName,
            branchKey = branchKey,
            gradeEntries = gradeEntries.toList()
        )
    }

    private fun persistedCustomBranchInput(
        branchId: String,
        branchName: String,
        vararg gradeEntries: PersistedGradeEntry
    ): PersistedBranchInput {
        return PersistedBranchInput(
            branchId = branchId,
            branchName = branchName,
            gradeEntries = gradeEntries.toList()
        )
    }

    private fun persistedBranchInput(
        role: BasketBranchRole,
        vararg gradeEntries: PersistedGradeEntry
    ): PersistedBranchInput {
        return persistedBranchInput(EditorBranchKey.fromBasketRole(role), *gradeEntries)
    }

    private class FakeSimulationEditorPersistence(
        loadedState: PersistedSimulationEditorState? = null
    ) : SimulationEditorPersistence {
        private val loadedState = loadedState
        var savedState: PersistedSimulationEditorState? = null
            private set

        override fun load(): PersistedSimulationEditorState? = loadedState

        override fun save(state: PersistedSimulationEditorState) {
            savedState = state
        }
    }
}
