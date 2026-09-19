# Auralift closed-testing brief

## 0.5.9 test-ad access

Use version code 14 or newer with the explicit closed-test ad configuration.
From a Free account, tap Prepare Ad Pass, complete any applicable privacy choices,
then Watch ad. Google's test ad should identify itself as a test ad. Completing it
through the SDK reward callback earns one real hour of Pro, including the floating
player and themes; no purchase or reviewer unlock is needed. Loading or closing
an ad alone must not grant a pass. When testing early dismissal, first ensure
there is no active pass and close before the reward has been earned.

After earning a pass, test the floating player while playing quietly: expand,
minimise, Stop boost, restart boost and Close. Check the pass persists when leaving
and reopening Auralift. At expiry the player should close and Pro controls lock,
while external music continues; gain above +15 dB is capped. Renewing must not
restore a previously higher gain. Use comfortable playback, not maximum gain.

Permanent Pro owners and accounts with reviewer access do not see the Ad Pass
offer. They need a separate eligible Free tester account to exercise this flow.
Report unavailable/loading/consent errors rather than making a purchase. This
tests the ad/reward integration, not production inventory, revenue or AdMob approval.

Suggested first five prompts: boost effectiveness, background reliability, Ad Pass
completion/unlock, floating-player behavior, and stop/restart. Rotate output changes,
EQ persistence, general usability and support into later checkpoints as supported
by the platform. Nobody needs to buy Pro to complete the ad/floating-player tests.

The 14 September [human testing goals](HUMAN-TEST-GOALS.md) refine this brief for
Jamie's planned group of 20, with device/player/output reporting and tests that
complement the new automation. The historical exchange description below does
not establish the current account's credits, recruited testers or Play eligibility.

13 September 2026. Prepared for Jamie's Get12Testers account; not submitted or
sent to anyone. The account and listing remain under Jamie's control.

Get12Testers describes 20 credits per app tested over checkpoints on days 1, 4,
7, 10 and 14; listing an app costs 60 credits. Completing three apps can therefore
fund one listing. This describes the site's advertised exchange, not independently
verified tester quality or a guarantee of Play approval. Its clock is not the
Play Console's authoritative eligibility record.
[Site description](https://get12testers.com/).

For personal developer accounts created after 13 November 2023, Google currently
requires at least 12 testers continuously opted in for the preceding 14 days
before applying for production access. Application and review still follow;
replacement testers need their own continuous period. Recruit a small buffer
above 12 and retain actual feedback/fix evidence. The requirement concerns Play
opt-in continuity, not simply collecting website credits.
[Official testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en).

## Before listing

Use the **public Play package** distributed from the appropriate Play test track.
Do not distribute Owner or upload an unsigned AAB. Resolve the 16 KB native-library
check, run an internally signed release, configure privacy and listing materials,
and supply the actual opt-in link. Test payments with designated licence testers;
recruiting a closed tester does not automatically make their purchases free. Use
Google test ads/test devices during ad QA, never ask testers to farm live ads.

Seek Pixel, Samsung and at least one other manufacturer, Android 8/older hardware
and current Android, speaker plus wired/USB and Bluetooth. Installation support is
Android 8.0+; audible boost varies by route and player. Test low gain first; nobody
needs to use maximum gain or disclose hearing/medical information.

## Eight features to request feedback on

| Feature | What testers should try | Evidence to request |
| --- | --- | --- |
| Boost and comparison | Familiar media at comfortable volume; enable, small gain change, Compare original, Stop | Phone/Android, player, output; audible change yes/no; distortion at comfortable settings |
| EQ and saved sound | Balanced vs Voice; custom curve; create/load a named Pro sound | Whether controls are understandable and the chosen sound returns |
| Background session | Play media, leave app and lock screen for 15–30 minutes; return and Stop | Time until any failure; notification visible; battery setting chosen |
| Route changes | Switch Bluetooth/wired/speaker; confirm default gain reset | Any unexpected jump, failed reconnection or stale status |
| Floating controls | Grant overlay permission; move, expand, adjust and close; lock/unlock | Layout/touch problems, hidden on lock, Stop remains usable |
| Ad Pass | Test ad: complete vs skip, offline, change privacy choices; observe the hour and expiry | No reward on skip/failure; reward on completion; no boost applied to ad; saved sound retained at expiry |
| Purchases | Licence testers only: test both products separately, cancel/pending/complete/restore/refund | Same entitlement; real Play price shown; no duplicate purchase offered; restore on same account |
| Appearance and reminders | Light/dark, language, larger font, themes; opt-in reboot reminder | Clipping or untranslated labels; no automatic boost after restart; useful reminder |

## Checkpoint plan

| Day | Focus | Record |
| --- | --- | --- |
| 1 | Installation, comfortable boost/compare, first impressions | Baseline device/player/output and specific setup failures |
| 4 | Screen lock, background, notification permissions, output changes | Reproduction steps and duration before failure |
| 7 | EQ, floating controls, accessibility, languages and themes | Usability findings; severity and screenshots with private content removed |
| 10 | Test-ad pass/expiry and designated billing tests; update installation | Entitlement transitions and whether any fix regressed audio |
| 14 | Retest fixes and normal listening; final feedback | What improved, remaining blockers, production-readiness assessment |

Ask for honest private testing feedback, not positive public ratings, incentivised
reviews or meaningless daily taps. Review recurring issues by feature. Maintain a
log: date, build, device/OS, steps, expected/actual, severity, fix commit, retest result.
The app's manual Feedback page can share a report; device diagnostics are opt-in.

## Production application evidence

Keep tester recruitment method/count, opted-in dates, actual usage/feedback,
changes made because of testing, retests and remaining limitations. Answer Play's
production-access questions from that evidence. A test-exchange membership is a
recruitment aid, not proof that this particular app is ready to ship.
