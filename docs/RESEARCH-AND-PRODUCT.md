# Research and product decisions

Historical research for the first preview. The implemented 0.3.0 scope and pricing
proposal are now in [Features and monetisation](FEATURES-AND-MONETISATION.md).

Researched 12 September 2026. Competitor features below are publisher claims from
their listings, not independently measured audio performance. The provided
screenshots directly identify the reference app's interface as **EZ Booster**;
the matching primary listing is Simple Design's `volumebooster.soundspeaker.louder`.
The screenshots show a gain dial, system-volume slider, eight shortcuts, an EQ tab,
player controls and advertising. The icon's “800%” is not evidence of an eightfold
increase in perceived loudness or hardware output.

## Comparison

| App | Relevant advertised capabilities | Decision for Auralift |
| --- | --- | --- |
| [EZ Booster / Volume Booster – Sound Booster](https://play.google.com/store/apps/details?id=volumebooster.soundspeaker.louder) | Gain presets, speaker/headphone/Bluetooth boosting, background use, player controls, spectrum, stereo effects, widgets and notification controls; listing claims broad coverage and up to 200% | Keep fast controls, gain and background access. Replace decorative dial interaction and promotions with accessible native controls. Treat broad compatibility/quality claims as unproven. |
| [Volume Booster GOODEV](https://play.google.com/store/apps/details?id=com.goodev.volume.booster) | Small, simple media booster; explicitly describes device variability and excludes phone-call boosting | Keep a direct on/off and gain interaction. Show limitations where they affect use. |
| [Poweramp Equalizer](https://play.google.com/store/apps/details?id=com.maxmpz.equalizer) | Configurable 5–32 bands, parametric mode, preamp, limiter, compressor, balance, presets, AutoEQ, advanced player tracking and themes | Adopt purposeful presets and saved setups. Advanced tracking/parametric processing is a later engineering project, not something to simulate with sliders. |
| [Flat Equalizer](https://play.google.com/store/apps/details?id=com.jazibkhan.equalizer) | Ten-band EQ, bass, virtualizer/reverb, gain, light/dark themes and presets | Keep the clean control hierarchy and appearance choice. Map real device bands rather than pretending every phone offers ten. |
| [Google Sound Amplifier](https://support.google.com/accessibility/android/answer/9157755?hl=en) | Headphone-based amplification and filtering; installed on Pixel 3 and newer; supports different workflows from a simple media booster | Surface Android accessibility settings. Do not duplicate microphone capture or imply this app is a medical hearing aid. |

## What Android actually permits

1. [LoudnessEnhancer](https://developer.android.com/reference/android/media/audiofx/LoudnessEnhancer)
   applies target gain to an audio session and compresses samples beyond the
   platform range. It does not measure sound pressure or guarantee safe listening.
2. [AudioEffect](https://developer.android.com/reference/android/media/audiofx/AudioEffect)
   documents session ownership and explicitly deprecates insert effects on global
   session zero. [AOSP's LoudnessEnhancer source](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/media/java/android/media/audiofx/LoudnessEnhancer.java)
   also warns about global output use. Keeping this route as a labelled compatibility
   mode is a practical inference from the reference app's function, not a universal
   platform guarantee or a reverse-engineered claim about EZ Booster's implementation.
3. Player-session broadcasts support cooperating players. They do not grant access
   to all playing apps. A notification/media-session token is not an AudioTrack
   session ID. Auralift does not use privileged player tracking or fabricated IDs.
4. [Playback capture](https://developer.android.com/media/platform/av-capture) needs
   consent and is restricted by usage, profile and capture policies. Capturing,
   amplifying and replaying other apps also creates delay/double playback issues;
   it is not a clean unrestricted replacement for audio effects. This build does
   not capture audio.
5. [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
   require the correct declared purpose. Auralift uses `specialUse` for user-started
   effect control because it is not itself a media player. Public Play submission
   will need this use case reviewed. No silent playback is used to keep it alive.
6. [Android accessibility guidance](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
   calls for controls at least 48 dp. Compose controls supply native semantics,
   keyboard/accessibility interaction and text scaling. The display dial is not
   the only means of changing gain.

## Scope of the personal preview

Implemented: media volume; gain in half-dB steps; 0/+3/+6 dB shortcuts; extended
range selection; actual effect capability reporting; EQ and voice/detail/warm
presets; explicit save/load for three output setups; notification, widget and tile;
basic current-player commands; inexact sleep timer; dark/light appearance; route
reset and native diagnostics. No accounts, advertising, tracking, cloud or asset
generation is required.

The app supports up to +18 dB requested gain (about 794% sample amplitude). The
default user range is +6 dB. An EQ-only fallback is limited by the device's band
range and a 12 dB ceiling. This fallback has no LoudnessEnhancer compression and
is described accordingly. Selected and applied gain are separate when limited.
Neither percentage expresses perceived loudness or a safety threshold.

## Sequenced next steps

| Stage | Work | Evidence required to advance |
| --- | --- | --- |
| Personal preview | Native interface and real Android effect lifecycle | Build/test results plus Pixel listening comparison, screen-off and output changes |
| Compatibility hardening | Test media players, wired/USB, Bluetooth and multiple Android vendors; diagnose session-zero rejection or offload bypass | A device/player matrix with actual observations; no blanket support claims |
| Feature expansion | Optional local-file player, album artwork/current-track metadata with explicitly requested access, more preset management; evaluate spatial effects | Clear added value without expanding permissions unnecessarily |
| Public beta | Final identity, release signing, accessible large-text QA, translated strings, crash handling, battery/lifecycle soak tests | Signed release build and repeatable acceptance evidence |
| Store launch | Verify current Play policies, `specialUse` declaration and review, privacy disclosures, target API, third-party notices and store content | Owner-reviewed product and store submission; installation works without development permissions |

Per-ear EQ, audiogram fitting, microphone hearing assistance, a calibrated output
meter, hard DSP limiting and system-wide mono/balance controls are **not** included.
Those require additional APIs, processing architecture or medical/product decisions.
Do not use decorative animation to pretend they are implemented.

The name “Auralift” is provisional. Name and trademark availability have not been
cleared; select the final product identity before release investment.

## 0.2.0 follow-up: full requested range

The owner reports that EZ Booster Premium offers +30 dB and requests that range
after hearing little distinction in the initial Auralift preview. This premium
range is user-reported, not independently verified. Auralift 0.2.0 now exposes
0–30 dB by default, replacing its original 18 dB application cap and 6 dB default
range. The expansion does not raise saved gain or Android media volume. Android
parameter readback, Compare original / Restore boost, and copyable connection
details accompany the change. Readback is a native setting, not measured loudness;
the device EQ fallback remains limited by its reported band range.
