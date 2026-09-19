# 2. Upload Fit — file-size preparation

**Decision: second validation target; broad evidence of friction, substantial engineering and acquisition risk.**

The target user has a photo or video that a form, email workflow or receiving service will not accept. Their goal is a usable file under specific limits, not learning codecs or editing a film. Examples include a work clip, an application photo, an insurance image or a video being transferred to a constrained device.

The proposed promise is: **“Tell us the file requirements. Get a compatible copy, verify it, and send it.”** Avoid promising unchanged quality, universal compatibility or successful submission to every destination.

## Competitive baseline

This category received **215 distinct sampled reviews across seven apps**, plus checks of open-source alternatives. All feature descriptions below are advertised capabilities, not benchmark results.

| Product | Existing strengths | Commercial / access evidence | What it means for us |
| --- | --- | --- | --- |
| [Panda Video Compressor](https://play.google.com/store/apps/details?id=com.pandavideocompressor) | Video reduction, resizing and batch workflow; 10M+ download band | Ads and paid options | Large adoption and many satisfied users; merely shrinking a video is not a differentiator |
| [Video Compressor & Converter — InverseAI](https://play.google.com/store/apps/details?id=com.video_converter.video_compressor) | **Target size, previews, batch queue and codec/resolution controls**; 5M+ | Ads and premium access; a recent reviewer liked rewarded access | Most of the obvious “new” features already exist |
| [VidCompact](https://play.google.com/store/apps/details?id=com.xvideostudio.videocompress) | Compression, conversion and lightweight editing; 10M+ | Ads and paid offers | Compete on finishing the task cleanly, not adding another editor |
| [Video Transcoder](https://play.google.com/store/apps/details?id=protect.videoeditor) | Free open-source local conversion; 100K+ | No paid model in the inspected listing; last listed update January 2019 | Useful free control, but sampled reviews are historical and do not establish current compatibility |
| [LitPhoto](https://play.google.com/store/apps/details?id=com.coffee.litphoto) | Simple photo compression, resizing and batch operation; 1M+ | Ads | Positive feedback values speed and simplicity; feature breadth is not the only selling point |
| [Photo & Picture Resizer — Farluner](https://play.google.com/store/apps/details?id=com.simplemobilephotoresizer) | Batch resizing, target KB/MB and dimensions; 10M+ | Ads/IAP; a July 2026 reviewer describes a lifetime purchase, but checkout was not inspected | Target size is already normal in image tools; don't assume every paid option is a subscription |
| [Image Toolbox](https://play.google.com/store/apps/details?id=ru.tech.imageresizershrinker) | Broad free image processing, including size targets and batch tools; 500K+ | Open-source/free alternative | Kills a generic “cheap resizer with lots of features” proposition |
| [FFShare](https://github.com/caydey/ffshare) | Open-source local media compression integrated with sharing | Free/open-source; distribution and device compatibility need hands-on checks | A direct control for the proposed share-to-compress workflow |

Built-in gallery editing, trimming and the receiving service's own compression are also competitors. They should be tested on the actual task. They are not assumed to offer every file-size or dimension constraint, but often make a separate app unnecessary.

## Recurring problems and positive counterevidence

The most actionable patterns are **selection/access failures**, **interruptions before a result is available**, **unclear or stalled progress**, and **interfaces that make a simple goal feel complicated**. These recur across products; they are stronger evidence than a single request for a missing codec. Exact frequency cannot be inferred from this sample.

| Priority | Evidence pattern | Consequence | Proposed improvement |
| --- | --- | --- | --- |
| 1 | Current complaints in several video apps describe selected files not appearing or processing not finishing | Users cannot complete the primary job regardless of feature count | Robust Android picker handoff, persistent queue state, actionable errors and verified outputs |
| 2 | Ad loops and difficult-to-close interruptions occur in both image and video samples | A short practical task becomes unpredictable | No forced interstitial during selection, processing or sharing |
| 3 | Complaints describe complexity, unwanted interface changes and difficulty finding the desired option | Users must understand the tool before solving the problem | Begin with size/type/dimension requirements; keep advanced encoding controls optional |
| 4 | Slow processing, heat and background disruption appear in video feedback | Users cannot tell whether to wait, retry or reduce scope | Honest progress, cancel, storage checks and clear limitations; no universal speed claim |
| 5 | Occasional-use and purchase-restoration concerns recur | Trust and willingness to pay suffer | Clear one-time convenience upgrade and dependable restoration |

These apps are not uniformly bad. The newest Panda, InverseAI and VidCompact samples contained many satisfied users. LitPhoto users often praised its simplicity. Image Toolbox's breadth was a strength for power users even when others found it difficult to learn. Old bugs with developer-confirmed fixes are not counted as current opportunities. See [dated examples](EVIDENCE.md).

Community checks also found people satisfied with [FFShare and Panda](https://www.reddit.com/r/androidapps/comments/1kduxf0/best_free_video_compressor/), and a [privacy-oriented request](https://www.reddit.com/r/privacy/comments/1lzkk42/looking_for_a_privacy_focused_image_video/) receiving an open-source recommendation. A local/no-ads claim is therefore a baseline to earn, not an empty market.

## Combine the best features into a finished task

**1. Receive the file where the user already is.** Accept Android's Share action and a system picker. Show the filename, thumbnail, duration or dimensions and current size immediately. If access fails, explain which file needs reselecting rather than returning to an empty picker loop.

**2. Ask for requirements, not encoding knowledge.** A simple form accepts maximum size, permitted file type and optional dimension limits. Clearly distinguish MB from MiB when necessary. A destination preset is a convenience, not a guarantee that an external service's rules have not changed; let users inspect and edit it.

**3. Explain the trade-off before processing.** Suggest a compatible output and show a short comparison preview when useful. If a long, high-detail video cannot remain visually acceptable at a tiny limit, say so and offer explicit choices such as a shorter clip or lower resolution. Do not silently crop, trim or drop audio.

**4. Process as a job with recoverable state.** Persist input references, requested constraints, completed outputs and failures. Process sequentially initially to control memory, storage and heat. After interruption, keep completed files and offer to retry the unfinished file. This is different from promising that every encoder can resume midway through one file.

**5. Verify the actual output.** Read the generated file's size, dimensions, type and relevant media properties. A bitrate estimate is not proof that the final file fits. If it exceeds the cap, try a bounded adjustment or clearly report the unresolved constraint. Never label an incomplete export as a finished file.

**6. Let the user inspect and hand it off.** Show original versus output size and a preview. Keep the original. Share a readable content URI with the required permission grant. Make the result easy to locate again after the receiving app opens. Successful sharing does not prove that the destination accepted the upload.

This combines the focused simplicity of small photo tools, the target/preview controls of larger compressors, and the convenience of share-based tools. The potential advantage is fewer failed handoffs and fewer decisions to make—not exclusive possession of any one feature.

## First release and expansion

| First release | Later, if the core proves useful | Exclude initially |
| --- | --- | --- |
| Photos with explicit size/dimension/type requirements | Named reusable presets and batch rules | PDF editing, scanning and OCR |
| A documented, tested subset of video inputs producing compatible MP4 copies | Wider codecs and HDR support after a device corpus exists | A full video editor |
| Android Share and system picker entry | Batch queues and richer history as paid convenience | Automatic uploads to users' accounts |
| Verified output, preview, cancel and clear retry | Bounded target-size refinement for more difficult media | “Lossless compression” marketing |
| Original preservation and useful basic sharing | Audited metadata controls with clear explanations | AI enhancement or remote processing by default |

Start the technical spike with SDR video and a small set of proven codecs. If the only reliable deliverable is another basic image resizer, **do not call that a successful validation**: Image Toolbox and existing focused resizers already cover it. The valuable test is the complete constrained-file workflow.

## Android implementation constraints

**Input permissions:** picker access does not automatically persist forever. For jobs continuing beyond the immediate UI session, retain the URI permission where supported or stage an authorised local copy. Handle moved/deleted sources and insufficient space explicitly. [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker), [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).

**Video support:** Media3 Transformer relies on device media capabilities. HDR editing/tone mapping has additional platform and codec requirements. Default encoder fallback may alter a requested resolution, which matters when dimensions are strict. A successful export therefore needs validation against the user's constraints, not just a success callback. [Supported formats](https://developer.android.com/media/media3/transformer/supported-formats), [Transformer customisation](https://developer.android.com/media/media3/transformer/customization).

**Progress and cancellation:** Transformer exposes progress and cancellation and reports export information. Build the product around those states, including failure and cancellation, rather than an indefinitely spinning screen. [Transformer implementation guide](https://developer.android.com/media/media3/transformer/getting-started).

**Background work:** long media jobs need an appropriate user-visible lifecycle. Android's media-processing foreground service has a time budget—typically six hours in a 24-hour period across the app—and timeout handling requirements. It is not permission to promise unlimited background conversion. [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types).

**Quality and metadata:** size alone is not enough. Verify orientation, aspect ratio, colour, playback, audio sync and completeness. Removing location metadata must not break rotation or essential colour information. Do not claim that “offline” means the app has no network activity if billing, crash reporting or ads are later added.

**Dependencies:** prefer a small supported stack for the initial scope. If FFmpeg or open-source competitor code is considered, review its actual licence, codec/build composition and distribution obligations first. Open source is not a blanket permission to copy a product into a proprietary app.

## Monetisation and discovery hypotheses

Test a **£4.99–£6.99 one-time checkout price** for convenience such as batch processing, saved presets and organised history. A single-file task should work genuinely in free, without a watermark or an unusable result. Do not make reliability a paid tier. Many one-off users will never buy; that is a commercial risk, not an argument for making the free task fail.

I would test optional rewarded access only after the clean workflow works and only if users understand it before starting a paid convenience task. A timed entitlement must not expire halfway through an export and invalidate completed work. Competitors already offer rewarded access, so it is not unique differentiation.

Discovery should centre on precise tasks: reducing an application photo to specified dimensions and size, making a work video small enough to attach, or preparing media for a limited device. These are proposed search/content themes, not verified search volumes. Tutorials should show the original requirements and the verified output; do not promise a particular website's acceptance without checking its current rules.

## Proceed / stop gates

Use a varied corpus of photos and videos from several phones, local and cloud-backed picker sources, short and long clips, unusual orientation, silent clips, insufficient-storage conditions and interrupted jobs. Record completion, final constraints, playback, time and manual interventions. Test against InverseAI, FFShare, Image Toolbox and built-in tools where applicable.

**Proceed** only if the supported scope reliably produces acceptable files and users complete the task more easily than with those controls. **Demote** if the only advantage is no ads, if it requires a costly backend to achieve the required quality, or if device-specific failures make support disproportionate to a small one-time purchase.

No such benchmark has yet been performed. The research supports testing this proposition, not announcing that its reliability problem has been solved.
