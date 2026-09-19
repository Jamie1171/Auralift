# AuraForge Labs — future app strategy

Last updated: 19 September 2026. This is the continuity brief for Jamie's future
Android app opportunities, kept in Auralift's repository at his request. It records
the earlier discussion and makes its research process reusable. It is not a new
competitor study or an instruction to begin building another app.

## Jamie's goals and preferences

- Start with focused Android tools/utilities in categories where apps already
  exist, but users complain about intrusive advertising or excessive pricing.
- Compare reviews of several leading apps to identify recurring unmet needs.
- Aim for approximately one app per month. This is a target, not a quota: allow
  time for evidence, testing, support and maintenance of existing apps.
- Explore games later; utilities are the initial focus.
- Build under the AuraForge Labs umbrella. Offer a useful, trustworthy experience
  with fair pricing and voluntary monetisation.
- Jamie expects enough Get12Testers credits for another five closed tests in a
  couple of weeks, as stated on 19 September. This is a planning estimate, not a
  verified balance or proof of demand for five products.

## Agreed approach

1. **Identify a specific job people need done.** Start with an understandable
   utility and who uses it, rather than a broad collection of unrelated features.
2. **Compare several established competitors.** Look for repeated recent review
   complaints, particularly ads interrupting the task, unclear subscriptions,
   poor reliability, missing practical features and confusing controls. Read
   positive reviews too: preserve what users value. Distinguish pricing complaints
   from functional problems.
3. **Check the alternatives.** Include good inexpensive/free apps and built-in
   Android features. Dissatisfaction with a popular app is not enough if a strong
   alternative already solves the problem and is easy to find.
4. **Check technical feasibility early.** Investigate Android API and permission
   limits, background operation, manufacturer differences and hardware dependence.
   Prototype the uncertain core behaviour before spending time polishing a full
   app. Auralift's device-dependent audio behaviour is a lesson to carry forward.
5. **Choose one clear improvement.** Explain why someone would install and keep
   our app. Better usability, reliability and fair monetisation should support a
   clear purpose; a lower price alone is not sufficient differentiation.
6. **Assess discovery and ongoing costs.** Consider how users will find it and
   the support, compatibility, SDK and service costs of maintaining it. A quick
   first build does not establish that a product will be profitable or easy to
   maintain. Keep revenue forecasts labelled as assumptions.
7. **Make an explicit proceed, investigate or reject decision.** Select a small
   first release only after the evidence supports it. Reuse suitable Auralift
   foundations, then test the new app's actual core behaviour on real devices.

## Monetisation principles

Auralift provides a working reference: useful free functionality, an optional
rewarded ad for temporary Pro, a one-time permanent Pro purchase and an optional
higher-priced Supporter purchase granting identical access. It has no forced ads,
banners or automatic ad interruptions.

Carry the experience and transparency principles forward. Do not automatically
copy Auralift's prices, one-hour reward duration or feature split into every new
app. Evaluate the new product's costs and usage first, and agree its offer with
Jamie. Lifetime pricing must be assessed against any recurring service costs.
Do not assume ad revenue will outperform purchases or that competitor download
counts reveal revenue. Auralift's current checkout prices come from Google Play;
early price proposals are not a definitive current regional price list.

## Reuse without mixing products

Potential foundations include native Kotlin/Compose UI patterns, theme/language
support, opt-in support email drafts, billing and purchase restoration, rewarded
access, release checks and a structured tester brief. Reassess which components
belong in each app. Policies, permissions and disclosures must describe that app's
actual behaviour rather than being copied blindly.

The support inbox is `auraforgelabssupport@gmail.com`; Auralift uses
`auraforgelabssupport+auralift@gmail.com` to identify incoming messages. Other apps
can follow the per-app tag convention. For future app identifiers, the proposed
brand convention is `com.auraforgelabs.<appname>`; naming is checked per project.
Keep Auralift's existing `com.jamiewardle.auralift` identity unchanged.

New products should have their own repository and release identity when selected.
This portfolio brief does not authorise mixing code into SoloRealm or changing
Auralift's scope. Continue supporting Auralift while researching the next product.

## Research handover format

For each candidate, record the following in a dated research note. This format
operationalises the earlier plan; it does not imply research has already occurred.

| Item | Evidence or decision to record |
| --- | --- |
| User and task | Who needs it, what they are trying to do and how often |
| Competitors and alternatives | Links, region, date checked, pricing and ad model |
| Recurring complaints | Review dates, app/version where available, sample size and examples |
| What already works well | Positive feedback and capabilities worth preserving |
| Proposed improvement | One clear reason to choose and retain our app |
| Feasibility | APIs, permissions, device limits and a prototype result where needed |
| Costs and discovery | Ongoing obligations, distribution ideas and unverified assumptions |
| First release | Core scope, exclusions and human testing needed |
| Recommendation | Proceed / investigate / reject, reasoning and unresolved questions |

Use current sources when this research begins. Avoid treating a few negative
reviews as representative of everyone or treating recruited testers as evidence
of organic demand. Keep observations, inferences and Jamie's preferences separate.

## Current checkpoint and next action

- Auralift 0.5.9 / code 14 was delivered with Google rewarded test ads.
- Jamie reported that revoking the test purchase restored Free access and that
  the Ad Pass then worked. This is user-observed device evidence; it does not by
  itself confirm one-hour expiry or behaviour on other manufacturers.
- Jamie submitted the closed release and listed Auralift on Get12Testers. His
  latest report is **two testers on board**. Do not infer that the full testing
  period or production-access requirements are complete.
- **No next app, niche, competitor shortlist or budget has been selected.**
- Next research task, when requested: produce a small evidence-backed shortlist
  of utility opportunities using the process above and recommend which uncertain
  core behaviour to prototype first. Do not start five apps merely because tester
  credits are available.

## Starting a new chat

Give the new assistant access to `Jamie1171/Auralift` and ask it to read this file
before researching future app opportunities. The current location is branch
`feat/support-localized-legal`; if it has subsequently been merged, use the latest
merged copy. Read the repository's working instructions before making changes.
Memory alone is not the source of truth for this plan. Update this document as
Jamie chooses a niche, changes constraints or gathers evidence, so later chats
can distinguish the original direction from subsequent decisions.
