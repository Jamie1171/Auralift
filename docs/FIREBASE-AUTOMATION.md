# Auralift targeted device testing

Prepared 14 September 2026. Use the matching **app** and **tests** APKs from one
Firebase kit. They are signed together. The previous Robo APK has a different
test certificate and must not be paired with this instrumentation APK.
These debug APKs are for isolated cloud/emulator tests, not the Play closed track.

## Upload from your phone

1. Firebase → Auralift → Test Lab → Run a test → **Instrumentation**.
2. App APK: select the file ending **-app.apk**.
3. Test APK: select the file ending **-tests.apk**.
4. Select devices, one Android version per device, **English (United States)**,
   portrait. Start with Pixel 8/API 35 and Galaxy A54/API 34 when quota permits.
   The four tests already run today plus the first Pixel consume the assumed
   five-physical-execution daily allowance; check the console's remaining quota.
5. Use a **10-minute timeout** for the normal suite. This includes a real two-minute
   timer check and a short 30-second screen-off check. A five-minute crawl and this
   suite are different tests. Check the duration/quota allowed by your current plan.
6. Leave sharding, Orchestrator and other optional settings off for the first run.
   Tests clean up their own app state. If a grant is unavailable, retain the failure
   details; do not treat an unexecuted permission test as passed.
7. Optional additional file directory to collect:
   `/sdcard/Android/data/com.jamiewardle.auralift/files/auralift-test-results`.
   The same diagnostics are also written under **AuraliftDeviceTest** in logcat.
8. Send the results summary and the failed test's stack trace/logcat, if any.
   The result page, screenshots and video help distinguish an app failure from
   device provisioning or an automation selector problem.

If Firebase exposes instrumentation environment variables, `soakSeconds=1200`
selects a 20-minute screen-off service run. Use a 30-minute timeout for that run
and confirm the plan permits it. GitHub's **Run workflow** offers the same
1,200-second check on API 36. Routine pull requests use 30 seconds; the initial
validation run separately requests 1,200 seconds. See the recorded results before
treating either duration as passed.

## What is checked

| Test group | Evidence | What remains unproven |
| --- | --- | --- |
| First use and Free controls | Consent cancellation, Free cap, settings after activity recreation | Every screen/layout and production upgrade |
| Native service | Start, notification Stop, app Stop, memory-off reset | Audible gain through speakers/headphones |
| Player sessions | Real muted AudioTrack session, repeated open/close, system/player mode changes | Third-party player cooperation, offload/bypass |
| Compare and capability | Real Android effect state/readback or explicit unsupported handling | Measured loudness, distortion or perceived clarity |
| Entitlements | Internal purchased-state and expired reward-clock fixtures; persisted gain clamp; no automatic increase on renewal | Ad delivery, paid transactions, refunds/restore via Play |
| Ad interruption | Effects suspended, reconnect blocked, late resume cannot restart Stop | Actual ad playback/SDK callbacks and consent UI |
| Floating controls | Real overlay permission, gain button, close, screen-off, grant removal, Pro loss | Every OEM/protected screen, physical touch usability |
| Timer | Cancellation survives old deadline; actual replacement deadline stops | Idle alarm delays and real-device long sleep fade |
| Screen-off session | 30-second default; manual CI/API 36 can request 1,200 seconds | Natural OEM battery management or uninterrupted real audio |
| Optional permissions | Separate host-driven run with microphone/notifications denied | First-prompt UX across every manufacturer |

`linked=false` is recorded as unavailable, not labelled an audio success. A test
can pass **unsupported-effect handling** while that device cannot boost its route.
Android readback is an effect parameter, never a speaker measurement.

The controlled audio fixture is silent/muted and exists only in the test APK.
It is used to test session ownership and whether Stop leaves that test track
playing. The application does not acquire a new silent-playback mechanism.
Purchase/reward fixtures are likewise test-only and do not validate monetisation.

## Permissions and result accounting

`PermissionDeniedDeviceTest` requires permissions to be denied **before** the
instrumentation process starts. Android can kill the app on revocation, so the
suite does not revoke a live runner's runtime permissions. A cloud runner that
pre-grants them will report that case **skipped**, with an explanation in JSON.
The CI script first executes the normal suite excluding this case, then revokes
the permissions from the host, launches the case separately and requires a
recorded pass. A skip is never counted as denied-permission coverage.

Do not use Orchestrator with clearPackageData for collecting the diagnostic JSON;
it can erase the files between cases. Do not run the fixtures on a personal
installation whose settings or sound profiles you want to retain.

## Reproduce on an emulator

```sh
./gradlew :app:assemblePlayDebug :app:assemblePlayDebugAndroidTest
AURALIFT_SOAK_SECONDS=30 bash scripts/device-test.sh
```

The workflow runs real Android emulators on API 26 and 36, separate from the
existing Robolectric unit/native-render tests on API 26/35. CI reports and JSON
are preserved even on failure. Native ARM/16 KB release checks remain separate;
an x86-64 emulator pass does not close that release gate.

Sources: [Firebase instrumentation](https://firebase.google.com/docs/test-lab/android/instrumentation-test),
[UI Automator](https://developer.android.com/training/testing/other-components/ui-automator),
[AndroidX Test releases](https://developer.android.com/jetpack/androidx/releases/test).
