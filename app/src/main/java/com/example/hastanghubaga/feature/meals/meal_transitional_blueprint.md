# Meal Scheduling System Blueprint (Current Transitional Model)

**Document Version Date:** 2026-04-04

---

# Purpose

This document explains the **meal system as it exists now** in HastangHubaga, using the
activity scheduling model as the target reference.

Meals are currently in a **transitional state**:

- native HH meals can appear as planned rows
- native HH meals can be logged from HH
- imported AK meals appear as completed/read-only rows
- full occurrence-aware meal reconciliation does **not** exist yet

This document is meant to help future devs and future AI assistants:

1. understand the current meal model
2. understand how it differs from the activity model
3. evolve meals toward the canonical:

**template -> occurrence -> actual log -> merge**

---

# Current Meal Model at a Glance

The current meal system has two main visible sources:

1. **Native HH meals**
2. **Imported AK meals**

Unlike activities, meals do **not yet** have a fully materialized meal occurrence system
driving planned-vs-logged replacement.

So the current meal behavior is best understood as:

- native HH meal rows = planned-style rows
- HH meal logging = actual user-entered meal event
- imported AK meal rows = already-actual imported truth
- no full occurrence-aware suppression/replacement yet

---

# Current Data Sources

## 1. Native HH Meals

Native meals are domain meals surfaced by:

- `GetMealsForDateUseCase`

These produce:

- `TimelineItem.MealTimelineItem`

In the UI mapper, native HH meal rows are intentionally shown as:

- `isCompleted = false`

Meaning:
- these are treated like pending / planned / user-loggable rows

This is the current behavior rule.

---

## 2. Imported AK Meals

Imported meals come from:

- `GetImportedMealsForDateUseCase`

These produce:

- `TimelineItem.ImportedMealTimelineItem`

In the UI mapper, imported AK meals are intentionally shown as:

- `isCompleted = true`

Meaning:
- these rows are treated as already-actual truth
- they are imported historical meal records
- they are read-only inside HH

This is also why imported meal sheets should be read-only and show no Save button.

---

# Current UI Rule

## Native HH meal row
Tap should open:
- editable bottom sheet
- nutrition fields
- Save button

## Imported AK meal row
Tap should open:
- read-only informational bottom sheet
- no Save button

This is a hard UX rule.

---

# Current Timeline Meaning

## Native HH MealTimelineItem
These currently represent:
- meal rows HH can present as user-loggable
- effectively "planned/native meal rows"

UI semantics:
- `isCompleted = false`

## ImportedMealTimelineItem
These currently represent:
- already-completed imported meal events from AK

UI semantics:
- `isCompleted = true`

---

# What Is Missing Compared to Activities

Activities currently have the clean model:

- template
- occurrence
- log
- occurrence-aware merge

Meals do **not** fully have that yet.

Current gaps include:

1. no true `MealOccurrenceEntity` materialization flow
2. no canonical occurrenceId bridge for meal logs
3. no suppression rule that says:
   - "hide planned meal row when linked actual meal log exists"
4. current meal logging is more draft/UI oriented than full occurrence-aware reconciliation

So meals currently behave more like:

**planned-ish native rows + actual imported rows + manual meal log capture**

rather than the full canonical merge model.

---

# Current Meal Logging Flow

## Native HH meal tap

When the user taps a native HH meal row:

- Today screen sends `LogMealTapped(mealType = item.mealType)`
- ViewModel creates a `MealLogInput` draft
- draft includes:
  - `mealType`
  - `logDate`
  - `startTime`
  - optional `endTime`
  - optional notes
  - optional nutrition
  - optional `occurrenceId` (currently nullable / mostly unused)
- UI opens editable meal logging bottom sheet
- user can edit:
  - date
  - start time
  - end time
  - notes
  - macros / nutrition values
- tapping Save sends `LogMealConfirmed(updatedInput)`

The log is then saved via:

- `LogMealUseCase`

---

# Current Imported Meal Flow

When user taps imported AK meal row:

- Today screen opens read-only imported meal bottom sheet
- no editable draft
- no Save button
- no HH mutation path

This preserves:
- imported truth remains read-only in HH
- HH does not incorrectly pretend imported AK rows are locally editable planned rows

---

# Current Merge Behavior for Meals

Meals currently do **not** use the same replacement algorithm that activities use.

Instead, current meaning is simpler:

## Native HH meal rows
Shown as incomplete/loggable rows.

## Imported AK rows
Shown as completed/read-only rows.

This means that meal rows are currently **co-existing categories**, not yet a true
occurrence-aware planned-vs-actual replacement system.

That is an important distinction.

---

# Current Conceptual Model

Today’s meal system is best described as:

## A. Native HH meal rows
User-loggable HH-side meal entries shown as incomplete rows.

## B. Imported AK meal rows
Read-only completed imported meal events shown as complete rows.

This is not yet the same as:

- planned meal occurrence row
- actual meal log row
- suppression of planned row when actual linked log exists

That future model is still to be built.

---

