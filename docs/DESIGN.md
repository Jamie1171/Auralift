# Native interface design

The product is a compact audio utility built in Kotlin and Jetpack Compose.
All screens are native. No HTML/CSS, WebView or browser-based UI is used.

## 0.5.2 floating-player feedback

The collapsed player is a 64 dp circular wave button. The expanded panel has
separate Minimise and Close actions, an explicit boost-on/off status and a
Start/Stop button. Stop releases all effects, cancels comparison/timer state and
resets gain and EQ to 0 dB/Balanced; it leaves the player available. Close disables
the Settings preference and leaves any active boost alone. Settings can start the
player without starting boost. Tapping a selected sound preset returns to Balanced;
a different preset replaces it. A 2 dp accent outline and selected accessibility
state persist on the chosen preset.

Ten palettes now each have light/dark versions: Mint, Ocean, Amber, Orchid, Rose,
Coral, Forest, Sunshine, Midnight and Slate. Mint remains included in Free.

## 0.5.1 feedback update

The Listen screen now places a distinct Ad Pass panel immediately below Enable
boost / Turn boost off. It uses the same rewarded-ad state and actions as Pro:
Prepare is explicit, then Watch needs a separate tap; only the SDK reward callback
grants access. The panel shows remaining time for an earned pass and disappears
for permanent Pro/Owner access. It does not start boost or change gain on unlock.

Floating Player includes expandable help for Android's "App was denied access"
message. Returning without the display permission opens that help. Open App info
targets only the installed Auralift package; Android owns the restricted-settings
decision. Opening App info does not enable the floating player. The instructions
name the installed app, include a fallback when the menu is missing, and keep the
main/notification controls available. No permission, hidden API or override is added.
[Android's documented recovery route](https://support.google.com/android/answer/12623953?hl=en).

## Visual language

| Theme | Dark background / panels | Character |
| --- | --- | --- |
| Mint (free) | Forest charcoal / green-grey | Clean single-ring dial, 26 dp panels |
| Ocean (Pro) | Deep navy / blue | Double-ring dial, cyan details, 18 dp panels |
| Amber (Pro) | Espresso / warm brown | Brass scale marks, heavier gain type, 10 dp panels |
| Orchid (Pro) | Deep plum / violet | Rose glow, soft filled dial, 32 dp panels |

Each includes a complete light palette. Main screens, navigation, buttons, sliders,
previews and native floating windows share its colours. Status remains expressed
in words. Static decoration is not presented as a live audio meter. All artwork
is original vector/Compose Canvas code; no fonts or image downloads are needed.

## Navigation and behavior

| Screen | Main job | Controls |
| --- | --- | --- |
| Listen | Change loudness and compare it with normal audio | Gain display, native slider, ±0.5 dB buttons, 5 dB shortcuts through +15 dB Free / +35 dB Pro, Compare original/Restore, Enable/Stop, Android readback, Android media volume, player transport, timer |
| Sound | Shape frequencies and recall a setup | Four starting presets, native frequency sliders, curve illustration, reset, three explicitly loaded setups |
| Settings | Find and configure audio/product controls | Featured Floating Player Pro card and dedicated page; Appearance, Audio controls, Permissions & background, Language, Pro, Help & feedback, Privacy and Terms |

The circle is an exact visual representation of the selected gain within the
chosen range. It is not a claimed microphone level meter. Interaction uses
standard sliders and buttons instead of requiring a precise rotary gesture.
No audio-reactive animation is faked. Gains increase gradually in the engine;
reductions and Stop act immediately.

The screen distinguishes OFF, WAITING and CONNECTED. “Connected” means the API
accepted and enabled an effect. The gain readback shows Android’s actual target, or the highest EQ band for
the fallback. Compare original sends zero boost and a flat EQ while keeping
the chosen settings; Restore boost ramps back to them. The adjacent explanation
asks for a listening comparison; the UI does not assert universal processing success.

System media volume never jumps to maximum automatically. Selecting a preset or
moving a slider while stopped saves a preference without enabling audio effects.
Loading a saved setup is explicit, and gain remains within the current range.

## Accessibility and interaction requirements

Native Compose sliders, buttons and switches provide semantics. Icon-only actions
have descriptions. Touch targets are at least 48 dp; the main Enable/Stop control
is at least 58 dp high. Screens scroll vertically and avoid fixed total heights.
Frequency sliders are horizontal for precision and readable labels. Text and
status are not conveyed through color alone. TalkBack and large font sizes must
also be tested on a physical device before public release.

The one-time first-enable dialog explains digital gain and high-volume risks.
There is no account flow or automatic opening promotion. Pro is a user-opened
page with a clear feature comparison and Ad Pass expiry. Locked convenience controls
link to it. Notifications, overlay and spectrum permissions are requested only
when their corresponding feature is enabled.

## Preview evidence

The native interface test renders screenshots through Android/Robolectric graphics
at a 412 × 915 dp configuration. See `docs/VALIDATION.md` for whether rendering was
successful and where screenshots were saved. A screenshot is evidence of layout,
not audio performance on a Pixel 9a.

## 0.3.0 additions

Settings uses grouped rows leading to scrolling native screens. Each optional
permission includes its purpose and current state. FAQ answers expand inline.
Feedback defaults to excluding diagnostics and attachments and opens the user's
chosen sharing app. Legal text is a native text view, not a WebView.

Version 0.5.0 expands the original colour variants into full interface themes.
No competitor skins or assets are used. No generated bitmap assets are needed. The Pro page states free and
paid capabilities before its voluntary Ad Pass controls; no charge starts at expiry.
The Owner-only test panel is clearly separate from public product controls.

The floating player is a compact native Android window. Its header can be tapped
to expand or dragged to move. Stop is not entitlement-gated; closing the bubble
only disables the overlay. The system keyboard does not need focus in the overlay.
The panel hides on the lock screen and when the main app is visible.

## 0.4.0 purchase and background screens

Pro has separate cards for current access, Ad Pass, and the two permanent purchase
options. “Supporter Pro” explicitly says it has exactly the same features. The
reward text explains the hour before opting in. A prepared ad needs another tap
to show, so a delayed network response cannot interrupt a different task. UMP
privacy choices remain reachable in Settings when required. No public prices are
invented while Play is unconfigured. Owner has local reward/purchase simulations.

Background settings report notification/battery state and offer ordinary Android
settings links. The optional reboot reminder is labelled as a reminder, never
“always alive” or automatic high-gain restoration. All additions have native
en/es/fr resources and use the existing scalable type, scrolling and touch sizes.

## Floating Player permission flow in 0.5.0

The separate Pro page describes controls, access, overlay permission and when the
bubble appears. Granting permission is separate from enabling boost. The pre-grant
dialog identifies the exact installed app label (Owner or public). Android 11+
opens an app list: select Auralift once. Earlier Android supports the package deep
link. Cancellation never enables the feature; returning with a grant only enables
it if the user requested activation and still has Pro. Protected apps can block
overlays. The page offers manual hide guidance without promising a global override
or adding foreground-app monitoring. Background settings keep their own purpose.
