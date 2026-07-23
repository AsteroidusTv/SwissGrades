# SwissGrades Repository Map

Audit started: 2026-07-22  
Working branch at start: `feature/branch-average-targets`  
Baseline HEAD at start: `3c8ab05 Prepare 1.5.0 release`  
Remote relation at start: branch tracks `origin/feature/branch-average-targets`; `origin/main` already contains merge commit `4e3337d`.

## Baseline Repository State

- `git status --short --branch` showed no modified tracked files before audit files were created.
- Pre-existing untracked files were present and were not deleted: `package.json`, `node_modules/`, `git-diagnostics-2026-06-12-1856.zip`.
- Tracked source/docs/config files total roughly 18k lines, excluding build output.
- Generated/build directories exist locally: `.gradle/`, `.kotlin/`, `build/`, `app/build/`, `.idea/`.

## Application Type

- Native Android application.
- Primary language: Kotlin.
- UI framework: Jetpack Compose + Material 3.
- Build system: Gradle Kotlin DSL.
- Runtime app entry point: `app/src/main/java/me/asteroidus/swissgrades/MainActivity.kt`.
- Main product package: `me.asteroidus.swissgrades.ui.app`.
- Additional older or secondary simulation package: `me.asteroidus.swissgrades.ui.simulation`.

## Product Purpose

SwissGrades is a local-first student grade tracker for Swiss students. It supports:

- onboarding with option subject selection;
- school year and semester selection;
- shared branches across semesters;
- weighted grade entry;
- simple and composite option subjects;
- promotion summary, basket total, insufficiencies, and promotion points;
- per-subject target average and grade simulator;
- optional grade attachments;
- local backup import/export;
- PlusPoints import;
- language and theme settings;
- app reset.

## Frontend / Backend Boundary

- There is no backend in this repository.
- There is no authentication or authorization model.
- Data is device-local.
- External interactions are Android platform APIs: SharedPreferences, file storage, Activity Result contracts, content resolver, camera/gallery/document picker, FileProvider, Android backup/restore.

## Persistence Model

- Main app state persists as one JSON blob in SharedPreferences:
  - prefs file: `grade_tracker_app_prefs`
  - key: `app_state`
  - implementation: `SharedPreferencesGradeTrackerRepository`
  - schema constant: `APP_STATE_SCHEMA_VERSION = 1`
- Optional grade attachments are copied into app-local files:
  - staged path: `files/attachments/staged`
  - committed path: `files/attachments/notes/{noteId}`
- Backup export/import serializes the app state and attachment files into a `.sgb` zip archive.
- Android system backup is enabled and includes the app SharedPreferences file and attachments directory.
- The secondary simulation editor has its own SharedPreferences persistence in `simulation_editor_prefs`.

## Major Modules

- `domain/GradeCalculator.kt`: weighted averages, half-point rounding, promotion points, basket sum.
- `domain/TargetSimulationCalculator.kt`: target-average simulation for needed next grade.
- `domain/PromotionEvaluator.kt`: promotion status, blocking rules, missing-data rules.
- `ui/app/PromotionDashboardPresenter.kt`: maps typed promotion results to localized dashboard copy and semantic tone.
- `ui/app/GradeTrackerRepository.kt`: app state model and JSON serialization.
- `ui/app/GradeTrackerViewModel.kt`: main state mutations, navigation, persistence orchestration, domain mapping.
- `ui/app/GradeTrackerApp.kt`: main Compose root, onboarding, dashboard, period picker, summary cards.
- `ui/app/GradeTrackerBranchDetailScreen.kt`: branch detail, notes, attachments, grade form, delete UI.
- `ui/app/SettingsScreen.kt`: language/theme/option/import/export/reset settings.
- `ui/app/AppBackupCoordinator.kt`: local backup zip import/export.
- `ui/app/PlusPointsImportCoordinator.kt`: PlusPoints plist/XML import.
- `ui/app/GradeAttachmentStorage.kt`: attachment staging, commit, deletion.
- `ui/app/TargetSimulationCard.kt`: expandable target grade simulator UI.
- `ui/simulation/*`: older or secondary standalone simulation UI/state holder, including its English-only presentation mapper; not currently called by `MainActivity`.

