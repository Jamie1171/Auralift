# Validation — Auralift 0.5.0

13 September 2026. Native Kotlin/Compose, Owner and Play editions. Machine-readable
results: [validation-0.5.0.json](validation-0.5.0.json). These are development checks,
not physical audio measurements, live transactions, ad delivery or Play approval.

| Check | Result |
| --- | --- |
| API 35 Owner | 43 passed; no failures, errors or skips |
| API 35 Play | 44 passed; no failures, errors or skips |
| API 26 Owner | 29 passed; no failures, errors or skips |
| API 26 Play | 32 passed; no failures, errors or skips |
| Lint Owner debug / Play release | 0 errors; 29 / 23 warnings |
| Builds | Owner debug APK, public test APK, optimized unsigned public APK and unsigned AAB succeed |
| Install identity | Both test APKs verify; Owner uses the same certificate as 0.3.0 / 0.4.0 |
| APK integrity | DEX checksums valid; no duplicate classes, ZIP names or temporary entries |
| Source-set separation | No Billing/GMA/UMP or internet permission in Owner; Owner simulation methods removed by public-release R8 |
| Locales and UI | 323 matching en/es/fr keys and format arguments; 23 native captures |
| 16 KB static checks | ZIP offsets and ELF LOAD pass; graphics-path 1.1.0 GNU_RELRO end remains flagged |

**148 test executions** repeat shared cases across editions/SDKs. This is not a count
of unique physical-device tests. The final full gate passed in 4m19s and the
separate Android 8/API 26 run in 50s. No failures were disabled or ignored.

## What changed and what the checks establish

- Free startup, direct edits and saved output setup loads cannot exceed +15 dB.
- Owner/Pro exposes +35 dB; a fake effect receives 3500 mB and returns its parameter.
  Existing tests still distinguish device rejection, silent caps and failed readback.
- A 0.4.0 full-range Owner update preserves current gain while exposing +35. Smaller
  legacy ceilings round down to a 5 dB step. A Free update caps and persists gain.
- Expiry while backgrounded/during ad suspension reduces active preferences above
  +15 dB; a later reward opens the range without restoring louder gain. Saved named
  profiles remain stored. Stop remains usable and does not start/stop external media.
- Native UI tests exercise +5 dB shortcuts, half-decibel buttons, lower ceilings,
  the Free range, the separate Floating Player Pro page and cancelling its permission
  explanation before Android settings. The explanation names only the installed app.
- API-version checks retain a package deep link only before Android 11; Android 11+
  deliberately uses its system app list. Tests cannot grant real overlay permission.
- Full dark/light themes render through Android native graphics. Visual review found
  dial-label overlap and stale background copy; both were corrected before the final
  full build/test gate. Theme previews and the final updated renders were inspected.
- Existing background/ad effect-release, reward clock/replay, purchase fixture, EQ,
  localisation, profile import and Owner-isolation tests remain active.

A simulated HAL cannot establish audible gain or real overlay behavior. The service
also independently clamps every ramp step against entitlement and hardware ceilings.
The existing fade multiplier can only decrease once a fade begins; gaining/losing
Pro never automatically restores a previous louder setting.

## Reproduce

```sh
./gradlew :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest \
  :app:lintOwnerDebug :app:lintPlayRelease \
  :app:assembleOwnerDebug :app:assemblePlayRelease :app:bundlePlayRelease \
  :app:assemblePlayDebug
./gradlew -Pauralift.testSdk=26 :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest
```

JDK 17, Gradle 8.13, SDK 36/build-tools 35.0.0. API 35 and API 26 run separately
to avoid the known mixed-SDK Robolectric native-font cache issue. Builds use a
temporary copy to avoid workspace sync artifacts. No private toolchain/proxy files
or signing material are in Git. Lint warnings include optional style/deprecation,
unused flavour resources, plurals, SDK annotations and overlay coordinates.

## Artifact identities

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| Auralift-0.5.0-owner.apk | 19,845,281 | 8719e81ffe3caa52adddb5b52073655f9d3c6a1d414151dcb119c84537aeaa47 |
| Auralift-0.5.0-play-test.apk | 27,042,413 | bd9125a37045d60f10daefdef53bd9c3413e45c89e3cf190d2b82b45068ed31c |
| Auralift-0.5.0-play-unsigned.aab | 8,261,938 | 2b4d49dd3ed727b791567e9cc66062b6c0c0397b131739b1e163465ff5e31710 |
| app-play-release-unsigned.apk | 4,517,740 | c3d05da90104ca3d84662a7c863ce00c50f7d8fe982c48207d45bcba6eb6e579 |

All use version code 5, minimum Android 8/API 26 and compile/target 36. Owner is
`com.jamiewardle.auralift.owner` / `0.5.0-owner`; public is
`com.jamiewardle.auralift` / `0.5.0`. Owner updates the existing Owner installation.
Public debug uses only Google sample-ad IDs. Production ads and checkout remain
unconfigured; the unsigned AAB is not submitted to Play.

Debug certificate SHA-256:
`7ba0540911fe9b3208e119d82a5710079d9ad982ea1c4259c500e0047a8f8f36`.

Unique DEX classes: Owner 26,961, public release 7,272,
public debug 41,732. No duplicate classes; DEX SHA-1/Adler-32 verified.
All four graphics-path ABIs remain packaged: ARM32/64 and x86/64.

## Permissions and release limits

No manifest permissions were added for this update. Overlay permission belongs to
Auralift itself; no usage-access, notification-reading, AccessibilityService or
installed-app inventory is used. Protected screens can still suppress the bubble.
The feature does not include automatic per-target-app exclusions. Closing the
bubble hides it while boost continues.

Owner has no billing/ad SDK or internet access. Public SDK data handling remains
described in the updated native policy drafts. Both one-time products retain the
same entitlement. Manual account configuration and real Play/AdMob tests remain
necessary; simulated receipts/rewards do not prove live purchase or ad delivery.

The existing graphics-path 1.1.0 libraries still have
`(GNU_RELRO.vaddr + memsz) % 16384 == 8192` across all four ABIs. ZIP/LOAD alignment
does not close this flag. No 16 KB runtime was available and no crash was reproduced.
Do not call this a Play-ready or validated 16 KB release.
[Android page-size guide](https://developer.android.com/guide/practices/page-sizes).

The owner previously reported Pixel 9a listening success and has supplied 0.4.0
interface feedback. New +35 dB acoustic behavior, protected-screen/real permission
behavior, OEM background survival, large text/TalkBack and live ad/purchase flows
still need physical checks. See [device checklist](PIXEL-TEST-PLAN.md),
[closed testing](CLOSED-TESTING-BRIEF.md) and [Play plan](GOOGLE-PLAY-PLAN.md).

Historical validation: [0.4.0](VALIDATION-0.4.0.md), [0.3.0](VALIDATION-0.3.0.md),
[0.2.0](VALIDATION-0.2.0.md), [0.1.0](VALIDATION-0.1.0.md).
