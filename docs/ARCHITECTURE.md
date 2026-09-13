# Architecture

## Stack

Kotlin 2.2.21; Jetpack Compose BOM 2025.08.01; Material 3; Android Gradle plugin
8.13.2; Gradle 8.13; JDK 17; compile/target API 36; minimum API 26. Standard Android
AudioManager, AudioEffect, LoudnessEnhancer and Equalizer do audio work. AOSP APIs
are used directly; there is no external DSP SDK, app-authored C/C++ engine or web runtime.

## Ownership

`AuraliftApplication` owns settings, feature access, named profiles, the distribution-specific
purchase adapter and observable engine state. `MainActivity`
renders Compose and makes user-initiated service requests. `SettingsStore` persists
preferences and manual output profiles locally in private SharedPreferences.
Enabled state is deliberately not persisted. Application backup is disabled.

`BoostService` alone owns effect chains, device callbacks, the temporary public
session receiver, timer and foreground notification. All mutations happen on the
main dispatcher. Gain increases ramp by 0.25 dB every 25 ms; reductions do not wait.
`EffectChain` owns and releases its two AudioEffect objects. Exceptions during
creation, enabling, configuration and release are handled. Effects require control
and enabled status before being reported as connected.

System mode creates session-zero effects. Player mode creates effects only for
cooperative positive session IDs announced while the service is active. At most
16 announced sessions are retained. Session IDs received externally are checked;
broadcast content is not interpreted as code or trusted as an audible-output proof.
Changing modes releases every old chain before creating a new one, so gain is not
applied simultaneously to a player and the global mix.

Equalizer curves are normalized so the largest positive value becomes zero, then
interpolated in log-frequency space onto actual hardware bands. Android owns
effect ordering; this is gain budgeting, not a custom sample-level DSP chain. LoudnessEnhancer
provides the requested preamp gain and its native sample compression when available.
If it is unavailable but a controllable EQ exists, uniform band gain provides a
compatibility fallback capped to the device’s own upper band limit and 35 dB. The UI reports that native compression is absent.
If both are unavailable, the service reports the failure instead of pretending to
boost. A connected effect can still be bypassed by a player or output route.

## Gain and comparison, updated in 0.5.0

The application ceiling is 35 dB for Pro/Owner and 15 dB for Free. One dB is
100 millibels, so the Pro top setting sends 3500 mB to LoudnessEnhancer. `GainController` reads `getTargetGain()` back, rather
than reporting the ramp’s requested value as applied gain. A failed/invalid read
is shown as unavailable; a different returned target remains visible. If the
device rejects an increase with IllegalArgumentException, the last accepted
value is restored and becomes the connection’s limit. A failed reduction or
broken effect propagates to its owner for release. Reconnect tries a fresh effect.
The EQ fallback reports native band levels, not a claimed loudness target.

Preferences migrate once to expose the full range without raising the saved
gain. Subsequent user-selected smaller ranges persist normally. Comparison state
is temporary and owned by the service: zero boost plus Balanced EQ, with the
user’s original preferences untouched. Restore uses the existing gradual ramp.
Stop releases both effects. Android reads and device limits do not establish
acoustic gain or confirm that another app routes audio through the effect.

