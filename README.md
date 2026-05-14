# SwissGrades

SwissGrades is an Android app for students in Switzerland who want to track their grades progressively during the school year.

Instead of waiting for a report card, you can enter grades as they come in, organize your subjects, and see how your current results affect your average, promotion points, basket, and insufficiencies.

## Why this app exists

A lot of grade calculators are either too rigid, too technical, or too focused on one final “simulation”.

SwissGrades is designed around a more realistic student workflow:

- choose your option once when you start
- add subjects gradually as the year goes on
- enter grades one by one
- keep everything saved locally on your phone
- understand where you stand without needing all subjects or all grades upfront

The app stays usable even when your data is incomplete:

- no crash when a subject has no grades yet
- empty subjects can exist while you build your school year setup
- the promotion summary becomes more precise as more grades are added

## Current features

- onboarding flow to choose your option
- local persistence of your setup and grades
- editable required Option subject
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
./gradlew testDebugUnitTest --no-parallel
./gradlew assembleDebug --no-parallel
```

## Test commands

There are 3 validation levels in this project:

1. JVM unit tests

```bash
./gradlew testDebugUnitTest --no-parallel
```

2. Compile Android instrumented tests

```bash
./gradlew assembleAndroidTest --no-parallel
```

3. Execute Android instrumented tests on a real managed emulator

```bash
./gradlew pixel2Api36DebugAndroidTest --no-parallel
```

This third command is the supported end-to-end Android validation path.

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

- [/home/achille/AndroidStudioProjects/SwissGrades/.github/workflows/android-validation.yml](/home/achille/AndroidStudioProjects/SwissGrades/.github/workflows/android-validation.yml)

## Release workflow

The project supports a manual GitHub release flow through:

- [/home/achille/AndroidStudioProjects/SwissGrades/.github/workflows/android-release.yml](/home/achille/AndroidStudioProjects/SwissGrades/.github/workflows/android-release.yml)

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
