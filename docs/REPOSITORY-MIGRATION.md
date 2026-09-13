# Repository migration — 13 September 2026

Jamie requested moving the existing native Android app into
[`Jamie1171/Auralift`](https://github.com/Jamie1171/Auralift). This repository is
the canonical home for Auralift, with the Android project at its root.

## Source and scope

- Version: Auralift **0.5.0**, Owner and Play editions, version code 5.
- Former repository: `Jamie1171/SoloRealm`.
- Former branch: `agent/auralift-android-DO-NOT-MERGE`.
- Former directory: `standalone/auralift/`.
- Source checkpoint: `103dbfa253810d2acc34ad1cd13a68dca6d60bc0`.
- The pre-migration project contains 113 files, including source, tests, Gradle
  wrapper, build configuration, legal drafts, product documents and 23 screenshots.

The move starts fresh Auralift history and does not import SoloRealm history or
application files. The old storage branch remains a historical copy and must
never be merged into SoloRealm or its GitLab/Lovable project.

## Migration changes

Repository instructions, README and the Play plan now identify this repository
as the project home. The existing Android workflow moves into the active root
location and preserves available reports even after a failed verification step.
No app source, dependencies, permissions, package IDs, version numbers, audio
logic, feature entitlements or runtime assets change as part of this move.

Signing keys, private signing backups, local machine settings, SDKs, build caches
and generated APK/AAB files are not repository source. The Owner installation
continues to require its existing private test key for compatible APK updates;
CI debug keys are temporary. Public-release signing is still unconfigured.

## Validation boundaries

The recorded 0.5.0 checks remain in [VALIDATION.md](VALIDATION.md): 148 automated
test executions across Owner/Play and simulated API 26/35. These records describe
the existing build, not a new set of physical-device tests. Migration verification
compares file identities, executable wrapper permissions and repository structure.
New workflow results are available under this repository's Actions tab.

Firebase Test Lab access and cloud-device runs are not configured by this move.
The documented 16 KB native-library runtime check, wider device testing, live
purchase/ad tests, final policies and Play publishing steps remain open.