# Current Tables / Concepts to Keep in Mind

This document does not overstate nonexistent pieces.  
Right now, the important meal-side concepts are:

- native meal domain rows used by `GetMealsForDateUseCase`
- imported meal rows used by `GetImportedMealsForDateUseCase`
- meal logging input through `MealLogInput`
- actual meal save path through `LogMealUseCase`

Unlike activities, we should **not yet pretend** there is a complete meal occurrence table flow
unless and until it is actually implemented.

---

# How Meals Should Eventually Evolve

The target model for meals should mirror activities.

---

## Future Target Meal Architecture

### 1. Meal template
Reusable HH meal definition

Possible future table:
- `meal`

### 2. Meal occurrence
Materialized planned instance for a specific date/time

Possible future table:
- `meal_occurrences`

Required fields would include:
- `occurrenceId`
- `mealId`
- `date`
- `plannedTime`
- maybe anchor / slot metadata

### 3. Actual meal log
Historical truth

Possible future table:
- canonical meal log table / occurrence-aware log path

Would need:
- `occurrenceId` nullable
- actual time
- actual nutrition
- notes

### 4. Merge rule
Same as activities:
- build planned meal rows
- build actual meal rows
- compute satisfied occurrenceIds
- suppress planned rows whose occurrenceIds are satisfied
- keep manual actual rows with no occurrenceId
- sort deterministically

---

# The Future Meal Merge Rule

When meals become fully occurrence-aware, the rule should be:

> A planned meal occurrence remains visible until an actual meal log with the same occurrenceId satisfies it.

That means:

## Scenario A: planned meal only
- show planned row
- `isCompleted = false`

## Scenario B: planned meal + linked actual meal log
- suppress planned row
- show actual row
- `isCompleted = true`

## Scenario C: actual meal log with no occurrenceId
- show actual row
- keep unrelated planned rows untouched

This is exactly the same conceptual rule as activities.

---

# Why We Are Not There Yet

The current system still needs:
- explicit meal occurrence materialization
- stable occurrence identity
- actual meal log persistence that preserves that linkage
- timeline merge logic that replaces planned meal rows with linked actual meal logs

Until those exist, meals should be described honestly as:
- native HH incomplete rows
- imported AK completed rows
- editable HH meal logging path
- no full occurrence-aware merge yet

---

# Current Hard Rules Future Devs Must Preserve

## 1. Native HH meals must be editable/loggable
Native HH meal rows should open a bottom sheet with:
- editable nutrition
- Save button

## 2. Imported AK meals must remain read-only in HH
Imported meal sheet must:
- show details only
- not show Save
- not behave like a pending HH meal row

## 3. Native HH rows start incomplete
Mapped UI rule:
- native HH meal rows = `isCompleted = false`

## 4. Imported rows start complete
Mapped UI rule:
- imported AK meal rows = `isCompleted = true`

## 5. Do not falsely claim occurrence-aware reconciliation exists yet
Until meal occurrence + linked actual suppression really exist, documentation and code
should not blur that distinction.

---

# Transitional Debug Checklist

If meal behavior looks wrong today, check:

1. Is the row native HH or imported AK?
2. Is the UI mapping setting completion correctly?
   - native HH -> false
   - imported AK -> true
3. Does native HH row tap open editable meal sheet?
4. Does imported AK row tap open read-only sheet?
5. Does native HH meal sheet show Save?
6. Does imported AK meal sheet correctly avoid Save?
7. Is `LogMealUseCase` being called for native HH meal saves?
8. Is the timeline updating after HH meal save?

---

# Recommended Next Engineering Milestones

To evolve meals into the canonical scheduling architecture, implement in this order:

## Milestone 1
Keep current UX stable:
- native HH editable
- imported AK read-only

## Milestone 2
Introduce true meal occurrence concept:
- materialized planned meal instances
- stable occurrenceId

## Milestone 3
Make meal logs occurrence-aware:
- actual meal logs can preserve occurrenceId

## Milestone 4
Teach `BuildTodayTimelineUseCase` the same replacement logic activities use:
- planned meal rows suppressed by satisfied occurrenceIds
- actual linked meal rows replace planned rows
- manual unlinked meal logs still appear

## Milestone 5
Document meal-specific merge blueprint once implemented

---

# Relationship to Activities

Activities are the current clean reference because they already implement:

- active template filtering
- materialized occurrences
- actual logs
- occurrence-aware suppression
- deterministic merge semantics

Meals should eventually converge on that same architecture.

For now, meals are still in the transitional phase described above.

---

# Short Summary

## Current reality
Meals currently behave as:

- **native HH meals** = incomplete, editable, user-loggable rows
- **imported AK meals** = completed, read-only imported truth rows

## Not yet implemented
Meals do **not yet** fully implement:

- occurrence materialization
- occurrence-aware actual log replacement
- planned suppression by satisfied occurrenceId

## Target future
Meals should eventually match the activity model:

**template -> occurrence -> actual log -> timeline merge**

That is the correct long-term destination.
