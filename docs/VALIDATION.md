# Validation — Auralift

## 0.5.10 ad-loading investigation — candidate, validation pending

Jamie subsequently supplied a screenshot of an actual test ad loading on his
phone. This establishes device-specific ad delivery, not a universal outage or
completed reward callback. Keep the original failure as an unconfirmed case.

Day-one tester feedback on 21 September: Ad Pass reports no ad available, preventing
Pro/floating-player testing. The screenshot does not include installed version,
device details or SDK error; its exact cause is not established. The 0.5.9 source
uses the correct Google demo IDs. No live AdMob account issue is inferred.

Repairs in this candidate:
- Consent reading time no longer consumes the 60-second ad-loading budget. Separate
  watchdogs cover consent-info networking and ad loading; a displayed consent form
  has no application-imposed reading deadline.
- On consent update/form errors, load only if UMP itself says canRequestAds=true,
  as specified in Google's [UMP guide](https://developers.google.com/admob/android/next-gen/privacy).
  No consent override or own cached-consent shortcut is introduced.
- Failure invalidates late callbacks, and retry clears the prior support reference.
- Network, timeout, configuration, no-fill and other SDK failures are distinguished
  in English, Spanish and French. A stage/error reference is visible for voluntary
  screenshots; it contains no advertising identifier, raw response or consent string.
- Small injectable SDK boundaries support controller regression tests. Demo IDs,
  opt-in/two-tap playback, earned-only rewards, expiry and ad-audio gate remain.

Review access is now controlled by explicit `auralift.reviewAccess` (default false;
true in the closed-test properties). A public-store build rejects that flag, and
when false the generated verifier is empty, hiding the entry and invalidating
previous review-only grants. Existing purchased/earned access is preserved.
The CI guard is prepared but not run. Play track selection is external: production
must use a fresh build with storeLive=true, testAds=false, reviewAccess=false,
not promote this closed-test binary. Update Play Console review instructions when
removing the former code. Existing End review access suffices for the failed-ad
fallback; it intentionally does not revoke purchases or an unexpired earned pass.

Candidate version 0.5.10 / code 15. No signed release or Play upload yet.
- Localization validation passed: 384 matching keys per locale, format arguments
  and all six legal documents checked. git diff --check passed.
- Both required Gradle invocations were attempted and failed before compilation
  at the Gradle 8.13 download (network unreachable). No Android test pass claimed.
- Seven new controller regression tests cover slow consent, UMP-approved fallback,
  denied consent, form errors, timeouts, stale callbacks/retries and host destruction.
  They have not executed in this environment.
- Automatic approval review rejected pushing the candidate branch because the
  current request was not considered authorization to publish repository contents.
  Jamie subsequently explicitly authorized Git pushes on 21 September.
  Remote validation is now being prepared.
The reported weak boost/crackling is a separate unresolved device/audio issue.

## 0.5.9 closed-test rewarded ads — built and signed

Version code 14 enables Google's official demo app and rewarded-unit IDs in the
optimized Play release, controlled by the explicit `auralift.testAds=true` property.
The runtime and manifest receive matching IDs. Test mode overrides supplied live
IDs; disabling it restores the existing production-property/disabled behavior.
The configuration rejects `testAds=true` with `storeLive=true`; CI now checks that
rejection and its expected error. The guard cannot detect a Play Console track:
this bundle is for internal/closed testing, not promotion into production.

There are no reward/consent/audio/expiry code changes. The same UMP checks and SDK
earned-reward callback grant one real hour, persisted across reopening; loading or
closing an ad alone grants nothing. Existing tests cover duplicate rewards,
failed claims, expiry, gain clamping, retained profiles and the ad audio gate.
Owner remains a separate SDK-free package and payment configuration is unchanged.
See Google's [test-ad instructions](https://developers.google.com/admob/android/next-gen/test-ads).

- Local localization and whitespace checks passed.
- Both prescribed local Gradle commands failed at the Gradle 8.13 download with
  network unreachable. The required GitHub Actions gate passed instead.
