# Auralift

A native Android volume booster, written in **Kotlin, Jetpack Compose and Android
audio APIs**. Version **0.5.2** adds a compact circular floating player, independent player/boost
controls, selected EQ outlines and ten full light/dark palettes. Version 0.5.1 put the one-hour Ad Pass directly below the Listen
screen's Enable/Stop button, with an active-pass countdown and help for Android's
restricted overlay settings. It retains the Floating Player Pro section, complete
interface themes and 5 dB gain shortcuts: Free up to +15 dB, Pro up to +35 dB.
Voluntary one-hour Ad Passes and equivalent £3.99 / £5.99 one-time products remain.
Owner remains fully unlocked and contains no advertising or billing SDK.

**Project home: [Jamie1171/Auralift](https://github.com/Jamie1171/Auralift).**
Open or clone this repository directly; the Android project is at its root.
Read [the project instructions](00-READ-FIRST-DO-NOT-MERGE.md) first. Auralift is
independent of SoloRealm and must never be merged into its GitLab/Lovable project.

## Features

| Free in the public edition | Pro; always included in Owner |
| --- | --- |
| Up to +15 dB gain, separate media volume, comparison and Stop | Up to +35 dB gain; floating player, media-volume and EQ-preset controls |
| Balanced, Voice, Warm, Detail and custom five-anchor EQ | Nine additional colour palettes, each with light/dark appearance |
| Three manually saved speaker/wired/Bluetooth setups | Up to 24 named sound profiles; JSON import and export |
| Sleep timer, notification, widget and Quick Settings tile | Gradual reduction of added gain during the final 30 seconds of a sleep timer |
| Light/dark Mint appearance; English, Spanish, French and follow-system language | One-hour earned Ad Pass in the public edition |
| Booster memory, haptics, optional real spectrum, background settings, FAQ and manual feedback | Owner controls to simulate Free, a reward, expiry and purchased Pro |

Native, offline-readable privacy and terms drafts describe this build. The Play
edition includes opt-in Google rewarded ads and their disclosed SDK data handling.
There is no developer account system, backend or microphone amplifier. The app
interface is native; the ad SDK can render its own web-based ad content.
The optional spectrum uses Android Visualizer; Android requires its audio
permission even though Auralift does not open or record the microphone. All
visualizer data stays in memory while the Listen screen is visible.

The floating player controls the current external player using media-key commands.
It is not a local music library and does not read track metadata or notifications.
Some players ignore these commands. Enable it in Settings after granting overlay permission; it appears while Auralift
is in the background and the screen is unlocked, with boost on or off. Stop in
the player releases effects and resets gain/EQ to 0 dB/Balanced without closing it.
Minimise returns to the 64 dp circle; × or the Settings switch closes the player.
The persistent notification offers Stop boost when running or Close when idle.

## Install the Owner edition

1. Install **Auralift-0.5.0-owner.apk**. Android may ask to allow this download source.
2. It updates **Auralift Owner 0.3.0 / 0.4.0** using the same test certificate, preserving
   settings, with legacy smaller gain ceilings rounded down to the new 5 dB steps.
   Upgrading never increases saved gain. It remains separate from the old public-package preview. Stop any
   other booster before testing.
3. Start familiar media, open Auralift Owner, and enable boost. Increase gradually
   from zero; use Compare original / Restore boost to judge your setup.
4. Explore Settings → Appearance, Audio controls, Permissions & background,
   Language, Help & feedback, Privacy policy and Terms of use.
5. For the floating player: open **Settings → Floating player**, enable it and
   follow the one-permission guide. On Android 11+, select **Auralift Owner** in
   the app list and allow it once; the list is not a list of target media apps.
   Leave Auralift; boost does not need to be enabled. Tap the circle to expand,
   or drag it/the expanded gain label to move it. Minimise returns to the circle.
   Closing the player does not stop your audio or any active boost.
6. Tap **Pro** for Owner test controls. Simulate Free, a completed ad, expiry,
   purchased Pro, or restore all features. Restarting the app process restores Owner
   access; simply leaving and reopening may reuse the same process.

No purchase is required in Owner. Its package is
`com.jamiewardle.auralift.owner`; the public package remains
`com.jamiewardle.auralift`. Do not distribute the Owner edition as the paid product.
The owner APK is debug-signed for personal testing. Its signing key is held outside
Git; keep the same key for future Owner updates. Production signing is separate.

## Android compatibility

Designed for Android **8.0/API 26 and newer**, with compile/target API 36 and no
Pixel/manufacturer restriction. The owner reported 0.2.0 working on a Pixel 9a.
The owner has also supplied feedback/screenshots from 0.4.0. Neither establishes
physical results for the new 0.5.0 gain range or another phone.
See [validation](docs/VALIDATION.md) and the [device matrix](docs/ANDROID-COMPATIBILITY.md).

An enabled Android effect does not prove audible amplification. Global-mix
attachment is deprecated and some phones, players and outputs bypass it.
Cooperative player-session mode is an alternative, not universal app detection.
Calls, alarms and ringtones are outside supported scope. The 35 dB Pro setting is
requested digital gain, not measured sound pressure; EQ fallback is capped by
the device's reported band limit. Stop or reduce gain if audio becomes distorted
or uncomfortable. No clinical hearing calibration or acoustic safety limiter is
claimed.

## Build and verification

Use JDK 17, Android SDK platform 36 and build-tools 35.0.0. Open this directory in
Android Studio or set `ANDROID_HOME`/a local `sdk.dir` and run:

```sh
./gradlew :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest \
  :app:lintOwnerDebug :app:lintPlayRelease \
  :app:assembleOwnerDebug :app:assemblePlayRelease :app:bundlePlayRelease
```

Run the oldest-OS checks separately with
`./gradlew -Pauralift.testSdk=26 :app:testOwnerDebugUnitTest :app:testPlayDebugUnitTest`.
The default test SDK is 35; separating SDKs avoids a Robolectric native-font
ZIP cache conflict. No test failures are ignored.

The wrapper pins Gradle 8.13 and its distribution checksum. Outputs:

- Owner APK: `app/build/outputs/apk/owner/debug/app-owner-debug.apk`
- Unsigned optimized public APK: `app/build/outputs/apk/play/release/app-play-release-unsigned.apk`
- Unsigned public AAB: `app/build/outputs/bundle/playRelease/app-play-release.aab`
- Native UI captures: `app/build/screenshots/`

Robolectric logic tests use API 26/35; native screenshot tests use API 35. These do
not establish physical audio output, OEM background reliability or Play checkout.
The [Android workflow](.github/workflows/android.yml) runs from this repository on
pushes to `main`, pull requests and manual dispatch. Download its APKs, unsigned
AAB and test reports from the run's artifacts. Owner APKs built by GitHub use
temporary CI debug keys and cannot update Jamie's privately signed Owner build.
The [device-test workflow](.github/workflows/device-tests.yml) builds a matching
public debug app/test APK pair for Firebase and runs targeted Android emulator
checks. See [Firebase upload instructions](docs/FIREBASE-AUTOMATION.md) and
[human tester goals](docs/HUMAN-TEST-GOALS.md). Jamie submits Firebase jobs through
his console; no Firebase service-account credentials are stored in this repository.

## Public edition and launch

The two one-time products use Google Play Billing 9.1.0 and unlock identical Pro
access. Ad Pass uses GMA Next-Gen 1.4.0 / UMP 4.0.0 and grants one hour only after
a completed reward callback. No automatic startup ads, banners or subscriptions.
Release purchases and ads stay unavailable until their account configuration is
supplied. Play debug builds use Google's test ads. Owner uses local simulations.

No install-time trial is granted. An earned pass survives normal process recreation
and reboot, but clearing its data loses it; reinstall alone earns nothing. Expiry
reduces gain above +15 dB to +15 dB, preserves lower gain/EQ and profiles, and
locks Pro features. Re-unlocking never raises gain automatically. Core controls
remain available. An opt-in reboot notification opens the app, never a
background service. See [background decisions](docs/BACKGROUND-RUNNING.md).

The public AAB is unsigned and not submitted to Play. Production still requires
account configuration, signing, hosted policies/contact, actual purchase/ad tests,
a wider hardware matrix and resolution of the documented 16 KB native-library
check. Do not advertise universal audio compatibility or guaranteed Play approval.

Read [Features and monetisation](docs/FEATURES-AND-MONETISATION.md) for both product
IDs, ad configuration and security limits, and the [closed-test brief](docs/CLOSED-TESTING-BRIEF.md)
for the tester-exchange plan.

## Project documents

- [Repository migration and provenance](docs/REPOSITORY-MIGRATION.md)
- [Validation and artifact identity](docs/VALIDATION.md)
- [Features and monetisation](docs/FEATURES-AND-MONETISATION.md)
- [Android compatibility](docs/ANDROID-COMPATIBILITY.md)
- [Google Play launch plan](docs/GOOGLE-PLAY-PLAN.md)
- [Architecture](docs/ARCHITECTURE.md) and [interface design](docs/DESIGN.md)
- [Original competitor research](docs/RESEARCH-AND-PRODUCT.md)
- [Physical listening test checklist](docs/PIXEL-TEST-PLAN.md)

Screenshots: [Listen](docs/screenshots/listen.png) · [Settings](docs/screenshots/settings.png)
· [Floating Player](docs/screenshots/floating-player.png) · [Themes](docs/screenshots/themes.png) · [Owner tools](docs/screenshots/pro-owner.png).

Application code, artwork and policy drafts were created for this project.
Competitor branding, code and artwork are not included. No open-source licence
has been chosen for the original work. Third-party components retain their own licences.
