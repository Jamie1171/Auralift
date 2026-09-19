# Cutting planner: customers, discovery and a complete premium product

Follow-up research: **19 September 2026**. Read alongside the [original cutting brief](01-workshop-cutting.md). This note qualifies the original recommendation; it does not record a decision to build.

## Decision changed by Jamie's challenge

Jamie correctly challenged the gap between a useful tool and a discoverable product: an occasional DIYer may not know cutting software exists or know its category name. We should not generalise that DIYers lack technical ability. The relevant differences are awareness of the task, frequency of work, existing methods and willingness to change them.

**Keep cutting on the shortlist, but validate a reachable customer and a reason to switch before committing to a full build.** The earlier study established a plausible practical benefit and a testable computational core. It did not establish affordable acquisition, market size, recurring use or purchase conversion. A technically good prototype alone would not close those gaps.

The preferred initial customer hypothesis is **a regular hobby woodworker or small maker building shelving, cabinets and furniture from rectangular sheet goods and straight timber**. This is narrower than all DIY and much narrower than industrial manufacturing. The product should complete this person's workflow; it should not absorb every feature found in every cutting industry.

## What the market evidence establishes

Current listing and vendor-page observations, checked on 19 September 2026:

| Product | Observable signal | What it tells us |
| --- | --- | --- |
| [CutList Optimizer](https://play.google.com/store/apps/details?id=com.cutlistoptimizer) | 1M+ Android downloads; advertises Android/web synchronisation, material and grain settings, edge banding and exports | This is an established category. Several proposed features already exist, and desktop-to-workshop continuity matters to competitors |
| [Cutter](https://play.google.com/store/apps/details?id=com.embarcadero.OptimizaCorte) | 100K+ downloads; focuses on linear stock, with mixed lengths, reusable offcuts, saved projects and spreadsheet/PDF handling | There is a distinct length-cutting market as well as a panel market. Offcut management is not a new invention |
| [optiCutter](https://www.opticutter.com/) | Free web tier; advertised Standard €9/month and Professional €19/month | A capable free/browser alternative constrains a basic calculator's price. Paid options exist, but published prices do not reveal sales |
| [CutList Plus](https://cutlistplus.com/) | Windows editions advertised at $89, $249 and $499 as one-time purchases; mobile viewers | Professional depth can support a very different offer from a small Android utility. This is not a like-for-like price comparison |
| [MaxCut](https://maxcutsoftware.com/pricing-subscription-plans/) | Free Community edition; Business $20/month or $200/year per device, excluding VAT | Quoting, repeat jobs and workshop administration are part of the business market, alongside optimisation |
| [Cutlist Evolution](https://cutlistevo.com/) | Browser product advertising imports, manual layout control, labels, costing and stock/offcut features; some features reserved for higher plans | A broad feature list alone would not distinguish us from modern competitors |

Download bands are cumulative global adoption signals, not active users, paying customers or revenue. The two Android bands cannot be added into a count of unique people. Listings and vendor pages describe capabilities; this follow-up did not install or independently benchmark these products. Web prices are the advertised currency and billing basis; only MaxCut's quoted tax treatment is explicit here.

Cutter's newly retrieved listing showed an update date of 19 September 2026. The earlier report contains an earlier listing observation and locale-dependent ratings. Do not interpret those differences as a measured trend or carry historical complaints forward as proof a current bug remains.

The defensible conclusion is **an established, competitive niche with both free tools and paid professional products**. There is no reliable market-revenue, growth-rate, keyword-volume or acquisition-cost estimate in this research. We cannot yet call it an underserved or highly profitable market.

## Choose the customer before choosing all the features

These are proposed segments and commercial judgements, not measured audience proportions.

| Segment | Typical starting point | Product and commercial implication |
| --- | --- | --- |
| Occasional DIYer | “I want shelves. How much wood do I buy?” May not have a parts list | Needs plain-language explanation and perhaps a guided project template. Harder to reach through category-name searches; repeat use is uncertain |
| Regular hobbyist / small maker | Has a sketch or parts list; repeatedly buys stock, cuts parts and stores leftovers | Best initial hypothesis: concrete value from easier entry, usable plans, saved projects and offcut reuse |
| Established cabinetry business | Designs, quotes and produces repeat jobs, often with existing desktop software | May require CAD integration, construction libraries, production labels, team workflows and accounting-related outputs. Stronger incumbents and a larger support burden |
| Industrial cutting business | Machine-specific constraints, automated production and specialist materials | Not the initial audience. CNC nesting, toolpaths and machine control are a separate commitment |

The hypothesis is not that hobbyists all reject subscriptions or professionals all pay them. Frequency, replacement effort and demonstrated value need to be observed. Nor should a smartphone be assumed to be the preferred place to enter a large cut list: support tablet layouts, spreadsheet import and portable project files, and test whether desktop authoring is essential before selecting an Android-only architecture.

## How people could discover it

There are two different acquisition jobs: reach people already seeking cutting software, and explain the benefit to people seeking help with a project. The following are **channels to test**, not channels already proven to deliver customers.

| Route | Proposed useful entry point | What needs proving |
| --- | --- | --- |
| Existing category searches | Clear store listing for cutting plans, plywood layouts and cut lists; show the actual result | Query volume, competition, listing conversion and a reason to choose us over free alternatives |
| Problem searches | A useful guide/calculator answering how many sheets a project needs or how to arrange shelf pieces | People actually use these phrases; content reaches them and leads to a completed project |
| Maker videos and project plans | Demonstrate one real build, provide its editable parts list, show purchased stock and practical cut order | A relevant creator's audience wants the workflow, rather than simply enjoying the video |
| Woodworking communities and classes | Useful worked examples and observation of real planning tasks | A recurring problem exists; participation and any promotion fit the community's rules |
| Timber merchants / cutting counters | A legible purchasing and cutting list that a customer can take to a supplier | Local suppliers accept its output and have a reason to recommend it; no partnership currently exists |
| Simple browser entry | Let a person try a small layout without installing; offer the Android app for saved jobs and workshop use | The demonstration produces qualified app use; the extra surface is worth maintaining |

Candidate search phrases include “plywood cut calculator”, “wood cutting planner”, “cut list optimiser” and “how many plywood sheets do I need for shelves”. These are hypotheses, not researched search-volume claims. Getting a page indexed is not proof that it will rank or acquire users cheaply.

Public woodworking discussions show both requests for cutting tools and sensitivity to recurring pricing. The sampled threads are historical and include developer promotion; they do not establish current demand size or unmet functionality. [Woodworking discussion, June 2022](https://www.reddit.com/r/woodworking/comments/v9e19d/cutlist_optimizer_app/), [beginner woodworking discussion, June 2022](https://www.reddit.com/r/BeginnerWoodWorking/comments/vlw8al/i_made_a_free_cutlist_optimizer/).

Retail cutting is a concrete purchase-time context: B&Q describes a timber-cutting service with restrictions on material, dimensions and equipment. That supports investigating supplier-friendly output, not assuming B&Q would partner with us or accept every generated plan. [B&Q timber cutting](https://www.diy.com/services/timber-cutting).

Plain-language positioning should connect the product with **buying enough material, avoiding unnecessary waste and following an understandable plan**. The technical term “cutting optimiser” can still appear for people who search for it. Education should show a useful result, not require a novice to learn industry vocabulary before starting.

## What a complete premium product would do

The proposed organising principle is **project → parts → available material → purchasing plan → practical cuts → reusable leftovers**. The following is our proposed product design, not a claim that competitors lack these features.

| Stage | Proposed capabilities | Why it earns its place |
| --- | --- | --- |
| Start a project | Fast manual entry; named parts and quantities; duplicate rows; paste/import CSV; a few reviewed templates for simple constructions | Reduces the main entry burden for regular users and offers a route for beginners who do not yet have a cut list |
| Describe materials | Material and thickness groups; metric and fractional inches; stock sizes, quantities, prices and shop location; offcuts as available stock | Prevents mixing incompatible materials and ties the plan to what can actually be bought or used |
| Set cutting constraints | Blade-width allowance, edge trim, grain direction, permitted rotation and finished/raw part dimensions; explicit edge-banding allowances where relevant | A geometrically neat layout is useless if it produces the wrong finished pieces |
| Compare valid plans | Sheet and straight-length planning; compare material cost, waste, cut complexity and useful offcut size | Lowest waste and lowest purchasing cost can be different answers. Show the trade-off rather than promise an unknowable universal optimum |
| Buy material | Stock purchase list, named part list, readable supplier PDF and approximate material cost | Makes the output useful before the person reaches a saw |
| Complete the cuts | Large labelled diagrams, an executable sequence for supported saw workflows, check-offs, undo and resume; printable part labels | Turns the plan into a working aid rather than a diagram the user must reinterpret |
| Save leftovers | Confirm actual completion, record remaining sizes/materials with optional photo/location, and offer them on the next job | Supports repeat value without silently treating an uncut plan as consumed stock |
| Reuse and cost work | Saved projects and templates; bulk quantity changes; material, hardware and optional labour estimates; simple quotes only if small-maker demand supports them | Reduces repeated entry while keeping bookkeeping from dominating a hobby workflow |
| Keep work dependable | Core planning offline; durable autosave, project revisions, backup/restore, open exports, readable tablet layouts and accessible controls | A person's saved plans and inventory must remain useful without a connection or continued subscription |

Templates need domain review. Dimensions of a finished cabinet are not automatically the dimensions of its panels: material thickness and construction method affect the result. Start with a small, well-tested library; do not promise to infer correct joinery from any photograph or certify structural designs.

“Practical cuts” also needs workshop expertise. A layout that fits rectangles can still require impossible or awkward cuts on the selected equipment. Verify the supported sequence with makers; do not label a layout optimal solely because it uses a high percentage of a sheet.

Several mature competitors already support significant parts of this workflow. CutList Plus describes manual diagram adjustment, stock management and project costing; MaxCut includes construction libraries and job administration; Cutlist Evolution includes CAD-related imports and manual layout tools. The proposed advantage must be **less entry effort and a clearer complete workflow for our chosen user**, demonstrated against those alternatives. [CutList Plus features](https://cutlistplus.com/Features), [MaxCut features](https://maxcutsoftware.com/features/), [Cutlist Evolution](https://cutlistevo.com/).

## Sequence the work without selling an incomplete core

1. **Validate customer and discovery.** Find relevant people with recent projects, learn how they obtained a plan and where they looked for help, and compare existing tools. Recruitment and interviews remain future work; none have been conducted or commissioned.
2. **Prove one complete workflow.** Accurate rectangular sheet planning, quick entry/import, material constraints, clear output, offline project persistence and a verified cut sequence for supported work. A paid first release must complete its advertised task, even if the template library is small.
3. **Develop the premium reasons to return.** Confirmed offcut inventory, reusable jobs, more reviewed templates, labels and practical costing. Test these with repeat projects, not only a single successful demonstration.
4. **Add adjacent depth when justified.** Straight-length optimisation, desktop handoff improvements and simple maker quotations are natural candidates. Prioritise by actual customer workflow; if linear stock is essential for the chosen audience, bring it forward rather than advertising unsupported completeness.
5. **Treat industrial expansion as a new decision.** Full CAD, CNC control, arbitrary shapes, roll nesting, multi-user production, accounting and unlimited cloud services are not automatic additions to this product.

This is not a commitment to a one-month delivery. Optimisation, unit handling and workshop correctness need independent checks even if the interface can be built quickly. No app code or technical benchmark was produced in this follow-up.

## Pricing and validation implications

A useful free result plus a one-time upgrade remains a reasonable **offer to investigate** for a predominantly local product. Premium could cover organised repeat jobs, inventory, templates and advanced export/labels. Avoid surprise restrictions after somebody enters a long project; keep access to existing work and essential exports dependable.

The original £6.99–£9.99 cutting range was an early hypothesis for a narrower product. It is not a settled price for the broader workshop workflow here. Test a price that can support maintenance against the demonstrated time/material benefit. Professional desktop prices are evidence of a different offer, not proof that our Android audience will pay the same amount. Optional future cloud work would need separate cost assessment.

Before committing to the full scope, proposed evidence is:

- Five to eight relevant makers attempt their own recent job using a strong existing alternative and our demonstration/prototype. This can expose workflow failures; it cannot estimate the market.
- Record their starting point, search language, existing tool, entry time, corrections, ability to follow the result and reason to use it again. Compare both tools fairly.
- A simple, truthful project demonstration reaches people outside recruited app-test exchanges. Track the path from qualified interest to completing a plan, returning for another job and eventually paying; do not substitute page views for value.
- Retain the original technical gate of varied independently verified projects before a paid release. Distinguish real material savings from merely different layouts, and do not claim savings against an artificially poor manual baseline.
- Identify a plausible acquisition route whose cost can fit measured purchase conversion and net receipts. No advertising budget or conversion threshold is approved yet.

**Demote this idea if** intended users are satisfied with free alternatives, prefer desktop workflows we cannot support economically, do not return, or cannot be reached without spending more than the product earns. A useful niche is worth investigating; usefulness alone does not earn first place in a build queue.

## Handover

Jamie's question materially sharpened the recommendation: **customer reach and switching benefit now precede or accompany the technical prototype**. Jamie has not selected this product, agreed a price or authorised a marketing campaign. No outreach, advertising, purchase, supplier agreement or app publication occurred. Update the [portfolio strategy](../../FUTURE-APP-STRATEGY.md) with actual findings when these proposals are tested.
