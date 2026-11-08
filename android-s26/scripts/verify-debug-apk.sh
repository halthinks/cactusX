#!/usr/bin/env bash
set -euo pipefail
apk="${1:-app/build/outputs/apk/debug/app-debug.apk}"
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/data/android-sdk}}"
test -f "$apk"
sha256sum "$apk"
"$sdk/build-tools/35.0.0/apksigner" verify --verbose "$apk"
"$sdk/build-tools/35.0.0/aapt" dump badging "$apk" | grep -E "^(package:|sdkVersion:|targetSdkVersion:|native-code:)"
unzip -t "$apk" | tail -1
