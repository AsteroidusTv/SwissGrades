# SwissGrades

SwissGrades is an Android app for students in Switzerland who want to track their grades progressively during the school year.

Instead of waiting for a report card, you can enter grades as they come in, organize your subjects, and see how your current results affect your average, promotion points, basket, and insufficiencies.

## Current features

- onboarding flow to choose the school year/semester and option subject
- local persistence of setup, subjects, grades, and optional attachments
- app settings for language, theme mode, option, imports, and backups
- custom subjects with name, icon, color, basket flag, and result inclusion flag
- simple and composite option subjects
- semester-aware grade tracking with shared branches across semesters
- multi-grade subject support with editable grade history
- weighted grades:
  - full grade
  - half grade
  - quarter grade
- target average simulator:
  - choose an official target average
  - plan one, two, or three future grades with a shared weight
  - see the needed grade or average using official half-point rounding
- promotion summary with:
  - overall average
  - promotion points
  - basket
  - insufficiencies
- subject detail pages with:
  - raw and official averages
  - current impact of each saved grade
  - optional composite sub-subject selector
  - recent grade evolution
  - grade history
- swipe-to-delete support for subjects and grades
- optional photo/gallery attachments for grades
- PlusPoints import support
- local backup import/export support
- edit and delete for subjects and grades

## Supported option types

Simple options:

- Spanish
- Italian
- Latin
- Music
- Philosophy
- Visual Arts
- Other

Composite options:

- PYAM
  - Physics
  - Applications of Mathematics
- BICH
  - Biology
  - Chemistry
- Economics-Law
  - Economics
  - Law

## Product direction

SwissGrades is currently focused on being a strong personal-use app:

- clear grade entry
- reliable averages
- useful promotion overview
- simple local persistence
- quick simulations for target averages

It is not yet a full school-management app, and it does not try to be one.

## Privacy

SwissGrades is currently built around local-first storage on your device.

- grades and subjects are stored locally
- optional exam photos are stored locally when you attach them
- no account or cloud sync is required

See the full privacy policy here:

- [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md)

## Project documentation

- [`docs/architecture.md`](docs/architecture.md) describes state ownership, persistence, domain rules, and trust boundaries.
- [`ROADMAP.md`](ROADMAP.md) contains active product and engineering candidates.

## Current release

- Version name: `1.5.0`
- Version code: `5`
- Minimum SDK: `24`
- Target SDK: `36`

## Tech stack

- Kotlin
- Jetpack Compose
- Android SDK 36
- Gradle
- pure Kotlin grade/promotion calculation engine

## Local setup

- JDK: 21
- Android SDK platform: 36
- Build tools: 36.x

The local Gradle environment is configured to use a stable JDK 21 instead of the system Java 25 EA runtime.
Gradle daemon usage is also disabled locally on this machine to avoid an observed JVM crash during unit-test execution.

## Useful commands

```bash
./scripts/gradlew21.sh testDebugUnitTest --no-parallel
./scripts/gradlew21.sh assembleDebug --no-parallel
./scripts/release-check.sh
```

## Test commands

There are 3 validation levels in this project:

1. JVM unit tests

```bash
./scripts/gradlew21.sh testDebugUnitTest --no-parallel
```

2. Compile Android instrumented tests

```bash
./scripts/gradlew21.sh assembleAndroidTest --no-parallel
```

3. Execute Android instrumented tests on a real managed emulator

```bash
./scripts/run-managed-device-tests.sh
```

This third command is the supported end-to-end Android validation path.

## Local JDK helper

On Fedora 44, the system Java may be 25 while this project needs JDK 21.

The helper script:

- reuses `JAVA_HOME` if it already points to JDK 21
- otherwise looks for a local JDK 21 under `~/.local/jdks`
- then runs Gradle with the correct runtime automatically

## Instrumented target

The project uses a Gradle Managed Device instead of relying on a manually created local AVD.

- Device: `Pixel 2`
- API level: `36`
- System image source: `google`
- ABI used in practice: `x86_64`

## CI validation

GitHub Actions runs validation in 3 separate layers:

1. JVM unit tests
2. Android assembly
3. Managed-device instrumented tests

Workflow:

- [`.github/workflows/android-validation.yml`](.github/workflows/android-validation.yml)

## Release workflow

The project supports a manual GitHub release flow through:

- [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml)

It can:

- create an automatic prerelease on every push to `main` / `master`
- build a signed release APK
- build a signed release AAB
- upload native debug symbols for Play Console
- create or reuse a Git tag
- publish a GitHub Release
- upload release artifacts

Automatic pushes create prereleases so the repository can always expose the latest installable build without turning every push into a final stable release.

### Required GitHub Secrets

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

`ANDROID_KEYSTORE_BASE64` must contain the base64-encoded contents of your Android signing keystore.

### Optional GitHub Secret

- `RELEASE_TOKEN`

`RELEASE_TOKEN` is only needed when the workflow must push tags or create releases with a token that has broader permissions than the default GitHub Actions token.

## Current state

SwissGrades has a release-ready personal grade tracking workflow with semester-aware subjects, weighted grades, target average simulation, localized option handling, local backups/imports, and automated validation in CI.
