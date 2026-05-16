# SwissGrades

SwissGrades is an Android app for students in Switzerland who want to track their grades progressively during the school year.

Instead of waiting for a report card, you can enter grades as they come in, organize your subjects, and see how your current results affect your average, promotion points, basket, and insufficiencies.

## Current features

- onboarding flow to choose your option
- local persistence of your setup and grades
- editable required Option subject
- app settings for language, theme mode, and option
- custom subjects with name, icon, color, and basket flag
- simple and composite options
- multi-grade subject support
- weighted grades:
  - full grade
  - half grade
  - quarter grade
- promotion summary with:
  - overall average
  - promotion points
  - basket
  - insufficiencies
- subject detail pages with grade history
- edit and delete for subjects and grades

## Supported option types

Simple options:

- Economics-Law
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

## Product direction

SwissGrades is currently focused on being a strong personal-use app:

- clear grade entry
- reliable averages
- useful promotion overview
- simple local persistence

It is not yet a full school-management app, and it does not try to be one.

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

- build a signed release APK
- build a signed release AAB
- create or reuse a Git tag
- publish a GitHub Release
- upload release artifacts

### Required GitHub Secrets

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

`ANDROID_KEYSTORE_BASE64` must contain the base64-encoded contents of your Android signing keystore.

## Current state

SwissGrades already has a usable first version of the student workflow, with local persistence, editable subjects, editable grades, and automated validation in CI.
