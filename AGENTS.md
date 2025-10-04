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
- Do not introduce mock data, seeded fixtures, or demo content into production sources; rely solely on Room-backed data.

# ChronoRex Agentenleitfaden (DE)

## Projektueberblick
- ChronoRex ist eine rein lokale Android App (Jetpack Compose, SDK 35) mit einem Modul.
- Quellcode liegt unter `app/src/main/java/com/dino/chronorex`, Ressourcen in `app/src/main/res`.
- Gradle Konfiguration: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`.
- Tests liegen in `app/src/test` (Unit) und `app/src/androidTest` (Instrumented).

## Build und Tests
- `./gradlew assembleDebug` baut das Debug APK.
- `./gradlew test` fuehrt JVM Tests aus.
- `./gradlew connectedAndroidTest` startet Instrumented und Compose UI Tests.
- `./gradlew lint` prueft Android Lint Regeln.

## Architektur und Schluesselpakete
- `ChronoRexApplication` initialisiert Room Datenbank und stellt Repositories, ReminderManager und ExportManager bereit.
- Datenlayer: `data/local` (Room DAO/Entities), `data/repository` fuer Day, Symptom, Activity, Settings, WeeklyReview.
- Modelle unter `model`, inkl. Settings mit Passcode, Smart Snooze, Before Four Preference.
- `notification` kapselt ReminderScheduler, Broadcast Receiver und Boot Handling.
- `export` generiert CSV und PDF und nutzt `InsightsCalculator`.
- `analytics` enthaelt `InsightsCalculator` (Trends, Korrelationen) und `WeeklyReviewGenerator`.
- `ui` ist nach Features gegliedert (`onboarding`, `checkin`, `home`, `daydetail`, `insights`, `export`, `weeklyreview`, `lock`, `settings`), `ChronoRexApp.kt` haelt Navigation und App Lock Overlay bereit.

## Kernablaeufe
- Onboarding fuehrt durch Privacy, Mascot, Reminder, App Lock, Ready; speichert Einstellungen, setzt Reminder und navigiert danach direkt zum Check-In Screen.
- Daily Check-In: Restedness (0-100), Sleep Qualitaet (1-5), Illness/Travel Flags, Notizen, Emoji Tags; Undo Snackbar fuer 5 Sekunden; vor 4 Uhr Option wird persistiert ueber Settings.
- Kalender Hub: Monatsraster mit Restedness Farbbands, Symptom/Activity Badges, 14 Tage Sparkline, Check-In Prompt wenn heute leer, Quick Action Bottom Sheet mit Log/Edit/Flag Option.
- Quick Logs: Karten fuer Symptome und Aktivitaeten teilen sich Draft State, speichern via Repositories, Duration Presets aus Historie, Footer Buttons fuer heutigen Tag.
- Day Detail: zeigt Check-In Werte, Listen der Symptome und Aktivitaeten und erlaubt Loeschen; Navigation via Kalender.
- Insights und Weekly Review: nutzen Analytics Ergebnis, praesentieren Trendlinien, Korrelationen, Wochenauszug, Banner wenn Review bereit; Quick Review erreichbar ueber Home.
- Export: `ExportViewModel` erzeugt CSV Dateien (inkl. Daten Dictionary) und einseitige PDF, Teilen ueber Share Sheet oder Downloads.
- Erinnerungen/App Lock: ReminderManager plant lokalen Alarm (inkl. Smart Snooze bei Illness/Travel), LockViewModel beobachtet passcodeHash, Biometrics und Auto Lock.

## Abgleich Anforderungen vs Umsetzung
- **FR4 Day Detail**: Requirements verlangen Editieren oder Loeschen; UI bietet derzeit nur Delete Chips, kein Bearbeiten der gespeicherten Eintraege.
- **FR5 Quick Logs**: Vorgaben nennen feste Standardchips (Brain fog, pain, dizziness, headache, nausea sowie Aktivitaetschips 30/60/90 min). Umsetzung erzeugt Vorschlaege ausschliesslich aus bisherigen Eintraegen und dynamischen Dauerstatistiken; Default Chips und festen Dauer Shortcuts fehlen.
- **Settings Zugriff**: `ChronoRexRoute.Settings` rendert nur eine Platzhalterkarte. Reminder-, Passcode- und Privacy-Einstellungen lassen sich ausserhalb des Onboardings nicht pflegen, obwohl Anforderungen erwaehnen, dass diese Optionen in Settings sichtbar bleiben sollen.
- **Tests**: In `app/src/test` existiert nur das generierte `ExampleUnitTest`, `androidTest` enthaelt einen Room Test. Anforderungen betonen sinnvolle Abdeckung fuer neue Logik; aktueller Stand deckt Kernflows noch nicht ab.

## Hinweise fuer neue Arbeiten
- Nutze bestehende UI Komponenten aus `ui/components` fuer Buttons, Chips und Karten, um das Design System einzuhalten.
- Repositories geben suspendierende Operationen frei; kombiniere sie mit `viewModelScope` und Flows wie im Codebeispiel von `DailyCheckInViewModel`.
- Export und Analytics rechnen synchron auf Dispatchers.IO; bei neuen Features dort Performancegrenzen (300 ms fuer ein Jahr Daten) beruecksichtigen.
- Bevor neue Funktionen in Settings oder Quick Logs implementiert werden, pruefe die Luecken in obiger Abgleich-Liste und plane Tests in `app/src/test` bzw. `app/src/androidTest`.
