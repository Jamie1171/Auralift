# Validation — Auralift

## 0.5.5 support and localization — required gates passed, 18 September 2026

Validated source `28b0f654e926cc9a683827b89aadea2ffda31bb3` (version code 10),
PR merge build `8ccaf8925f771027425f3a18fbb488a282e9bd0a`.

- [Required unit/lint/build gate](https://github.com/Jamie1171/Auralift/actions/runs/35397434159): passed both editions, both lint tasks, Owner APK,
  optimized public APK and public AAB. API 35: Owner 68 / Play 70 tests; separate
  API 26 invocation: Owner 45 / Play 48 tests. Total 231, no failures or skips.
- [Android 8 device suite](https://github.com/Jamie1171/Auralift/actions/runs/35397434336/job/105769370241): all 17 tests passed, plus the separate denied-permission check.
  Real app language selection cycles EN/ES/FR, survives activity recreation,
  retains agreement and opens the matching offline policies and support screen.
- Android 16 device suite in the same run: pending completion.
- [Optimized 16 KB startup and controls](https://github.com/Jamie1171/Auralift/actions/runs/35397434170): passed on the final application source.
- Inspected native Spanish/French support and policy captures: readable titles,
  controls and document text. Unit UI checks also cover the explicit English
  override and accepting the language actually displayed.
- Resource check passes for all 379 strings in each locale, matching format
  arguments, complete legal sections and byte-identical archived terms. Terms
  version 2026-09-18.2 replaces the preview text with the public publisher terms.
- Support tests check exact recipient, accented/special-character message text,
  optional diagnostics, screenshot URI and read-only attachment permission.
  The form opens an email draft and never claims delivery. No email was sent by
  automated testing; individual third-party email-client behaviour is not certified.
- Six static web policies match the bundled document text; all language links
  resolve within the generated site. [GitHub Pages deployment](https://github.com/Jamie1171/auralift-policies/actions/runs/35397462810) passed for policy
  commit `87c2c176f50f5c61a1f6f751c281fa1d2a047d8f`.
- Original CI APK SHA-256: `3d0db4fcd01b646ea4f1024c0be7068ba601becc05c36728766dc4cb57993dff`.
- Delivered `Auralift-0.5.5-Play-Test.apk` is re-signed with the retained personal-test
  certificate used for 0.5.2, avoiding another disposable signing identity.
  APK v2/v3 signatures verify; all 469 non-META-INF payload entries are byte-identical
  to the CI-tested APK. Size: 26,213,758 bytes. SHA-256:
  `49ece36b52adb721d1093f45614edd7d4b808af7d4e83a79b955d1a676eb1bb4`.
  Certificate SHA-256:
  `7ba0540911fe9b3208e119d82a5710079d9ad982ea1c4259c500e0047a8f8f36`.
  Private signing material stays outside Git and CI.
- The previously supplied 0.5.4 screenshot APK has certificate SHA-256
  `9fb6c57963bf8af99ffdd00d16c65bf80a1fabee6646ffc933760ced8f7b9219`;
  its temporary private key is not available. Neither it nor the original CI-signed
  0.5.5 APK can be updated in place with this retained-key build. Export wanted
  profiles before uninstalling those builds; reinstalling clears local app data.
  Future personal APKs should use the retained key. This is not a Play upload key.

Earlier attempts are retained: both prescribed local Gradle invocations failed
before compilation because the distribution download reports `Network is unreachable`.
First CI run 35397019133 compiled successfully and passed localized UI/receipt
checks, but a support test exposed Android MailTo parsing query text before
splitting parameters, truncating messages containing ampersands. Drafts now put
the recipient in the mailto URI and preserve subject/body in standard Intent
extras. The follow-up required gate above passed. No core audio behavior changed.

These are software/emulator checks, not acoustic safety evidence, independent
legal/translation review, live ad or payment certification. No production signing
or Google Play upload was performed. See [implementation](SUPPORT-AND-LANGUAGES.md).

## 0.5.4 explicit terms acceptance — passed, 18 September 2026

Validated source `3ef5f6b23c92856b8379bcc10752c8bac39124a3` (version code 9).
Production application source is unchanged since `9e65379d53fcda29ccfc0c31bb1e5598fc9d2866`;
subsequent changes update test fixtures, an overlay-window wait and documentation.

- [Required unit/lint/build gate](https://github.com/Jamie1171/Auralift/actions/runs/35358225185): passed both editions on API 35, both lint tasks, Owner APK,
  optimized public APK and public AAB, then both API 26 unit tasks separately.
- [Android 8 device suite](https://github.com/Jamie1171/Auralift/actions/runs/35358225526/job/105642739220): passed, including the corrected floating-window synchronisation check.
- [Android 16 device suite](https://github.com/Jamie1171/Auralift/actions/runs/35358225526/job/105642738819): passed on the same final source.
- [Optimized 16 KB startup and controls](https://github.com/Jamie1171/Auralift/actions/runs/35358225143): passed, including real first-launch acceptance before starting boost.
- Inspected native captures at 360 × 640 dp: readable warning/document links,
  scrolling acceptance action, no layout overlap. Unit UI checks cover reading
  both documents before agreement, decline, acceptance without audio/gain changes,
  activity recreation and subsequent Settings access.
- Receipt tests cover persistence, exact version/text metadata, old warning flags,
  corrupt/missing records, write failure, material version changes and service
  startup bypass attempts. Resource XML parses in all three languages, with no
  duplicate keys. The archived terms match the shipped asset byte-for-byte:
  SHA-256 `2fc55ff77581389affa0c1f12ca16814e24e073703784239ca3b407400899ad4`.

Earlier attempts are retained. Both prescribed local Gradle invocations stopped
before compilation because the wrapper download is blocked by this environment's
network. First CI run 35357229704 compiled both editions and ran 61 Owner tests;
the new receipt/acceptance tests passed, but two pre-existing Owner UI cases needed
to establish agreement before navigating to unrelated controls. Run 35357746094
then passed the full required gate. Device runs 35357229700 and 35357746082 exposed
an Android 8 floating-window test timing issue: after the expanded window vanished,
the test immediately queried its replacement. It now waits up to five seconds for
the same required circle. No production floating-player changes were needed.

See [acceptance design and competitor review](TERMS-ACCEPTANCE.md).
These are simulated tests, not acoustic safety evidence or legal approval. Existing
physical-device, live monetisation and ARM64/native-library scope limits below
remain. No website, Play upload, production signing or public release is performed.

## 0.5.3 reviewer access — passed, 18 September 2026

Validated application/test source `e25e703b7084c0894ecedca210f2aa33071fc0d7`.
This record is a documentation-only follow-up and does not alter the tested app.

- [Required unit/lint/build gate](https://github.com/Jamie1171/Auralift/actions/runs/35352929223): passed both editions on API 35, both lint tasks, Owner APK,
  optimized public APK and public AAB, then both API 26 unit tasks separately.
  The new native 360 dp UI test enters an invalid code, corrects it, activates
  review access and returns to Free. Entitlement regressions cover persistence,
  code reuse, rejection, Owner isolation, changed verifier, purchase/pass
  independence and gain clamping without an automatic gain increase.
- [Device API 26 and 36 suites](https://github.com/Jamie1171/Auralift/actions/runs/35352929222): both jobs passed.
- [Optimized 16 KB smoke](https://github.com/Jamie1171/Auralift/actions/runs/35352929691): passed. Its pre-existing x86-64 scope and ARM64/native-library
  limitations below remain unchanged.
- The private reviewer code was checked locally against the configured digest;
  all resource XML parsed and the Console instructions fit its 500-character field.
  Neither the real code nor signing material is in the public repository.

Earlier attempts are retained: both local Gradle commands stopped before
compilation because the wrapper download was network blocked. CI run
35352186337 passed entitlement tests but failed the new dialog screen test with
Compose AppNotIdleException before text entry. The final implementation expands
code entry inline on the scrolling Pro page; the same assertions now pass.
Initial 16 KB run 35352186413 failed before app installation when the emulator
lost its package service (Broken pipe / Can't find service: package). It is not
counted as an app pass. The later linked optimized run passed.

Version 0.5.3 / code 8 remains unsubmitted. This change does not configure
production signing, live purchases/ads or claim physical audio validation.
A signed Play build containing this feature must accompany the reviewer
instructions; the existing 0.5.2 installation does not accept the new code.
See [reviewer setup](PLAY-REVIEW-ACCESS.md).

## 0.5.2 floating player and ten palettes — passed, 17 September 2026

Validated source `64a826c0a2b27857fee3b62847a6e86aea9083af`, tested PR merge
`02ce2f83efc7a4339e70032cd44e28f803d7d271`. This record is a documentation-only
follow-up; it does not change the tested application or delivered APK.

- [Required unit/lint/build gate](https://github.com/Jamie1171/Auralift/actions/runs/35243466406):
  passed all required tasks, then the separate API 26 invocation. **164 unit
  executions**, zero failures/errors/skips: API 35 Owner 49 / Play 50; API 26
  Owner 31 / Play 34. Optimized APK/AAB and Owner builds succeeded.
- [Android device suites](https://github.com/Jamie1171/Auralift/actions/runs/35243466494):
  **34 checks passed**, 17 each on API 26 and 36, including the isolated denied-
  permission check. Downloaded per-test reports confirm all passes. The new tests
  exercise Settings launch without boost, off/on, circular bounds, minimise,
  reopen, selected preset toggles and Stop/reset/restart without reopening the app.
- [16 KB smoke](https://github.com/Jamie1171/Auralift/actions/runs/35243466484):
  optimized startup/controls and driver checks passed. Its existing x86-64 scope
  and native-library/ARM64 limitations below still apply.
- Native captures cover all **20 palette appearances**. Inspected all six new
  palettes, expanded selected/stopped controls, and the compact circle. Main body,
  muted and accent text contrast against each surface is at least 5.81:1; this is
  a targeted colour check, not a full accessibility certification.

Delivered **Auralift-0.5.2.apk**, version code 7, public personal-test build with
Google test ads: **26,135,528 bytes**, SHA-256
`ef784f48e201a9f836c691f2bfb686c959dbf885c536204930f97e73978389c8`.
Re-signed locally with the previous personal certificate
`7ba0540911fe9b3208e119d82a5710079d9ad982ea1c4259c500e0047a8f8f36`;
APK v2/v3 signatures verify. All 463 non-META-INF payload entries match the CI
app before re-signing. Signing material stays outside Git and CI.

The circle is 64 dp and expands with separate Minimise/Close. Stop in the player
releases effects, cancels comparison/timers and resets gain/EQ to 0 dB/Balanced,
while leaving controls open. Close disables the Settings preference and leaves
any active boost alone. Starting the player never starts effects. Nine extra
palettes supplement free Mint, each with light/dark support.

Earlier attempts are retained rather than counted as passes. Runs 35242045148 /
35242045298 / 35242045142 stopped before compilation because SDK setup requested
the removed `tools` package; workflows now explicitly request `platform-tools`.
Source `14cc3d3` passed 160 unit executions/builds/16 KB, but device runs exposed
memory-off startup being mistaken for idle shutdown, a test scrolling the fixed
nav bar, and a notification check observing the initial Close action before Stop.
The final source fixes these and adds a regression for the memory-off startup.

Screen-off checks here last 30 seconds; the older 20-minute result is historical.
Physical listening, OEM idle reliability, live ads/purchases, production signing,
and native-library/ARM64/AAB-derived validation remain separate launch work.
See [machine-readable identity](validation-0.5.2.json).

## 16 KB emulator setup recovery, 14 September 2026

[Run 34879590587](https://github.com/Jamie1171/Auralift/actions/runs/34879590587)
failed at `adb root` before installing or testing the app. This newer failure
followed a documentation-only commit and must not be replaced by the earlier
successful runtime result in the 0.5.1 history below. The other latest
[unit/lint/build](https://github.com/Jamie1171/Auralift/actions/runs/34879590817) and
[API 26/36 device](https://github.com/Jamie1171/Auralift/actions/runs/34879590783)
workflows succeeded on that commit.

The driver now targets only the isolated emulator, records ADB stdout/stderr and
timeouts, and permits bounded recovery during the debug-daemon restart. A separate
successful `id -u` must confirm UID 0; a successful restart message alone cannot
pass setup. This follows [AOSP's documented adbd restart behavior](https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/main/docs/dev/root.md).
The driver still requires 16384-byte pages, both disabled compatibility settings,
and all existing app startup, gain, background and Stop assertions. Setup retries
cannot retry app assertions or mark an unverified runtime as passed. Failure
reports distinguish setup from app checks, and diagnostic capture has timeouts.

[The first recovery run, 34884348311](https://github.com/Jamie1171/Auralift/actions/runs/34884348311),
passed all eight driver tests and the optimized build/signature/ZIP checks, then
failed earlier in the external emulator launcher: `input keyevent 82` returned
exit 224 with `Failure calling service input: Broken pipe (32)`. Our smoke driver
was never invoked, so there was no runtime report or app compatibility result.

The 16 KB job now launches the same SDK emulator/image directly and waits for
two responsive samples from the same `system_server`, including input, settings
and package services. The readiness wait has a three-minute limit, and emulator
boot output is retained even if setup fails. The finished Gradle daemon is stopped
before starting the 4 GB emulator. Animation settings, 16 KB properties and every
app assertion remain checked. The ordinary API 26/36 device launcher is unchanged.

[Run 34885312243](https://github.com/Jamie1171/Auralift/actions/runs/34885312243)
then correctly failed its readiness deadline without running app checks. Its
retained boot log exposed a launcher configuration error introduced by this
change: `Unknown AVD name [auralift16k]`. The AVD manager and emulator had different
default lookup folders. Both now use an explicit `ANDROID_AVD_HOME` under the
runner's temporary directory and an explicit AVD data path. File existence and
the emulator's own `-list-avds` result must both confirm the device before launch.
This failure is retained as a failed setup attempt, not compatibility evidence.

**Twelve host-side driver regression tests passed** with
`python3 -m unittest discover -s scripts/tests -v`; no failures or skips. Cases
include healthy restart, lost restart reply, lost request, timeout after restart,
false success without UID 0, failed UID readback, a persistent disconnect deadline,
and refusing physical/non-debuggable builds. Four additional cases cover an early
boot flag with broken input, system-server restart, persistent broken input and
missing boot completion. Python compilation and whitespace
checks also passed. These tests simulate daemon states; they are not a 16 KB
runtime pass. The same regression command runs before the optimized CI build.

Runtime outcomes for this correction are recorded with exact source/run links in
[PR #1](https://github.com/Jamie1171/Auralift/pull/1). Each run retains
`page-size-report.json` and `adb-transcript.jsonl`; a pass requires `stage=complete`,
`bootSetup.ready=true`, `verifiedUid=0`, disabled compatibility workarounds and all three app checks. The
0.5.1 app source, dependencies and downloadable APK are unchanged. The separate
graphics-path GNU_RELRO, ARM64 and AAB-derived release limitations remain open.

## 0.5.1 main-screen Ad Pass and permission help

14 September 2026. Adds the shared Ad Pass panel below Enable/Stop, earned-pass
remaining time, and optional help for Android-restricted overlay settings. Version
code is 6. No audio engine, reward-granting logic, permissions or dependencies change.
English, Spanish and French have 333 matching string keys and format arguments.

New/extended UI checks exercise explicit Prepare versus Watch, disabled/unavailable
ads, active/permanent/Owner states, main-screen pass expiry without starting boost,
and the App info intent for this package. Native captures include the main Ad Pass,
active-pass state and permission recovery help and were visually reviewed.

Validated source: `9e858f4ff8a6fd6ebb8b158e259af4d621309694`; tested PR merge
checkout: `6680af5e8affe798c6430d434c42c5150f135ade`. Downloaded artifact digests
were verified before reading their reports. Machine-readable evidence:
[validation-0.5.1.json](validation-0.5.1.json).

| Check | Verified result |
| --- | --- |
| Unit tests, API 35 | 47 Owner + 48 Play passed |
| Unit tests, separate API 26 invocation | 29 Owner + 32 Play passed |
| Total unit executions | 156; zero failures, errors or skips |
| Lint Owner debug / Play release | Zero errors; 39 / 35 warnings |
| Required builds | Owner debug APK, Play release APK and AAB succeeded |
| API 26 / API 36 device suites | 15 cases each passed; 30 passing diagnostic reports |
| Optimized 16 KB smoke | API 35 x86-64 passed with compatibility workarounds disabled |

[Required unit/lint/package gate](https://github.com/Jamie1171/Auralift/actions/runs/34877826129),
[device suites](https://github.com/Jamie1171/Auralift/actions/runs/34877826099),
[16 KB smoke](https://github.com/Jamie1171/Auralift/actions/runs/34877826132).
Each device suite has 14 normal JUnit cases and one separately launched optional
permission-denial case. Both use 4 KB pages. The normal screen-off case lasts
30 seconds; a new 20-minute test was not run for 0.5.1.

The 16 KB smoke exercises first launch, gain selection, service start, background
return and Stop through the public UI. The four `graphics-path:1.1.0` GNU_RELRO
flags remain open even though ELF LOAD segments and APK ZIP alignment pass. This
does not establish ARM64, AAB-derived release or acoustic compatibility, or prove
that the flagged library was loaded. Full scope is preserved in the JSON report.

The existing CI gate also retains Google's apksigner tool so a downloaded test APK
can be signed locally with the existing private update key. The key remains outside
Git and CI artifacts. A personal test build and a Firebase kit have separate signing
identities; never mix one kit's instrumentation APK with another signed app APK.

`Auralift-0.5.1-play-test.apk` is signed with the original personal-test certificate
(`7ba0540911fe9b3208e119d82a5710079d9ad982ea1c4259c500e0047a8f8f36`). Signature
verification passed, all application ZIP payloads match the tested Firebase app,
and uncompressed native-library offsets remain aligned to 16 KB. APK SHA-256:
`224a785c3b09fd585891d4dc50bffe049fd0bc6b7769486ec63060550d7d9091`.
This public debug build uses Google sample ads; it does not validate live ad
delivery or production purchases. Android's restricted-settings grant remains a
manual system action. The final result-recording commit changes documentation
only; the code validated above is unchanged.

## 0.5.0 baseline: automated device coverage, 14 September 2026

The new targeted instrumentation suite passed **15 executed cases on Android 8
(API 26) and 15 on Android 16 (API 36)**. Each run contains 14 normal JUnit cases
plus a separately launched permission-denial case. All 30 diagnostic JSON reports
record `passed`; none failed or skipped. Both emulators use x86-64 and 4 KB pages.

Validated suite source: `874fa892bbb4db9d5ec7d0d80dbb70d22c573b93`; GitHub's tested
PR merge checkout: `0a94e694f4e1c65ae230f66c67dcfc0c9db2e305`.
[Device run and artifacts](https://github.com/Jamie1171/Auralift/actions/runs/34870619274).
The 15 cases cover first use, Free controls and persisted settings, foreground
service/notification Stop, activity recreation versus leaving the app, real muted
test-player sessions, compare/readback, entitlement/reward expiry, ad suspension,
floating controls, optional permission denial, timer replacement and screen-off.

The existing required unit/lint/package gate also passed, followed by its separate
API 26 invocation: **148 unit-test executions** (43 Owner + 44 Play on API 35;
29 Owner + 32 Play on API 26), with zero failures, errors or skips. Current lint
has zero errors and 37 Owner / 31 Play warnings, including dependency-update,
unused-resource, style and SDK notices. Release APK/AAB and Owner debug builds
succeeded. [Full gate](https://github.com/Jamie1171/Auralift/actions/runs/34870619181).
The initial run's downloaded XML confirms the same 148 counts and lint totals:
[Initial gate](https://github.com/Jamie1171/Auralift/actions/runs/34868842150).

Production Kotlin, permissions, version and production dependencies are unchanged.
Instrumentation fixtures live in the separate test APK. The matching app/test
APKs have verified signatures and hashes in their `build-identity.json`. Their
ephemeral CI certificate is different from older downloads: use the supplied
pair together, not an older Robo APK with a newer instrumentation APK.

Normal emulated screen-off checks last 30 seconds. A separate API 36 run passed
the **1,200-second screen-off service test** (1,206.355 seconds including setup,
return and Stop). Its 14 normal cases and separate permission case all passed:
[20-minute run](https://github.com/Jamie1171/Auralift/actions/runs/34868842227),
source `efeca00a6d58b3ab7cbae5516a697efd9ee0b4fe`. The timed test body is unchanged
in the supplied Firebase pair. Instrumentation remains active; this is service
state evidence, not uninterrupted audio or natural OEM idle behaviour.

The optimized public APK **passed the 16 KB emulator smoke check** in
[run 34873143964](https://github.com/Jamie1171/Auralift/actions/runs/34873143964).
The job's JSON report confirms API 35, x86-64, `PAGE_SIZE=16384` and disabled
16 KB compatibility workarounds. It exercised first launch, +5 dB selection,
service start, a ten-second background interval, return and Stop through the
public UI. The APK's signature verification and `zipalign -c -P 16` also passed.
The emulator used 4 GB RAM and density 320; this is not small-screen coverage.

Validated branch source: `e64ca9f8690aaf6eaa6831f230fd63d718ce1203`;
tested PR merge checkout: `326aa24244fe4c90b5fdc0d732628107e5f951ee`.
APK SHA-256: `9fe0b39e4b8cdf97b38511afa550062ee20e16c95694fe5fe27a2b7bd9e7255c`.
The [retained evidence](https://github.com/Jamie1171/Auralift/actions/runs/34873143964/artifacts/10360260115)
contains the APK, JSON, UI captures, logcat and memory reports. Its archive digest
is `9b33888c495480b4565b87adc73b98a0aa316122615edbadaa3a72b28c29396d`, as
reported by GitHub. The workflow log's complete JSON report is preserved in
[the dated device report](validation-device-2026-09-14.json).

This resolves the outstanding emulator run, not the separate native-library
release check. All four bundled `graphics-path:1.1.0` libraries still report
aligned LOAD segments but unaligned GNU_RELRO ends. The smoke driver does not
assert that this specific library was loaded. ARM64 execution and AAB-derived
release validation remain open. Do not infer those results from this pass.

The earlier failures remain recorded: run 34871163768 lost the app to emulator
memory pressure; run 34872129209 failed to obtain a UI hierarchy during first-boot
configuration. Increasing emulator RAM and waiting for a fresh hierarchy resolved
the observed test-environment issues. Assertions were retained, and a previous
screen is never reused. Neither failed attempt is counted as a compatibility pass.

This handover update changes documentation only. The recorded unit, lint, build
and device gates above apply to the unchanged application/test sources. No new
Firebase job or manual 20-minute run was requested for this update; normal CI may
rerun automatically when the documentation commit updates the PR.

The native gain readback
check observed gain above the Free ceiling before expiry, then verified a reduction
to at most +15.1 dB. This is Android parameter feedback, not a sound measurement.

Jamie also supplied five passing physical Robo summaries (Pixel 5/API 30,
S24 Ultra/API 36, Galaxy A54/API 34, moto g 5G 2022/API 33, Pixel 8/API 35).
Those screenshots establish reported crawl success; their full logs/videos have
not been independently reviewed here. Real player/output cooperation, acoustic
quality, normal OEM battery management, release updates and live monetisation
remain distinct tests. [Firebase instructions](FIREBASE-AUTOMATION.md) and
[human tester goals](HUMAN-TEST-GOALS.md).

## Historical 13 September build validation

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

At the 13 September static inspection, the graphics-path 1.1.0 libraries had
`(GNU_RELRO.vaddr + memsz) % 16384 == 8192` across all four ABIs. ZIP/LOAD alignment
does not close this flag. No 16 KB runtime was available for that historical build.
The 14 September optimized x86-64 smoke check above now passes, but its report
retains these static flags and does not prove the flagged library was exercised.
Do not call this a Play-ready or fully validated 16 KB release.
[Android page-size guide](https://developer.android.com/guide/practices/page-sizes).

The owner previously reported Pixel 9a listening success and has supplied 0.4.0
interface feedback. New +35 dB acoustic behavior, protected-screen/real permission
behavior, OEM background survival, large text/TalkBack and live ad/purchase flows
still need physical checks. See [device checklist](PIXEL-TEST-PLAN.md),
[closed testing](CLOSED-TESTING-BRIEF.md) and [Play plan](GOOGLE-PLAY-PLAN.md).

Historical validation: [0.4.0](VALIDATION-0.4.0.md), [0.3.0](VALIDATION-0.3.0.md),
[0.2.0](VALIDATION-0.2.0.md), [0.1.0](VALIDATION-0.1.0.md).
