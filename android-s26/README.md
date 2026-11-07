# CactusX S26 Android prototype

This is an Android **debug prototype** that loads Needle 3, Whistle and Mirage into the shared Cactus native engine. It includes a simple UI for structured tool routing, recording a 3-second microphone sample, and selecting an image for visual description. It does **not** automatically execute model-proposed tools.

## Build on Linux

Requires Java 17+, Android SDK API 35 / build tools 35, Android NDK 27, CMake 3.22, Gradle 8.10.2 and Python 3.9+.

From the repository root:

```sh
python3 -m venv .venv
.venv/bin/pip install -e .
.venv/bin/cactus download android-arm64 --out /tmp/cactus-platform
.venv/bin/cactus download needle3 --out /tmp/cactus-weights
.venv/bin/cactus download whistle --out /tmp/cactus-weights
.venv/bin/cactus download mirage --out /tmp/cactus-weights
mkdir -p android-s26/native/arm64-v8a android-s26/runtime-assets
cp /tmp/cactus-platform/android-arm64/{libcactus.a,cactus.h} android-s26/native/arm64-v8a/
cp /tmp/cactus-weights/*.cact android-s26/runtime-assets/
cd android-s26
gradle assembleDebug
```

The resulting APK is `app/build/outputs/apk/debug/app-debug.apk`. The Gradle configuration deliberately packages models offline; the weights and platform binary are excluded from Git. Verify source, runtime version, and model hashes before distributing.

## Verification completed

- Linux engine: Needle 3 tool selection, Whistle silence transcription and Mirage synthetic-image structured description.
- Android: `assembleDebug` succeeded on an 8-vCPU / 16-GB InstaBox using the Android ARM64 native Cactus archive.
- **Not yet validated:** installation/launch on a Samsung Galaxy S26 Ultra; microphone, photo picker, NPU, battery, throughput and repeated model switching.

## Safety and limitations

The app holds all three models in a single process; native calls are serialized on a background executor and a mutex. Speech recording is user-initiated only. The model's proposed tool call is displayed as JSON and **never executed automatically**. This is an engineering prototype, not an OS-level assistant. Debug APKs must not be distributed as production-signed releases.

See [Cactus](https://github.com/cactus-compute/cactus) for the upstream runtime and API.
