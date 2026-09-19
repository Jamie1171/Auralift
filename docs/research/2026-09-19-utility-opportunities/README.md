# Three Android utility opportunities

Research date: **19 September 2026**. Prepared for Jamie / AuraForge Labs.

**Recommendation: validate an offline workshop cutting planner first; keep a file-size preparation assistant second and an everyday multi-timer third.** These are the strongest propositions from this investigation, not three proven businesses. The first two have a clearer connection to a valuable completed task. The timer has substantial evidence of usability problems, but the weakest pricing power.

No new app has been selected by Jamie, built, named, or submitted. Product names below describe concepts; availability and trademarks have not been checked. This research belongs in Auralift's repository for continuity. Any selected product should get its own repository.

## The decision

| Priority | Specific proposition | Why it survives the research | Main reason it could fail | Next decision |
| --- | --- | --- | --- | --- |
| **1** | **Offline Cut & Offcut Planner:** turn a parts list and available stock into an understandable, executable cutting plan | Material has a real cost; the task benefits from local computation, clear diagrams, reusable offcuts and occasional-use pricing | Good free web tools and existing offline Android competitors already exist. A weak optimiser or awkward entry screen destroys the advantage | Validate a reachable customer and reason to switch, then benchmark a small native prototype before committing to a full app |
| **2** | **Upload Fit:** make a photo or video meet explicit file-size, format and dimension requirements | Similar selection, interruption and completion problems recur across several large apps; users have an immediate reason to finish the task | Target-size compression already exists. Device codecs and background processing make dependable delivery expensive | Prove the complete select → fit → verify → share workflow on varied phones |
| **3** | **Everyday Timer Board:** run several named tasks and reusable stages with unmistakable controls and alerts | Confusing running controls and alarm identity recur across competing apps; cooking, hobbies and everyday routines recur in positive feedback | Excellent free and £1.25 alternatives set a low price ceiling; Android background limits remain | Compare a simple interaction prototype against the best alternatives, then validate alarms |

The ordering is a judgement about **value, a plausible reason to switch, and suitability for a small developer**, not a numerical market forecast. Media has the broadest complaint evidence. Cutting comes first because the customer benefit can be demonstrated in a concrete project and the core computation can be tested without depending on a device manufacturer's media stack. Its review evidence is narrower, so this recommendation is conditional.

Read the full proposals:

1. [Workshop cutting: competitors, product design and technical gates](01-workshop-cutting.md)
2. [File-size preparation: competitors, product design and technical gates](02-upload-fit.md)
3. [Everyday timers: competitors, product design and technical gates](03-everyday-timers.md)
4. [What was demoted, rejected, or left in reserve](SCREENING.md)
5. [Review method, sample coverage and dated evidence](EVIDENCE.md)
6. [Validation sequence, pricing experiments and economics](VALIDATION.md)

**Follow-up after Jamie's discovery challenge:** [Cutting market, customer reach and complete premium scope](04-cutting-market-and-premium-scope.md). The market has established free and professional alternatives. The recommendation is now explicitly to validate customer reach before or alongside the technical prototype; an occasional DIYer may not know this category exists. Regular hobbyists and small makers are the proposed initial audience, not all DIY or industrial production. No acquisition channel or willingness to pay has been validated.

## What the investigation actually covered

I inspected **740 review entries, representing 698 distinct sampled reviews across 25 Android apps**. Twelve apps received separate newest-review checks as well as the initial “Most relevant” sample. The breakdown is 215 media reviews, 150 timer reviews, 178 inventory reviews, 75 cutting reviews and 80 reviews across shift planning, subscription tracking and a craft counter. Additional native, web, open-source and built-in alternatives were checked at listing or documentation level.

This is a purposeful qualitative sample. It is not a random survey, a scrape of every review, or a measure of complaint rates. “Most relevant” often surfaced old problems; the newest samples were frequently positive. Developer replies sometimes showed that a requested feature already existed or a reported bug had been fixed. Those cases changed the recommendations.

Google Play listings establish advertised capabilities and visible commercial models, not independently verified reliability. Download bands are cumulative adoption signals, not active users or revenue. Prices reported in old reviews are not current checkout quotes. The [evidence register](EVIDENCE.md) records these distinctions.

No competitor APKs were installed, purchases made, private analytics obtained, user interviews conducted, or performance benchmarks run. There is no measured acquisition cost, willingness-to-pay estimate or search-volume forecast. Technical constraints were checked against Android documentation; the proposed implementations still need prototypes.

## How the shortlist changed

The early inventory hypothesis looked attractive: people complained about subscriptions, entry friction, backups and export. It weakened substantially after checking **Find My Stuff**, **Magic Home Inventory** and **HouseBook**. Find My Stuff's current listing and positive reviews already support much of the proposed offline, flexible, portable experience. A packing-specific variation also faces established moving organisers. [Find My Stuff](https://play.google.com/store/apps/details?id=com.miquelcms.homeorganizer), [Magic Home Inventory](https://play.google.com/store/apps/details?id=net.twisterrob.inventory), [HouseBook](https://play.google.com/store/apps/details?id=chenige.chkchk.wairz), [MOVINGBOXES](https://play.google.com/store/apps/details?id=de.scalan.movingbox).

A generic image resizer was also demoted. **Image Toolbox** already supplies a powerful free alternative. The surviving media proposition is a guided completion workflow, not a claim to have invented target-size compression. [Image Toolbox](https://play.google.com/store/apps/details?id=ru.tech.imageresizershrinker).

Timers survived only in a narrower form. **Catfantom** and **TimeRMachine** already cover sophisticated sequences at very low or no upfront cost. The case is clearer operation during a busy task, not a longer feature list. [Catfantom](https://play.google.com/store/apps/details?id=org.catfantom.multitimer), [TimeRMachine](https://play.google.com/store/apps/details?id=io.github.deweyreed.timer.google).

Cutting moved up after comparing the value of a completed job with the operational burden of the other ideas. That does **not** mean the market is empty: offline support, offcuts, CSV input and cutting diagrams all exist in competing products. The proposition must combine them into an easier workshop process and pass a real comparison.

## Common product principles

- Start with one recognisable job and make the first successful result easy.
- Keep original files, saved projects, basic controls and user exports safe. Do not turn previously saved work into a ransom point.
- Make price and free limits clear before users invest effort. Evaluate one-time pricing against the actual support burden.
- Do not place an ad between a person and an active alarm, an unfinished export, or a cutting instruction. Auralift's optional ad model is a reference, not a requirement for every app.
- Treat privacy and offline operation as engineering commitments. A marketing claim is not a substitute for testing.
- Preserve useful simplicity as features grow. “The ultimate tool” should mean a coherent workflow, not every competitor's menu combined.

Jamie's one-app-per-month aspiration remains a planning preference. It should not determine the answer before the riskiest behaviour and a reason to switch have been demonstrated. Get12Testers participation can help find defects; it does not establish organic demand or commercial viability.
