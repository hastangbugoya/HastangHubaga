# General Scheduling Algorithm Blueprint

## Purpose

This document defines the **general scheduling algorithm blueprint** for the app suite.

It is intentionally **domain-agnostic** and should serve as the reusable foundation for:
- supplements
- activities
- meals
- any future system that needs:
  - reusable templates
  - date-specific planned instances
  - actual user logging
  - timeline reconciliation

This is the **general plan first**.  
Domain-specific documents should later specialize this blueprint for:
- supplements
- activities
- meals

---

# The Core Scheduling Model

The scheduling system should be understood as four layers:

1. **Template**
2. **Occurrence**
3. **Actual Log**
4. **Projection / Merge**

These layers must stay conceptually separate.

---

## 1. Template Layer

A **template** is the reusable user-authored definition.

Examples:
- supplement definition
- activity definition
- meal definition
- future habit/task definition

A template answers:

- What is the thing?
- Is it active?
- What are its reusable default properties?
- What scheduling rules should generate planned instances?

A template is **not** tied to a specific day.

### Template responsibilities
A template may store:
- title / name
- type / category
- notes / metadata
- active / enabled state
- scheduling configuration
- default values for logs or occurrences

### Template non-responsibilities
A template should **not** represent:
- a specific day’s instance
- whether today’s instance is complete
- historical truth of what already happened

That belongs to occurrences and logs.

---

## 2. Occurrence Layer

An **occurrence** is a materialized planned instance for a specific date.

A template says:
> this kind of thing can recur

An occurrence says:
> this specific instance is planned for this specific day/time

Examples:
- Sleep scheduled for 11:00 PM on April 4
- Magnesium planned for 8:00 AM on April 4
- Lunch planned for 12:30 PM on April 4

### Core property: occurrenceId
Every occurrence should have a stable business identifier:

**`occurrenceId`**

This is the bridge between:
- planning
- actual execution/logging
- timeline reconciliation

### Occurrence responsibilities
An occurrence should represent:
- template linkage
- scheduled date
- resolved planned time
- any date-specific resolved scheduling context
- stable occurrence identity

### Occurrence non-responsibilities
An occurrence should not be treated as:
- proof that the thing happened
- the actual event itself

An occurrence is a **plan**, not truth.

---

## 3. Actual Log Layer

An **actual log** represents what the user says actually happened.

Examples:
- user took supplement at 8:12 AM
- user started workout at 6:05 PM
- user ate dinner at 7:10 PM

The log is the historical truth layer.

### Log responsibilities
A log should record:
- actual timestamp or time range
- actual values entered by the user
- optional linkage to an occurrenceId
- actual notes / actual quantities / actual metadata

### Linked vs unlinked logs

#### Linked log
A log with an `occurrenceId` means:
> this actual event satisfies that specific planned occurrence

#### Unlinked log
A log without an `occurrenceId` means:
> this actual event happened, but it was not explicitly tied to a planned occurrence

This can happen for:
- force-log flows
- manual extra events
- ad hoc user actions
- migration states during rollout

Both linked and unlinked logs are valid.

---

## 4. Projection / Merge Layer

The UI timeline, agenda, or daily screen should be treated as a **projection layer**.

Its job is not to own business truth.
Its job is to combine:
- planned occurrences
- actual logs
- domain display rules

into a single, deterministic presentation.

This merge layer is where we decide:
- what planned items still need to be shown
- what actual items should replace planned ones
- what extra actual items should appear independently

---

# The Canonical Flow

The canonical scheduling flow is:

**Template -> Occurrence -> Actual Log -> Timeline Merge**

Detailed:

1. user defines template
2. scheduling rules generate occurrences for a date
3. user logs actual event
4. log optionally links to occurrenceId
5. timeline merge reconciles planned and actual data

This is the model future systems should follow.

---

# General Scheduling Algorithm

## Step 1: Load domain templates
Load all relevant templates for the target date context.

Usually this means:
- active templates for planning
- possibly all templates for reference if historical reconciliation needs them

### Important principle
Planning should usually be driven only by **active/enabled** templates.

Historical logs, however, should still remain valid even if a template later becomes inactive.

---

## Step 2: Resolve schedule rules for the target date
For each eligible template:
- interpret recurrence rules
- interpret anchored rules
- interpret date limits
- interpret intervals
- interpret day-of-week rules
- interpret supporting context

Examples of supporting context:
- meals for supplement anchoring
- workouts for anchored activity/supplement timing
- user default event times
- imported schedule context

This step determines:
- whether the template should exist on that date
- if so, what its resolved planned time should be

