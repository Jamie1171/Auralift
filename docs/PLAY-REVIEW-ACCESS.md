# Google Play reviewer access — 0.5.4

On first launch, reviewers can read/save the Terms of Use and Privacy Policy,
then choose **Agree and continue**. This local agreement step does not require
sign-in and does not start audio processing. It is the same flow every user sees.

The public Play build has an explicit **Settings → Auralift Pro → Review access**
entry. Paste the privately supplied reusable code and select **Unlock Pro for review**.
No account, network, purchase, ad, time limit or country restriction is involved.
All normal Pro features use the same entitlement checks as paid/earned access.
Android overlay/notification/spectrum permission prompts remain user controlled.

The code is not committed to this public repository. Only its SHA-256 digest is
in Play Distribution. Owner has no review entry or verifier; Owner simulations
remain isolated. The review grant is separate from purchases and earned passes,
persists across app/process/device restarts, and never starts boost or raises gain.
**End review access** restores the actual purchase/pass/free entitlement; without
other Pro access, gain above 15 dB is immediately clamped. Clearing app data or
reinstalling requires re-entering the same code. Purchase refresh cannot revoke it.

This is an explicitly documented complimentary Pro grant, not reviewer detection
or a different app experience. It makes no claim of tamper-proof DRM: a shared
code can be reused by others, and a modified client can bypass local validation.
Keep the code in private Play Console app-access instructions. Do not put it in
GitHub issues, PR descriptions, screenshots or test fixtures. Retain the digest
for subsequent releases; rotating it requires updating Play instructions and
re-entering the replacement code.

## Console fields

- Restricted: Yes.
- Name: Auralift – access to Pro features.
- Username/password: blank.
- Other information: paste the separately supplied instructions with the real code.
- Confirm full access only when submitting a build containing this tested feature.

Source: [Google reviewer access requirements](https://support.google.com/googleplay/android-developer/answer/15748846?hl=en), checked 18 September 2026. Google retains the review decision.
