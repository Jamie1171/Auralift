# Support and languages — 0.5.5

- Feedback & support creates a draft for `auraforgelabssupport+auralift@gmail.com` using an
  email application. Nothing is sent automatically; Auralift cannot confirm
  delivery by another app. The copy-address action remains available if no email
  handler is installed. Categories and instructions are localized in EN/ES/FR.
- Screenshot access and diagnostics are opt-in. The URI receives read permission
  only. Errors opening the draft leave the form intact. No credentials or email
  sending service are embedded in the app.
- Existing English, Spanish and French interface resource sets are complete.
  Android/AppCompat retains the chosen app language; Follow system delegates to
  the device. Region variants use their base language; unsupported legal locales
  use English. All documents are bundled, including when offline.
- Terms/privacy default to the active app language. An explicit English option
  switches both documents within the initial acceptance flow. Returning from a
  document preserves that selection. Exports contain the displayed text and a
  language-labelled filename, never the acceptance receipt.
- The local receipt stores agreement version, language, SHA-256 of the accepted
  text, app version/code and device timestamp. Legacy receipts without language
  are read as English. Switching language preserves an existing agreement.
  Version 2026-09-18.2 replaces the preview contract with the public publisher
  terms, so one renewed agreement is required. No audio starts on acceptance.
- English policies are reconciled with the public 18 September documents; full
  Spanish and French translations retain the same sections and user rights.
  No claim is made that these translations received independent legal review.
  Contact details, support handling and local receipt-language disclosure match
  implementation. The translations do not add an English-precedence waiver.
- `scripts/check-localization.py` checks resources, placeholders, section coverage
  and exact archived terms. `scripts/export-policy-site.py <directory>` renders
  the same assets as static HTML, preventing app/site text drift. It does not
  publish anything itself. Policy-site URLs retain the existing English paths;
  `privacy-es.html`, `terms-es.html`, `privacy-fr.html`, `terms-fr.html` add languages.

Android implementation references:
https://developer.android.com/guide/components/intents-common#Email
https://developer.android.com/guide/topics/resources/app-languages

## Shared support inbox

Auralift uses `auraforgelabssupport+auralift@gmail.com`, which delivers to
Jamie’s confirmed `auraforgelabssupport@gmail.com` mailbox. Filter the tagged
recipient into the Auralift label. The subject also identifies Auralift.
Use the untagged address for the shared merchant/developer contact.
The contact-only correction keeps agreement version 2026-09-18.2; its current
EN/ES/FR archive mirrors are updated to match the bundled documents. Historical
preview terms and earlier validation evidence retain their original contacts.
