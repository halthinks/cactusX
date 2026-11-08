# Android 16 official emulator runtime attempt — 2026-11-08

## Environment
- Existing InstaBox build host: `cactusx-android-s26`, 8 vCPU / 16 GiB, no `/dev/kvm`.
- Installed official Android Emulator 37.2.13.0, Android SDK Platform 36, Google APIs Android 16 x86_64 system image.
- Emulator launched with `-no-window -no-audio -no-snapshot -no-boot-anim -gpu swiftshader_indirect -accel off -memory 3072 -cores 4 -port 5564`.
- Emulator AVD userdata at `/var/tmp/cactusx-android16-google.avd` on root filesystem; APK and source unchanged.

## Verified Android guest facts
- `adb devices` showed `emulator-5564 device`.
- `getprop ro.build.version.release` = `16`.
- `getprop ro.build.version.sdk` = `36`.
- `getprop ro.dalvik.vm.native.bridge` = `libndk_translation.so`.
- `getprop ro.product.cpu.abilist` = `x86_64,arm64-v8a`.
- Android `zygote64`, `surfaceflinger` and `system_server` launched.
- The `package` service eventually registered, but `sys.boot_completed` remained empty after over nine minutes of software CPU emulation.
- Attempt to install exact API36 app debug APK was rejected first with `Error: device is still booting`, later with `Can't find service: package` during a `system_server` restart. Crash buffer recorded `DeadSystemException: The system died` for system UI/phone/permission controller.
- **No APK installation or Android 16 CactusX runtime pass is claimed.**

## Other findings
- Android 16 official AOSP ATD x86_64 image started and reported API36, but did not offer ARM64 native translation. A dual-ABI attempt failed linking x86_64 because the only Cactus native static library is `native/arm64-v8a/libcactus.a`; the trial change was reverted.
- Android 16 ARM64 official emulator image cannot boot under the x86_64 QEMU2 host (`Avd's CPU Architecture 'arm64' is not supported`).
- Android 16 redroid regular and 64-only images both exited 129 on the InstaBox Android kernel.
- To make this test robust: run Android 16 with KVM on an existing KVM-capable host, or obtain an x86_64 Cactus core binary built from the original native sources for the faster x86 emulator. Do not treat API36 compile success as runtime compatibility proof.
