# Final Verification

Status: convergence criteria reached for the inspected and remediated scope.

## Completed Work

- Created the full persistent audit document set under `docs/audit/`.
- Hardened PlusPoints XML import against DOCTYPE/external entity parsing and oversized selected files.
- Hardened SwissGrades backup import/export attachment path handling and rejected unsafe note/attachment ids before filesystem restore.
- Added negative regression tests for hostile PlusPoints XML and unsafe backup manifests.
- Fixed saved target average synchronization with the grade simulator.
- Added regression coverage for simulator target synchronization.
- Fixed French blocked dashboard status tone.
- Added regression coverage for French blocked promotion tone.
- Disabled Android cloud backup for local grades and attachments while preserving Android 12+ device-transfer rules.
- Updated `PRIVACY_POLICY.md` to match backup and transfer behavior.
- Implemented the post-audit Promotion setup assistant roadmap item.
- Removed promotion presentation strings/models from the domain layer and replaced string-based calculability/tone decisions with structured state.
- Restricted official average targets to whole and half-grade steps while preserving quarter-step grade and simulator results.
- Added compatibility normalization for previously saved arbitrary-decimal targets.
- Extended the grade simulator to plan one, two, or three equal-weight future grades without persisting speculative data.
- Added current grade-impact explanations to the grade editor using official simple, composite, and cumulative semester rules.

## Commands Executed

- `git diff --check`: passed.
- `python3` XML parse check for `backup_rules.xml` and `data_extraction_rules.xml`: passed.
- `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`: passed.
- `./scripts/release-check.sh`: passed.
- `./scripts/run-managed-device-tests.sh`: passed.

## Test And Build Results

- Debug JVM unit tests: passed.
- Release unit tests, lint, R8/minify, and release bundle checks through `release-check`: passed.
- Managed-device Android tests: passed, 44 tests completed on `pixel2Api36`, 0 failed.

## Security And Privacy Verification

- PlusPoints parser now rejects `DOCTYPE` input and uses best-effort external entity protections.
- PlusPoints selected-file reading is size-limited before parsing.
- Backup restore now resolves manifest attachment paths canonically under `attachments/`.
- Backup restore/export rejects note and attachment ids that are unsafe as filesystem path components.
- Android cloud backup excludes app shared preferences and file storage; device-transfer rules still allow explicit migration behavior.

## UI/UX Verification

- Simulator default target state is covered by unit tests for untouched sync, manual expanded input preservation, and collapsed reset.
- French blocked dashboard tone is covered by a ViewModel regression test.
- Promotion setup assistant diagnostics are covered by ViewModel tests and an instrumented dashboard action smoke test.
- Promotion dashboard localization/tone/calculability are covered by pure presenter tests, and the detached simulation presentation remains covered by JVM and instrumented tests.
- Official target parsing, legacy normalization, persistence, and simulator rejection behavior are covered by JVM tests.
- Multi-grade aggregate calculations are covered by JVM tests, and the two-grade selector/result flow is covered by a managed-device smoke test.
- Grade-impact calculations are covered for simple, weighted, composite, incalculable, and cumulative-semester cases; the edit-sheet presentation is covered by a managed-device smoke test.
- Existing managed-device tests pass after the UI/state changes.

## Resolved Findings

- High: SEC-001, TEST-003.
- Medium: SEC-002, SEC-003, FUNC-001, FUNC-002, FUNC-004, UI-001, UI-002, TEST-001, TEST-002, TEST-EXEC-001.

## Unresolved Or Deferred Findings

- TECH-001 remains deferred to incremental coherent extraction cycles.
- TECH-002 is resolved and verified.
- Low-severity/product-decision items remain open: TECH-003, FUNC-003, UI-003, UI-004, TEST-004.

## Roadmap Summary

- Near-term: human-readable export. Promotion setup assistant is implemented.
- Medium-term: historical grade-impact explanations and configurable grading rules. The current-impact MVP is implemented.
- Long-term: privacy-preserving sync and timetable/reminder integrations.
- Experimental: multi-grade what-if planning is implemented; accessibility preferences remain a candidate.

## Recommended Next Milestone

Continue TECH-001 with one coherent extraction from the oversized ViewModel or Compose screens, then address attachment accessibility semantics under UI-003.

## Convergence Statement

The application has reached the defined convergence criteria for this audit/remediation pass. This does not mean the app is bug-free; it means no unresolved Critical or High findings remain, implemented Medium findings have passing validation, remaining Medium work is explicitly deferred with justification, and two re-audit passes did not reveal new actionable Critical, High, or Medium findings in the inspected scope.
