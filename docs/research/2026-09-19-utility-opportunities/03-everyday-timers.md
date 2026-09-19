# 3. Everyday Timer Board

**Decision: third validation target, with the lowest commercial confidence of the three.**

The target user runs several practical tasks at once—cooking, baking, craft stages or everyday routines—and needs to see what is happening and recognise what needs attention. The opportunity is interaction design during a busy task. It is not simply “multiple timers” or “a timer without ads.”

The proposed promise is: **“See every task, hear which one needs you, and repeat a routine without setting it up again.”** Do not position it as medical monitoring, an industrial safety system or a guarantee against every Android background restriction.

## Competitive baseline

This category received **150 distinct sampled reviews across six apps**. Two of the larger products also received newest-review checks. Capabilities below are from listings and developer material; runtime reliability has not been independently tested.

| Product | Existing strengths | Commercial evidence | Implication |
| --- | --- | --- | --- |
| [Multi Timer StopWatch — LemonClip](https://play.google.com/store/apps/details?id=com.jee.timer) | Parallel/sequential timers, groups, speech, widgets and history; 1M+ | Ads and purchases; some users like rewarded access | Groups and spoken alerts already exist; interruptions and active-state clarity are worth comparing |
| [MultiTimer — Persapps](https://play.google.com/store/apps/details?id=com.persapps.multitimer) | Boards, flexible timer types, running adjustments and history; 100K+ | Generous free tier with purchases; no ads label observed | A strong modern alternative; hidden or misunderstood controls may matter more than missing features |
| [Kitchen Multi-Timer](https://play.google.com/store/apps/details?id=com.maxxt.kitchentimer) | Multiple kitchen timers, presets, widgets and customisation; 1M+ | Ads and purchases | Quick setup and readable controls matter; basic multi-timer functionality is not scarce |
| [Multi Timer — Catfantom](https://play.google.com/store/apps/details?id=org.catfantom.multitimer) | Linked groups, speech, saved configurations and history; 10K+ | **£1.25 upfront in the observed UK listing** | Major price/feature counterexample. A bakery reviewer praised it in August 2026 |
| [TimeRMachine](https://play.google.com/store/apps/details?id=io.github.deweyreed.timer.google) | Flexible stages, concurrent timers, speech, scheduling and backup; 50K+ | Free/open-source alternative with in-app purchases on Play | Sophisticated routines do not by themselves create a gap |
| [Multi Timer and Stopwatch — Millenium](https://play.google.com/store/apps/details?id=com.milleniumapps.timerstopwatch) | Multi/sequential timing, widgets and voice options; 10K+ | Ads/IAP; last listed update February 2021 | Useful design/history control; its old review sample is not evidence of current widespread defects |
| [Google Clock](https://play.google.com/store/apps/details?id=com.google.android.deskclock) | Basic clock, alarm, stopwatch and timer tasks | Free built-in/common alternative | Many users already have everything they need; compare the real multi-task workflow |

[TimeRMachine's source project](https://github.com/timer-machine/timer-machine-android) and [F-Droid listing](https://f-droid.org/en/packages/io.github.deweyreed.timer.other/) strengthen the free-alternative check. A [2025 community request for interval cues](https://www.reddit.com/r/androidapps/comments/1npqjga/looking_for_a_timer_app_that_buzzes_at_certain/) received Catfantom/TimeRMachine suggestions. That is evidence of an existing solution, not an unserved feature request.

## What the reviews suggest

The strongest repeated issues concern **what is running, what a tap will do, which alarm rang, and whether the alert remains useful outside the app**. Ad interruptions add friction in some competitors, but are not the whole opportunity.

| Priority | Problem cluster | Design response | Comparison task |
| --- | --- | --- | --- |
| 1 | Pause, reset, edit and stop are hard to distinguish or reach | Explicit controls; no destructive reset hidden on a normal timer tap | Pause one of three running tasks, extend another and keep the third unchanged |
| 2 | Concurrent alerts or notifications make alarm identity unclear | Name, colour/icon and optional spoken label; visible completed-task queue | Identify and dismiss two near-simultaneous completions correctly |
| 3 | Powerful sequences require too much setup or hidden knowledge | A small reusable routine editor with explicit automatic/manual stage transitions | Save and rerun a three-stage routine without rebuilding it |
| 4 | Background, lock-screen and sound behaviour varies | Honest setup state, a preview/test alert and appropriate notification handling | Run the routine screen-off while another app is in use |
| 5 | Ads interrupt an action with immediate timing consequences | Keep active timing and alarm controls free of advertising | Start, pause and dismiss without an unrelated interaction |

Positive reviews show why users stay: easy daily operation, reusable groups and flexible timing are valuable. Persapps users praised its free allowance; Catfantom users described useful linked stages; TimeRMachine users tolerated some learning in exchange for flexibility. This argues for preserving power while making common actions obvious, not stripping the app down until it cannot do the job.

Some complaints were discoverability problems. A Persapps request for a clock-time alarm received a reply pointing to an existing timer type. A Kitchen Multi-Timer user asking for a second timer was directed to an existing swipe action. Conversely, Catfantom's developer said in August 2026 that it was in maintenance rather than active feature development. None of this establishes that a new app will be preferred. [Dated evidence](EVIDENCE.md).

## Combine the best features around the active screen

**1. Make a quick timer without entering a setup system.** Name, duration and Start should be enough. Offer sensible recent durations and a preview of the alert. Do not require an account, a routine template or a purchase to test the first alarm.

**2. Keep the active board readable.** Each task shows its name, remaining time, expected finish time and state. Separate Pause, Add time and Stop. Reset should be explicit and reversible where possible; it should not unexpectedly erase an accumulated stopwatch. Support large text and screen readers, with more than colour alone identifying state.

**3. Make completion unmistakable.** Keep finished tasks visible until acknowledged. Identify the task in the notification and optional speech. If several finish together, preserve all of them rather than letting the last alert overwrite the others. Let the user preview the chosen behaviour without waiting through a whole timer.

**4. Promote a useful set of timers into a saved routine.** A cooking or craft session can contain parallel tasks and ordered stages. Each stage clearly says whether the next begins automatically or waits for confirmation. Persist the last-used routine while keeping a separate copy of its saved definition, so an adjustment during today's task does not silently rewrite tomorrow's plan.

**5. Explain unusual states.** Paused, elapsed, overdue and waiting-for-confirmation are different states. Show them as such. If the phone rebooted or relevant permission changed, present a clear recovered status rather than pretending every earlier alert certainly fired.

**6. Add finish-together planning only after the basic model is sound.** “Finish the vegetables and rice together” is attractive, but real cooking stages may depend on human readiness. Show calculated start times and allow revision. Do not automatically launch steps that require a person to prepare equipment or ingredients.

The coherent combination is a quick timer, an understandable active board and reusable routines. The design should be judged while people are distracted, not by how impressive its settings screen looks.

## First release and later scope

| First release | Later, with evidence | Exclude initially |
| --- | --- | --- |
| Named concurrent countdowns and clear running controls | More advanced interval/repeat patterns | Fitness coaching and health claims |
| Distinct alerts, reliable state and a test-alert flow | Optional speech and additional accessibility refinements | Voice assistant integration |
| Simple saved routines and explicit stage transitions | Finish-together planning | Recipe scraping or automatic cooking instructions |
| Large text, meaningful labels, local persistence | Widgets and portable routine import/export | Cloud accounts and shared household sync |
| Correct permission-denied, interrupted and reboot states | Wearable integration only after Android demand is demonstrated | Promising identical behaviour on every phone |

Do not use a staged feature plan to postpone alarm correctness. The least glamorous behaviour is the core product. Likewise, the main active board must work well before adding more routine types.

## Android constraints: why this is not a trivial timer

A countdown must derive its display from a stored deadline and an appropriate clock, not assume that UI ticks continue when the process is suspended. Distinguish durations from wall-clock appointments. Reboot, device time changes, time zones and restored state need explicit semantics.

Exact alarms have Android permission/access rules. Access can be absent or revoked; code must check it and provide an understandable alternative. Which exact-alarm permission is appropriate also needs a current Play-policy check for the final implementation. [Android alarm documentation](https://developer.android.com/develop/background-work/services/alarms).

Frequent short stages cannot be implemented by assuming unrestricted idle alarms. Android documents that allow-while-idle alarms cannot fire more than once per nine minutes per app while in Doze. A continuous user-started timing session may need a different, policy-appropriate lifecycle; its design must be validated rather than borrowing an unrelated service type to bypass limits. [Doze and standby](https://developer.android.com/training/monitoring-device-state/doze-standby), [foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types).

Notifications and full-screen behaviour depend on permissions and device state. An app in use may receive a heads-up notification instead of a full-screen presentation. Neither a setting screenshot nor one successful Pixel test proves identical Samsung or other manufacturer behaviour. [Time-sensitive notifications](https://developer.android.com/develop/ui/views/notifications/time-sensitive).

Test alert volume/routing, silent and Do Not Disturb states, headphones, concurrent media, denied notifications, denied alarm access, process death, force-stop, reboot and battery restrictions. Report force-stop honestly; do not promise to defeat a user's explicit decision to stop an app. [Persapps' own troubleshooting material](https://help.multitimer.net/troubleshooting-for-android) is further evidence that configuration and device behaviour are part of the support burden.

No timer prototype or cross-device alarm run was performed in this research.

## Monetisation and discovery hypotheses

Test **£2.99–£3.99 one-time** for extra saved routines, organisation and cosmetic convenience. Basic concurrent timing, sound, stopping and accessibility must remain useful for free. Catfantom's observed £1.25 price and TimeRMachine's free option make this the weakest commercial proposition in the shortlist; the proposed price requires demonstrated usability value.

An ad pass is particularly awkward here. It should never expire into a changed running routine or block an alarm. I would start without ads and test willingness to pay for the saved-routine experience. If that produces too little revenue for compatibility support, move on rather than adding intrusive monetisation.

Discovery can show concrete use: three dishes finishing, repeating a bread routine, or several craft stages. “Timer” alone is too generic a positioning concept; exact search demand was not measured. This product could be easy to explain in a short demonstration, but that does not establish an economical acquisition channel.

## Proceed / stop gates

First compare interaction prototypes with the best incumbents. Observe people creating three tasks, pausing one, changing another, identifying two completions and saving/repeating a routine. Count errors and interventions, not just whether they say the screen looks nicer.

Only then test a working alarm engine across actual devices and restrictions. A successful usability comparison cannot compensate for missed alerts.

**Proceed** if users consistently make fewer mistakes and choose the new workflow over Persapps, Catfantom, TimeRMachine and their built-in clock. **Stop** if a short explanation of an existing app solves the problem just as well, if the advantage reduces to styling, or if dependable operation requires more support than the low price can fund.

This remains a credible product-design experiment, not an equally strong commercial bet to the two higher-ranked ideas.
