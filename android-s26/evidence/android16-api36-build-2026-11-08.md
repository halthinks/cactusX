# CactusX Android 16 / API 36 build evidence — 2026-11-08

## Build result
- Host: existing InstaBox `cactusx-android-s26`, 8 vCPU / 16 GiB.
- Android SDK Platform 36 installed via Android command-line SDK manager.
- App Gradle config: `compileSdk 36`, `targetSdk 36`, `minSdk 28`.
- Native build: `/data/gradle/gradle-8.10.2/bin/gradle --offline --no-daemon :app:assembleDebug`.
- Result: **BUILD SUCCESSFUL** (35 tasks, 10 executed).
- Artifact: `/data/cactusx-unified-build/android-s26/app/build/outputs/apk/debug/app-debug.apk`.
- Size: 74,432,096 bytes.
- SHA-256: `d927d97997d1cddae5d2a872c8308a746ead934d2bd5e49a6ddea2a3bbc0127d`.
- Android build-tools `aapt dump badging` reports `platformBuildVersionName='16'`, `compileSdkVersion='36'`, `targetSdkVersion:'36'`, package `com.halthinks.cactusx`.

## Runtime qualification boundary
- Existing InstaBox Android runtime is Android 14 and previously passed full APK UI smoke tests.
- Android 16 `redroid/redroid:16.0.0-latest` (digest sha256:d89f5204...) and `redroid/redroid:16.0.0_64only-latest` (digest sha256:7b1e389b...) were pulled and attempted separately with software rendering and memfd on the Android host (kernel `6.18.60-instabox-android`).
- Both Android 16 images exited with code **129** during boot, including with the Android 14 system service stopped. Consequently **no Android 16 runtime app-test pass is claimed**.
- The original Android 14 systemd service was restored to active running state.
- Android Gradle Plugin 8.7.3 emits an unsupported compileSdk 36 warning; build succeeds but a follow-up AGP upgrade should remove that warning after compatibility verification.

## Required next test
Boot an Android 16 device/emulator with verified `ro.build.version.sdk=36`, install this exact APK, and exercise Needle 3 approval/denial, Whistle permission/recording and Mirage image analysis. Physical Galaxy S26 behavior remains a separate acceptance claim.
