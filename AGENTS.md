# Auralift working instructions

Read `00-READ-FIRST-DO-NOT-MERGE.md` before edits. The canonical repository is
`Jamie1171/Auralift`; this standalone native Android project lives at its root.
Jamie authorised this move on 13 September 2026. Use this repository for future
Auralift work. Normal Auralift branches and PRs are allowed here; never merge or
sync this product into SoloRealm, GitLab or Lovable.

- Kotlin, Jetpack Compose and Android APIs. No WebView/HTML/CSS wrapper.
- The September 2026 user request authorises optional overlays, native spectrum,
  haptics, language selection, manual feedback, legal drafts and public Pro billing.
  The user also authorises opt-in rewarded ads for one-hour Pro passes and two
  equivalent one-time Pro/Supporter products (September 2026). No forced ads.
  Do not add analytics, accounts, a backend, microphone recording/amplification,
  notification reading, accessibility services, root or hidden APIs without a
  concrete authorised feature. Visualizer buffers must remain in memory only.
- Owner and Play are different packages/source sets. Keep Owner always unlocked
  by default, exclude billing and ad SDKs from Owner, and never ship owner unlock controls in
  the public APK. Never commit signing keys, tokens, SDKs or build caches.
- The latest September 2026 user request sets Free to 0–15 dB and Pro/Owner to
  0–35 dB with 5 dB shortcuts. This supersedes the earlier free 0–30 dB model.
  Expiry must lower any active gain above 15 dB; unlocking must never raise it.
- Preserve separate media volume and gain, honest device/readback
  reporting, no stacked global/player chains and deterministic release on Stop.
- Ad Pass expiry must not raise gain, erase profiles, start/stop the external player
  or remove free Stop/reset controls. A running sleep fade must not jump back up.
- Policies must match actual permissions, SDKs and sharing. No invented support
  address, live listing, paid price, medical claim or purchase verification claim.
- Validate changes with `./gradlew :app:testOwnerDebugUnitTest
  :app:testPlayDebugUnitTest :app:lintOwnerDebug :app:lintPlayRelease
  :app:assembleOwnerDebug :app:assemblePlayRelease :app:bundlePlayRelease`
  as a single shell invocation, then separately run the two test tasks with
  `-Pauralift.testSdk=26` for oldest-OS coverage. Record actual results in docs/VALIDATION.md.
- UI captures are generated in app/build/screenshots. Keep physical-device and
  acoustic evidence separate from simulated tests and static APK checks.
- Use fast-forward updates and preserve other contributors' work. Do not force-push.
- The former SoloRealm storage branch is historical. Do not use it for new app
  work or copy SoloRealm files/history into this repository.
