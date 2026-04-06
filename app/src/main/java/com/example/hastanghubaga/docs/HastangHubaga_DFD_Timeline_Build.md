# HastangHubaga — Data Flow Diagram (DFD)
_Date: 2026-04-06_

## Purpose

This document describes the **day timeline build flow** in **HastangHubaga (HH)** using a DFD-style view.

It focuses on how HH turns:

- reusable templates
- scheduling rules
- planned occurrences
- actual logs
- imported meal data

into the final **day timeline UI**.

This is intended as a practical architecture reference for future development across:

- Supplements
- Activities
- Meals

---

# 1. High-Level DFD

```text
┌───────────────────────┐
│     User opens day    │
│   (Today / future)    │
└──────────┬────────────┘
           │
           ▼
┌──────────────────────────────┐
│ TodayScreen / date selection │
└──────────┬───────────────────┘
           │ sends selected date
           ▼
┌─────────────────────────────────────────────┐
│ TodayScreenViewModel.materializeSelectedDate│
└───────┬───────────────────────┬─────────────┘
        │                       │
        │                       │
        ▼                       ▼
┌───────────────────┐   ┌────────────────────┐
│ Shared schedulers │   │ Imported meal read │
│ materialize day   │   │ (AK bridge)        │
└───────┬───────────┘   └──────────┬─────────┘
        │                          │
        ▼                          ▼
┌──────────────────────────────────────────────────────┐
│ Persist day snapshot rows                            │
│ supplement_occurrences / activity_occurrences /      │
│ meal_occurrences                                     │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│ Observe day data flows                               │
│ - planned occurrences                                │
│ - templates                                          │
│ - logs                                               │
│ - imported meals                                     │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│ BuildTodayTimelineUseCase                            │
│ - build planned items                                │
│ - merge logs over planned by occurrenceId            │
│ - add adhoc logs                                     │
│ - sort                                               │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│ TimelineItem list                                    │
│ domain timeline -> UI timeline models                │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌───────────────────────┐
│   Today timeline UI   │
└───────────────────────┘
```

---

# 2. Core Domain Model

HH uses this canonical pipeline:

```text
Template → Schedule → Occurrence → Log → Timeline
```

## Meaning of each layer

| Layer | Meaning | Example |
|---|---|---|
| Template | Reusable definition of a thing | MealEntity, ActivityEntity, SupplementEntity |
| Schedule | Rule for when it should happen | daily, weekly, anchored, fixed time |
| Occurrence | Planned instance for one date | MealOccurrenceEntity |
| Log | What actually happened | MealLogEntity |
| Timeline | Final merged display row | TimelineItem |

---

# 3. Main DFD by Responsibility

## External Entity: User

The user:
- opens a date
- views the timeline
- logs meals / activities / supplements
- may open future dates

This user action is the trigger for the whole day pipeline.

---

## Process A — Day Open / Date Selection

**Main entry point:**
- `TodayScreen`
- `TodayScreenViewModel`

### Input
- selected `LocalDate`

### Output
- day materialization request
- observation of date-scoped flows
- final UI state

### Important rule
When the date changes, HH must ensure planned occurrences exist for that date before expecting them to appear in the timeline.

---

## Process B — Shared Scheduling / Materialization

**Main use cases:**
- `MaterializeSupplementOccurrencesForDateUseCase`
- `MaterializeActivityOccurrencesForDateUseCase`
- `MaterializeMealOccurrencesForDateUseCase`

### Input
- target date
- schedule rules
- templates as needed
- imported data as needed

### Output
- persisted occurrence rows for that date

### DFD

```text
┌──────────────────────────┐
│ Selected date            │
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ Materialize X for date   │
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ Build planned occurrences│
│ from schedules           │
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ Replace date snapshot in │
│ occurrence repository    │
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│ occurrence table updated │
└──────────────────────────┘
```

### Storage targets
- `supplement_occurrences`
- `activity_occurrences`
- `meal_occurrences`

### Architectural intent
These tables are the canonical **planned ledger** for that date.

---

## Process C — Planned Data Observation

**Use cases / repos:**
- `GetSupplementOccurrencesForDateUseCase`
- `GetActivityOccurrencesForDateUseCase`
- `GetMealOccurrencesForDateUseCase`

### Input
- selected date

### Output
- current persisted occurrence rows for that day

### Important note
These readers **observe persisted rows**. They do not create them on their own.

That is why materialization must happen first.

---

## Process D — Template Lookup

**Used to resolve occurrence IDs back to full domain objects**

### Examples
- `GetActiveSupplementsUseCase`
- `GetActivitiesForDateUseCase`
- `GetMealsForDateUseCase`

### Purpose
Occurrences usually contain IDs and schedule-derived timing, but the UI needs full display data:
- name
- type
- dose/unit
- other metadata

So the timeline build combines:
- occurrence rows
- template/domain rows

---

## Process E — Actual Log Observation

**Examples**
- supplement dose logs
- meal logs
- activity logs

### Purpose
The log side is the canonical **actual ledger**.

These flows answer:
- what was actually taken / eaten / done
- whether a planned item should be shown as completed
- whether actual time should replace scheduled time

---

## Process F — Imported Meal Bridge

HH also reads imported meal data from AdobongKangkong.

### Source
- imported meal storage / bridge tables

### Purpose
This is separate from native HH meal scheduling.

Imported meals:
- are read into the timeline
- are considered completed
- do not replace the native occurrence architecture

---

## Process G — Timeline Build / Merge

**Main use case:**
- `BuildTodayTimelineUseCase`

This is where all observed day data is combined.

