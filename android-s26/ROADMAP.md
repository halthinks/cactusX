# CactusX S26 Android Roadmap

Status: **prototype built, device execution unverified**. Source: [PR #2](https://github.com/halthinks/cactusX/pull/2).

## Acceptance gates (in dependency order)

| ID | Work | Exit evidence | State |
|---|---|---|---|
| A01 | Preserve prototype source and provenance | Git branch, native/runtime/model hashes, reproducible build instructions | PARTIAL — source in PR #2 |
| A02 | Verify debug APK integrity | APK signature, package ID, native ABI, assets, install constraints, checksum retained | IN PROGRESS |
| A03 | Deliver installable artifact | Uploaded binary (prefer Google Drive), SHA256 match after download, phone-accessible link | BLOCKED — InstaBox binary not yet transported |
| A04 | Run Android 14 UI tests | Android container adb install/launch/permission/photo picker/crash log, using x86-compatible UI test flavor | COMPLETE for UI-only harness — evidence/android14-ui-smoke-20261107.md; native inference untested |
| A05 | Validate ARM64 runtime on Galaxy S26 Ultra | Install, launch, load all engines, capture device logs and version | BLOCKED on A03 |
| A06 | Validate three models separately | Needle tool JSON, Whistle mic/noise/silence, Mirage images and refusal/error paths | BLOCKED on A05 |
| A07 | Stabilize lifecycle and resource use | Repeated switching, process recovery, peak memory, cold/warm latency, thermal, battery | PLANNED |
| A08 | Unified user-controlled pipeline | Speech → intent → optional image → answer; explicit approval for real tool actions | PLANNED |
| A09 | Production delivery | Release signing, installer, privacy/permissions, reproducible build, regression suite | PLANNED |

## Architectural constraints

- Cactus 4.1.0 publishes `android-arm64` and `android-armv7` but **not Android x86_64**. InstaBox Android 14 is x86_64. Its real Android container can verify UI/install behavior with an x86-compatible test flavor; it cannot exercise the ARM64 Cactus native engine.
- Keep the ARM64 production-target variant separate from x86 UI-only tests, and label simulation/mock outputs clearly.
- The current APK is a **debug prototype**, not a production app. Do not mark real-device tests passed without captured evidence.
- Model files and native runtime are excluded from git; archive exact source URLs, versions, SHA256s and license review. Do not commit binary weights.
- Never automatically execute proposed tools; user authorization must gate any device action.
- No Google Play dependencies for core offline functions.

## Next implementation tranche

1. Collect `sha256sum`, `apksigner verify --verbose`, `aapt dump badging`, and `zipinfo` evidence on the existing APK.
2. Create repeatable artifact integrity validation and a manifest with APK/native/model hashes.
3. Establish transfer from the persistent InstaBox file to Drive or a permitted binary release endpoint; verify bytes after transfer.
4. Add a distinct x86_64 UI smoke-test build without native Cactus execution; use it solely for Android framework/permission tests.
5. Run ADB smoke tests on Android 14 and retain output in GitHub.
6. Test native ARM64 inference on the user's S26 Ultra, then address failures and build release candidate.

## Current artifact

On InstaBox `cactusx-android-s26`: `/data/workspace/cactusX/android-s26/app/build/outputs/apk/debug/app-debug.apk`

Last observed SHA256: `d31d46e8b2848abd84e9cd2475092c64d864321a4f8cabc4d8e956b055155cbe`. Recompute before release.

## A04 execution note

On 2026-11-07 UTC, the distinct x86_64 UI-only harness built and passed ADB installation, launch, three-button UI presence, Needle button interaction, Android microphone permission prompt/grant, media picker launch/cancel, and bounded crash-log scan. Full evidence: [Android 14 UI smoke](evidence/android14-ui-smoke-20261107.md). This does not validate native ARM64 models.
