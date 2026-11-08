# Samsung Remote Test Lab: isolated ARM64 qualification

This is **not** a personal-phone test. The device must be a Samsung Remote Test Lab reservation or another explicitly dedicated lab handset. It is never safe to infer a native model PASS from the Android 14 x86_64 UI-only InstaBox harness.

## Required execution topology

Run the **Samsung web client and Samsung-provided Linux RDB executable on the same InstaBox Linux desktop**, alongside `adb` and the verified ARM64 APK. RDB is the supported link between Remote Test Lab and local ADB; a browser in one cloud environment cannot automatically use RDB or files located on a separate InstaBox. Never copy account secrets to the box through scripts or logs.

Samsung source: https://developer.samsung.com/remote-test-lab/blog/en/2022/07/13/connect-to-devices-on-remote-test-lab-using-rdb-in-android-studio

1. Prepare a single existing isolated InstaBox with Chromium/desktop, Android platform-tools, and the APK.
2. In its own browser session, authenticate to Samsung through the interactive account flow. Do not automate passwords, 2FA, CAPTCHA, or account creation, or persist credentials in repositories.
3. Download Samsung's Linux RDB ZIP **from the Remote Debug Bridge menu in that same web client**. Inspect the archive and run the provided `rdb` binary according to Samsung's instructions. Do not invent an RDB download URL or package an unofficial binary.
4. In the web client choose Remote Debug Bridge → Connect. Confirm that `adb devices` sees **one** authorized device; if it doesn't, **do not start installation attempts**.
5. Verify the expected APK SHA-256 independently. Example for the 2026-11-07 debug build: `9f66304b353421ced6793327b1cecbfec813bb589bd32463b92d2be807f2db89`. Recompute for every new build.
6. Invoke `bash android-s26/tools/rtl_adb_smoke.sh APK EXPECTED_SHA256 EVIDENCE_DIR`. The runner verifies ARM64 and SDK, installs, launches, captures a screenshot, logcat and activity state, and reports only an **attempt** until the evidence has been inspected.
7. Separately exercise Needle, Whistle, Mirage and their failure modes. Record each model's exact source, hash, UI outcome, native logs, model load latency, memory, thermal behavior, and permission behavior. No model is marked passed by the installation smoke runner.
8. Retain sanitized test evidence in the authorized GitHub PR. Remote Lab policies prohibit personal data on the device. Remove device-side test artifacts using Samsung's normal session cleanup where allowed.

## Current limitation

The 2026-11-07 Samsung session was reserved for two hours (8 credits) in a distinct cloud browser. Device `SM-S948U-US01` / Android 16 was identified, but the display remained black in that client and the APK was **not installed**. The private APK transfer attempted in that browser was rejected by its URL safety policy; do not circumvent that restriction. The legitimate path is to provision the documented co-located RDB + ADB workflow for a future session. Do not spend further Samsung credits until device pairing and APK supply are ready.

## Acceptance states

- RDB ready: Samsung web-client RDB status connected **and** `adb devices` recognizes the isolated test phone.
- Install: `adb install` succeeds, package is present.
- UI: launcher starts, screenshot is recognizable and logcat has no bounded fatal startup failure.
- Native models: each of Needle, Whistle and Mirage has independently observed inference output and retained evidence.
- TextPCB: preview-only proposals against canonical command registry; authenticated, approval-gated execution and TextPCB receipts must be tested separately.
