# Pixel 9a acceptance test

Do not mark any physical-device row passed from compilation, a screenshot or a
simulated Android environment. Record Android version/build, media app/version,
output device and observed result. No access to the user's phone is available in
the build environment.

Begin at moderate Android media volume and zero extra gain. Turn off EZ Booster
and other equalizers. Use familiar speech/music instead of test tones or silence.

| Check | Expected result | Physical result |
| --- | --- | --- |
| Update Owner 0.4.0 → 0.5.0 | Same-signature update; Pro +35 available; old gain never raised; smaller ceiling rounds down | Pending |
| Full range and readback | +35 dB selectable in Owner/Pro; Free stops at +15 dB; record Android target or EQ peak; device limits remain explicit | Pending |
| Floating permission | Settings → Floating player; select only Auralift Owner in Android list; cancel/grant/revoke; bubble hidden on lock/protected screens | Pending |
| Themes | All ten palettes visibly change screens and floating panel; inspect light/dark and large text | Pending |
| Ad Pass expiry | Simulate Free, earn pass, select >15; expire: gain ≤15, media continues, renewing does not increase gain | Pending |
| Compare original / Restore | Boost and EQ return to zero, restore preserves chosen settings and ramps | Pending |
| Install and first launch | Opens without network or login; boost off | Pending |
| First Enable | One-time explanation; notification choice; no volume jump | Pending |
| Gain 0 → +0.5 → +3 dB | Small changes; actual difference verified by on/off comparison | Pending |
| EQ Voice / Balanced | Frequency change when supported; no phantom success if rejected | Pending |
| Pixel speaker | Test familiar local and streaming media; record connection details | Pending |
| YouTube, Spotify, VLC, TikTok/browser | Test separately, including pause/resume and next item | Pending |
| System mode fails/bypassed | Try Player sessions; restart playback in a cooperating player | Pending |
| USB-C headphones / Bluetooth | Repeat low-gain comparison on each available output | Pending |
| Unplug / connect / disconnect | Extra gain resets to zero by default; no loud jump | Pending |
| Screen lock / other apps | Service continues; Stop remains available in notification | Pending |
| Notification denied | App remains usable; on-screen Stop works | Pending |
| Notification, widget, Quick Settings Stop | Effects release; media keeps playing normally | Pending |
| Service killed / reboot | No automatic high-gain restart | Pending |
| Sleep timer | Turns off effects around deadline; permits Android idle delay | Pending |
| Cancel / replace timer | Old deadline cannot stop a newly configured timer | Pending |
| Device volume keys | Android media slider follows real media volume | Pending |
| Saved setup | Explicit load restores EQ and gain within the chosen range | Pending |
| TalkBack / largest font / landscape | All actions reachable; no essential clipped text | Pending |
| Incoming call / notification / alarm | Observe at low gain; these are not supported boost targets | Pending |
| 30–60 min use, pause and output switching | No crash, stuck gain, runaway battery use or escalating level | Pending |

For an issue, record the connection-details text and the player/output used.
Do not send private audio or notification content. “Connected but no audible
difference” is a valid failure case and should remain in the compatibility matrix.

## Initial user feedback

The user reports no clear audible difference with preview 0.1.0 on the Pixel 9a.
The selected gain, output and media player have not been established. This is
not a physical compatibility pass. Version 0.2.0 expands the application limit
and adds native parameter readback and a comparison control to distinguish a
range issue from an effect-routing issue. See the later owner report below for the first positive result with 0.2.0.

## Follow-up owner report — 13 September 2026

After receiving preview 0.2.0, the owner reports that it works great on the
Pixel 9a. Record this as a positive general listening result. The exact Android
build, media player, gain, output and completion of the individual checklist
items have not been established, so the detailed rows remain pending. Use the
[Android compatibility matrix](ANDROID-COMPATIBILITY.md) for wider testing.
