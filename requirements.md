# ChronoRex MVP Requirements

ChronoRex is a friendly, fully offline fatigue tracker with a light dino vibe. The MVP surfaces simple, honest patterns without cloud accounts or heavy analytics.

## 1. Product Overview

* **Purpose**: Help people log how they feel each morning, capture notable symptoms or activities, and spot simple trends that inform pacing and care conversations.
* **Value Proposition**: Fast daily capture, readable history, on-device insights, and easy export for clinicians.
* **Primary Outcomes**: Under-10-second daily check-in, approachable insights that build trust, and a clinician-ready data export.

## 2. Scope

### In Scope (MVP)

* Onboarding
* Daily AM check-in
* Calendar hub
* Day detail
* Symptom and activity quick logs
* Insights (trend and correlations)
* Weekly review
* Local notifications
* App lock (passcode and biometrics)
* CSV and PDF export
* Light design system

### Out of Scope

* Cloud sync or accounts
* Social features
* Wearable or network integrations
* A/B testing
* Advanced statistics or ML
* Heavy charting

## 3. Jobs To Be Done

* Log fatigue quickly each morning (target: under 10 seconds end-to-end).
* Review past days to spot patterns at a glance.
* Export data that is useful in clinician conversations.

## 4. Experience and Brand Principles

