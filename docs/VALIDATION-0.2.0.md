# Validation record

12 September 2026. Personal preview 0.2.0. Native Kotlin / Jetpack Compose.

## Completed

| Verification | Result |
| --- | --- |
| Clean Android compilation and APK packaging | Passed |
| Gain units, 30 dB conversion, EQ and comparison settings | 6 JVM tests passed |
| Native gain-controller boundary, readback, rejection and reduction failures | 5 fake-port tests passed |
| Upgrade migration, full-range persistence and saved profiles | 5 simulated Android tests passed |
| Native UI controls, full range, first-use flow and readback presentation | 3 simulated Android tests passed |
| Android lint | 0 errors; 1 advisory about a newer Robolectric test dependency |
| Native screenshots | Updated Listen and Settings rendered and inspected; comparison UI inspected using an explicit engine-state fixture |
| APK signature | APK Signature Scheme v2 verified; certificate matches delivered 0.1.0 APK |
| Application identity | Same package; versionCode increased from 1 to 2; versionName 0.2.0 |
| APK alignment | 16 KiB ZIP/native-library alignment check passed |
| DEX integrity | 6 DEX files; 25,817 unique class definitions; no duplicate classes or temporary entries |
| Permissions | Unchanged; no internet, recording, storage, notification-reading or accessibility-service access |

All 19 tests passed with zero failures/errors. Build command:
`./gradlew :app:clean :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
The pinned Gradle distribution ran directly with workspace-only proxy/trust
configuration. One initial build encountered a transient workspace synchronization
path. The final clean build used an isolated temporary directory containing the
same application source; no build failure was suppressed.

Robolectric ran Android API 35, with native graphics and a 412 × 915 dp / 2×
density configuration. The gain-controller tests use fake Android IO ports to
verify requests and returned values; they do not process PCM audio. Native
screenshots render the Android view tree. The comparison UI test deliberately
injects a differing reported target to check presentation, not claim device
support. No physical Pixel or acoustic measurement was available.

## Delivered APK

- Filename: `Auralift-0.2.0-preview.apk`
- Size: 17,551,536 bytes
- Application ID: `com.jamiewardle.auralift`
- Minimum API: 26; compile/target API: 36
- Version code: 2
- SHA-256: `80ff49044d69f3a729043cc3fa43a57946125deb600815a74d585b2e4c57da1b`
- Signing certificate SHA-256: `351d23947b918de4564e87f2c9f8f25fa54a48ab5afa431e0cf0f54b1b8577b7`

The signed APK is a separate downloadable artifact, not checked into Git.
This debug certificate matches the previous preview, allowing an in-place
update with preferences retained; production signing still needs a separate
decision. No signing keys are committed.

## Device evidence and limits

The user reports little or no audible difference with preview 0.1.0 on the
Pixel 9a. The selected gain, player and output are not yet established.
Version 0.2.0 removes the app’s former 18 dB cap, expands the initial range
from 6 to 30 dB, reads native parameters back and adds a comparison control.
It does not establish why the original test sounded unchanged.

Audible amplification, actual 30 dB device support, effect routing, Bluetooth/USB,
call/alarm interactions, Android 17 service behavior, TalkBack, largest fonts and
battery behavior still need the [physical test plan](PIXEL-TEST-PLAN.md).
A target readback confirms an effect parameter, not that a player passes audio
through the effect or that the sound reaching the listener is 30 dB louder.

The previous build’s validation is retained in [VALIDATION-0.1.0.md](VALIDATION-0.1.0.md).
Screenshots: [Listen](screenshots/listen.png), [Sound](screenshots/sound.png),
[Settings](screenshots/settings.png). Sound is unchanged from the original preview.

## Compatibility review — 13 September 2026

The owner now reports success with 0.2.0 on the Pixel 9a. This supersedes the
absence of any positive user listening report at the time of the build above;
it does not establish a result for each player, output or checklist item.

Source configuration confirms Android 8.0/API 26 minimum and Android 16/API 36
target, with no Pixel restriction. Static inspection of the delivered APK,
verified against its recorded SHA-256, found ARM 32-bit/64-bit and x86/x86-64
versions of the native graphics library. ZIP and LOAD-segment 16 KB alignment
checks passed. The GNU_RELRO-end check flagged the graphics library, requiring
release-artifact and 16 KB runtime investigation before declaring full support.
This was not an observed crash; no new runtime test or release build was run.

The documentation changes in this review require no replacement APK. Previous
19-test results remain historical build evidence. See the
[compatibility plan](ANDROID-COMPATIBILITY.md) and
[Play release plan](GOOGLE-PLAY-PLAN.md) for open requirements.
