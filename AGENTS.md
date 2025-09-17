# Repository Guidelines

## Project Structure & Module Organization
ChronoRex is a single-module Android app powered by Jetpack Compose. Core source lives in `app/src/main` with Kotlin code under `java/com/dino/chronorex` and resources in `res/`. Unit tests sit in `app/src/test`, instrumentation and Compose UI tests in `app/src/androidTest`, and shared fixtures can be placed in `app/src/testShared` if added. Gradle build logic and version catalogs are in the root `build.gradle.kts`, `app/build.gradle.kts`, and `gradle/libs.versions.toml`.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK for local install.
- `./gradlew test` runs JVM unit tests in `app/src/test`.
- `./gradlew connectedAndroidTest` executes instrumentation and Compose UI tests on an attached device or emulator.
- `./gradlew lint` enforces Android lint checks; run before opening a PR.
Use Android Studio for Compose previews, but rely on the Gradle commands above for CI parity.

## Coding Style & Naming Conventions
Write Kotlin using the official style: four-space indentation, trailing commas for multi-line constructors, and descriptive `val` names. Compose entry points use PascalCase (for example `DailyCheckInScreen`) and preview methods end with `Preview`. View-models end with `ViewModel`, repositories with `Repository`, and coroutine scopes prefer `viewModelScope`. Keep files focused: one top-level screen or domain type per file, and align packages to features (e.g., `ui.checkin`, `data.symptom`).

## Testing Guidelines
Default unit tests to JUnit4 with MockK or Turbine when asynchronous flows are introduced. UI interactions should live in Compose UI tests with descriptive method names such as `DailyCheckInScreen_savesEntry()`. Keep instrumentation tests deterministic and reset local storage between runs. Target meaningful coverage for new logic and run both `./gradlew test` and `./gradlew connectedAndroidTest` before requesting review.

## Commit & Pull Request Guidelines
Use imperative, focused commit messages (e.g., `Add restedness slider state`). When a change is large, include a brief body describing rationale or follow-up work. Pull requests should outline the change, reference relevant requirements (for example sections in `requirements.md`), list testing performed, and attach screenshots or screen recordings for UI updates. Request a design check whenever visual tokens change.

## Security & Configuration Tips
Keep the project offline-first: do not add analytics SDKs or background network calls without approval. Never commit keystores, API keys, or personal data. Each contributor maintains a `local.properties` pointing to the Android SDK; regenerate `google-services.json` only if cloud features are introduced later.
