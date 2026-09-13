# Validation record

12 September 2026. Personal preview 0.1.0. Native Kotlin / Jetpack Compose.

## Completed

| Verification | Result |
| --- | --- |
| Clean Android compilation and APK packaging | Passed |
| JVM gain conversion, invalid-input limits and EQ interpolation | 5 tests passed |
| Settings persistence, profile round-trip and range clamping | 3 tests passed |
| Native interface controls, real settings updates and first-enable flow | 2 tests passed |
| Android lint | Passed: 0 errors, 2 non-blocking advisories |
| Native screenshots | Listen, Sound and Settings rendered and inspected |
| APK signature | Android debug certificate; APK Signature Scheme v2 verified |
| APK ZIP/native-library alignment | 16 KiB ZIP alignment check passed |
| Final manifest permissions | No internet, microphone, media/storage read, notification-reading or accessibility-service permission |
| APK temporary files / duplicate classes | None; 6 DEX files, 25,807 unique class definitions |
| Credential pattern scan | No GitHub/GitLab token or private-key patterns in source |

Command: `./gradlew :app:clean :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
The build ran using the same pinned Gradle distribution directly in the execution
workspace, with local-only proxy and trust configuration. That configuration is
not part of the app or committed source. The checked-in wrapper uses the official
Gradle distribution SHA-256. No test baseline was used to hide failures.

The test runtime was Robolectric API 35, with native Android graphics and a
412 × 915 dp / 2× density configuration. Screenshots render the actual Android
view tree to a Bitmap; no browser rendering, web mockup or generated image is used.
This is a simulated framework, not physical Pixel evidence.

One lint suppression is narrowly applied to the Quick Settings legacy
`startActivityAndCollapse(Intent)` call: it executes only below API 34, where the
replacement PendingIntent overload does not exist. API 34+ uses PendingIntent.
The two remaining advisory messages reported a newer lifecycle dependency and an
empty obsolete resource folder left by a file move. Dependencies remain pinned;
the empty folder is not included in Git. These are not runtime permission failures.

Earlier unsuccessful attempts were resolved: initial dependency access needed
workspace-specific network configuration; an incremental packaging attempt observed
a transient workspace sync path; a clean build succeeded. The first screenshot
method waited for a simulator frame callback; direct native view rendering fixed it.
Lint's legacy API and receiver-registration findings were corrected and rechecked.

## Delivered APK

- Filename: `Auralift-0.1.0-preview.apk`
- Size: 22,749,486 bytes
- Application ID: `com.jamiewardle.auralift`
- Minimum API: 26; compile/target API: 36
- SHA-256: `3a71b553d3d3966c2355bd1b8744d0d5b42f5a980efc3b7751929a7fd916b7ee`
- Signing certificate SHA-256: `351d23947b918de4564e87f2c9f8f25fa54a48ab5afa431e0cf0f54b1b8577b7`

The APK was saved as a separate downloadable artifact. Generated APKs and signing
keys are not committed to the source repository. This debug certificate is not a
production signing identity.

## Still unverified

No physical Pixel 9a or other Android phone was connected. Actual audible gain,
per-player effect routing, call/alarm interactions, headphones/Bluetooth,
foreground-service behavior on Android 17, TalkBack, largest fonts and long-session
battery behavior need the [physical acceptance test](PIXEL-TEST-PLAN.md).

The service's native AudioEffect behavior cannot be established from these JVM/UI
tests. A successful effect attachment still does not prove a player uses that
processing path. This is a personal preview, not a store-ready release.

Native screenshots: [Listen](screenshots/listen.png), [Sound](screenshots/sound.png),
[Settings](screenshots/settings.png).
