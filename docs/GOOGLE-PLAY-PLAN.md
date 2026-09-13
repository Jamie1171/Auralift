# Auralift: Google Play and monetisation plan

13 September 2026, updated for 0.5.0. Rewarded Ad Passes, equivalent one-time products,
an Owner flavour and background settings are implemented. No store submission, real purchase
or developer-account registration has been made. See
[Features and monetisation](FEATURES-AND-MONETISATION.md) for the exact implementation.

## Starting position

The app is native Kotlin/Compose and already supports Android 8.0+ by build
configuration. The owner now reports success with the updated personal preview
on the Pixel 9a. That is enough to proceed with product validation, not to claim
universal audio compatibility. See [Android compatibility](ANDROID-COMPATIBILITY.md).

The intended first product is a small audio utility: straightforward controls,
no forced advertising interruptions, clear feedback when a player cannot be boosted,
and honest limits. Completing a small launch teaches release signing, store
presentation, real-user testing, purchase handling and maintenance even if sales
are modest. Commercial demand and conversion have not been measured.

## Approved model in 0.5.0

The free app offers voluntary one-hour rewarded Ad Passes and two equivalent
non-consumable products: `auralift_pro` at an intended £3.99 and
`auralift_supporter` at an intended £5.99. The higher option explicitly supports
development and adds no exclusive features. There are no subscriptions, opening
ads, banners or timer-based interruptions. Prices come from Play at checkout.
Full available gain, EQ and background controls remain free. Owner stays unlocked.

The code includes both purchase products and an opt-in GMA Next-Gen/UMP adapter.
Release ads and checkout are unavailable until the account-side configuration is
supplied. Client-side receipts and reward clocks are not tamper-proof DRM; no
backend has been deployed. Read [Features and monetisation](FEATURES-AND-MONETISATION.md)
for configuration, expiry, restoration and precise limits.

Use Play Billing for both feature-unlocking products. This Supporter option grants
Pro and is not represented as a charitable donation or a payment-policy exception.
[Google Play payments policy](https://support.google.com/googleplay/android-developer/answer/9858738?hl=en).

## Costs and earnings

The Play Console developer account has a US$25 one-time registration fee.
Budget separately for a domain or support hosting if chosen, devices/testing,
maintenance and any later purchase-verification service. The current audio app
does not need paid inference, streaming servers or an account backend.
[Account setup](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en).

Google's fees vary by region and programme. Its current eligible first-US$1M tier
is effectively 15% with Play billing; the EEA/UK/US table separates service and
billing components. Confirm enrolment and the applicable terms in the actual
account. Do not assume every transaction or account has the same fee.
[Current service fees](https://support.google.com/googleplay/android-developer/answer/112622?hl=en).

For scale only: 100 purchases at a £3.99 customer price produce £399 in customer
payments. Payout is lower after applicable taxes, platform fees, refunds and
expenses. This arithmetic is not a sales forecast. Free downloads alone do not
produce purchase revenue.

## Route to a first public release

| Stage | Concrete work | Completion condition |
| --- | --- | --- |
| 1. Product identity | Choose final name/package, dedicated private repo, publisher identity, support address and payment model | Choices recorded before the first Play upload |
| 2. Release engineering | Build an optimized release AAB, configure upload signing and Play App Signing, verify release permissions/dependencies and upgrade behaviour | A signed release bundle installs and runs through internal testing |
| 3. Wider compatibility | Execute the device matrix; resolve the 16 KB graphics-library check; test audible gain and failure messages | Recorded results support the proposed listing claims |
| 4. Store materials | App icon, feature graphic, real release screenshots, concise listing, privacy policy and support contact | Assets and hosted policy match the actual release |
| 5. Play declarations | Data safety, ads, content rating, audience, app access and foreground-service declaration | Accurate completed forms and a demonstration video |
| 6. Closed testing | Recruit a varied group, record failures and fixes, retest purchase flows if included | Applicable personal-account testing gate met and production access approved |
| 7. Launch and learn | Submit for review, monitor crashes/feedback/refunds, maintain compatibility | Review accepted; support and update process operating |

New Play apps use an Android App Bundle (`.aab`). Google turns it into optimized
APK downloads for each device. The delivered debug APK is for personal testing;
its certificate is not the production signing identity. Configure the permanent
identity and keep upload keys outside Git with a recoverable backup. Moving to
a different signing identity may require reinstalling the personal preview.
[App Bundles](https://developer.android.com/guide/app-bundle),
[App signing](https://developer.android.com/studio/publish/app-signing).

Google currently requires new phone apps and updates to target Android 16/API 36
from 31 August 2026. The present target meets that requirement while preserving
the Android 8 minimum. Recheck the rule at submission time.
[Target API policy](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en).

New personal accounts created after 13 November 2023 need at least 12 testers
continuously opted in for 14 days before applying for production access. This is
not automatic approval after two weeks. Account setup also includes identity and
Android-device verification. Recruitment and review time are additional.
[Testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465),
[Account setup](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en).

## Auralift-specific release details

**Background audio controls:** The current service uses `specialUse` to retain
audio effects after the controls leave the foreground. Prepare an explanation
of the user-started function, what interruption does to listening, and a short
video showing Enable, background operation, the notification and Stop. The
manifest declaration is already present; Play Console review is still required.
Do not switch to an inaccurate service type to avoid review.
[Foreground-service declarations](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en).

**Privacy:** Even an app collecting no user data needs a privacy-policy link and
Data safety form for closed/public distribution. Version 0.5.0 processes
audio locally. It includes optional Visualizer/overlay permissions, manual
sharing, and Play-only billing, rewarded-ad and privacy SDKs. It does not open or record the
microphone. Original in-app privacy and terms drafts describe these flows. Finalise
the publisher/support contact, host the privacy policy and audit the merged release
manifest/SDKs when completing the form. The public edition contains ads and disclosed SDK data sharing; Owner has no
ad/billing SDK. Audit any later backend or analytics before introducing it.
[Data safety requirements](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

**Release validation:** CI now defines Owner/public tests, oldest/current simulated
OS checks, lint and unsigned optimized APK/AAB builds. The workflow is now at the
root of `Jamie1171/Auralift`. See [validation](VALIDATION.md) for actual
results and open checks. Configure permanent upload signing, exercise AAB-derived
APKs on real 4 KB/16 KB devices and complete Play purchase tests before release.
Build success does not establish store acceptance or audible gain.

**Store positioning:** Describe an audio utility rather than a medical device.
Do not promise measured 35 dB louder output, every media app, or every Android
phone. Allow users to test compatibility before presenting a purchase.

**Ownership and branding:** The name is provisional. Check it before commissioning
store assets; retain third-party notices and choose licensing deliberately.
Package names are permanent on Google Play.
[Create an app](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en).

## Next launch work

The authorised 0.5.0 feature implementation is complete as a test build; use it to
collect real feature/compatibility feedback. The dedicated repository is now
`Jamie1171/Auralift`. Next establish the final package/name, support identity,
signed internal Play track and product
catalogue. Complete device, purchase, privacy and release checks before any public
submission. £3.99 / £5.99 are the approved UK price targets; configure actual
products and prices in Play Console before offering checkout.

Continue this plan in `Jamie1171/Auralift`; it must never be merged into SoloRealm
or pushed to its GitLab/Lovable project. The former storage branch is historical.

## Closed-testing exchange

Jamie has joined Get12Testers. The prepared [closed-testing brief](CLOSED-TESTING-BRIEF.md)
contains eight feature requests and five feedback checkpoints. It has not been
submitted. Use actual Play opt-in dates and genuine feedback, not website credit
totals, as evidence for the production-access application.
