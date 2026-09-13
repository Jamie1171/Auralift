# Validation — Auralift 0.4.0

13 September 2026. Native Kotlin/Compose, Owner and Play source sets. Machine-readable
results: [validation-0.4.0.json](validation-0.4.0.json). These are development checks,
not a physical audio measurement, live ad/transaction test or Play approval.

| Check | Result |
| --- | --- |
| API 35 Owner | 34 passed; no failures, errors or skips |
| API 35 Play | 35 passed; no failures, errors or skips |
| API 26 Owner | 23 passed; no failures, errors or skips |
| API 26 Play | 26 passed; no failures, errors or skips |
| Lint Owner debug / Play release | 0 errors; 27 / 21 warnings |
| Builds | Owner debug APK, Play debug APK, optimized unsigned Play APK and unsigned AAB succeed |
| Install identity | Both debug APKs verify with v2 signatures; Owner uses the same certificate as 0.3.0 |
| APK integrity | DEX SHA-1/Adler-32 valid; no duplicate classes, duplicate ZIP names or temporary entries |
| Source-set separation | Owner has no Billing/GMA/UMP classes or internet permission; Play release removes Owner override methods |
| Locales and native UI | 301 matching en/es/fr keys and format arguments; 13 native captures |
| 16 KB static checks | ZIP and ELF LOAD pass; graphics-path 1.1.0 GNU_RELRO end still fails the documented alignment check |

**118 test executions** repeat shared tests across editions/SDKs; they are not 118
unique physical tests. New checks exercise earned reward vs failed/duplicate callback,
no stacking, repeat after expiry, process recreation, reboot/rollback clocks,
permanent access and fallback, retained gain/profiles at expiry, both signed product
IDs, owner isolation, native Pro/settings screens and background service behaviour.

Service tests inspect the owned effect inventory because a simulated HAL cannot
prove acoustic gain. They verify synchronous release during ad suspension, no
reattachment from reconnect/session/settings callbacks while suspended, reattachment
only for a still-running session, and no resurrection after Stop. The reboot
receiver defaults off and never starts audio. Existing gain/readback, EQ, migration,
profile import and localisation tests remain active.

The first run found an old Owner UI test expecting the removed courtesy-preview
button. It was updated to exercise the new earned-pass simulation; final full and
oldest-OS runs pass. No failing test was disabled and no lint error baseline was
added. Lint warnings are primarily flavour-specific unused resources, optional KTX
style, plural candidates, version/resource annotations, overlay coordinates and a
newer test dependency. Real checkout and ad callback delivery remain untested.

## Build commands

```sh
./gradlew :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest \
  :app:lintOwnerDebug :app:lintPlayRelease \
  :app:assembleOwnerDebug :app:assemblePlayRelease :app:bundlePlayRelease \
  :app:assemblePlayDebug
./gradlew -Pauralift.testSdk=26 :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest
```

JDK 17, Gradle 8.13, SDK 36/build-tools 35.0.0. Builds run in a temporary copy so
workspace synchronization cannot add temporary entries to artifacts. Tests default
to API 35, then run API 26 separately to avoid the known Robolectric mixed-SDK
native-font cache issue. Both runs succeeded (4m25s / 38s); no private environment
proxy, trust or toolchain files are committed.

## Artifact identities

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| Auralift-0.4.0-owner.apk | 18,958,641 | 675996d5e80aa638bb5f0ebf652fd1817033cf7dc0a60f4858310b4416b3b7cc |
| Auralift-0.4.0-play-test.apk | 26,006,090 | c5ace8f67f914544a80e3096f279d478bd643052a88530b4db1cca60f7449fd2 |
| app-play-release-unsigned.apk | 4,504,140 | 31a1887a19fdd672c729902bd73d3e9113bb5800349e2d21ea824c48f019bcbf |
| Auralift-0.4.0-play-unsigned.aab | 8,227,682 | 14c9b180ef33410ca88ed8c50c2f764f7af0d3d7f4266026e8a1418be28d194f |

All use version code 4, minimum API 26 and target/compile API 36. Owner's package
is `com.jamiewardle.auralift.owner`, version `0.4.0-owner`; Play uses
`com.jamiewardle.auralift`, version `0.4.0`. Owner updates 0.3.0 in place. The public
test APK uses the public package and is for sample-ad testing, not Play submission.
Stop other booster editions before testing a listening session.

Debug certificate SHA-256:
`7ba0540911fe9b3208e119d82a5710079d9ad982ea1c4259c500e0047a8f8f36`.
Signing material remains privately backed up outside Git. Production/upload signing
must be configured separately. The AAB has no signing entries and is not uploaded.

Owner/public-release/public-debug contain 26,914 / 7,256 / 41,685 unique DEX classes.
All contain graphics-path's four ABIs: arm64-v8a, armeabi-v7a, x86 and x86_64. The
release shrinker removes unused UMP API names in this unconfigured build; source
activation requires rebuilding with the configuration, not editing the installed APK.

## Permissions and SDKs

Owner adds RECEIVE_BOOT_COMPLETED for the optional notification reminder, alongside
existing audio/FGS/notification/overlay/haptic/Visualizer permissions. No internet,
Billing, advertising, notification-listener or accessibility service is included.
The audio engine does not record the microphone or request a battery exemption.

Play additionally includes BILLING, INTERNET, ACCESS_NETWORK_STATE, AD_ID,
READ_BASIC_PHONE_STATE and WAKE_LOCK from its Google dependencies. The full list
is recorded in the JSON. The engine itself does not acquire a wake lock. Public
policy drafts describe billing diagnostics and optional ad SDK data handling.
Production ads/checkout are unavailable without configuration. Debug uses only
Google's test ad IDs. The merged SDK manifest does not add a MobileAds auto-init
provider; app code initializes it only after the requested consent flow permits ads.

R8's usage report removes `simulateFree`, `simulateExpired`, `simulateReward`,
`simulatePurchase` and `restoreOwner` in the public release. Runtime guards are
also tested. These checks are distribution separation, not tamper-proof DRM.

## Remaining release gates

No device was attached for 0.4.0. Jamie's earlier Pixel 9a success was with 0.2.0.
Actual audible boost, OEM background survival, overlay behaviour, notification
and reboot delivery, TalkBack/large fonts, spectrum, UMP and sample-ad playback
need device checks. Play transactions need configured products, signing and licence
testers. Neither a signed local receipt fixture nor a simulated reward proves a
live purchase/ad flow. See the [closed-test brief](CLOSED-TESTING-BRIEF.md).

The native graphics library still has `(GNU_RELRO.vaddr + memsz) % 16384 == 8192`
on all four ABIs. Do not claim validated 16 KB compatibility or production readiness
until that library issue is resolved and release-derived APKs run on a 16 KB runtime.
No such runtime was available and no crash was reproduced here.
[Android page-size checks](https://developer.android.com/guide/practices/page-sizes).

Final identity/contact/policy hosting, account configuration, purchase/reward fraud
strategy, release signing, store declarations and actual Play review remain launch
work. `specialUse` is an explained engineering choice, not pre-approved by Google.
[Background rationale](BACKGROUND-RUNNING.md), [Play plan](GOOGLE-PLAY-PLAN.md).

Historical validation: [0.3.0](VALIDATION-0.3.0.md), [0.2.0](VALIDATION-0.2.0.md),
[0.1.0](VALIDATION-0.1.0.md).