- Validated source `6cb42719e52b784049028c39d82aab839a556eb8`, PR merge
  `8ebe0fba23697740f480f66fe08c26d78e9ab52c`.
- [Required build gate](https://github.com/Jamie1171/Auralift/actions/runs/35446179323):
  both lint tasks, APKs, AAB and the public-store/test-ad conflict check passed.
  API 35: Owner 71 / Play 75; API 26: Owner 48 / Play 53. Total 247 unit-test
  executions, no failures/errors/skips; result XML and guard log inspected.
- [Device workflow](https://github.com/Jamie1171/Auralift/actions/runs/35446179320):
  Android 8 and Android 16 jobs passed.
- [16 KB optimized runtime](https://github.com/Jamie1171/Auralift/actions/runs/35446179287):
  passed on x86-64. This does not add physical-device or ARM runtime evidence.
- Signed deliverable: `Auralift-0.5.9-Closed-Test.aab`, 8,469,933 bytes; SHA-256
  `3bf8bbfe3c708c9cd7145c4d8ebe71c1b1c49d777860173ce368173ac5dc183f`.
  CI archive SHA-256:
  `2d9efab3a100b3d0b2863c9620817f8dc40b1cbb96f2b422ebcfa8e06ab8a504`.
  All 547 original bundle entries remain unchanged after signing and ZIP CRCs pass.
- Strict signature verification passed with the existing upload certificate trusted;
  certificate SHA-256
  `281699a8a22822d97772c1cba3f5f5d6e0048807468ce7e4f2dbceacc40be85f`.
  Inspected manifest and DEX: version 0.5.9 / code 14, non-debuggable, matching
  Google demo app/unit IDs, billing permission, scoped email query, existing public
  billing key and lifetime option IDs. All six legal support addresses remain.
- No Play Console upload/submission or actual ad viewing was performed here.
- Actual ad loading/completion, consent UI and the one-hour timer must be checked
  on an installed Free test account. No live inventory, revenue or account-side
  AdMob readiness is claimed. No test purchase is required to earn a pass.

## 0.5.8 support email repair — built and signed

Jamie reported a successful 0.5.7 Play test-card purchase and Pro unlock/restore.
This is user-observed evidence, not a claim of testing Supporter, refunds or pending
payments. The same device opened support drafts with only a recipient, and failed
to open drafts containing a screenshot.

Version code 13 uses ACTION_SEND for the complete draft, discovers email packages
through a scoped mailto query, and targets their actual SEND activities. It removes
the SENDTO selector from screenshot sharing. The chooser and its targets carry the
image URI and temporary read permission. Message, subject and optional diagnostics
remain together; requested device details have a fallback before the audio service
has produced diagnostics. The form remains local and the user sends in their email
app. No backend, automatic sending or broad package visibility is introduced.
The address is centered with 12 dp spacing below it.

Regression coverage checks text/Unicode preservation, the actual share activity,
attachment access through the chooser, exclusion of non-email apps, unsupported
clients, and optional diagnostic fallback. The implementation follows Android's
[email intent guidance](https://developer.android.com/guide/components/intents-common#Email)
and [sharing guidance](https://developer.android.com/develop/ui/compose/sharing/send).

- Local localization and whitespace checks passed.
- Both prescribed local Gradle invocations failed downloading Gradle 8.13 because
  the network was unreachable. The required GitHub Actions gate passed instead.
- Validated source `1982292a82f595bc56a491df06df8ba9bd3cbfe6`, PR merge
  `a4af4ba1dcc7d96a1de0bb1a9ac237fdaf5f3b59`.
- [Required build gate](https://github.com/Jamie1171/Auralift/actions/runs/35442256461):
  both lint tasks, APKs and AAB passed. API 35: Owner 71 / Play 75; API 26:
  Owner 48 / Play 53. Total 247 unit-test executions, no failures/errors/skips.
  All five support regression cases passed in each edition on each SDK.
- Inspected the generated feedback screenshot: centered address and separation
  above the category controls are visible; the form and email action remain usable.
- [Device workflow](https://github.com/Jamie1171/Auralift/actions/runs/35442256475):
  Android 8 passed; Android 16 later ended cancelled, so no successful Android 16
  completion is claimed for this build.
- [16 KB optimized runtime](https://github.com/Jamie1171/Auralift/actions/runs/35442256468):
  initial attempt failed during background checks. Logs show a native SIGSEGV in
  emulator `system_server` / SettingsProvider, followed by DeadSystemException in
  Auralift, Phone and Google Play services. A single fresh-emulator retry passed
  (job 105895873549); the initial failed attempt is retained as infrastructure
  failure evidence, not reclassified as an app pass. Existing ARM runtime limits
  remain; the exercised runtime is x86-64.
- Signed deliverable: `Auralift-0.5.8-Closed-Test.aab`, 8,469,890 bytes; SHA-256
  `5fda94815077ecdcfe5eb252ec5dfd6bb62fbf445e02dbbc5b9565e7bc9a15ef`.
  CI archive SHA-256:
  `626203d5adbd487e8d0a0470864e8f4445db307dac564f185a34e61ddf8e1060`.
  All 547 original bundle entries remain unchanged after signing and ZIP CRCs pass.
- Strict signature verification passed with the existing upload certificate trusted;
  certificate SHA-256
  `281699a8a22822d97772c1cba3f5f5d6e0048807468ce7e4f2dbceacc40be85f`.
  Manifest confirms `com.jamiewardle.auralift`, 0.5.8 / code 13, min SDK 26,
  target SDK 36, non-debuggable, billing permission and the scoped mailto query.
  No QUERY_ALL_PACKAGES permission. Existing public billing key, lifetime option
  IDs and all six legal support addresses remain present in the signed bundle.
- Actual Gmail draft population and screenshot opening must be confirmed on the
  device after installation; automated intent tests do not prove client behavior.
  No email was sent and no Play Console upload/submission was performed.

Subsequent user evidence: Jamie confirmed 0.5.8 on Pixel 9a / Android 17 populated
the Gmail subject and message, included optional diagnostics, and attached the
chosen screenshot. He then promoted the bundle to the closed track for review.

## 0.5.7 Play billing configuration — built and signed

Version code 12 configures Jamie's supplied RSA-2048 public licensing key.
Public-key DER SHA-256: `5fc0dad47336ec054b129200dd2285dddfed82082665ef5396b5e6e520b2105c`.
Pro and Supporter accept their `pro-lifetime` / `supporter-lifetime` base options,
prefer them over legacy `buy`, and reject unrelated/promotional offers. Regression
coverage checks both mappings and rejection cases. Prices remain supplied by Play.
Release advertising IDs remain unset; Owner remains separate and unlocked.

The emulator ADB collector now replaces invalid UTF-8 in diagnostics instead of
crashing after completed app checks. Command exit-code checks are unchanged.

Both prescribed local Gradle invocations failed while downloading Gradle 8.13
(network unreachable). Required gates will run in GitHub Actions instead.
Validated source `5afa89245f39a76f84e28a61c1582999ddbc1957`, PR merge
`1da1289fb701890ac8b2433cbea4eea67b9eab07`.
- [Required build gate](https://github.com/Jamie1171/Auralift/actions/runs/35411054760):
  both lint tasks, APKs, AAB and both unit-test invocations passed. API 35: Owner 68 /
  Play 72; API 26: Owner 45 / Play 50. Total 235, no failures/errors/skips.
- [Android 8 and Android 16 device jobs](https://github.com/Jamie1171/Auralift/actions/runs/35411054636): both passed.
- [16 KB optimized runtime workflow](https://github.com/Jamie1171/Auralift/actions/runs/35411054688): passed, including diagnostic collection.
  This does not add physical-device or ARM runtime evidence to the earlier limits.
- Local localization checks passed for all 379 keys per locale and six legal documents.
  All 12 emulator-helper tests passed; an additional invalid-byte diagnostic check
  confirmed decoding recovery while preserving failed-command rejection.
- Signed deliverable: `Auralift-0.5.7-Closed-Test.aab`, 8,468,437 bytes; SHA-256
  `b7d8d206ed812e7a22921be125e966d8e8c9f2e87533bfc90d59ef47099c6600`.
- CI archive SHA-256:
  `fce73af1d82e65c190a1959601b0c495381cf42a032cc45e8d6e844107e307e3`.
  All 547 original bundle entries remain byte-identical after signing; ZIP CRCs pass.
- Strict signature verification passed with the existing RSA-4096 upload certificate
  trusted; certificate SHA-256
  `281699a8a22822d97772c1cba3f5f5d6e0048807468ce7e4f2dbceacc40be85f`.
- Bundle manifest inspected: `com.jamiewardle.auralift`, code 12, min SDK 26,
  target SDK 36, non-debuggable, billing permission present. DEX contains the exact
  supplied verification key and both lifetime option IDs. All six bundled legal
  documents retain the confirmed shared support address.
- Saved signed bundle for Jamie to upload as the next closed-track release.
  No Play Console submission, standalone bundletool validation or real transaction
  testing was performed by this build process.
No actual Play purchase, restore, refund, pending payment or approval is claimed.

## 0.5.6 shared support mailbox — built and signed

Validated source `c24b364f223dc7f15a163bb9d26f6e303b89b97b`, PR merge
`b3290a50da7ce32e59381ad605ebe804eaca7560`; version 0.5.6 / code 11.

- [Required build gate](https://github.com/Jamie1171/Auralift/actions/runs/35405225416)
  passed: both editions, both lint tasks, APKs and AAB. API 35: Owner 68 / Play 70;
  API 26: Owner 45 / Play 48. Total 231 unit-test executions, no failures/errors/skips.
- [Android 8 and Android 16 device jobs](https://github.com/Jamie1171/Auralift/actions/runs/35405225367)
  both passed. Android 16 finished all 17 primary tests and its separate denied-permission
  test. This includes the language/support scenario that previously timed out;
  the earlier timeout evidence below is retained, not reclassified as a pass.
- [16 KB optimized runtime job](https://github.com/Jamie1171/Auralift/actions/runs/35405225427)
  is **failed**, although its saved `page-size-report.json` reports all four app checks
  passed on x86-64 API 35 with 16 KB pages and compatibility workarounds disabled.
  Its final diagnostic `logcat` collection raised UnicodeDecodeError after those checks.
  Do not describe the whole workflow as successful. Existing ARM/RELRO limits remain.
- The recipient and copy-address fallback are
  `auraforgelabssupport+auralift@gmail.com`. All six bundled policies and current
  terms mirrors use the same confirmed shared inbox; existing subjects identify Auralift.
  The contact-only correction keeps agreement version 2026-09-18.2.
- [Policy website deployment](https://github.com/Jamie1171/auralift-policies/actions/runs/35405237463)
  passed for `47927e5b134e17affe4c7ebe12e96180d05aba62`: six document pages and the
  index use the new address. Local page links and 379 resource keys per locale checked.
- Signed deliverable: `Auralift-0.5.6-Closed-Test.aab`, 8,467,839 bytes; SHA-256
  `981cdd5eb5db0e677b0cc77801c787174c8b3ad5f168594373319c430f5773f9`.
- CI archive SHA-256:
  `0e88acdc9c6282b9b2e8e68efe41e3a315f405c25ce6066e6f35ae26c969902d`.
  All 547 original AAB entries are byte-identical after signing; ZIP CRC checks pass.
- Strict JAR signature verification passed with the existing RSA-4096 upload key
  trusted. Upload certificate SHA-256:
  `281699a8a22822d97772c1cba3f5f5d6e0048807468ce7e4f2dbceacc40be85f`.
  Private key material remains outside Git.
- Bundle protobuf manifest inspected: `com.jamiewardle.auralift`, code 11,
  min SDK 26, target SDK 36, non-debuggable; `com.android.vending.BILLING` present.
  All six bundled policies checked for the new address and absence of the old address.
- Both prescribed local Gradle commands were attempted but the wrapper download was
  network-blocked; the successful CI run above executed the required tasks instead.

This signed bundle is for upload to a Play Console draft and product configuration.
Live purchases and ads remain disabled pending account configuration. No Play upload,
review submission, purchase/ad certification, standalone bundletool validation or
AAB-derived device test is claimed. Historical documents/evidence preserve their
original contact details.

## 0.5.5 support and localization — required gates passed, 18 September 2026

Validated source `28b0f654e926cc9a683827b89aadea2ffda31bb3` (version code 10),
PR merge build `8ccaf8925f771027425f3a18fbb488a282e9bd0a`.

- [Required unit/lint/build gate](https://github.com/Jamie1171/Auralift/actions/runs/35397434159): passed both editions, both lint tasks, Owner APK,
  optimized public APK and public AAB. API 35: Owner 68 / Play 70 tests; separate
  API 26 invocation: Owner 45 / Play 48 tests. Total 231, no failures or skips.
- [Android 8 device suite](https://github.com/Jamie1171/Auralift/actions/runs/35397434336/job/105769370241): all 17 tests passed, plus the separate denied-permission check.
  Real app language selection cycles EN/ES/FR, survives activity recreation,
  retains agreement and opens the matching offline policies and support screen.
- Android 16 device suite in the same run: attempt 1 timed out after 45 minutes.
  Its log reached 14/17 tests with zero reported failures, then stopped progressing.
  This is an incomplete run, not a pass or proof of an application failure.
  Job 105781123445 retries only the cancelled API 36 job; completion is pending.
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

## 0.5.5 signed closed-test bundle — prepared, rollout verification pending

Prepared from the exact CI archive above (archive SHA-256
`5811ce8f37651bca399debc24d1f2e7471b6b37699b7427084082463f6b8f9f8`),
without rebuilding or changing application contents.

- File: `Auralift-0.5.5-Closed-Test.aab`, 8,467,738 bytes.
- SHA-256: `1163ada1f312cb182fe398576ea757d1515de6b88fb5645d0de143916d66afa8`.
- Manifest inspected from the bundle: `com.jamiewardle.auralift`, version 0.5.5 /
  code 10, min SDK 26, target SDK 36, non-debuggable.
- Signed with a dedicated RSA-4096 upload key, SHA256withRSA and SHA-256 digests.
  Strict JAR signature verification passes with its upload certificate trusted.
  All 547 original bundle entries are byte-identical; only signature metadata
  was added. ZIP CRC verification passes.
- Upload certificate SHA-256:
  `281699a8a22822d97772c1cba3f5f5d6e0048807468ce7e4f2dbceacc40be85f`.
  Alias `auralift-upload`, PKCS12. A private backup was delivered separately to
  Jamie; no key or password is committed. Reuse this upload key for future bundles.
  It is separate from Google's app-signing key and the sideloaded-test certificate.
- Live ads and checkout are disabled in this bundle. AdMob and Play purchase
  configuration remain separate work; software tests do not certify live payments.
- The original API 36 timeout and retry above must be resolved before rollout.
  The existing 16 KB/ARM64 native-library limits remain recorded below. Direct
  inspection confirms all native LOAD alignments are 16 KB; the graphics-path
  GNU_RELRO end modulo 16 KB is still 8192 in all four ABIs.
- Standalone bundletool validation could not be run because the official tool
  download did not complete in this environment. The CI Gradle bundle build and
  local signature/identity/content checks passed. Play Console validation of the
  uploaded draft and AAB-derived runtime testing are not yet claimed.

This file can be uploaded as a draft for Console validation. It has not been
uploaded or submitted by the assistant; no reviewer/tester invitation was sent.

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
