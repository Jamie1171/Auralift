# Features and monetisation — 0.5.7

19 September 2026. Jamie has created and activated both one-time products.
The public licensing key is configured in 0.5.7; actual Play transactions remain
to be tested. Live AdMob configuration is still absent.

| Option | Bulk base price (before local tax/rounding) | Product ID | Access |
| --- | --- | --- | --- |
| Free | £0 | None | All core audio controls |
| Ad Pass | Complete one rewarded ad | No Play purchase product | All Pro features for one hour; repeat after expiry when ads are available |
| Lifetime Pro | £3.99 once | `auralift_pro` | Permanent Pro entitlement |
| Supporter Pro | £5.99 once | `auralift_supporter` | Exactly the same permanent Pro entitlement; optional extra support |

Both purchase products are **non-consumable one-time products**, not subscriptions
or two separate feature tiers. Pro uses purchase option `pro-lifetime`; Supporter uses `supporter-lifetime`.
Both use the base offer (no offer ID). The original `buy` option remains supported
as a fallback, with each product-specific lifetime option preferred. Configure the actual local prices in Play Console; the app displays
Google's returned formatted prices rather than hard-coding sterling at checkout.
Existing owners are not offered another purchase of the same access. Supporter
is labelled as the same features for a higher price, never an implied requirement.
“Lifetime” describes this product's entitlement, not unlimited future products or
perpetual Android compatibility. Refund and statutory rights still apply.

## Free and Pro

| Capability | Free | Ad Pass / either Pro product / Owner |
| --- | --- | --- |
| Requested extra gain | 0–15 dB | 0–35 dB |
| Separate media volume; compare, reset and Stop | Yes | Yes |
| Balanced, Voice, Warm, Detail and custom EQ | Yes | Yes |
| Three output setups; normal sleep timer; widget, tile, notifications | Yes | Yes |
| Background controls, reboot reminder, languages, memory, haptics, spectrum | Yes | Yes |
| Mint theme and light/dark appearance | Yes | Yes |
| Floating player, gain, volume and EQ controls | No | Yes |
| Nine additional full light/dark palettes (ten total) | No | Yes |
| Up to 24 named sounds and JSON import/export | No | Yes |
| Final 30-second fade of added gain | No | Yes |
| Help, feedback, policies, permission controls | Yes | Yes |

Auralift requests gain through Android effects; device/player limits still apply.
Do not lock necessary Stop/reset controls or claim +35 dB measured acoustic gain.

## Ad Pass behaviour

1. User selects **Prepare Ad Pass**. UMP updates applicable consent; show a consent
   form only when required for this requested flow. Initialise the ad SDK only
   after consent permits a request. Subsequent launches can refresh existing
   privacy information without an unsolicited ad or consent form.
2. Load one rewarded ad. No automatic preloading/reloading, banners, startup ads
   or interstitials. The user explicitly selects **Watch ad · unlock 1 hour**.
3. Before calling the SDK's show method, synchronously release Auralift's effects.
   A shared gate prevents reconnects, settings changes, route changes or a new
   service from attaching effects during the ad. The overlay hides; spectrum
   releases when its lifecycle/gate changes. System media volume is unchanged.
4. Grant only from the SDK's earned-reward callback. Loading, impression, clicking,
   failed show or early dismissal do not grant access. The claim is idempotent.
   Closing an ad alone is not proof of completion. Late reward callbacks remain
   valid after dismissal; failed-show claims are invalidated.
5. On close/failure, release the gate. A still-running listening session recreates
   its effects at zero and ramps up using the saved settings. Stop and timer expiry
   remain effective; the callback never starts a stopped service. Destruction of
   the hosting Activity during an ad stops the listening session instead of
   enabling effects while SDK playback might continue.
6. The hour starts when the reward is earned and runs outside the app too. No
   stacking or ads during an active pass/permanent access. After expiry another
   ad can earn another hour, subject to availability. Ads can be videos or other
   rewarded formats: use “complete an ad” copy, not a guaranteed video duration.

Expiry closes the overlay and restores Mint, locks Pro profile operations while
retaining saved sounds, and caps current gain at +15 dB. Lower gain and EQ stay
as chosen. Renewing Pro opens the larger range without restoring louder gain. Core free controls keep
working. An already-running sleep fade cannot jump back up on expiry.

Pass time uses monotonic and wall clocks during a boot, wall time across a reboot,
and a persisted last-seen wall time to reject detected clock rollback. It does
not claim tamper resistance against rooted/modified clients. Clearing local data
loses an earned pass; reinstalling does **not** create another reward. No account,
fingerprinting or hidden shared-storage marker is used. Existing active 0.3 previews
are honoured until their original hour ends. For stronger commercial fraud control,
consider server-side reward verification and purchase verification before scale;
no such server or SSV endpoint has been deployed.

## Owner and Play separation

