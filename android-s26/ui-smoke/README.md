# Android 14 UI-only test flavor

This standalone **x86-compatible Java-only** build checks Android UI, permissions and photo-picker behavior. It is intentionally NOT an inference build, NOT the Samsung deliverable, and never claims to exercise Needle, Whistle, Mirage or ARM64 native Cactus. Build with `gradle assembleDebug` from this directory, install with `adb install -r app/build/outputs/apk/debug/app-debug.apk`, and launch `adb shell am start -n com.halthinks.cactusx.smoke/.SmokeActivity`. Compare framework flow with production Activity separately.
