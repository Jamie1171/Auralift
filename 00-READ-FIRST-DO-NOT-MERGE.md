# Auralift is a separate Android product — DO NOT MERGE INTO SOLOREALM

The canonical repository is **[Jamie1171/Auralift](https://github.com/Jamie1171/Auralift)**.
Jamie authorised moving this project here on **13 September 2026**. All Auralift
source, tests, build configuration and documentation now belong at this repository's
root. Use this repository for future work, builds and Auralift pull requests.

**Do not merge or cherry-pick Auralift into SoloRealm, push it to SoloRealm's
GitLab repository, or connect it to Lovable.** It is a separate Android product.
Do not run SoloRealm's intake workflow on it. Auralift branches and pull requests
within this repository are permitted.

The old storage location was `Jamie1171/SoloRealm`, branch
`agent/auralift-android-DO-NOT-MERGE`, under `standalone/auralift/`. Treat that copy
as historical. The migration imports only Auralift's files with fresh repository
history. See [the migration record](docs/REPOSITORY-MIGRATION.md).

## First read

1. `README.md` — build/install and current delivery status.
2. `docs/RESEARCH-AND-PRODUCT.md` — sourced comparison, scope, and platform limits.
3. `docs/DESIGN.md` — native interface and interaction decisions.
4. `docs/ARCHITECTURE.md` — audio ownership, service lifecycle and permissions.
5. `docs/PIXEL-TEST-PLAN.md` — on-device acceptance before any public release.
6. `docs/VALIDATION.md` — tests actually run, without assuming hardware results.
7. `docs/ANDROID-COMPATIBILITY.md` — Android requirements and wider device coverage.
8. `docs/GOOGLE-PLAY-PLAN.md` — publishing stages, current requirements and monetisation choices.

9. `docs/FEATURES-AND-MONETISATION.md` — 0.5.0 feature tiers, Owner testing, Ad Pass and both purchase products.
10. `docs/BACKGROUND-RUNNING.md` — manifest rationale and reboot reminders.
11. `docs/CLOSED-TESTING-BRIEF.md` — tester recruitment and structured feedback plan.

For future app opportunities, also read `docs/FUTURE-APP-STRATEGY.md`: Jamie's
AuraForge Labs goals, agreed niche-research process and current decisions. This
portfolio brief does not expand Auralift's implementation scope.

## Build automation and credentials

`.github/workflows/android.yml` is now at the repository root and defines the
Android test/build workflow. It runs on pushes to `main`, Auralift pull requests,
and manual dispatch. These are simulated Android checks, not cloud-device tests.
The additional device-test workflow builds a Firebase instrumentation kit and runs
Android emulators. See `docs/FIREBASE-AUTOMATION.md`. Jamie controls physical Test
Lab submissions through his Firebase console; cloud credentials are not in Git.

Keep signing keys, account credentials, Android SDKs and build caches outside
Git. GitHub's Owner debug builds use temporary CI signing keys; the private Owner
key used for Jamie's installed APK remains separate and is needed for compatible
Owner updates. No production signing is configured.

The app name and package identity are provisional. Decide final naming and
release signing before a store launch. This preview is not a production release.
