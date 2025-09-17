# ChronoRex

ChronoRex is a friendly, fully offline fatigue tracker with a light dino vibe. The Android MVP focuses on fast daily capture, readable trends, and clinician-ready exports while keeping every byte of data on the device.

## MVP Highlights
- Morning check-in card with restedness slider, sleep score, notes, and emoji tags
- Calendar hub that visualizes daily fatigue bands and badges for symptoms and activities
- Quick symptom and activity logging with customizable presets and smart snooze reminders
- Honest on-device insights: 7-day trends, same-day and lag correlations, and weekly reviews
- Secure local storage with SQLCipher-backed Room database, optional passcode or biometric lock, and privacy-first exports (CSV and PDF)

## Project Structure
```
ChronoRex/
|-- app/
|   |-- src/
|   |   |-- main/            # Compose UI, domain, and data code
|   |   |-- test/            # JVM unit tests
|   |   `-- androidTest/     # Instrumented and Compose UI tests
|-- gradle/                  # Version catalog and wrapper metadata
|-- AGENTS.md                # Contributor workflow guidelines
|-- requirements.md          # Product and engineering requirements
|-- build.gradle.kts         # Root Gradle configuration
`-- settings.gradle.kts      # Module wiring
```

## Getting Started
1. Install Android Studio Ladybug or newer with Android SDK 35 and Build-Tools 35.x.
2. Ensure JDK 11 is available (bundled with recent Studio releases).
3. Clone the repository and open the root folder in Android Studio.
4. Sync Gradle when prompted; the Kotlin Compose plugin is already configured via the version catalog.

## Build and Run
- `./gradlew assembleDebug` - compile a debuggable APK.
- `./gradlew installDebug` - deploy the debug build to a connected device or emulator.
- `./gradlew lint` - run Android lint checks before raising a pull request.
You can also use Android Studio Run or Debug configurations for quick iterations and Compose previews.

## Testing
- `./gradlew test` - execute JVM unit tests in `app/src/test`.
- `./gradlew connectedAndroidTest` - run instrumentation and Compose UI tests on an attached device or emulator.
Keep tests deterministic by resetting local storage between runs and aim for coverage on new logic.

## Documentation and Resources
- [requirements.md](requirements.md) captures the end-to-end MVP scope, flows, and acceptance criteria.
- [AGENTS.md](AGENTS.md) outlines contributor expectations, coding standards, and review etiquette.
- Update this README whenever build steps, tooling, or architecture decisions change.