### Inputs
- supplement occurrences
- supplement templates
- supplement logs
- supplement ingredient display data
- meal occurrences
- meal templates
- meal logs
- imported meals
- activity occurrences
- activity templates
- activity logs

### Output
- sorted `List<TimelineItem>`

---

# 4. Detailed Timeline Merge DFD

## Supplements

```text
planned supplement occurrences
          +
supplement templates
          +
supplement dose logs
          +
ingredient display data
          ▼
build planned supplement rows
          ▼
remove planned rows already satisfied by logs
          ▼
append logged supplement rows
          ▼
supplement timeline items
```

## Activities

```text
planned activity occurrences
        +
activity templates
        +
activity logs
        ▼
build planned rows
        ▼
match logs by occurrenceId
        ▼
if log exists:
  replace planned with completed row
else:
  keep planned row
        ▼
append adhoc logs
        ▼
activity timeline items
```

## Meals

```text
planned meal occurrences
       +
meal templates
       +
meal logs
       ▼
build planned rows
       ▼
match logs by occurrenceId
       ▼
if log exists:
  replace planned with completed row
else:
  keep planned row
       ▼
append adhoc logs
       ▼
meal timeline items
```

## Imported Meals

```text
imported meals for date
       ▼
map directly to ImportedMealTimelineItem
       ▼
imported meal timeline items
```

---

# 5. Full Timeline Assembly DFD

```text
supplement timeline items
          +
meal timeline items
          +
imported meal timeline items
          +
activity timeline items
          ▼
combine all items
          ▼
sort by:
- time
- item type order
- stable keys
          ▼
final domain timeline
          ▼
map to UI timeline models
          ▼
render in TodayScreen
```

---

# 6. Data Stores

## Planned Stores
- `supplement_occurrences`
- `activity_occurrences`
- `meal_occurrences`

These hold the materialized planned snapshot for each date.

## Actual Stores
- supplement log table(s)
- `activity_logs`
- `meal_logs`

These hold what actually happened.

## Template Stores
- supplement table(s)
- activities table(s)
- meals table(s)

These hold reusable definitions.

## Imported Store
- imported meal table(s) from AK bridge

---

# 7. Key Invariants

## 1. Occurrences are planned, logs are actual
Do not collapse these concepts into one table.

## 2. Materialize before observe
A date-scoped occurrence observer expects persisted rows to already exist.

## 3. Merge by `occurrenceId`
Planned and actual rows reconcile through occurrence identity.

## 4. Adhoc logs remain standalone
If there is no `occurrenceId`, the item still appears as an actual row.

## 5. Timeline is built from multiple ledgers
The UI is not reading one single source table. It is reading:
- planned rows
- actual rows
- templates
- imported data

and assembling them into one timeline.

---

# 8. Recent Meal Bug: DFD Interpretation

## Symptom
Future meals did not appear when the user opened a future day.

## What was happening
The system was:
- observing meal occurrences for the selected day
- but not materializing meal occurrences first

So the meal occurrence observer had no rows to return.

## Why supplements and activities worked
They already had date materialization wired into the day-open flow.

## Root cause in DFD terms
The pipeline for meals skipped this process:

```text
Selected date
   ▼
MaterializeMealOccurrencesForDateUseCase
   ▼
Persist meal_occurrences for date
```

Without that step, the following read process returned an empty result:

```text
GetMealOccurrencesForDateUseCase
```

## Fix
Add meal materialization into the same day-open pipeline used for supplements and activities.

### Correct day-open sequence

```text
selected date
   ▼
TodayScreenViewModel.materializeSelectedDate(date)
   ├─ materializeSupplementOccurrencesForDate(...)
   ├─ materializeMealOccurrencesForDate(date)
   └─ materializeActivityOccurrencesForDate(date)
```

Result:
- future meal occurrences now exist
- timeline merge now sees them
- future meals appear correctly

---

# 9. Recommended Mental Model for Future Development

When something is missing from the timeline, debug in this order:

## Step 1 — Was the date opened correctly?
Did the selected date flow into the VM?

## Step 2 — Was the planned snapshot materialized?
Did the correct `materializeXOccurrencesForDate(date)` run?

## Step 3 — Did rows get persisted?
Does the occurrence table contain rows for that date?

## Step 4 — Are the readers observing the correct date?
Does `GetXOccurrencesForDateUseCase` return rows?

## Step 5 — Does template lookup succeed?
Can occurrence IDs resolve back to the template/domain object?

## Step 6 — Does merge logic replace planned with logged correctly?
Are occurrence IDs aligned between planned and actual?

## Step 7 — Does the UI receive the merged result?
Are `TimelineItem` rows mapped and rendered?

---

# 10. Short Reference DFD

```text
User opens date
   ▼
TodayScreen / VM
   ▼
Materialize occurrences for date
   ▼
Persist planned occurrence snapshot
   ▼
Observe:
- occurrences
- templates
- logs
- imported meals
   ▼
BuildTodayTimelineUseCase
   ▼
Merge planned + actual + imported
   ▼
Sort and map to UI
   ▼
Render timeline
```

---

# 11. Guidance for Future AI / Dev Work

## Always preserve this architecture:
- templates are reusable definitions
- schedules define recurrence
- occurrences are date-scoped planned rows
- logs are actual events
- timeline is a merged projection

## Do not shortcut the occurrence layer
If a domain is planner-first, it should appear in the timeline through occurrences.

## Reuse the shared scheduling pattern
New scheduled systems should follow the same day-open materialization pipeline.

## When adding a new domain
Add all of the following:
1. template entity
2. schedule entity / child timing rows
3. occurrence entity
4. log entity
5. materialize use case
6. get-occurrences-for-date use case
7. merge logic in `BuildTodayTimelineUseCase`

---
