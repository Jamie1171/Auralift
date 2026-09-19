# What to prove before building the next app

This file describes **future work**, not tests already performed. The research has not produced a prototype, benchmark, paying customer or validated acquisition channel.

## Recommended sequence

**Investigate cutting first, including customer reach.** Jamie's follow-up challenged whether intended users know this category exists. First establish a specific audience, how they currently find help and a plausible reason to switch. Before or alongside that work, a small local prototype can accept stock and pieces, produce a verifiable plan and present it intelligibly. Technical success alone does not justify the full build. See the [market and premium-scope follow-up](04-cutting-market-and-premium-scope.md).

| Stage | Work | Evidence required to continue |
| --- | --- | --- |
| 0. Establish a reachable customer | Observe recent projects and existing discovery paths; test a plain-language demonstration with relevant makers | A specific recurring task, a reason to try the product and a credible route to those people; neither download bands nor recruited app testers establish this |
| 1. Confirm the comparison | Install the strongest applicable alternatives; verify current free limits, offline behaviour and checkout | Accurate current baseline, including free tools |
| 2. Prove the uncertain core | Small implementation plus an independent output check | Valid results on a defined corpus; known unsupported cases |
| 3. Observe real tasks | Intended users bring their own parts list, file or routine; compare with their preferred alternative | Concrete improvement in completion, time, errors or confidence |
| 4. Test the offer | Show the actual useful free tier and a clear one-time price for specific convenience | Evidence of willingness to pay, not just approval of the idea |
| 5. Build one closed-test candidate | Add necessary product polish, support, billing and release work | Core success survives a wider device/user sample |
| 6. Review actual usage and support | Observe repeat use and failure reports with consent and suitable disclosures | A reason to maintain and distribute the product rather than immediately starting another |

If cutting fails its gates, move to Upload Fit. If that fails, test the timer interaction hypothesis. Do not build all three in parallel just to use credits or fill a launch calendar.

## Candidate-specific gates

| Candidate | Technical gate | User gate | Strong controls | Reason to stop |
| --- | --- | --- | --- | --- |
| Cutting | At least 30 varied projects; independently valid geometry/quantities/constraints; competitive material use | Intended users enter, understand and use their own plan with less rework | CutListOptimizer, Cutter, an offline Android alternative, optiCutter; OpenCutList for existing SketchUp users | No switching benefit or materially worse plans |
| Upload Fit | Varied files and several device families; no falsely labelled success; acceptable playback and constrained outputs | Users finish the whole select-to-share task more easily | InverseAI, Panda, FFShare, Image Toolbox and native tools as applicable | Advantage is only no ads; compatibility/support burden overwhelms value |
| Timer | Defined foreground/background/permission/reboot matrix; no silently missed expected alerts in supported conditions | Users make fewer control/identification mistakes during concurrent tasks | Persapps, Catfantom, TimeRMachine and built-in Clock | Existing app plus a brief explanation works just as well |

These corpus sizes and gates are proposed minimum working checks, not statistical guarantees. Add cases to explain actual failures rather than multiplying tests without a reason. Record unsupported conditions plainly. Do not average away an invalid cut plan, a corrupt export or an unreported missed alarm behind a high overall success percentage.

For early user sessions, five to eight relevant people can expose obvious workflow failures. They cannot establish market demand. Avoid giving only the new prototype helpful instructions: give comparable orientation to each alternative, vary the order where practical and record the user's prior familiarity. Ask them to perform the task before asking which product they prefer.

Useful questions are: “Show me the last time you needed this,” “What did you do instead?”, “What was difficult?”, and “Which result would you use now?” A generic “Would you use an app like this?” is weak evidence.

## Pricing experiments

These are **proposed UK customer checkout prices**, not tax-exclusive bulk-entry values or expected developer receipts. They are not changes to Auralift's prices. Confirm the actual Play checkout preview when a product is eventually configured.

