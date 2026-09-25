# Future update: ten-band Pro equaliser and genre presets

Agreed with Jamie, 25 September 2026. **Planning only; excluded from 0.5.11.**

## Product scope

- Free retains its existing five EQ controls and Balanced, Warm, Voice and Detail.
- Pro (Lifetime, Supporter, earned Ad Pass and Owner) adds a genuine ten-band EQ
  and a compact expandable genre selector immediately above the equaliser.
- Selecting a genre replaces the active curve and deselects any existing preset.
  Selecting one of the four basic presets clears the genre. Manual edits show Custom.
- Begin with meaningfully different Pop, Rock/Metal, Electronic, Jazz and Classical
  curves; consider Grunge and other genres after listening tests. Final list/curves
  remain a design decision, not copied settings from another product.
- Avoid increasing page length substantially. Ten sliders must retain readable
  values, useful touch targets and accessible precise adjustments.
- Preserve existing sound on unlock, existing saved profiles and free functionality.
  Save the ten-band configuration across expiry; restore the previous Free setting
  without an abrupt gain increase. Unlock alone must never raise gain.
- Saved personal curves are valuable alongside genres. Jamie uses a BAHA through
  Bluetooth; test the actual route as a listening-preference use case. Do not claim
  medical benefit or hearing-aid compatibility without evidence.

## Technical direction and evidence

Auralift currently maps its five-anchor curve onto the device's legacy Android
Equalizer bands. Ten UI sliders alone would not provide ten independent bands.
The supplied XEQ 38.7.0 package was statically inspected, not installed or run.
Its DEX contains constructors and calls for DynamicsProcessing, its Config.Builder,
EqBand, pre/post EQ updates, limiter and multiband compressor. This supports
investigating Android DynamicsProcessing as a genuine configurable-band route.
No XEQ source, assets, preset values or binaries are included in Auralift.

DynamicsProcessing is available from API 28 (Android 9); Auralift still supports
API 26. Preserve the legacy fallback and establish capability before offering
paid ten-band functionality. Android API availability alone does not establish
successful processing for every external player, manufacturer or Bluetooth route.

Sources inspected 25 September 2026:
- https://developer.android.com/reference/android/media/audiofx/DynamicsProcessing
- https://developer.android.com/reference/android/media/audiofx/Equalizer
- https://xeq.frackstudio.com/manual/

XEQ's manual describes configurable bands, gain, limiting and compatibility
limitations. Popularity/reviews support it as a product reference, not proof that
Auralift will work identically on all devices. No universal compatibility claim.

## Engineering acceptance before delivery

1. Prototype true independent bands using supported public APIs. Verify effect
   attachment, readback and audible results across existing session modes.
2. Account for EQ headroom plus overall gain, clipping, limiting and smooth preset
   transitions. Do not silently stack legacy EQ and new processing chains.
3. Preserve Stop/release, comparison, sleep fade, route change and entitlement caps.
4. Migrate five-band custom curves and profiles without unwanted loudness changes.
5. Verify Android 8 fallback, unsupported effects, Free/Pro transitions, reward
   expiry, saved settings and restoration.
6. Physically test speaker, headphones and Bluetooth, including Jamie's actual
   BAHA route and external media apps. Record compatibility rather than assume it.
7. Validate narrow screens, large fonts, TalkBack, numeric adjustment and localization.

This is a separate future release after floating-player usability feedback. No
promise of a specific release date or increased odds of Play approval is made.
