# SwissGrades

Android app in Kotlin + Jetpack Compose for tracking and simulating Swiss grades.

## Local setup

- JDK: 21
- Android SDK platform: 36
- Build tools: 36.x

The local Gradle environment is configured to use a stable JDK 21 instead of the system Java 25 EA runtime.
Gradle daemon usage is also disabled locally on this machine to avoid an observed JVM crash during unit-test execution.

## Useful commands

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## Test commands

There are 3 different levels of test validation in this project:

1. JVM unit tests only

```bash
./gradlew testDebugUnitTest --no-parallel
```

2. Compile Android instrumented tests only

```bash
./gradlew assembleAndroidTest --no-parallel
```

3. Execute Android instrumented tests on a real managed emulator

```bash
./gradlew pixel2Api36DebugAndroidTest --no-parallel
```

This third command is the supported end-to-end instrumented test flow for the project.
It creates or reuses the Gradle Managed Device `pixel2Api36`, installs the app and test APKs,
and executes the current Android instrumented suite on that target.

## Instrumented target

The project uses a Gradle Managed Device instead of relying on a manually created local AVD.

- Device: `Pixel 2`
- API level: `36`
- System image source: `google`
- ABI used in practice: `x86_64`

This makes the instrumented test target reproducible from the project itself.

## CI validation

GitHub Actions runs the same validation path in 3 separate layers:

1. JVM unit tests

```bash
./gradlew testDebugUnitTest --no-parallel
```

2. Android assembly

```bash
./gradlew assembleDebug --no-parallel
./gradlew assembleAndroidTest --no-parallel
```

3. Managed-device instrumented tests

```bash
./gradlew pixel2Api36DebugAndroidTest --no-parallel
```

The workflow file is [.github/workflows/android-validation.yml](/home/achille/AndroidStudioProjects/SwissGrades/.github/workflows/android-validation.yml).
CI runs these layers as separate jobs so it is obvious whether a failure comes from unit tests,
Android assembly, or the real managed-emulator execution.

## Current project state

- Compose app with persisted editable grade simulation flow
- JVM unit test suite
- Android instrumented test suite
- Gradle Managed Device support for reproducible instrumented execution
