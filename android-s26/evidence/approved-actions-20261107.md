# CactusX bounded Android action approval — 2026-11-07 UTC

Source commits: `6b53df1` (native schemas), `d486fa5` (UI parser and approvals).

## Contract

- Native Needle init advertises only `get_time` and `open_settings`, both with empty argument schemas.
- Model envelope parsed only if `type=call` and `function_calls` is an array.
- Unknown tool names are displayed, not executed.
- Unexpected arguments are rejected.
- Each recognized proposal displays an Android AlertDialog with **Deny** and **Approve**.
- `get_time` returns Android device wall-clock time after approval.
- `open_settings` launches `Settings.ACTION_SETTINGS` after approval.
- Denied operations record a visible denial.
- Model-proposed code/shell/URLs are never executed.

## Build verification

- InstaBox `cactusx-android-s26`.
- Gradle 8.10.2 Android SDK 35: **BUILD SUCCESSFUL** (35 tasks).
- Output `/data/cactusx-unified-build/android-s26/app/build/outputs/apk/debug/app-debug.apk`.
- SHA256: `9f66304b353421ced6793327b1cecbfec813bb589bd32463b92d2be807f2db89`.
- ARM64 only. Signature and device behavior of this exact build not yet independently exercised.
- Approval logic is not a substitute for end-to-end testing; do not consider tool execution qualified on physical device yet.

## Next adapters

Calls/messages drafts, calendar/reminders, local file selection, camera capture, share sheet, application launch (allowlisted and package-visibility-aware), and optional authenticated services. Each needs typed arguments, explicit user confirmation, platform permission handling, and negative tests.
