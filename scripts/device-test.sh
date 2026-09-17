#!/usr/bin/env bash
# Isolated lab/emulator only. Changes permissions of the test installation.
set -euo pipefail
package=com.jamiewardle.auralift
denied_test=com.jamiewardle.auralift.device.PermissionDeniedDeviceTest
results=build/device-results
mkdir -p "$results"
collect() {
  adb logcat -d > "$results/logcat.txt" 2>&1 || true
  adb pull "/sdcard/Android/data/$package/files/auralift-test-results" "$results/" > "$results/pull.txt" 2>&1 || true
}
trap collect EXIT

./gradlew :app:connectedPlayDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.notClass="$denied_test" \
  -Pandroid.testInstrumentationRunnerArguments.soakSeconds="${AURALIFT_SOAK_SECONDS:-30}"

# Permission revocation must happen outside instrumentation because Android kills
# the target process. Reinstall in case the Gradle device runner removed the APKs.
adb install -r -t app/build/outputs/apk/play/debug/app-play-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/play/debug/app-play-debug-androidTest.apk
adb shell pm revoke "$package" android.permission.RECORD_AUDIO
api=$(adb shell getprop ro.build.version.sdk | tr -d '\r')
if [ "$api" -ge 33 ]; then
  adb shell pm revoke "$package" android.permission.POST_NOTIFICATIONS
fi
adb shell am instrument -w -r -e class "$denied_test" \
  "$package.test/androidx.test.runner.AndroidJUnitRunner" | tee "$results/permission-denied-run.txt"
collect
python3 - <<'PY'
import json, pathlib, re
root = pathlib.Path('build/device-results')
output = (root / 'permission-denied-run.txt').read_text()
assert re.search(r'OK \(1 test\)', output), output
report = root / 'auralift-test-results/PermissionDeniedDeviceTest-deniedMicrophoneAndNotificationsStillAllowBoostAndStop.json'
assert report.is_file(), 'Missing denied-permission evidence'
data = json.loads(report.read_text())
assert data['result'] == 'passed', data
print('Denied-permission path: one executed test passed (not skipped).')
PY