## Build, Test, Lint, Release Commands

- JVM unit tests:
  - `./scripts/gradlew21.sh testDebugUnitTest --no-parallel`
  - direct equivalent: `./gradlew :app:testDebugUnitTest --no-parallel`
- Debug assemble:
  - `./scripts/gradlew21.sh assembleDebug --no-parallel`
- Android test APK compile:
  - `./scripts/gradlew21.sh assembleAndroidTest --no-parallel`
- Managed device tests:
  - `./scripts/run-managed-device-tests.sh`
- Release validation:
  - `./scripts/release-check.sh`
  - runs `git diff --check`, `:app:lintRelease`, `:app:testReleaseUnitTest`, `:app:bundleRelease`, and checks AAB, mapping, native symbols.

## CI / Release Workflows

- `.github/workflows/android-validation.yml`
  - unit tests;
  - debug assemble;
  - Android test assemble;
  - release check;
  - managed-device instrumented tests.
- `.github/workflows/android-release.yml`
  - automatic prereleases on push to `main`/`master`;
  - manual release dispatch;
  - signed APK/AAB build;
  - tag/release publishing.

## Routing / Navigation Architecture

- Single-activity Compose app.
- No Navigation component.
- Navigation state is modeled by `ScreenUiState` and private `InternalScreen`.
- `GradeTrackerViewModel` owns current screen state.
- `GradeTrackerApp` switches screens through `AnimatedContent`.
- Back behavior is handled by `BackHandler` and delegated to ViewModel methods.

## State Management

- `GradeTrackerViewModel` holds mutable private `GradeTrackerAppState` plus mutable `InternalScreen`.
- UI observes `StateFlow<GradeTrackerUiState>`.
- Persistence writes are serialized on `Dispatchers.IO.limitedParallelism(1)`.
- In-memory repository path is used for unit tests and synchronous saves.

## Design System / Styling Strategy

- Material 3 theme in `ui/theme`.
- App-level tokens in `GradeTrackerUiTokens.kt`.
- Shared card shape: `DashboardCardShape = RoundedCornerShape(24.dp)`.
- Core screen padding: `AppScreenHorizontalPadding = 16.dp`, `AppScreenTopPadding = 16.dp`.
- UI is mostly bespoke Compose components rather than a formal component library.

## Existing Test Coverage

- JVM domain tests:
  - `GradeCalculatorTest`
  - `PromotionEvaluatorTest`
  - `TargetSimulationCalculatorTest`
- JVM app logic tests:
  - repository serialization;
  - ViewModel behavior;
  - localized promotion dashboard presentation;
  - PlusPoints import;
  - localization;
  - secondary simulation state holder and English presentation mapping.
- Instrumented tests:
  - app flows;
  - backup coordinator;
  - repository serialization;
  - secondary simulation UI.

## Major User Journeys

- First launch: choose option -> choose school period -> dashboard.
- Dashboard: view summary, open settings, open period picker, open option/custom subject, add/delete custom subject.
- Subject detail: view averages, set target average, expand grade simulator, choose composite sub-subject, view grades, add/edit/delete grade, manage attachments.
- Settings: change language/theme/option, export/import backups, import PlusPoints export, reset app.
- Semester flow: branches are shared across semesters; semester 1 metrics include semester 1 only; semester 2 metrics include semester 1 and semester 2.

## Known Assumptions To Verify

- The expected Swiss promotion model is represented by exactly three basket subjects plus option; ordering of basket subjects may imply German/French/Math roles.
- The current app has no account, cloud sync, or server API.
- The secondary `ui/simulation` package is not part of the current launcher flow.

## Areas Not Yet Fully Inspected

- Every Compose screen has not yet been manually executed on a device/emulator in this audit session.
- Accessibility has not yet been verified with TalkBack or automated accessibility tooling.
- Dependency vulnerability scanning has not yet been run; network access is restricted in the current environment.
- Untracked `node_modules/`, `package.json`, and diagnostics zip have only been structurally inspected so far.
