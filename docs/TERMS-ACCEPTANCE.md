# Terms acceptance and competitor review — 18 September 2026

## Implementation in 0.5.4 / version code 9

Before access to the main interface, both editions show a native, scrollable
listening-safety and terms screen. Terms and Privacy open offline before agreement
and remain available in Settings. Both can be selected/copied and saved using
Android's document picker; no storage permission or external destination is chosen
for the user. The English full documents are labelled as such; the summary and
actions have English, Spanish and French resources.

Agree and continue writes a local record and reveals the controls. It does not
start boost, raise gain, purchase anything or consent to advertising. Not now and
Back close the activity without acceptance. The old first-enable warning remains
at the point where audio starts. Existing users' `onboarded` preference does not
satisfy terms acceptance. MainActivity and BoostService reject unaccepted startup,
including old floating-control preferences and direct service commands. Stop stays
available through existing notification/widget controls.

`TermsStore` writes an atomic JSON record in private no-backup storage: agreement
version, exact terms SHA-256, app version/code and device-reported time. No user ID,
name, email, network request or remote logging is added. Failed/corrupt/missing
records do not grant access. A repeat acceptance is idempotent. A new acceptance
replaces the old local receipt; app-data deletion/uninstall removes it.

This is evidence of a local action, not a verified identity, a trusted timestamp,
proof of understanding or tamper-proof legal evidence. The developer cannot query
it remotely. The privacy notice describes the actual record and document export.

## Maintaining terms

- Archive exact accepted text under `docs/legal/`; the initial document is
  `terms-2026-09-18.1.txt` and must match the shipped asset byte-for-byte.
- Increase `TermsStore.CURRENT_VERSION` and the terms' agreement version for material
  changes requiring fresh agreement. Add a new archive; never overwrite an older
  accepted document. Explain specific material changes prominently in the next
  release's acceptance summary before requesting renewed agreement.
- An app version bump alone does not require acceptance. Editorial-only changes may
  retain the agreement version: archive their distinct text/digest too. The receipt
  continues to identify the exact earlier text accepted. Do not silently treat a
  material rewrite as an editorial correction.
- The first rollout requires explicit acceptance from existing preview users. It
  preserves saved settings and entitlements. Future changes must respect paid and
  statutory rights rather than coercing consumers into losing purchased rights.
- These remain accurately labelled pre-release documents. The separate proposed
  public policies still need confirmed operator/business particulars and legal
  review. This feature does not publish a website or finish Play policy declarations.

## Review of the competitor text supplied by Jamie

This assesses the pasted 360 Tool terms dated 25 September 2025, not a verified
current contract or a download-count claim. Large installation numbers establish
neither legal review nor enforceability. No competitor prose has been copied.

| Topic | Auralift decision |
|---|---|
| Explain functionality and warn about volume | Keep and strengthen: permanent hearing damage/tinnitus, equipment risks, gradual adjustments, breaks, manufacturer guidance, and no safe-level guarantee from gain/comfort/distortion. |
| Private-device audio processing | Explain our actual audio engine separately from Google billing/ads. Do not promise that no information ever reaches a third party. |
| Privacy notice | Keep separate and accessible; agreement to terms is not advertising or general data-processing consent. |
| Licence, IP and lawful use | Already covered, with statutory/open-source rights preserved. Do not copy an absolute reverse-engineering ban. |
| Platform and third-party services | Already covered; clarify their separate terms without purporting to excuse our own legal obligations. |
| Subscription prices, trials and cancellation | Inapplicable. Auralift has two equivalent one-time products and optional one-hour Ad Passes. Retain accurate restoration and refund terms. |
| Termination/discontinuation | Added clear effects of uninstalling and a duty to address paid access/remedies if the product ends. Do not reserve arbitrary cancellation of paid rights. |
| Changes effective merely on posting | Do not copy. Explain material changes before effect, seek renewed agreement where appropriate and preserve accrued rights. |
| Whole injury release, broad warranty exclusions and USD 100 cap | Do not copy. They are no substitute for fair terms and do not remove mandatory UK rights or negligence liability for death/personal injury. |
| Eligibility age 13 | Do not import an arbitrary competitor threshold. Resolve target audience, presentation and any age-related requirements for Auralift before publication. |
| Criminal-history ban | Unrelated to this audio utility; do not add it or collect criminal-history information. |
| User accounts, member content and account termination | Inapplicable to Auralift's current account-free utility; evidence that the competitor text contains generic platform clauses. |
| Assignment and entire-agreement boilerplate | Do not copy broad rights to transfer obligations or erase promises; these can affect consumer rights. Add only if a concrete future business need is reviewed. |
| Severability and support | Already covered; updated the app to the confirmed auralift.support@gmail.com contact. |

The main worthwhile additions are clarity and accurate coverage, not more sweeping
exclusions. A solicitor should review the final public policies and presentation.

## Sources

- [CMA: writing a fair contract](https://www.gov.uk/guidance/writing-a-fair-contract-for-customers): transparency, pre-contract access, unfair limitations and changes.
- [ICO: data minimisation](https://ico.org.uk/for-organisations/uk-gdpr-guidance-and-resources/data-protection-principles/a-guide-to-the-data-protection-principles/data-minimisation/): collect no more information than the defined purpose needs.
- [WHO: safe listening](https://www.who.int/news-room/questions-and-answers/item/deafness-and-hearing-loss-safe-listening): permanent hearing damage can develop with repeated exposure without obvious warning signs.

## Validation scope

New regression cases cover old warning flags, direct-service bypass attempts,
record persistence/corruption/write failure, material version changes, document
access before acceptance, decline, rotation/recreation and no audio/gain change
on acceptance. Existing audio/device tests explicitly establish terms acceptance
in their fixtures. The optimized APK smoke driver goes through the real new screen.
Results are recorded in `docs/VALIDATION.md`; simulated tests are not legal approval
or a physical acoustic-safety certification.