| Candidate | Initial one-time range to test | Useful free result | Possible paid convenience | Commercial concern |
| --- | --- | --- | --- | --- |
| Cutting | £6.99–£9.99 | A small real job, persistent draft and basic output | Several organised projects, offcut bank, CSV and label templates | Occasional use; capable free competitors |
| Upload Fit | £4.99–£6.99 | A supported single file that genuinely meets requirements | Batch work, saved destination presets and history organisation | Many users need it only once; broad compatibility costs |
| Timer | £2.99–£3.99 | Useful simultaneous timers, correct alerts and controls | More saved routines and organisation | £1.25/free alternatives; low revenue per customer |

The ranges should be challenged in observed sessions and later by actual purchase behaviour. The cutting range describes the original narrower proposition; it is not a settled price for the broader workshop workflow in the follow-up. Do not assume that being cheaper than a subscription automatically yields buyers. A lifetime model fits best when the product remains local and does not promise unlimited recurring cloud work. It still carries maintenance and support costs.

An optional Supporter tier can express support without different functionality, but it is not evidence of a viable primary business. Optional ads should be assessed for each tool, including how an entitlement behaves during a running task. No forced ads is part of the intended experience, not a licence to make unprofitable promises.

## Economics without invented forecasts

The central question is **how much developer revenue an acquired user produces over time**, compared with acquisition, support and maintenance costs. We do not yet know any of those values for these products.

For illustration only, assume 10,000 installs and £4 received per completed paid purchase. This is an assumed net receipt, not a claim about store fees or tax treatment. Advertising revenue is zero in this illustration.

| Assumed paid conversion | Purchases | Assumed developer receipts | Receipts per install |
| ---: | ---: | ---: | ---: |
| 0.5% | 50 | £200 | £0.02 |
| 2% | 200 | £800 | £0.08 |
| 5% | 500 | £2,000 | £0.20 |

These are arithmetic scenarios, not forecasts. They exclude the cost of development time, ongoing support, acquisition and other operating expenses. At the middle assumption, paying £0.08 per install would already use the entire assumed receipt before those costs. That is why broad paid advertising should not be the initial distribution plan for a low-priced utility.

Download bands and angry subscription reviews cannot fill in the missing conversion number. Nor do enthusiastic recruited testers necessarily behave like customers who find the app organically.

## Discovery experiments worth doing

- **Cutting:** show one reproducible project, the competing plans and the actual stock/cuts. A useful guide or maker demonstration can explain value without vague claims about “AI optimisation.”
- **Upload Fit:** show a real file requirement and verified result. Keep destination-specific rules current; a dated preset is not a permanent promise.
- **Timer:** show a concurrent task with a clear before/after interaction difference. Demonstrating fewer mistakes is stronger than displaying many colourful presets.

Search phrases in the briefs are hypotheses. Their volume, competition and conversion have not been measured. Before spending money, test whether a clear demonstration produces qualified interest and whether people complete the core task once installed. No advertising, outreach, posting or purchases were performed as part of this study.

## Maintenance and reuse

| Candidate | Main continuing burden | What can potentially carry over from Auralift |
| --- | --- | --- |
| Cutting | Solver correctness, units/imports, project migrations and domain support | Native UI patterns, support drafts, billing/restore and release discipline |
| Upload Fit | Media/codec variations, picker access, storage, interrupted jobs and SDK changes | UI/support/billing foundation; not Auralift's audio-effect assumptions |
| Timer | OEM alarm behaviour, notifications, permissions and state recovery | Native UI/support/release patterns; alarm lifecycle must be designed separately |

Reuse components after reviewing dependencies and policies for the new app. Do not copy all permissions, ad SDKs or legal text because they exist in Auralift. Keep new products separate from both Auralift's implementation and SoloRealm.

## Handover

The research recommendation is **validate customer reach and switching benefit for cutting first, before or alongside its technical prototype**, subject to Jamie's choice. No launch date, app name, budget or new product has been approved. Update [the portfolio strategy](../../FUTURE-APP-STRATEGY.md) when Jamie chooses a direction or new evidence changes the ranking.

For the next research or implementation session, read the [decision brief](README.md), the chosen product brief and the evidence limits first. Then record what was actually tested, including failures and reasons to stop. Do not silently turn this shortlist into an assertion that demand or reliability has already been proved.
