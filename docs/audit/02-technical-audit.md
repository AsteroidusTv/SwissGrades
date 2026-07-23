# Technical Audit

Status: initial static audit pass in progress.

## Findings

### TECH-001: Main ViewModel And Compose Screens Are Oversized

- ID: TECH-001
- Title: Main ViewModel and Compose screens are oversized.
- Category: Architecture and maintainability.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: `app/src/main/java/me/asteroidus/swissgrades/ui/app/GradeTrackerViewModel.kt`, `GradeTrackerBranchDetailScreen.kt`, `GradeTrackerApp.kt`.
- Observed behavior: `GradeTrackerViewModel.kt` is 1,758 lines, `GradeTrackerBranchDetailScreen.kt` is 1,575 lines, and `GradeTrackerApp.kt` is 1,396 lines. The ViewModel handles navigation, persistence, import/export, reset, attachment deletion, period merging, domain mapping, and UI-state formatting.
- Expected behavior: high-risk flows such as import/export, period merging, target simulation state, and attachment lifecycle should be separable enough to test and review independently.
- Reproduction steps where applicable: run `wc -l $(git ls-files) | sort -n | tail -20`.
- Technical or UX impact: future fixes are more likely to create regressions because unrelated concerns are edited in the same files; reviewing UI changes is slower.
- Root cause, when known: feature growth accumulated in a single state holder and large Compose files before all helper modules were extracted.
- Proposed remediation: continue extracting import/merge logic, branch-detail sections, and presentation mapping into focused classes/composables with existing tests preserved.
- Verification method: targeted unit tests for extracted logic plus full app validation; no behavioral diff except the intended fix.
- Status: deferred to a dedicated refactor cycle; disproportionately risky to mix with release-hardening fixes.

### TECH-002: Domain Presentation Mapper Still Owns English Copy

- ID: TECH-002
- Title: Promotion presentation mapper emits English strings from the domain layer.
- Category: Architecture, localization, maintainability.
- Severity: Low.
- Confidence: High.
- Affected files or screens: `app/src/main/java/me/asteroidus/swissgrades/domain/PromotionPresentationMapper.kt`, `app/src/main/java/me/asteroidus/swissgrades/ui/app/GradeTrackerViewModel.kt`, secondary `ui/simulation` screens.
- Observed behavior: before remediation, `PromotionPresentationMapper` returned labels such as `Basket total`, `Not available`, and full blocking messages in English. The main ViewModel localized only status and headline, then checked the English sentinel `Not available` for calculability.
- Expected behavior: domain should expose structured data, and UI-localized copy should be produced at the UI boundary for each active language.
- Reproduction steps where applicable: inspect `PromotionPresentationMapper.map()` and `PromotionPresentation.localized(strings)`.
- Technical or UX impact: localization is brittle and future changes could accidentally surface English in French or break calculability checks.
- Root cause, when known: earlier simulation UI used English presentation strings directly; the main app later added Kotlin localization around part of the output.
- Proposed remediation: replace string sentinel checks with structured nullable/domain values; move visible labels/messages to `AppStrings`.
- Verification method: localization tests that assert French dashboard/promotion messages do not contain English fallback; unit tests for calculability without English literals.
- Implemented remediation:
  - The domain now returns only `PromotionEvaluationResult` and typed statuses/reasons.
  - The main app uses `PromotionDashboardPresenter` to map `PromotionStatus` to localized copy and semantic tone.
  - Calculability derives from the nullable domain `basketTotal`, not a formatted label.
  - Branch-detail tone derives from structured averages/counting state, not localized labels.
  - The detached simulation package owns its unchanged English presentation mapper and presentation models.
- Verification:
  - `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`: passed.
  - `./scripts/release-check.sh`: passed.
  - `./scripts/run-managed-device-tests.sh`: passed, 42 tests, 0 failures.
  - Static search found no presentation models/mapper in `domain` and no remaining `Not available`/localized-status comparisons in the main app flow.
- Status: resolved and verified.

### TECH-003: Secondary Simulation Package Appears Detached From Main App

- ID: TECH-003
- Title: Secondary simulation package appears detached from the launcher app.
- Category: Architecture and product coherence.
- Severity: Low.
- Confidence: Medium.
- Affected files or screens: `app/src/main/java/me/asteroidus/swissgrades/ui/simulation/*`, `app/src/androidTest/java/me/asteroidus/swissgrades/ui/simulation/*`, `app/src/test/java/me/asteroidus/swissgrades/ui/simulation/*`.
- Observed behavior: `MainActivity` launches `me.asteroidus.swissgrades.ui.app.GradeTrackerApp`; the `ui/simulation` package has separate state/persistence and extensive tests but no observed route from the current app.
- Expected behavior: either every maintained UI package is reachable from the product, or legacy code is clearly marked/removed to reduce maintenance cost.
- Reproduction steps where applicable: run `rg -n "ui.simulation|PromotionSummaryScreen|SimulationEditor" app/src/main/java`.
- Technical or UX impact: tests and code may keep an obsolete mental model alive; changes to domain behavior must support two app surfaces.
- Root cause, when known: older simulator implementation remained after the main grade tracker became the primary product.
- Proposed remediation: decide whether to remove the secondary package, move it behind an explicit developer/demo route, or document it as legacy test harness.
- Verification method: dependency search confirms either removal or intentional route/documentation.
- Status: open, product/maintenance decision required.
