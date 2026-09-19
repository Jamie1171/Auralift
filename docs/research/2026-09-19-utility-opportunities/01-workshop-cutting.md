# 1. Offline Cut & Offcut Planner

**Decision: first prototype to validate, not an instruction to build the full product.**

The target user is an occasional woodworker, serious DIYer or small workshop owner who has a list of required pieces and needs to decide what stock to buy, what existing material to use, and how to make the cuts. Start with straight lengths and rectangular sheet goods. Do not start with industrial nesting, structural design or CNC.

The proposed promise is: **“Enter your pieces, use what you already have, and follow a clear cutting plan—even without a connection.”** Material savings are a plausible benefit, not a promise that every result beats every competing optimiser.

## Competitive baseline

These are observed listings and product documentation, not on-device benchmarks. Review evidence came from **75 distinct sampled reviews across CutListOptimizer and Cutter**. The smaller native alternatives did not expose comparable review samples in the inspected pages, so confidence about them is lower.

| Product | Observed strengths | Commercial / access evidence | Implication |
| --- | --- | --- | --- |
| [CutListOptimizer](https://play.google.com/store/apps/details?id=com.cutlistoptimizer) | Sheet layouts, grain, kerf, edge banding and exports; 1M+ download band | Ads and purchases; sampled complaints about payment/access sit beside strong positive feedback | Familiar planning and clear output are a substantial baseline |
| [Cutter](https://play.google.com/store/apps/details?id=com.embarcadero.OptimizaCorte) | Linear stock, mixed lengths, offcuts, materials and exports; 100K+ | Ads/subscription; developer said in November 2025 that internet was required for subscription checks | Offcuts and mixed materials are not missing inventions; offline continuity is worth checking |
| [Mio Cut Optimize](https://play.google.com/store/apps/details?id=com.djy.incise.global) | Linear, glass and sheet optimisation, including angle-related features; 50K+ | In-app purchases; exact checkout not inspected | Multi-material breadth already exists; stay narrower and clearer |
| [Bar Cutting Optimizer](https://play.google.com/store/apps/details?id=com.steel.bar.cut.list.optimizer) | Advertises offline linear plans, units, kerf, saved jobs and exports; 5K+ | Ads/IAP; recent notes include ad removal and CSV import | “Works offline” alone is not a sufficient advantage |
| [Cutting Optimizer](https://play.google.com/store/apps/details?id=com.cuttingoptimizer.himoucut.cutting_optimizer) | 2D sheet layouts for glass/aluminium and other materials; 10K+ | Ads/IAP | There are additional native entrants beyond the best-known apps |
| [optiCutter](https://www.opticutter.com/cut-list-optimizer) | Free web baseline for linear/sheet planning, stock, units and practical cutting constraints | Free access with commercial options | Must be included in a real task comparison; “mobile app” is not automatically better |
| [Cutlist Evolution](https://cutlistevo.com/) | Sheet, linear and roll workflows, imports, manual adjustment and richer outputs | Free tier and paid plans | Advanced capability is already available without inventing a new solver category |
| [Cutlistor](https://www.cutlistor.com/) | Web plans, imports and stock/project management | Observed free tier; listed Pro $12/month or $99/year | Subscription alternatives leave room for occasional-use pricing, but also provide services |
| [CutList Plus](https://cutlistplus.com/) | Established desktop cut planning | Listed Silver $89 and Gold $249 one-time | A one-time model is not unique; desktop and mobile use differ |
| [OpenCutList](https://docs.opencutlist.org/) | Open-source SketchUp extension for parts lists, cutting diagrams, labels and estimates | Open-source project; requires its host workflow | Excellent control for people already designing in SketchUp; not a standalone Android app |

Prices above are page observations on 19 September 2026, not a complete international price audit. The exact current Android IAP prices were not verified. Historical review prices are intentionally excluded from price comparisons.

## What users value and what causes friction

The strongest positive signal is practical usefulness: making work easier and avoiding manual layout calculation. Negative themes include task interruptions, subscription economics for occasional use, access failures, difficult entry and a plan that does not match the user's actual cutting process. The evidence is uneven: some detailed cutting-workflow requests are old, while many recent reviews are brief and positive. The [dated register](EVIDENCE.md) preserves that distinction.

| Priority | Problem to solve | Product response | How to tell whether it is better |
| --- | --- | --- | --- |
| 1 | A plausible-looking layout may still be difficult to cut with the available tools | Ask about saw/process constraints; show an ordered plan with clear dimensions and part labels | A user can explain and execute the sequence without reworking the drawing |
| 2 | Repeated entry, unit mistakes and loss of a half-entered job | Fast quantity entry, sensible duplication, persistent drafts and explicit units | Fewer corrections and less time from parts list to valid plan |
| 3 | Planning should remain usable in a workshop or at a timber rack | Local calculation and local saved projects; useful offline entitlement behaviour | Reopen, edit and export a previously usable project without network access |
| 4 | Small leftovers are lost or treated like unusable waste | Separate usable offcuts from scrap; carry labelled remnants into later projects | Users actually retrieve and reuse remnants rather than merely viewing a waste percentage |
| 5 | A casual job does not justify a continuing subscription for everyone | Transparent one-time upgrade for convenience and project organisation | Real users choose to pay after completing a useful free job |

One important correction: Cutter's developer replied in June 2026 that support for different materials had been added. That old missing-feature complaint is not a current gap. Another review criticised it for lacking sheet optimisation; a 1D product not doing 2D is a mismatch of expectations, not necessarily a defect. [Cutter reviews](https://play.google.com/store/apps/details?id=com.embarcadero.OptimizaCorte).

## Combine the best ideas into one workflow

The following is a proposed design, not a claim that no competitor has each individual feature.

**1. Start a job from the material.** Choose linear stock or rectangular sheet, then a material group. Thickness, section and finish must remain explicit so that incompatible material is never substituted simply because its length fits. A saved material preset reduces retyping; a new user can just enter one stock size.

**2. Add pieces quickly.** Each row needs dimensions, quantity, a readable label and any rotation/grain restriction. Support metric and fractional-inch entry, but store one consistent internal representation. Show how an entered fraction was interpreted. Pasting rows or importing CSV should produce a preview with highlighted ambiguities, never silent guessing.

**3. Add available stock and leftovers.** A project can use new stock, existing full stock and measured remnants. Give remnants physical labels such as “pine shelf A, 820 mm”; a photo and location can help later. Optimisation should not decrement inventory merely because someone experiments with a layout.

**4. Compare useful alternatives.** Offer clearly labelled objectives such as fewer purchased sheets, lower material cost, or a simpler cutting sequence. Do not bury trade-offs in a single unexplained score. A plan with marginally less waste can be less useful if it requires awkward cuts or leaves unusable fragments.

**5. Work from the result.** Show a high-contrast diagram, cut order, labels and a printable/shareable report. A user can mark pieces completed without changing their requested dimensions. If they revise the job midway, preserve the completed work and explain which remaining cuts change.

**6. Close the job deliberately.** Confirm what stock was actually consumed and which leftovers were kept. Save those remnants only after confirmation. This connects the next job to the current one and is a stronger retention mechanism than another calculation button.

The combined value is a loop: **plan → buy less or buy correctly → cut with confidence → retain useful material → start the next job faster**. The differentiator must be demonstrated in that loop, not asserted from a feature comparison.

## First release and later expansion

| First usable release | Later, if demand supports it | Exclude initially |
| --- | --- | --- |
| Linear lengths and rectangular guillotine-cut sheets, subject to prototype results | Cost-aware alternatives and richer stock purchasing choices | Irregular polygon nesting and CNC toolpaths |
| Kerf, trim allowances, quantities, material separation and rotation/grain restrictions | More specialised saw constraints and interactive manual plan adjustment | Structural calculations or automatic splicing of oversized parts |
| Fast manual entry, saved draft, explicit unit handling | Reviewed CSV templates and broader import support | Photo-to-measurement promises or AI-generated dimensions |
| Clear diagram, basic PDF output and persistent job state | Label-sheet templates, richer reports and job history | Accounts, team billing and cloud sync |
| Manual remnants in the active project | Persistent cross-project offcut bank and usage history | CAD integration before the simpler workflow earns adoption |

A narrow 1D vertical slice is useful for testing data entry, arithmetic and the optimiser interface. It is **not automatically a sufficient commercial release**. If target users mostly work with sheets, postponing 2D just because 1D is easier would miss their job. Let prototype sessions settle that scope.

## Technical feasibility and risk

Local storage, calculation and PDF creation do not inherently require a server. The difficult part is a correct, understandable result under real constraints. This is a good fit for an independently testable Kotlin core with Compose around it; it is not permission-heavy Android work.

**Correctness precedes optimisation quality.** A valid plan must contain every requested piece in the right quantity, keep pieces within stock, honour orientation and material groups, account for kerf and trim, and never overlap pieces. A feasible rectangular layout is not enough if the declared cutting method cannot produce it.

Use exact internal units rather than repeated display-unit rounding. Specify how kerf is counted at boundaries and between cuts. Validate a plan independently of the algorithm that generated it. A defect in the optimiser should not be able to mark its own output valid.

For small cases, compare against exhaustive or exact reference solutions. For larger cases, compare several heuristic approaches under the same time budget. “Best plan found” is a more defensible label than “perfect” unless optimality has actually been established for that case. Keep the input, constraints and solver version with the result so a discrepancy can be reproduced.

**The first benchmark must include the free alternatives.** Use the same dimensions, stock limits and constraints. Compare valid material consumption, usable remnants, calculation time and the human's time to enter and understand the job. A prettier screen does not compensate for routinely consuming extra material.

Suggested corpus: at least 30 varied real or realistically constructed projects, including exact fits, fractional units, kerf-sensitive fits, limited stock, mixed material groups, grain constraints, narrow strips, many duplicates, unusable requests and reusable leftovers. These cases have **not yet been run**.

## Monetisation and discovery hypotheses

Test **£6.99–£9.99 as a one-time customer checkout price**, with a useful small-job free tier. This range is a proposal, not measured willingness to pay. Accuracy, saved input recovery and a basic usable export belong in free. Paid convenience could include several organised projects, a persistent offcut bank, CSV import and label templates. Existing data should remain readable and exportable.

An optional ad should never interrupt measurements or cuts. I would initially test this concept without an ad SDK: the trust and offline proposition are stronger when the workshop workflow remains uncomplicated. Add monetisation complexity only if evidence justifies it.

Acquisition should demonstrate a real job: a shelf, storage unit or workbench with the input list, the resulting cuts and the measured material used. Search phrases such as “plywood cut list,” “timber cutting planner” and “offcut organiser” are hypotheses to test, not verified high-volume keywords. A maker's tutorial or an honest comparison may be more useful than broad paid installs. Any outreach requires a separate concrete plan; none has been sent.

## Proceed / stop gates

- **Proceed** if the prototype produces valid plans, is competitive on real material usage and lets intended users complete jobs more easily than their preferred free/paid alternative.
- **Narrow** if one material/process clearly dominates the useful cases. A very good workshop-specific planner can be more credible than a generic “all materials” tool.
- **Stop or demote** if people are already satisfied with optiCutter, OpenCutList or an offline Android app and cannot show a meaningful switching benefit; if results are routinely worse; or if the needed scope becomes industrial CAD/ERP.

The recommended next action is a small comparison prototype and observed task sessions. It is not a full launch commitment, and the sample does not establish that workshop users will pay the proposed price.