---

## Step 3: Materialize occurrences
Create or load date-specific occurrences.

An occurrence should represent:
- exact date
- resolved planned time
- template linkage
- stable occurrenceId

### Why materialization matters
Materialization gives the system:
- stable identity
- reconciliation target for logs
- deterministic day-level planning
- easier debugging
- easier upsert behavior later

Without occurrences, the bridge between plan and execution is weaker.

---

## Step 4: Load actual logs
Load actual user-recorded events for the same date.

Each log may:
- link to an occurrenceId
- or remain unlinked

Both must be supported.

---

## Step 5: Build planned projection rows
Convert occurrences into planned display items.

Planned rows usually mean:
- scheduled expectation
- not yet satisfied
- display as pending / incomplete

Typical planned-row properties:
- planned time
- title
- subtitle
- occurrenceId
- completion = false

---

## Step 6: Build actual projection rows
Convert logs into actual display items.

Actual rows usually mean:
- historical truth
- actually happened
- display as completed / done

Typical actual-row properties:
- actual time
- actual notes / actual quantity
- occurrenceId if linked
- completion = true

If the log is unlinked, it still should generate a display row.

---

## Step 7: Compute satisfied occurrenceIds
Collect the occurrenceIds referenced by actual logs.

Conceptually:

```kotlin
val satisfiedOccurrenceIds =
    logs.mapNotNull { it.occurrenceId }.toSet()
```

These are the planned instances that have been fulfilled by actual logs.

---

## Step 8: Suppress satisfied planned rows
Planned rows whose occurrenceId is in the satisfied set should be removed from the pending/planned set.

Conceptually:

```kotlin
val remainingPlannedRows =
    plannedRows.filterNot { it.occurrenceId in satisfiedOccurrenceIds }
```

This is the heart of the merge algorithm.

---

## Step 9: Keep unlinked actual rows
Actual logs without occurrenceId should still appear.

These represent:
- extra events
- manual events
- ad hoc truth
- force logs

They should not be discarded just because they have no occurrence link.

---

## Step 10: Merge and sort deterministically
The final projection should combine:
- remaining planned rows
- actual rows
- other domain rows if applicable

Then sort deterministically using:
1. resolved display time
2. type order
3. stable primary identity
4. stable secondary identity

Deterministic sorting is important for:
- stable UI
- testing
- predictable recomposition
- avoiding flicker and duplicate weirdness

---

# The Core Reconciliation Rule

The general scheduling reconciliation rule is:

> A planned occurrence is replaced only when an actual log explicitly satisfies it via occurrenceId.

This means:

- same title is not enough
- same time is not enough
- same template id alone is not enough

The correct bridge is:

**occurrenceId**

---

# Why OccurrenceId Is Required

A template can recur many times.

Example:
- same supplement twice a day
- same activity morning and evening
- same meal type multiple times

If reconciliation used only template id or title, the system would not know which specific planned instance was satisfied.

OccurrenceId solves that problem by representing:

- this exact planned instance
- on this exact date
- at this resolved scheduling position

---

# Planned vs Actual Truth Model

The system should preserve this mental model:

## Planned
What the system expects or reminds

## Actual
What the user says happened

The system should never assume:
- planned means completed
- actual should mutate planning backward
- template state should erase history

Instead:

- planning produces occurrences
- logging produces truth
- merge decides what to show

---

# Active vs Historical Rules

## Active state should affect planning
Inactive templates should typically stop generating new planned occurrences.

## Active state should not erase history
Historical actual logs should still exist and still display even if the template later becomes inactive.

This distinction is critical.

---

# Manual / Extra Log Rule

The system must support actual logs that do not correspond to planned occurrences.

Examples:
- extra supplement
- spontaneous workout
- unplanned meal
- user correction after the fact

These rows should still appear in history/timeline.

They may use a synthetic timeline identity if needed for ordering/stability, but that synthetic identity is **not** a substitute for a true occurrenceId.

---

# Scheduling Rule Resolution

The scheduling algorithm should be able to support multiple scheduling styles.

## Fixed-time scheduling
Example:
- every day at 8:00 AM

## Weekly scheduling
Example:
- Monday / Wednesday / Friday at 7:00 PM

## Interval scheduling
Example:
- every 3 days

## Anchored scheduling
Example:
- 30 minutes before workout
- with breakfast
- after dinner
- before sleep

## Default-event-time fallback
Example:
- if anchor exists conceptually but exact same-day source does not, use a configured default event time

The domain-specific system decides the exact rules, but all should still resolve into:

**occurrence(date, time, occurrenceId)**

