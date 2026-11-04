# SwissGrades Architecture

SwissGrades is a single-activity, local-first Android application written in Kotlin with Jetpack Compose and Material 3. It has no backend, account, authentication, analytics, or network API.

## Runtime boundaries

- `MainActivity` launches the Compose application in `ui/app`.
- `GradeTrackerViewModel` owns navigation, editable screen state, and mutations of the persisted application state.
- `GradeTrackerUiState` is the immutable presentation state observed by Compose.
- Pure calculations live under `domain`; they must not depend on Android, persistence, colors, or localized strings.
- Android storage, file pickers, attachments, backup archives, and PlusPoints files are infrastructure boundaries under `ui/app`.
- `GradeReportBuilder` creates an immutable report snapshot, while `LocalGradeReportPdfExporter`
  owns Android PDF rendering and destination-URI writes.

Navigation uses typed internal screen state rather than the Android Navigation component. Back handling delegates to the ViewModel. Scroll positions are temporary UI state and are not persisted across process death.

The Carnet refactor adds three root destinations: subjects, promotion overview, and settings. `MainSection` is transient ViewModel state, never part of the persisted JSON. `CarnetComponents` owns the new subject list and navigation; the detail and full-page editor use Carnet presentation components while file workflows remain connected. The full UI composition plan is in `CARNET_COMPONENT_PLAN.md`, with implementation decisions in `CARNET_ARCHITECTURE.md`.

## Persistence and ownership

The authoritative application state is `GradeTrackerAppState`.

- It is serialized as a versioned JSON document in `grade_tracker_app_prefs`.
- Subjects are shared between semesters.
- Semester 1 calculations include semester 1 grades only.
- Semester 2 calculations include grades from both semesters.
- Optional attachments are staged under app-private storage and committed under `files/attachments/notes/{noteId}`.
- Manual `.sgb` backups contain serialized state and referenced attachment files.
- Manual PDF reports contain calculated results and grade details for the selected period, but
  never contain attachments, targets, or simulator state.

Editor and simulator inputs are temporary. They never become authoritative until explicitly saved. Multi-grade plans and grade-impact values are derived in memory and are not persisted.

## Domain rules

- Individual grades range from `1.0` to `6.0` in quarter-grade steps.
- Assessment weights are full (`1.0`), half (`0.5`), or quarter (`0.25`).
- Official branch averages round to the nearest half grade.
- Composite options contain exactly two sub-subjects. Each internal weighted average is rounded to two decimals before the combined result is rounded to a half grade.
- Saved average targets accept only whole or half grades.
- Promotion evaluation uses structured domain statuses and reasons. UI labels and tones are localized at the presentation boundary.
- A negative total of promotion points is an explicit blocking reason, including when the basket reaches 16.

The promotion model currently expects exactly three counted non-option basket branches plus the option branch. Any future support for different school rules requires an explicit product decision and migration strategy.

## Trust and privacy boundaries

All selected files and serialized content are untrusted.

- PlusPoints XML reads are bounded and reject DTD/entity input.
- Backup entries, manifest paths, note identifiers, and attachment identifiers are validated before filesystem use.
- Restore targets are canonicalized under app-controlled directories.
- Android cloud backup excludes grades and attachments. Manual exports are explicit user actions; Android device-transfer behavior is documented in the privacy policy.
- PDF reports are written only to a destination selected by the user and are never retained in
  app storage after export.
- Secrets and signing credentials are supplied through local Gradle properties or environment variables and are never committed.

## Validation

Supported checks:

```bash
./scripts/gradlew21.sh testDebugUnitTest --no-parallel
./scripts/release-check.sh
./scripts/run-managed-device-tests.sh
```

CI runs unit tests, app/test assembly, release validation, and managed-device tests. The release check includes lint, release unit tests, R8/minification, bundle creation, mapping output, and native-symbol packaging.

## Known engineering work

- Continue extracting coherent responsibilities from the large ViewModel and branch-detail Compose screen.
- Clarify promotion basket roles instead of relying on positional interpretation if school-specific rules expand.
- Improve attachment accessibility semantics and add a stable attachment UI smoke test.
- Continue consolidating spacing, typography, and component tokens without speculative redesign.


The Carnet presentation now includes compact root navigation, subject and grade rows, four structured promotion conditions, and settings rows with focused dialogs. Subject/grade deletion uses the existing swipe and confirmation flow; screen readers receive custom semantic actions without a persistent trash button. Simulation inputs use saveable UI state so scrolling them out of a lazy list does not discard a temporary plan. `CarnetLocalization.kt` groups the feature copy to keep the large `AppStrings` constructor within the JVM method-signature limit.
