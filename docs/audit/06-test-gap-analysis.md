# Test Gap Analysis

Status: initial structure created. Findings will be updated as audit cycles progress.

## Current Known Coverage

- JVM domain tests cover grade calculation, promotion evaluation, presentation mapping, and target simulation.
- JVM app tests cover repository serialization, localization basics, ViewModel behavior, PlusPoints import, and secondary simulation state.
- Instrumented tests cover selected app flows, backup coordinator behavior, repository serialization on Android, and the secondary simulation UI.

## Gaps

### TEST-001: Missing Regression Coverage For Target Synchronization

- ID: TEST-001
- Title: missing regression coverage for target synchronization.
- Category: Testing and quality controls.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: branch target card, `TargetSimulationCard`.
- Observed behavior: existing tests cover target persistence and simulator math separately, but not target edit -> simulator initial input synchronization.
- Expected behavior: a regression test should fail if the simulator uses a stale saved target.
- Reproduction steps where applicable: inspect `GradeTrackerViewModelTest` and `TargetSimulationCalculatorTest`; no UI-level sync test exists.
- Technical or UX impact: a core “what grade do I need?” feature can regress without test failure.
- Root cause, when known: target storage and simulator UI state live in separate components.
- Proposed remediation: add a Compose instrumented test or extract a small state policy that can be unit-tested.
- Verification method: test fails before FUNC-001 fix and passes after.
- Status: resolved and verified.

### TEST-002: Missing Regression Coverage For French Blocked Status Tone

- ID: TEST-002
- Title: missing regression coverage for French blocked status tone.
- Category: Testing and quality controls.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: dashboard summary tone mapping.
- Observed behavior: tests assert the French blocked/promoted label in some flows, but not the `DashboardStatusTone` produced for French blocked status.
- Expected behavior: tests should verify semantic tone, not only visible text.
- Reproduction steps where applicable: inspect `GradeTrackerViewModelTest` and `GradeTrackerAppInstrumentedTest`.
- Technical or UX impact: visual severity can break while labels remain correct.
- Root cause, when known: tone is derived from localized text instead of structured status.
- Proposed remediation: add unit test coverage for a blocked French dashboard state.
- Verification method: targeted JVM test in `GradeTrackerViewModelTest`.
- Status: resolved and verified.

### TEST-003: Missing Negative Import Tests

- ID: TEST-003
- Title: missing negative import tests.
- Category: Testing, security, privacy.
- Severity: High.
- Confidence: High.
- Affected files or screens: PlusPoints import and backup import.
- Observed behavior: tests cover valid PlusPoints parsing, backup round-trip, and missing backup attachment files; they do not cover malicious XML parser features, oversized XML, absolute attachment paths, `../` paths, or IDs with path separators.
- Expected behavior: untrusted import surfaces should have negative tests for malformed and hostile inputs.
- Reproduction steps where applicable: inspect `PlusPointsImportCoordinatorTest` and `AppBackupCoordinatorInstrumentedTest`.
- Technical or UX impact: security and data-integrity regressions can ship without local or CI failures.
- Root cause, when known: tests were built around expected exports, not adversarial files.
- Proposed remediation: add JVM XML parser tests and instrumented backup path validation tests.
- Verification method: tests fail before hardening and pass after fixes.
- Status: resolved and verified.

### TEST-004: Main App Instrumented Coverage Is Very Thin

- ID: TEST-004
- Title: main app instrumented coverage is very thin.
- Category: Testing and quality controls.
- Severity: Low.
- Confidence: High.
- Affected files or screens: app critical journeys.
- Observed behavior: `GradeTrackerAppInstrumentedTest` currently covers a visible promotion label and minimal restored composite option launch behavior. Most end-to-end flows are covered only at ViewModel/unit level.
- Expected behavior: critical mobile workflows should have at least smoke tests: onboarding, period selection, add/edit/delete grade, target edit/simulator expand, settings reset confirmation, backup/PlusPoints confirmation states.
- Reproduction steps where applicable: inspect `app/src/androidTest/java/me/asteroidus/swissgrades/ui/app/GradeTrackerAppInstrumentedTest.kt`.
- Technical or UX impact: UI regressions in Compose layout, semantics, buttons, and dialogs may not be caught.
- Root cause, when known: ViewModel tests were expanded faster than instrumented UI tests.
- Proposed remediation: add a small, stable set of critical-flow instrumented tests instead of broad brittle visual coverage.
- Verification method: managed-device tests pass in CI.
- Status: open.

## Execution Blockers

- ID: TEST-EXEC-001
- Title: Current sandbox cannot run Gradle validation tasks.
- Category: Testing and quality controls.
- Severity: Medium.
- Confidence: High.
- Affected files or screens: repository validation workflow; not an application screen.
- Observed behavior: Gradle test execution initially failed before task execution because the earlier sandbox could not provide the expected JDK helper path and then could not initialize Gradle file-lock networking after redirecting `GRADLE_USER_HOME`.
- Expected behavior: `testDebugUnitTest`, assemble, lint, and release-check commands should be runnable from a clean developer environment or CI.
- Reproduction steps: run `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`; then run `GRADLE_USER_HOME="$PWD/.gradle" ./gradlew testDebugUnitTest --no-parallel`.
- Technical or UX impact: this session cannot prove test/build health locally, so remediation verification must rely on static review until CI or a normal shell runs the commands.
- Root cause, when known: current execution sandbox restricts access to the default Gradle cache and appears to prevent Gradle's file-lock contention service from resolving a wildcard IP.
- Proposed remediation: use JDK 21 and a normal Gradle cache/network-capable environment for final validation.
- Verification method: run `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`, `./scripts/release-check.sh`, and `./scripts/run-managed-device-tests.sh`.
- Status: resolved and verified.