---

# Materialization Strategy

The general recommendation is:

## Materialize occurrences per date
For the requested date:
- resolve scheduling rules
- create or ensure occurrences exist
- then merge with actual logs

Benefits:
- deterministic behavior
- stable occurrenceId
- easier logging linkage
- easier debug inspection
- easier future analytics

This is especially valuable for date-focused screens like:
- Today
- Day agenda
- calendar day detail

---

# Upsert Rule for Logs

A strong recommended invariant is:

> One occurrenceId should map to at most one final actual log record in the canonical “completed occurrence” path.

This usually means:
- log insert should upsert by occurrenceId when occurrence-aware logging is intended

Benefits:
- no duplicate completion rows
- clean replacement behavior
- easier timeline merge
- stronger data integrity

Unlinked manual logs are separate and do not violate this rule.

---

# UI Projection Semantics

The merge result should preserve a meaningful distinction between planned and actual rows.

Typical semantics:

## Planned row
- completion = false
- reminder / expectation styling

## Actual row
- completion = true
- historical truth styling

This makes the timeline understandable at a glance.

---

# Deterministic Identity Rules

The system needs two kinds of identity:

## Business identity
Used for reconciliation:
- occurrenceId

## Display identity
Used for stable ordering / row keys:
- occurrenceId when available
- otherwise synthetic actual-only stable id

These should not be confused.

A synthetic display identity is useful for UI stability, but it should not be used as the business merge key.

---

# Recommended General Pseudocode

```kotlin
fun buildDailyProjection(
    templates: List<Template>,
    occurrences: List<Occurrence>,
    logs: List<ActualLog>
): List<ProjectionRow> {
    val activeTemplates = templates.filter { it.isActive }
    val templateLookup = activeTemplates.associateBy { it.id }

    val plannedRows =
        occurrences.mapNotNull { occurrence ->
            val template = templateLookup[occurrence.templateId] ?: return@mapNotNull null
            buildPlannedRow(template, occurrence)
        }

    val actualRows =
        logs.map { log ->
            buildActualRow(log)
        }

    val satisfiedOccurrenceIds =
        logs.mapNotNull { it.occurrenceId }.toSet()

    val remainingPlannedRows =
        plannedRows.filterNot { row ->
            row.occurrenceId in satisfiedOccurrenceIds
        }

    return (remainingPlannedRows + actualRows)
        .sortedWith(stableProjectionComparator())
}
```

---

# Guardrails Future Devs and Future AI Must Preserve

## 1. Do not merge by title or time alone
These are display properties, not reconciliation identities.

## 2. Do not let template state erase history
Inactive should stop future planning, not remove past truth.

## 3. Do not treat occurrence as proof of execution
Occurrence is planned intent only.

## 4. Do not discard unlinked manual logs
Manual actual events are first-class truth.

## 5. Do not collapse actual logs back into planned rows
The distinction between expectation and truth is essential.

## 6. Keep merge logic in domain/use case layer
UI should consume projection output, not invent reconciliation rules.

## 7. Keep sorting deterministic
Avoid unstable order based on incidental object ordering.

## 8. Preserve occurrenceId as the canonical bridge
If occurrence-aware logging exists, occurrenceId is the primary business merge key.

---

# How This General Blueprint Specializes Later

## Activities
This is currently the best reference implementation of:
- planned occurrence rows
- actual log rows
- occurrence-aware suppression

## Supplements
Will use the same general model, with additional anchored-timing complexity and dose-specific metadata.

## Meals
Will eventually need the same structure if they gain:
- full planned occurrences
- occurrence-linked actual meal logs
- consistent merge replacement behavior

---

# Debugging Checklist

When scheduling or merge behavior looks wrong, check in this order:

1. Was the template active?
2. Did schedule resolution say this template should occur on this date?
3. Was an occurrence materialized?
4. Did the occurrence get the expected occurrenceId?
5. Did the actual log carry that occurrenceId?
6. Did satisfiedOccurrenceIds include it?
7. Was the planned row filtered out correctly?
8. Was the actual row still included?
9. Was the final merged list sorted deterministically?

This checklist should solve most scheduling/merge defects.

---

# Short Summary

The general scheduling architecture is:

- **Template** defines reusable intent
- **Occurrence** defines one planned date-specific instance
- **Actual Log** defines historical truth
- **Merge** reconciles planned and actual using occurrenceId

The canonical rule is:

> planned rows remain visible until an actual log with the same occurrenceId satisfies them

And:

> unlinked manual actual logs still appear as real events

This is the foundation all future scheduling systems in the app suite should follow.
