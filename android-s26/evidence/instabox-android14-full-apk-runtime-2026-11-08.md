# CactusX full APK — InstaBox Android 14 runtime evidence

Date: 2026-11-08 UTC (2026-11-07 PST)
Test environment: existing InstaBox machine `cactusx-android14-ui-test`, Android template `2026-11-02.7-android.1`; Android 14 / API 34; ADB serial `emulator-5554`; redroid x86_64 with reported ABI list `x86_64,arm64-v8a,x86,armeabi-v7a,armeabi`.
**This is a redroid virtual Android environment, not a physical Samsung Galaxy S26 Ultra and not Android 16.**

Artifact: `android-s26/app/build/outputs/apk/debug/app-debug.apk` built on the existing `cactusx-android-s26` machine, copied into the Android 14 test machine using the user-owned private InstaBox port, checksum-verified before installation.
SHA-256: `9f66304b353421ced6793327b1cecbfec813bb589bd32463b92d2be807f2db89`.
APK size previously recorded: 74,432,096 bytes.

## Observations

| Check | Observed evidence | Result |
| --- | --- | --- |
| Install | `adb install -r CactusX-S26-debug.apk` → `Success` | PASS (Android 14 redroid) |
| Launch | `adb shell am start -W -n com.halthinks.cactusx/.MainActivity` → `Status: ok`, `LaunchState: COLD`, `TotalTime: 1248` ms | PASS |
| Process | `adb shell pidof com.halthinks.cactusx` returned a live process | PASS |
| Native model initialization | UI status reported `All three models loaded • On-device` | PASS (reported initialization only) |
| Needle approval | Submitted `time`; app proposed `get_time` with `Arguments: {}` and displayed `Approve phone action?`; Approve generated `Device time: Nov 8, 2026 1:29:02 AM` | PASS |
| Needle denial | Submitted another time request and tapped Deny; UI displayed `Action denied: get_time` | PASS |
| Whistle flow | Android microphone permission requested and accepted; app recorded a 3-second voice note; on silent test device output was `Whistle detected no speech` | PASS for permission/recording/no-speech handling; speech recognition accuracy UNTESTED |
| Mirage flow | Test image copied to Android Pictures, picked through Android photo selector, run through Mirage; app local history contained a user `Photo attached` event and assistant scene JSON with `scene.background = dark gray`, `scene.style = digital interface`, and **29 objects**, with object names, bounding boxes, confidence scores and masks. | PASS for picker → image pipeline → structured output; visual accuracy NOT independently assessed |

## Reproduction commands

```bash
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
sha256sum CactusX-S26-debug.apk
adb install -r CactusX-S26-debug.apk
adb shell am start -W -n com.halthinks.cactusx/.MainActivity
adb shell pidof com.halthinks.cactusx
adb shell uiautomator dump /sdcard/window.xml
adb exec-out screencap -p > full-apk-chat-smoke.png
```

The interactive tests were performed via ADB tap/text input and checked against the accessibility hierarchy. Mirage structured output was checked by reading the **test app's own local history** on the redroid instance with `adb shell run-as com.halthinks.cactusx cat shared_prefs/MainActivity.xml`. History was enabled only for this test.

## Not established

- Samsung Galaxy S26 Ultra / Android 16 installation, launch, compatibility or performance.
- Correctness of Mirage's object detection beyond the presence of valid structured output.
- Whistle recognition of actual spoken words, environmental audio quality or microphone hardware behavior.
- Needle's full arbitrary natural-language quality, extended tool coverage, performance/memory/thermal behavior, or security beyond the approval and denial paths exercised above.

The Samsung Remote Test Lab reservation and its authenticated cloud browser are separate from the InstaBox ADB environment. Samsung RDB was not paired. No new Samsung reservation or credit was consumed for this evidence.
