# Auralift: goals for the 20 human testers

Draft prepared 14 September 2026 for Jamie's planned tester group. Not sent or
posted. Use the public **Play test-track build**, not Owner or the Firebase APK
pair. Supply the actual opt-in/install link and build number when the track is
ready; none is invented here. Ask for honest private feedback, including failures.

## What every tester should do

Start with familiar speech or music at a comfortable media volume and **zero
boost**. Stop other boosters/equalizers. Increase gradually; testing maximum gain
is not required. Reduce or stop if sound is distorted or uncomfortable.

1. **Does boosting actually work?** Try your usual player and one different player.
   Compare boost on/off using Compare original, then Stop. Record audible change,
   no difference, distortion, or any interruption. A "connected" label alone is
   not a pass. Include speaker and one headphone/Bluetooth output if available.
2. **Does it keep working?** Play for 20–30 minutes, switch apps and lock the screen.
   Compare again afterwards. Record whether audible boost stopped, when, and whether
   Auralift still claimed it was running. Stop should remove boost while media continues.
3. **Does changing output behave sensibly?** Connect/disconnect Bluetooth or USB/wired
   headphones if owned. Keep the default reset-on-route-change setting. Extra gain
   should return to zero; there should be no sudden increase or stuck effect.
4. **Can you use the controls comfortably?** Try large system text, portrait and
   landscape, EQ presets and saved setups. Report unreachable controls, confusing
   wording or clipping. Try another supported language if you know it.
5. **Retest a fix.** After Jamie supplies an update, repeat the exact failing steps
   and say whether the original issue was fixed or a new problem appeared.

## Split additional coverage across the group

Assignments are targets based on the devices/accessories people actually own;
do not require anyone to buy hardware. Everyone still completes the core checks.

| Group | Target allocation | Extra focus |
| --- | ---: | --- |
| Google Pixel | 5 people | Common streaming players, screen lock, output changes |
| Samsung | 5 people | Samsung battery settings and media apps; recent and older Galaxy |
| Other manufacturers | 5 people | Motorola, Xiaomi/Redmi, OnePlus/Oppo or other available phones |
| Older/smaller devices and accessibility | 5 people | Old supported Android, low memory, large text, landscape/TalkBack when already used |

If the group does not have that mix, record the actual coverage and missing
combinations. Twenty testers on one model do not establish twenty-device coverage.

## Pro and monetisation checks: only when ready

- **Floating player:** testers with a legitimate test Ad Pass or test Pro access
  grant Auralift's overlay permission, move/expand/close it, use controls over their
  real media app, then lock/unlock and revoke permission. Closing the bubble should
  leave boost running; Stop should end boost. Check protected screens separately.
- **Ad Pass:** when configured, use Google test ads/test devices. Complete versus
  dismiss an ad, test offline/unavailable inventory, and observe an actual one-hour
  pass expiry. Ad audio should not receive boost. Expiry lowers gain above +15 dB,
  retains saved profiles and never increases loudness after renewing access.
- **Purchases:** designated Play licence testers only, with test payment methods.
  Check the two products separately, cancellation/pending, restore and refund.
  Both products grant identical Pro features. Ordinary closed-test membership does
  not make purchases free; do not ask the general group to spend money.
- **Updates and restarts:** test an update through the Play track, saved settings,
  force-stop/reopen and a normal phone restart. Boost must not automatically restart
  at remembered high gain. Test the optional restart reminder if enabled.

These goals complement automation; they have not been marked complete. Account
configuration and a signed release build are still needed before track-specific
ad, purchase and upgrade tests can begin.

## Simple report to return

Copy and fill in; no medical/hearing details, account credentials or private audio
are needed. Screenshots should exclude personal notifications and other private content.

```text
App build/version:
Phone model and Android version:
Player app (and version if known):
Output (speaker / headphone model / Bluetooth model):
Auralift mode (System mix / Player sessions):
Selected boost and Android readback, if shown:
Goal tested:
Steps:
Expected:
What actually happened:
Audible boost (yes / no / uncertain):
Time until failure, if background testing:
Battery setting changed? If so, which:
Can you repeat it?:
After update: fixed / still present / new issue:
```

Use a few meaningful check-ins across the testing period: initial install/audio,
background/routes, usability/Pro if available, and retesting fixes. Follow the
actual Play Console opt-in period rather than assuming the exchange site's credit
schedule establishes eligibility. Do not request positive public ratings.