* **Theme**: Light dino mascot that feels friendly and reassuring.
* **Palette**:

  * Bone White (#FFFBF2)
  * Dino Mint (#BFF5C8)
  * Fern (#55A66A)
  * Sky Egg (#E8F6FF)
  * Lava Accent (#FF7A59)
  * Ink (#1A1A1A)
* **Typography**: Rounded sans-serif headings with a neutral humanist body typeface.
* **Motion**: 150 ms ease-in-out for taps; respect system-level reduced motion settings.
* **Accessibility**: Contrast >= 4.5:1, hit targets >= 44 px, support dynamic type throughout.
* **Tone**: Playful but clear. Success toast copy: "Logged. T-Rexcellent."

## 5. Functional Requirements

### FR1. Onboarding (first launch)

* **Goals**:

  * Explain privacy stance.
  * Introduce the theme.
  * Capture reminder preferences.
  * Offer optional app lock setup.
  * Guide the user directly into the first AM check-in.
* **Flow**:

  1. Privacy promise.
  2. Theme preview with the tiny dino.
  3. Pick reminder time (prefilled to 8:00 AM next morning).
  4. Optional passcode or biometrics setup.
  5. Drop directly into the AM check-in with defaults ready.
* **Logic and Edge Cases**:

  * Request local notification permission with clear, local-only language.
  * Completing Save during onboarding creates Day 1 and teaches the flow.
  * If notifications are denied, continue onboarding and surface an in-app nudge later.
* **Acceptance Criteria**:

  * User can complete onboarding in <= 60 seconds using default choices.
  * App handles denied notifications gracefully without blocking progress.

### FR2. Daily AM Check-In

* **Trigger**: Display the AM check-in card when the app opens and today has no entry.
* **Inputs**:

  * Restedness slider (0-100) with anchors: 0 Wiped out, 25 Heavy, 50 Meh, 75 Pretty good, 100 Fully charged.
  * Sleep quality scale (1-5) presented as five labeled dots with accessible affordances.
  * Optional note and emoji tags (collapsed by default under a chevron; show the six most recent emojis).
* **Interactions**:

  * Full-width Save button.
  * Undo action available for 5 seconds via non-blocking toast.
* **Day Assignment**:

  * If logging before 4:00 AM, ask once whether such entries should default to "yesterday" and persist the preference.
* **Acceptance Criteria**:

  * Save persists instantly and Undo restores the prior state entirely.
  * When today is already logged, the check-in card collapses within the calendar hub.

### FR3. Calendar Hub

* **Layout**:

  * Month grid with day pills colored by restedness band.
  * Compact 14-day sparkline at the top; tapping it opens Insights.
  * No per-tile sparklines.
* **Badges and Cards**:

  * Small dot badge for symptoms, small triangle for activities (max two glyphs per day; overflow shows a plus).
  * If today is unlogged, pin the AM check-in card at the top; collapse it into the day tile after saving.
* **Interactions**:

  * Long-press a date to open a quick actions sheet: Log symptom, Log activity, Edit AM check-in, Mark illness, Mark travel.
  * Sticky footer with two large buttons: Symptoms and Activities (open today's quick logs).
* **Acceptance Criteria**:

  * Month navigation feels smooth and maintains required contrast against Bone White.
  * Long-press sheet appears within 200 ms and respects reduced motion settings.

### FR4. Day Detail

* **Content**:

  * Show the AM check-in values followed by a reverse-chronological list of symptoms and activities.
* **Controls**:

  * Allow editing or deleting entries locally.
* **Acceptance Criteria**:

  * Edits and deletions update immediately in calendar badges and Insights calculations.

### FR5. Quick Logs

* **Symptoms**:

  * Provide defaults: Brain fog, pain, dizziness, headache, nausea.
  * Allow custom symptom names.
  * Capture severity (1-10), auto-set timestamp to now, and allow an optional note.
* **Activities**:

  * Offer type chips: Physical, Social, Cognitive, Work, Outdoors, Errands, Custom.
  * Inputs include optional duration (minutes), perceived exhaustion (1-10), and optional note.
  * Quick duration row with 30, 60, and 90 minute shortcuts plus manual entry.
* **Placement**:

  * Attach logs to the currently selected day, or to today when launched via footer buttons.
* **Acceptance Criteria**:

  * New items appear instantly in Day Detail and badges refresh without delay.

### FR6. Insights

* **Principles**:

  * Compute everything on-device; keep charts readable and honest.
* **Trend**:

  * Show a 7-day moving average for the fatigue proxy (fatigue_today_0_100 = 100 - restedness_0_100).
* **Correlations**:

  * Pearson r only with two views:

    * Same-day: fatigue_today vs symptom_avg_today and activity_exhaustion_avg_today.
    * Lag-1: fatigue_today vs symptom_avg_yesterday and activity_exhaustion_avg_yesterday.
* **Effect Sizes and Copy**:

  * Label effect sizes as small, medium, large at thresholds 0.1, 0.3, 0.5.
  * Confidence language: Low if n < 10; Medium if 10-20; High if >= 21.
  * Example copy: "Higher symptom severity yesterday correlated with more fatigue today (r 0.28, medium estimate). Not causal."
* **Acceptance Criteria**:

  * Rendering finishes in < 300 ms for one year of typical data.
  * Copy avoids causal claims and clarifies estimates.

### FR7. Weekly Review

* **Generation**:

  * Generate locally every seven days.
  * If notifications are blocked, show an in-app banner on next launch: "Your weekly review is ready."
* **Content**:

  * Top three trend highlights, top three candidate correlations, best day and toughest day, and adherence summary.
* **Acceptance Criteria**:

  * Opens in <= 1 second from tap.
  * No network usage during generation or display.

### FR8. Export

* **Formats**:

  * Provide CSV and one-page PDF exports aimed at clinicians.
* **Distribution**:

  * Allow saving to device or sharing via the system share sheet.
  * Data stays local unless the user initiates an export.
* **CSV Content**:

  * Include all entities plus a separate data dictionary CSV.
* **PDF Content**:

  * Overview summary, last 14-day chart, today's status, symptom and activity summary, correlation table, and a non-medical-advice disclaimer.
* **Acceptance Criteria**:

  * One year of data exports to CSV in <= 1 second and to PDF in <= 3 seconds on a mid-tier device.

### FR9. Notifications

* **Behavior**:

  * Schedule a single local reminder at the user-selected time.
  * Smart snooze: If the user marks illness or travel, automatically snooze the next day's reminder for 24 hours.
  * If the OS blocks notifications, display an in-app nudge on the next launch.
* **Acceptance Criteria**:

  * Reminders persist across device reboots and respect locale-specific time formatting.

### FR10. Security and App Lock

* **Features**:

  * Offer passcode or biometric app lock with optional auto-lock on backgrounding.
  * Reinforce the privacy promise within onboarding and settings.
  * Exclude analytics SDKs; only include crash reporting if it functions completely offline.
* **Acceptance Criteria**:

  * Lock and unlock flows complete in < 500 ms.
  * No sensitive content appears in the task switcher when the app is locked.

## 6. Data and Storage

* **Storage**: SQLite via Room. All data remains on-device.
* **Entities**:

  * **Day**:

    * date (ISO string, unique per day)
    * timezone_offset_minutes (int)
    * restedness_0_100 (int)
    * sleep_quality_1_5 (int)
    * notes (string, optional)
    * emoji_tags (string[], optional)
    * flags: illness (bool), travel (bool)
  * **SymptomEntry**:

    * id (UUID)
    * date (ISO string)
    * time (ISO timestamp)
    * name (string)
    * severity_1_10 (int)
    * note (string, optional)
  * **ActivityEntry**:

    * id (UUID)
    * date (ISO string)
    * time (ISO timestamp)
    * type (string)
    * duration_minutes (int, optional)
    * perceived_exhaustion_1_10 (int)
    * note (string, optional)
  * **Settings**:

    * reminder_time (local time)
    * passcode_hash (optional)
    * biometrics_enabled (bool)
    * theme (light only)
    * smart_snooze_enabled (bool, default true)
* **Derived Fields**:

  * fatigue_today_0_100 = 100 - restedness_0_100
  * symptom_avg_today = mean of SymptomEntry.severity for the day
  * activity_exhaustion_avg_today = mean of perceived_exhaustion for the day
  * activity_minutes_today segmented by activity type
* **Day Assignment Rule**:

  * When the user opts into "before 4 AM counts as yesterday," apply that rule for any entry created before 04:00 local time.

## 7. Analytics Engine

* Preprocess at a daily grain with no imputation of missing days.
* Winsorize values only when they are impossible (for example, outside defined ranges).
* Provide a 7-day moving average trend and rolling min or max bands across the last 28 days.
* Compute Pearson r correlations for same-day and lag-1 views using the effect size thresholds above and confidence language based on sample size.

## 8. Security, Privacy, and Compliance

* Keep all logic on-device; do not perform background network calls.
* Offer app lock with passcode or biometrics and optional auto-lock.
* Allow network access only when the user explicitly triggers an export through the share sheet.

## 9. Performance Targets

* Cold start in under 2 seconds on mid-tier Android devices.
* Daily check-in flow finishes in under 10 seconds end-to-end.
* Insights computations complete in under 300 ms for one year of typical data.
* Local database stays under 50 MB after one year of typical usage.

## 10. Design System Tokens

* Spacing uses a 4 px base grid; containers pad at 16 px; cards have a 16 px radius; mascot circle is 32 px.
* Color roles: Background Bone White; Card Sky Egg; Primary Fern; Accent Lava for CTAs and highlights; Success Fern; Warning Lava at 80%; Text Ink; Focus rings Fern with 2 px width.
* Iconography and emoji: Use sparingly. Feature the dino in empty states and success toasts; rely on system icons for add, edit, and export actions.

## 11. Release Checklist

* Onboarding completes with default choices, schedules the reminder, and optionally sets the lock.
* AM check-in saves with Undo and assigns the correct day per user preference.
* Calendar hub shows color bands, badges, quick actions, and working footer buttons.
* Day detail reflects edits and deletions immediately across views.
* Quick logs add items with correct timestamps and refresh badges instantly.
* Insights render trend and correlation tiles with non-causal copy.
* Weekly review generates locally and remains accessible even without notification permissions.
* Export produces CSV and one-page PDF outputs that work offline.
* App lock functions correctly.
* Performance targets are met on the mid-tier reference device.

## 12. Testing Approach

* Testing will be performed by the developer in Android Studio using a virtual device. No separate testing strategy is specified in this document.
