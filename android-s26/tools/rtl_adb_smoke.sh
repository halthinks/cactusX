#!/usr/bin/env bash
# Isolated Samsung Remote Test Lab ARM64 smoke test; never targets a personal device.
set -Eeuo pipefail

usage() {
  printf 'Usage: %s APK_PATH EXPECTED_SHA256 EVIDENCE_DIRECTORY\n' "$0" >&2
  printf 'Set ADB_SERIAL explicitly if more than one adb target is connected.\n' >&2
  exit 2
}
[[ $# -eq 3 ]] || usage
apk=$1
expected=$2
evidence=$3
[[ -f "$apk" && "$expected" =~ ^[0-9a-f]{64}$ ]] || usage
command -v adb >/dev/null || { echo 'adb unavailable' >&2; exit 2; }
command -v sha256sum >/dev/null || { echo 'sha256sum unavailable' >&2; exit 2; }
actual=$(sha256sum "$apk" | awk '{print $1}')
[[ "$actual" == "$expected" ]] || { echo 'APK SHA256 mismatch' >&2; exit 3; }

if [[ -n "${ADB_SERIAL:-}" ]]; then
  serial=$ADB_SERIAL
else
  mapfile -t serials < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  [[ ${#serials[@]} -eq 1 ]] || { echo 'Exactly one adb device required, or set ADB_SERIAL' >&2; exit 4; }
  serial=${serials[0]}
fi
adb_cmd=(adb -s "$serial")
[[ "$("${adb_cmd[@]}" get-state)" == device ]] || { echo 'ADB target unavailable' >&2; exit 4; }
abi=$("${adb_cmd[@]}" shell getprop ro.product.cpu.abilist | tr -d '\r')
sdk=$("${adb_cmd[@]}" shell getprop ro.build.version.sdk | tr -d '\r')
model=$("${adb_cmd[@]}" shell getprop ro.product.model | tr -d '\r')
if [[ ",$abi," != *,arm64-v8a,* || ! "$sdk" =~ ^[0-9]+$ || "$sdk" -lt 28 ]]; then
  echo "Incompatible target: ABI=$abi SDK=$sdk" >&2
  exit 5
fi
mkdir -p "$evidence"
{
  printf 'status=attempted\n'
  printf 'serial=%s\nmodel=%s\nabi=%s\nsdk=%s\n' "$serial" "$model" "$abi" "$sdk"
  printf 'apk_sha256=%s\napk_bytes=%s\n' "$actual" "$(stat -c '%s' "$apk")"
  printf 'test_scope=install_launch_only; native inference not proven\n'
} > "$evidence/device.txt"
"${adb_cmd[@]}" logcat -c || true
if ! "${adb_cmd[@]}" install -r "$apk" > "$evidence/install.txt" 2>&1; then
  "${adb_cmd[@]}" logcat -d -v threadtime > "$evidence/logcat.txt" || true
  echo 'APK installation failed; evidence retained' >&2
  exit 6
fi
if ! "${adb_cmd[@]}" shell am start -W -n com.halthinks.cactusx/.MainActivity > "$evidence/launch.txt" 2>&1; then
  "${adb_cmd[@]}" logcat -d -v threadtime > "$evidence/logcat.txt" || true
  echo 'APK launch failed; evidence retained' >&2
  exit 7
fi
"${adb_cmd[@]}" exec-out screencap -p > "$evidence/screen.png" || true
"${adb_cmd[@]}" logcat -d -v threadtime > "$evidence/logcat.txt" || true
"${adb_cmd[@]}" shell dumpsys activity activities > "$evidence/activities.txt" || true
printf 'status=install_and_launch_attempted\n' >> "$evidence/device.txt"
echo "Retained evidence in $evidence; inspect logs and screenshot before PASS."
