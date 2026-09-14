# Android compatibility

Reviewed 13 September 2026; updated for Owner/public preview 0.5.0. This is a support and testing
plan, not a claim that all Android devices have passed audio tests.

## Current requirements

| Term | Auralift setting | What it means |
| --- | --- | --- |
| Minimum Android version (`minSdk`) | Android 8.0 / API 26 | The oldest OS allowed to install the app |
| Target Android version (`targetSdk`) | Android 16 / API 36 | The modern platform behaviour and permission rules the app opts into; it does not raise the minimum |
| Compile SDK | API 36 | APIs available to the build tools |
| Phone manufacturer | No Pixel or manufacturer allowlist | Uses public Android audio APIs and checks effect availability |
| Processor support in the delivered APK | ARM 32-bit, ARM 64-bit, x86, x86-64 | Native libraries are packaged for all four architectures |
| Display | Native, scrolling Compose interface | Phone and tablet layouts need testing at narrow widths, large text and landscape |
| RAM / CPU speed | No measured minimum established | Do not advertise an invented RAM or GHz requirement; profile on an older low-memory device |
| Storage | See current artifact sizes in [validation](VALIDATION.md) | Download size differs from installed size; Play serves device-specific packages |
| Audio | A supported native effect and compatible media route | OS eligibility alone does not establish that another app's audio is boosted |

Phone age is not the criterion: its installed Android version and supported
hardware matter. Android 7 and earlier are outside the current build's support
range. Do not lower the minimum without dependency, UI and lifecycle validation.

