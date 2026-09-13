# Background sessions and manifest — 0.4.0

Reviewed 13 September 2026 against the linked Android documentation. This is the
rationale for the actual code, not a guarantee that Google will approve a declaration
or that an OEM will never terminate a service.

## The current manifest

```xml
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<service android:name=".audio.BoostService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="User-started audio equalizer and gain control for media apps. Maintains Android AudioEffect objects and optional floating controls during a listening session. Persistent notification offers Stop. No silent playback or background recording." />
</service>
```

See the full [AndroidManifest.xml](../app/src/main/AndroidManifest.xml), which also
contains optional overlay, haptics and Visualizer permissions. The Play source set
adds the AdMob metadata and SDK-transitive permissions; inspect the merged release
manifest when completing store declarations.

## Why not mediaPlayback?

Android 14+ requires an **appropriate** foreground-service type and its permission,
not `mediaPlayback` for every service that touches sound. Google's mediaPlayback
category describes continued audio/video playback and DVR. Auralift instead owns
audio effects used by another player's playback. It neither plays files nor emits
silent audio. `specialUse`, with its explicit explanation and Play review, is the
current engineering choice for that operation. `startForeground` uses the matching
SPECIAL_USE type on API 34+ and the older overload before API 34.

This is an inference from the documented categories and actual app behaviour;
Google decides whether the justification is accepted. Do not add a fake media
session or silent track merely to claim the playback category. Target-35+ apps also
cannot start a mediaPlayback foreground service from BOOT_COMPLETED, so the copied
“mediaPlayback plus automatic boot” recipe is not generally valid.
[Foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types).

## What keeps the session running

The user starts a foreground service while interacting with the app. It retains
AudioEffect objects when the activity leaves or the screen locks. A low-importance
notification has an explicit Stop action, no notification sound and no vibration.
The app does not steal audio focus. Run in background is on by default; turning it
off ends the session when the activity leaves (configuration recreation and a
requested ad pause are handled separately). No always-on microphone or wake lock
is added by the audio engine.

Android/OEM termination and Force stop still end the session. START_NOT_STICKY is
intentional: a killed or deliberately stopped process must not surprise the user
by reapplying saved high gain. No notification text proves actual acoustic output.

## Notifications are user-controlled

POST_NOTIFICATIONS is requested at a relevant user action on Android 13+, and can
also be enabled or reviewed in Settings. An FGS must supply its notification even
when the user denies drawer notifications. Android then exposes the active task
through its foreground-services Task Manager. Denial is not a reason to crash or
pretend the drawer notification is visible. On Android 14, users can dismiss many
ongoing notifications while unlocked; `setOngoing(true)` is not a universal
non-dismissible guarantee.
[Notification permission](https://developer.android.com/develop/ui/views/notifications/notification-permission),
[Android 14 behaviour](https://developer.android.com/about/versions/14/behavior-changes-all#non-dismissable-notifications).

## Battery settings

The settings page reports `isIgnoringBatteryOptimizations`, opens the app details
page for OEM-specific battery controls, and can open the system optimization list.
Neither route needs REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. That permission is for
the **direct exemption request**, not ordinary settings navigation. Google limits
direct exemption requests to acceptable core-function use cases; putting a button
in settings does not by itself satisfy the policy. We therefore do not request
that permission or present a first-launch exemption popup. Doze can defer work;
it is not accurately described as indiscriminately killing all background apps.
[Doze guidance](https://developer.android.com/training/monitoring-device-state/doze-standby),
[Settings intents](https://developer.android.com/reference/android/provider/Settings#ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).

## Reboot behaviour

**Remind me after restarting** is opt-in and free. The protected BOOT_COMPLETED
broadcast can post a normal low-importance reminder if notifications are allowed.
Tapping it opens Auralift; the user then enables a listening session. The receiver
never creates effects, starts an FGS, acquires audio focus or changes media volume.
Force-stopped apps may not receive the broadcast until reopened. A reminder is
not represented as automatic audio recovery.

## Before release

Record actual lock-screen/background results on Pixel, Samsung and another OEM,
on the oldest supported Android and current Android. Test notification denial,
channel blocking/dismissal, battery restriction, route switches, Force stop,
reboot reminder off/on and ad interruption. Submit Play's foreground-service
justification/video showing start, background use and Stop. Simulated service tests
and manifest checks cannot establish OEM survival or store approval.