Owner (`com.jamiewardle.auralift.owner`) excludes billing, advertising and UMP SDKs,
has no internet permission and unlocks all features by default. The Pro page can
simulate Free, an earned pass, expiry, and the identical purchased entitlement.
Restarting the Owner process restores Owner access. Its private app data and
signing key remain separate from Play. Public source-set UI has no Owner panel;
runtime guards reject Owner simulation methods on the public AccessStore.

Play (`com.jamiewardle.auralift`) uses Google Play Billing 9.1.0, GMA Next-Gen 1.4.0
and its transitive UMP 4.0.0. Ad SDK internals can use web content to render ads;
the Auralift interface and audio engine remain native Kotlin/Compose/Android APIs.

## Activation checklist

- Finalise public package, publisher/support details, signing and hosted policies.
- Both product IDs are created with active purchase options. Match the option IDs
  above and confirm country availability for testers.
- Gradle property `auralift.playPublicKey` now contains the supplied public licensing key.
  The key is public verification material; no private signing key belongs in Git.
- Set `auralift.admobAppId` and `auralift.rewardedAdId` to the production IDs only
  for a deliberate release. Defaults leave release ads unavailable. Play **debug**
  forcibly uses Google's official test app/ad IDs regardless of these properties.
- Configure the one-hour in-app reward and relevant UMP Privacy & messaging forms
  in AdMob. Test accept, decline, privacy changes, no network and no fill. Privacy
  choices appear in Settings and Pro whenever UMP requires an entry point.
- Use test ads/test devices only during development. Use Play licence testers for
  both products, pending/cancelled/approved purchases, restore, refund and offline
  cases. A successful query with either valid product maintains Pro; losing one
  while still owning the other must not revoke it. A failed query retains a
  previously verified cached receipt. Acknowledge completed purchases; never consume.
- Verify production receipt signatures on a trusted backend before relying on
  strong anti-tamper guarantees. Current RSA/product/package/token/state checks
  are client-side defence in depth. Play owns checkout; this project cannot
  configure an account's products or prove a live purchase with unit tests.
- Complete Data safety and the **contains ads** declaration for the Play app.
  Reconcile advertising identifiers, approximate location from IP, diagnostics,
  interactions and SDK permissions with the final build. Do not copy Owner's
  offline/no-ad claims into the public listing.
- Set `auralift.storeLive=true` only once its public listing actually exists.

## Revenue and product judgement

This model earns from voluntary usage without recurring interruption. No revenue
forecast is justified by competitor install totals. Ad revenue is paid impressions
/ 1,000 × realised eCPM; purchase proceeds additionally depend on taxes, regional
Play fees, refunds and costs. Measure retention, completed rewards and purchases
before changing prices. There is no automatic analytics collection in this app
beyond the disclosed SDK behaviour. Do not require a positive rating or claim
that Supporter must be chosen to keep core features working.

## Sources checked 13 September 2026

- [Play Billing integration](https://developer.android.com/google/play/billing/integrate)
- [Billing security](https://developer.android.com/google/play/billing/security)
- [Rewarded-ad policy](https://support.google.com/admob/answer/7313578?hl=en-GB)
- [Single rewarded ads](https://developers.google.com/admob/android/next-gen/rewarded/single-load)
- [GMA Next-Gen setup](https://developers.google.com/admob/android/next-gen/quick-start)
- [UMP privacy choices](https://developers.google.com/admob/android/next-gen/privacy)
- [SDK data disclosure](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure)

## Gain and appearance changes in 0.5.0

The owner explicitly changed the prior free 0–30 dB model to Free 0–15 dB and
Pro/Owner 0–35 dB. These latest figures are not an exact half split. Shortcuts
advance in 5 dB steps, with the existing 0.5 dB buttons/slider for fine adjustment.
A selected smaller ceiling remains respected. Hardware rejection/readback controls
still apply; the platform does not document a universal 30 dB cap.

SettingsStore enforces the current entitlement on startup, every update and
profile loading. Access changes synchronously clamp saved active settings. The
service independently refreshes access and caps every gain-ramp step. Profiles
are retained at expiry; importing a profile never applies gain. The existing
three free output setups also load through the cap. Once a sleep fade starts,
expiry can only reduce its effective gain further.

Floating Player is a prominent Pro card and separate Settings page. Android's
overlay grant is for Auralift itself. One grant covers eligible apps; protected
screens and system decisions remain authoritative. There is no monitoring-based
per-app exclusion list. The 64 dp circle expands with Minimise and Close. Close disables the player
without stopping boost. Stop in the player resets gain/EQ to 0 dB/Balanced and
leaves the player available; Settings can launch it without enabling boost.
The app adds no usage-access, AccessibilityService or package-inventory permission.

Themes include complete dark/light palettes, surfaces, controls, corner shapes,
dial designs and static backdrop details. Native floating windows use the same
palette. Previews depict the implemented themes, not downloaded competitor artwork.

- [Android 11 overlay settings](https://developer.android.com/about/versions/11/privacy/permissions#system-alert-window)
- [Protected activities and overlays](https://developer.android.com/security/fraud-prevention/activities)
- [LoudnessEnhancer target gain](https://developer.android.com/reference/android/media/audiofx/LoudnessEnhancer)
