# Functional Audit

Status: initial static audit pass in progress.

## Findings

### FUNC-001: Saved Target Average Can Desynchronize From Simulator Input

- ID: FUNC-001
- Title: saved target average can desynchronize from simulator input.
- Category: Functional correctness and UX.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: branch detail target card and grade simulator; `app/src/main/java/me/asteroidus/swissgrades/ui/app/TargetSimulationCard.kt`.
- Observed behavior: `TargetSimulationCard` initializes `targetInput` with `remember(targetKey) { mutableStateOf(initialTargetInput ?: "5.0") }`. When the user edits and saves the branch target on the same subject, `initialTargetInput` changes but `targetKey` does not, so the collapsed simulator can reopen with the previous local value.
- Expected behavior: when the simulator is collapsed and the saved branch target changes, reopening it should start from the saved target average.
- Reproduction steps where applicable: open a branch with no target; save target `5.5`; expand the simulator without leaving the branch; observe that the simulator can still use `5.0`.
- Technical or UX impact: the simulator can answer a different question from the displayed branch target, causing misleading “needed next grade” results.
- Root cause, when known: local Compose state is keyed only by subject id, not by the saved target value or edit lifecycle.
- Proposed remediation: synchronize `targetInput` from `initialTargetInput` when the card is collapsed or when the subject changes, without overwriting active user edits while expanded.
- Verification method: Compose or ViewModel-adjacent test for target edit -> expand simulator; manual check on branch detail.
- Status: resolved and verified.

### FUNC-002: French Blocked Dashboard Status Uses Neutral Tone

- ID: FUNC-002
- Title: French blocked dashboard status uses neutral tone.
- Category: Functional presentation.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: dashboard summary promotion status; `app/src/main/java/me/asteroidus/swissgrades/ui/app/GradeTrackerViewModel.kt`.
- Observed behavior: `String.toDashboardStatusTone()` maps `"Bloque"` to negative but the French localized status is `"Bloqué"`. A blocked French dashboard can therefore display with neutral styling.
- Expected behavior: blocked promotion status should use the negative/warning tone in every supported language.
- Reproduction steps where applicable: create calculable blocked data in French; inspect `summary.statusTone`; it resolves to `NEUTRAL` for `Bloqué`.
- Technical or UX impact: users can miss a negative academic status because color/visual tone no longer matches the label.
- Root cause, when known: string-based status-tone mapping and a missing accent in the French case.
- Proposed remediation: map tone from structured `PromotionStatus` or include the correct localized string as an immediate patch.
- Verification method: unit test for French blocked dashboard state and visual status tone.
- Status: resolved and verified.

### FUNC-003: Promotion Role Assignment Depends On Basket Subject Order

- ID: FUNC-003
- Title: promotion role assignment depends on basket subject order.
- Category: Product coherence and domain modeling.
- Severity: Low.
- Confidence: High.
- Affected files or screens: dashboard promotion summary; `GradeTrackerViewModel.buildPromotionPresentation()`.
- Observed behavior: the first three non-option basket subjects are passed into domain roles named German, French, and Math. Current UI copy intentionally tells users to keep exactly three non-option basket subjects, not to assign specific roles.
- Expected behavior: if roles are semantically important, users should assign them explicitly; if only the basket sum matters, role names should not leak into domain/presentation logic.
- Reproduction steps where applicable: add three arbitrary basket subjects and grades; promotion unlocks, as covered by `manualBasketSubjectsUnlockPromotionWithoutOfficialSubjectNames`.
- Technical or UX impact: low current user impact because main UI does not expose German/French/Math labels for these roles, but the model is confusing and affects secondary simulation messages.
- Root cause, when known: domain model mirrors official basket roles while the current product chose a simpler “exactly three basket branches” workflow.
- Proposed remediation: either introduce explicit role assignment in a future product change or rename internal role concepts for the simplified basket workflow.
- Verification method: domain tests and UI copy review after model decision.
- Status: open, product decision required.