Both 0.5.0 flavours target general Android devices. The Owner APK uses a separate
package and installs alongside 0.2.0, with fresh preferences; stop the old booster
before trying the new edition. No Pixel-specific code path is used. Installation eligibility and successful audio processing are separate
checks. Android documents that effect attachment uses audio sessions; global-mix
attachment is deprecated. Some players or outputs can bypass the connected effect.
[AudioEffect](https://developer.android.com/reference/android/media/audiofx/AudioEffect),
[LoudnessEnhancer](https://developer.android.com/reference/android/media/audiofx/LoudnessEnhancer).

## Evidence so far

On 14 September Jamie supplied Firebase screenshots showing **five passing Robo
executions**: Pixel 5/API 30, Galaxy S24 Ultra/API 36, Galaxy A54 5G/API 34,
moto g 5G (2022)/API 33 and Pixel 8/API 35. All used en-US/portrait. These are
five device/OS combinations across three manufacturers, not measurements of
audible boost or confirmation of every feature. Logs/videos have not been
independently reviewed. See [targeted automation](FIREBASE-AUTOMATION.md) and
[human testing goals](HUMAN-TEST-GOALS.md) for remaining coverage.

| Evidence | Result | Limit |
| --- | --- | --- |
| 0.2.0 automated checks, 12 September | 19 tests passed; lint zero errors; APK verified | Simulated API 35 framework and fake effect IO; no acoustic measurement |
| Owner feedback, 13 September | Reports the update works great on the Pixel 9a | Exact OS build, player, selected gain, output and individual features not recorded |
| Build configuration and manifest review | minSdk 26, targetSdk 36; no brand restriction or maximum SDK declared | Does not certify future Android versions |
| Delivered APK inspected, 13 September | Four CPU architectures; matching delivered SHA-256 | Static artifact inspection only |
| 16 KB memory layout | ZIP offsets and ELF LOAD segments are 16 KB aligned | RELRO-end alignment check flags the graphics library; runtime/release validation remains open |

Historical 0.2.0 artifact (current identities are in [validation](VALIDATION.md)):

APK SHA-256: `80ff49044d69f3a729043cc3fa43a57946125deb600815a74d585b2e4c57da1b`.
Raw inspection: [compatibility-artifact-inspection.json](compatibility-artifact-inspection.json).

That historical build bundled `androidx.graphics:graphics-path:1.0.1`, which has 16 KB LOAD alignment,
but its GNU_RELRO end is not a multiple of 16 KB. Treat this as a flagged check
under the current Android guidance, not an observed crash. Before release,
inspect the optimized release artifact and reproduce on a 16 KB runtime; evaluate
the current stable graphics dependency if necessary and rerun the checks.
Prior ZIP-alignment success alone is insufficient to close this item.
[Android's 16 KB validation guide](https://developer.android.com/guide/practices/page-sizes),
[Graphics library releases](https://developer.android.com/jetpack/androidx/releases/graphics).

## Test matrix before advertising broad support

These are coverage targets, not certification for every model from each brand.

| Test group | Coverage sought | Status |
| --- | --- | --- |
| Oldest supported OS | Android 8.0/API 26: install, launch, settings, notification, effect start/stop | Framework smoke/logic tests added in 0.3.0; physical install/audio pending |
| Older phone | Android 9–11, preferably 32-bit ARM and low memory | Pending |
| Modern permission transitions | Android 12, 13 and 14: service start, notification denial, widget/tile and Stop | Pending |
| Current Android | Android 15, 16 and 17: release build, background service and audio route changes | Pending |
| Google Pixel 9a | Initial successful user report; complete the detailed checklist | Partial evidence |
| Samsung | Recent Galaxy plus an older supported model | Pending |
| Additional manufacturers | At least one Xiaomi/Redmi and one OnePlus/Oppo/Motorola device, as testers permit | Pending |
| Other displays | Tablet, landscape, split screen, large text and TalkBack | Pending |
| Memory page size | 16 KB arm64 runtime and standard 4 KB runtime using the release artifact | Pending |

For each physical phone, try local media and at least one streaming player,
speaker output, and Bluetooth or USB headphones when available. Record system-mix
versus player-session mode. Check zero gain, a gradual increase, Compare original,
Restore, Stop, output changes, screen lock, playback restart and a longer session.
Use familiar speech/music; maximum gain is not a mandatory test step.

Emulators and automated test services are useful for crashes and layouts; an
audio effect returning a parameter is not evidence of acoustic output. Use the
[detailed checklist](PIXEL-TEST-PLAN.md) on every brand, despite its original filename.

Each result should retain: date, app version, model, Android version/build, player
and version, output, mode, selected gain, Android readback, comparison result and
any failure. Obtain only the details needed to reproduce the problem.

## Public support wording

Proposed wording: “Designed for Android 8.0 and newer. Volume boosting depends
on your device, media app and audio output. Try the comparison control to check
your setup.” Do not claim every Android phone, every audio source, measured
35 dB extra output, or medically calibrated hearing assistance.

Use Play Console's device catalogue and actual test results to manage unsupported
devices. Recheck compatibility after dependency and Android updates. Keep the
full compatibility check usable before any purchase is requested.

## 0.3.0 coverage and new feature checks

See [current validation](VALIDATION.md) for results against this version. API 26
and API 35 run separately under Robolectric because mixed SDK sandboxes encountered
a native-font ZIP conflict. Screenshot tests use native graphics on API 35.
These tests exercise startup, persisted settings, access expiry, profile validation,
localisation and owner UI transitions; they do not simulate real acoustic output.

New permission and interaction checks on each physical test phone:

- Deny/allow notifications, disable the audio-control channel and confirm Stop remains
  reachable. Test foreground service start/stop on the device's actual Android build.
- Enable/deny/revoke floating permission, expand/drag/collapse the panel, rotate,
  lock/unlock, return to the main app and try large text. Verify transport and EQ.
- Turn background operation off, leave the app and confirm effects are released;
  verify changing language/rotating does not unintentionally stop an active session.
- Turn spectrum on/off, deny audio permission, test a silent route, leave Listen,
  then background/foreground. No microphone recording or synthetic bars should occur.
- Try en/es/fr, follow-system and offline switching. AAB language splitting is
  disabled so all supported strings are installed even from Google Play.
- Save/load/export/import sounds; invalid or oversized JSON must not alter stored
  sounds or current gain. Trial expiry retains data and free audio controls.
- Test sleep fade and expiry during a fade. Check no increase at expiry; the media
  player continues normally after the boost timer ends.
- Test the signed public release's actual Play purchase lifecycle when configured.
  Owner simulations cannot certify payment, refund, restore or server verification.

The graphics-path dependency is now explicitly 1.1.0. Static inspection of both
0.3.0 APK types still reports aligned LOAD segments and an 8192-byte GNU_RELRO end
remainder against 16 KB. The upgrade alone does not close the previous flag.
Run AAB-derived APKs on a real 16 KB Android runtime before claiming validated
16 KB support. No physical 16 KB runtime was available for this build.

## 0.5.0 additions

Free now supports up to +15 dB requested gain and Pro up to +35 dB. Device
limits still apply. The new floating-player page explains the single app-level
overlay grant; protected apps can still suppress it. Full light/dark themes have
native screenshots, but real overlay, accessibility and hardware checks remain open.

The minimum remains Android 8/API 26, target/compile 36. Rewarded ads require a
network and eligible ad availability; Play purchases require a working Play Store
account and catalogue. Neither is required for core audio or Owner. No Pixel-only
code or manufacturer allowlist was introduced. New service tests cover ad gating
and reboot receiver behaviour on API 26/35; these are not acoustic/OEM evidence.
Recheck foreground survival, ads, billing and 16 KB native dependencies using the
actual release build. [Background rationale](BACKGROUND-RUNNING.md).
