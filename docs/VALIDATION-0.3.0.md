# Validation — Auralift 0.3.0

13 September 2026. Native Kotlin/Jetpack Compose; separate Owner and Play flavours.
This is a tested development build, not Play approval or physical audio certification.
Machine-readable evidence: [validation-0.3.0.json](validation-0.3.0.json).

## Completed checks

| Check | Result |
| --- | --- |
| API 35 Owner automated checks | 29 passed, zero failures/errors/skips |
| API 35 Play automated checks | 30 passed, zero failures/errors/skips |
| API 26 Owner automated checks | 19 passed, zero failures/errors/skips |
| API 26 Play automated checks | 19 passed, zero failures/errors/skips |
| Lint Owner debug / Play release | Zero errors in each; 18 warnings in each |
| Owner debug APK | Builds and verifies with APK Signature Scheme v2 |
| Optimized Play release APK and AAB | Both build; intentionally unsigned, not uploaded |
| APK code integrity | DEX SHA-1/checksum checks pass; no duplicate class definitions or temporary ZIP entries |
| Flavour separation | Owner APK has no Billing classes/network permissions; public R8 removes owner override methods |
| Localisation | en/es/fr have matching 274 string keys; API 26/35 resource/decimal checks pass; bundle language splitting disabled |
| Native UI | Ten API 35 captures; main controls, settings, themes, permissions, help, feedback and owner simulations exercised |
| 16 KB checks | ZIP and ELF LOAD alignment pass; GNU_RELRO end check still fails in graphics-path 1.1.0; release blocker remains |

The **97 executions** include shared tests repeated across editions and SDKs; they
are not 97 unique test methods or physical-device runs. Checks cover native gain
units/readback/failure handling, preference migration, comparison, preview clocks
and reinstall-local persistence, owner guards, expiry retaining audio/profiles,
atomic bounded profile import, native UI transitions, localisation and app startup.
The comparison screenshot deliberately injects a +12 dB readback beside a +30 dB
selection to verify that the UI preserves differing Android-reported values; it is
not a connected-device result. Three public-only tests generate RSA test keys to verify valid, tampered, pending,
wrong-product, wrong-package and empty-token receipts. They do not transact with Play.

Lint's remaining warnings concern unused flavour-specific strings, pluralisation,
compatibility resource/API annotations, absolute overlay coordinates, optional KTX
style and a newer test dependency. The actionable language-split and conflicting
resource-name warnings were fixed. No error baseline or ignoreFailures was added.

Commands, from the project root:

```sh
./gradlew :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest \
  :app:lintOwnerDebug :app:lintPlayRelease \
  :app:assembleOwnerDebug :app:assemblePlayRelease :app:bundlePlayRelease
./gradlew -Pauralift.testSdk=26 :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest
```

Builds ran in an isolated temporary copy using JDK 17, Gradle 8.13, SDK 36 and
build-tools 35.0.0. Workspace-only proxy/trust configuration allowed Gradle and
Robolectric dependency downloads; none is in Git. Early attempts failed while
restoring the JDK/SDK and simulator dependencies. Mixed API 26/35 Robolectric
sandboxes also collided over a native font ZIP. The final configuration runs one
SDK per test invocation, with isolated classes and legacy graphics for non-visual
checks. Native screenshot tests remain on API 35. Final runs completed successfully;
no failed test was suppressed.

## Artifacts

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| Auralift-0.3.0-owner.apk | 19,733,605 | 5f1093870cc21e303d73ef96da3ae3b4ffc17a4430ecd163f093906ebc0dc4b9 |
| app-play-release-unsigned.apk | 2,629,621 | 9b0b66b325aaba6d0803e1bb96921e389d22f5752253d29be4819dc039b04d30 |
| Auralift-0.3.0-play-unsigned.aab | 4,466,777 | 367a04f2e854ec29ff2be0f8f099dd61e5907ef284117aa41d99ba759656944b |

Both flavours use version code 3, minimum API 26 and target API 36. The owner
version name is 0.3.0-owner. Owner package: `com.jamiewardle.auralift.owner`.
Public package: `com.jamiewardle.auralift`.

Owner certificate SHA-256:
`7ba0540911fe9b3208e119d82a5710079d9ad982ea1c4259c500e0047a8f8f36`.
This is a development signing certificate for the new Owner package, preserved
privately outside Git. Owner installs alongside 0.2.0 and begins with fresh
preferences. It does not replace that previous public-package preview. Stop
other boosters before starting the new one. Future Owner updates must reuse its
key; production Play signing must be configured separately.

The Owner APK contains 26,889 unique class definitions; the optimized public APK
contains 3,326. Both have four native architectures: armeabi-v7a, arm64-v8a, x86
and x86_64. Bundles and keys are separate artifacts, not committed to source.

## Permissions and billing review

Owner's merged APK contains audio-settings, user-started foreground-service,
notifications, optional overlay, vibration and optional Visualizer audio permission,
plus AndroidX's app-specific receiver signature permission. It has no INTERNET,
ACCESS_NETWORK_STATE, Billing, notification-listener or accessibility-service access.
It does not open/record the microphone. Spectrum is optional and visible-screen only.

Public additionally contains BILLING and INTERNET/ACCESS_NETWORK_STATE from Google's
billing/data-transport dependencies. Its client is not initialized until a real
verification key is configured. Purchase UI remains disabled without a valid
catalogue. Privacy text discloses Google billing API/connection diagnostics when
activated; no separate advertising, audience analytics or crash-report SDK was added.
The release shrinker's usage report lists `simulateFree`, `simulateExpired` and
`restoreOwner` as removed. Public source supplies an empty owner-tools composable.

## Physical testing and launch gates

The owner previously reported **0.2.0 working on a Pixel 9a**. No physical phone
was attached for 0.3.0. Acoustic boost, real effect routing, floating windows,
background reliability, spectrum availability, TalkBack, large fonts, landscape,
Android 16/17 behaviour and manufacturer variations need on-device checks.
[Compatibility matrix](ANDROID-COMPATIBILITY.md), [listening checklist](PIXEL-TEST-PLAN.md).

The updated graphics-path 1.1.0 library passes 16 KB ZIP/LOAD checks but has
`(GNU_RELRO.vaddr + memsz) % 16384 == 8192` on every packaged architecture. Android's
current guidance requires zero. **Do not claim validated 16 KB compatibility or
ship to those devices until the library is fixed/rebuilt and release-derived APKs
are tested on a 16 KB runtime.** The current environment did not reproduce a crash;
static evidence is retained rather than hidden by a successful zipalign command.
[Android page-size guidance](https://developer.android.com/guide/practices/page-sizes).

Live Play purchase, pending payment, acknowledgement, refund/revocation, reinstall,
second-device restore and offline behaviour still need Play licence-tester runs
with a signed release and real product. Client-side RSA checks are not tamper-proof
server verification. Publisher/contact details, final policy review/hosting,
production signing and store declarations/review remain launch work.
[Play plan](GOOGLE-PLAY-PLAN.md), [billing and trial details](FEATURES-AND-MONETISATION.md).

Historical records: [0.2.0](VALIDATION-0.2.0.md), [0.1.0](VALIDATION-0.1.0.md).
