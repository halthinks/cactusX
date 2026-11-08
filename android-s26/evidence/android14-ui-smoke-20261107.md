# CactusX Android 14 UI smoke execution — 2026-11-07 UTC

Environment: dedicated InstaBox `cactusx-android14-ui-test`, Android 14 x86_64, 4 vCPU/8 GiB, template `android` (2026-11-02.7-android.1).

Test subject: **UI-only Java harness**, `com.halthinks.cactusx.smoke`, built from PR #2 branch. Not the ARM64 Cactus AI APK.

| Check | Observed evidence | Result |
|---|---|---|
| Gradle build | Gradle 8.7, Android plugin 8.5.2, SDK 34; `assembleDebug` 32 tasks succeeded | PASS |
| Install | `adb install -r .../app-debug.apk` → `Success` | PASS |
| Launch | `adb shell am start -n com.halthinks.cactusx.smoke/.SmokeActivity` → `Displayed` | PASS |
| UI presence | uiautomator hierarchy shows Needle, Whistle, Mirage controls | PASS |
| Needle button | status displays `UI route invoked ... (NO INFERENCE)` | PASS UI ONLY |
| Audio permission prompt | Android displayed `Allow CactusX UI Smoke to record audio?` | PASS |
| Audio permission granted | `pm grant` then button → `Microphone permission granted (NO TRANSCRIPTION)` | PASS UI ONLY |
| Image picker | Android media picker opened | PASS |
| Picker cancel | Back returned to app and displayed `Photo picker cancelled` | PASS |
| Crash scan | No `FATAL EXCEPTION` found in bounded logcat scan | PASS bounded |
| Binary checksum | `d86bfa94cb07ac2d98ad305542006a46fb3d2f8b312b54f9b2e3a252232a8f6f` | RECORDED |

**Limitations:** This is a deliberately separate x86_64 UI harness. It does not load Needle 3, Whistle, Mirage, Cactus native runtime, or perform inference. Results do not qualify ARM64 behavior or physical Samsung S26 Ultra usability.

The persistent ARM64 APK remains on `cactusx-android-s26`, checksum `d31d46e8b2848abd84e9cd2475092c64d864321a4f8cabc4d8e956b055155cbe`. Delivery and physical device validation remain open.