Reference: [Android LoudnessEnhancer API](https://developer.android.com/reference/android/media/audiofx/LoudnessEnhancer)
and [Equalizer API](https://developer.android.com/reference/android/media/audiofx/Equalizer).

## Lifecycle

- Audio starts only from a visible user action. An optional boot receiver posts
  a reminder to open the app; it never starts effects or a foreground service.
- Uses `START_NOT_STICKY`; service termination releases the processing with it.
- Foreground `specialUse` describes the real effect-control task. It does not
  request audio focus or play silence, which would interfere with the user's player.
- Output device changes and AUDIO_BECOMING_NOISY rebuild effects. By default the
  requested gain is reset to zero first. An output-device list is not represented
  as reliable per-player routing telemetry; saved profiles are chosen manually.
- Stop releases controlled effects, removes listeners, cancels timers, updates quick
  controls and removes the notification. It does not command the external player
  to pause, mute or stop.
- Sleep uses an elapsed-real-time deadline checked while running and an inexact
  allow-while-idle alarm. Delayed alarms check the current deadline, preventing an
  old alarm from stopping a newly timed session. Exact-alarm permission is unnecessary.

## Permissions

| Permission/component | Reason |
| --- | --- |
| MODIFY_AUDIO_SETTINGS | Media volume and audio effects |
| FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE | Keep user-enabled effects alive with a Stop notification |
| POST_NOTIFICATIONS | Optional visible background controls on Android 13+ |
| BIND_QUICK_SETTINGS_TILE | System-only binding to the declared tile service |
| Temporarily exported broadcast receiver | Cooperating media apps announce audio-effect session IDs |

Optional RECORD_AUDIO permits foreground-only Visualizer FFT sampling, not microphone
recording. SYSTEM_ALERT_WINDOW permits the user-enabled floating controls. VIBRATE
supports optional short haptics. No INTERNET, QUERY_ALL_PACKAGES, READ_MEDIA_AUDIO,
storage, location, Bluetooth-connect, notification-listener, accessibility-service,
battery-optimization exemption, exact-alarm or root permission is requested by app
source. RECEIVE_BOOT_COMPLETED supports only the optional reminder. The Play SDKs
contribute BILLING, INTERNET, ACCESS_NETWORK_STATE, AD_ID, READ_BASIC_PHONE_STATE
and WAKE_LOCK via Google Billing/advertising dependencies. The billing client is
lazy and is not initialized while its verification key is unconfigured.
Owner excludes billing, advertising and UMP dependencies. External feedback/file providers may use networking
under their own permissions after the user chooses them.
Player transport uses Android's media-key dispatch without reading track metadata.
All application services except the system-bound tile are non-exported.

## Known boundaries

Global mode is deprecated; the Pixel/device/player compatibility test is a release
gate. It cannot honestly guarantee media-only processing on every vendor. Calls,
alarms and ringtones are outside supported scope. Device resets use connection
events; an audio app changing routes without such an event may need manual reconnect.
OEM battery management can stop a foreground service. Widgets can retain stale text
after abrupt process death until refreshed; this must not be treated as active DSP.
There is no acoustic measurement, clinical calibration, universal player discovery,
sound-pressure safety limiter, native local player or root-based routing.

## Product state and optional tools in 0.4.0

`AccessStore` models owner, purchased and earned-pass access independently of
sound settings. Root UI and the service refresh expiry once per second while
running. PassClock compares wall/monotonic time and boot count; wall time carries a pass
across a reboot, with detected rollback ending access. Access checks
also happen at profile operations and overlay actions. Free gain is capped at 15 dB; active Pro extends the ceiling to 35 dB.
SettingsStore owns a requested smaller ceiling separately from its effective
entitlement-limited ceiling. Expiry persists any gain reduction. Verified cached
purchases load before startup clamping, and renewal never restores louder gain.
Settings updates refresh access; a synchronous entitlement callback enforces
reductions. The service also refreshes access and caps each ramp step.

`ProfileStore` stores up to 24 names, gains and five EQ anchors. Import parses and
validates the entire bounded JSON before one preference write. It does not apply
audio. Loading passes through the same settings sanitisation and gain ramp.

The service owns `FloatingControls` and closes its Android window on Stop,
permission loss, lock, foreground return or access expiry. An expanded panel scrolls
and remains draggable. Media actions use existing Android media-key dispatch;
there is no notification listener or AccessibilityService.

`SpectrumPanel` owns/relinquishes its Visualizer with the visible Listen screen's
lifecycle; cancellation releases it. It reads FFT into transient memory at 10 Hz,
with 24 relative bands. It does not measure sound pressure or capture PCM/microphone
files. An unavailable effect is reported; animations are not fabricated.

`MainActivity` is an AppCompatActivity for application locales on Android 8–12.
Shared Android resources provide en/es/fr and are applied to service/widget/overlay
contexts. Base audio processing survives locale activity recreation when enabled.
Background-off stops a session when the activity actually leaves, not on a
configuration change. Booster memory off resets extra gain before a new session.

Sleep fade reduces the added dB gain over the last 30 seconds, retains chosen
preferences and never commands the external player's volume. Once a fade begins,
expiry/setting changes do not increase its multiplier. A deliberately replaced or
cancelled timer may restore chosen gain through the normal ramp.

Billing's `play` source set owns only public receipts and checkout. `owner` supplies
a no-op purchase adapter and owner test UI. Details and production security limits
are in [Features and monetisation](FEATURES-AND-MONETISATION.md). No private key,
backend or live product configuration is committed.

## Ads and purchase state in 0.4.0

PlayPurchases maps two non-consumable product IDs to the same verified entitlement.
It checks both before checkout, caches acknowledged signed receipts, acknowledges
outstanding purchases and only revokes an entitlement after a successful query.
One refunded product cannot revoke another still-valid purchase.

PlayAdPasses lives only in the Play source set. UMP gates SDK initialization and
ad requests. Loading and showing are two explicit user actions; no automatic
inventory preloader is used. RewardClaim makes the earned callback idempotent.
AdAudioGate releases service-owned chains synchronously before SDK show. It gates
chain creation and ramps during ads, including route/session callbacks. It hides
the overlay and cancels foreground spectrum collection. Release of the gate
rebuilds/reramps only an existing session. Stop/timer expiry cannot be undone by
an ad callback. Activity destruction during an ad ends the session rather than
resuming processing while SDK playback might continue.

[Background manifest rationale](BACKGROUND-RUNNING.md) explains specialUse,
notification permissions, ordinary battery settings and opt-in reboot reminders.
