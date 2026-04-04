# BuildTodayTimelineUseCase: Activity Planned vs Logged Merge

## Purpose

This document describes the **current activity merge behavior** implemented inside `BuildTodayTimelineUseCase`.

It is intended to be the reference blueprint for future work on:
- meals
- supplements
- any other domain that follows the pattern:

**template -> occurrence -> actual log -> timeline reconciliation**

This document is intentionally focused on **activities only**.

---

## Canonical Activity Model

The activity system currently has three important layers for timeline purposes:

### 1. Activity template
A reusable HH activity definition.

Examples:
- Sleep
- Walk
- Lift
- Run

Important note:
- only **active** activity templates are allowed to produce **planned** timeline rows

This is enforced in the use case by filtering:

```kotlin
val activeActivities = activities.filter { it.isActive }
val activityLookup = activeActivities.associateBy { it.id }
```

So:
- inactive activity templates do **not** create planned timeline rows
- historical logs may still appear even if the template later becomes inactive

---

### 2. Planned occurrence
A materialized, date-specific plan row.

Source input:
- `activityOccurrences: List<ActivityOccurrenceEntity>`

Each occurrence represents:
- a specific activity
- on a specific date
- at a specific planned time
- with a stable `occurrenceId`

The planned timeline row is created as:

```kotlin
TimelineItem.ActivityTimelineItem(
    time = plannedTime,
    occurrenceId = occurrence.id,
    activityId = activity.id,
    title = activity.type.toDisplayLabel(),
    subtitle = activity.notes,
    isWorkout = occurrence.isWorkout,
    scheduledTime = plannedTime,
    isCompleted = false
)
```

Important properties of planned rows:
- `occurrenceId = occurrence.id`
- `isCompleted = false`
- time comes from `occurrence.plannedTimeSeconds`

---

### 3. Actual logged activity
What the user says actually happened.

Source input:
- `activityLogs: List<ActivityLog>`

Actual timeline rows are created as:

```kotlin
TimelineItem.ActivityTimelineItem(
    time = actualTime,
    occurrenceId = log.occurrenceId ?: buildActualActivityTimelineId(
        activityId = log.activityId,
        time = actualTime
    ),
    activityId = log.activityId ?: -1L,
    title = log.activityType.toDisplayLabel(),
    subtitle = log.notes,
    isWorkout = false,
    scheduledTime = actualTime,
    isCompleted = true
)
```

Important properties of actual rows:
- `time = log.start.time`
- `isCompleted = true`
- if the log is linked to a planned occurrence, it keeps that same `occurrenceId`
- if the log has no occurrenceId, a synthetic actual-only timeline id is built

---

## The Merge Rule

The current activity merge is **occurrence-aware**.

### The rule:
- show planned activity rows **unless** their `occurrenceId` has already been satisfied by a linked activity log
- always show actual logged rows
- actual logs with no `occurrenceId` are treated as manual / extra / ad hoc actual events

In practice, the use case computes:

```kotlin
val satisfiedPlannedActivityOccurrenceIds =
    activityLogs
        .mapNotNull { it.occurrenceId }
        .toSet()
```

Then suppresses planned rows whose occurrenceId is in that set:

```kotlin
val filteredPlannedActivityItems =
    plannedActivityItems.filterNot { item ->
        item.occurrenceId in satisfiedPlannedActivityOccurrenceIds
    }
```

This means the merge identity is:

**planned occurrence <-> actual log**
matched by:
**`occurrenceId`**

---

## Behavior by Scenario

## Scenario A: planned activity exists, no log exists

Example:
- Sleep planned for 11:00 PM
- no one has logged it yet

Result:
- timeline shows the planned activity row
- `isCompleted = false`

The planned row remains visible because its occurrenceId is **not** in the satisfied set.

---

## Scenario B: planned activity exists, linked log exists

Example:
- Sleep planned for 11:00 PM with occurrenceId `occ_123`
- user logs the activity and the log also carries occurrenceId `occ_123`

Result:
- planned row is suppressed
- actual logged row is shown
- actual row is `isCompleted = true`

This is the core replacement behavior.

The user sees:
- what actually happened
- not both planned and actual for the same occurrence

---

## Scenario C: actual log exists with no occurrenceId

Example:
- user manually logs a walk that was never planned
- or a force-log path creates a real log not linked to any occurrence

Result:
- actual row is shown
- no planned row is suppressed unless there is also a matching occurrenceId
- this behaves as an extra/manual event

This is why actual rows generate a fallback synthetic id when `log.occurrenceId` is null:

```kotlin
buildActualActivityTimelineId(
    activityId = log.activityId,
    time = actualTime
)
```

That synthetic id is only for timeline row stability.
It is **not** a reconciliation key with planned rows.

---

## Scenario D: activity template becomes inactive later

Example:
- the user previously logged an activity
- then later disables or deactivates that activity template

Result:
- future planned rows should stop appearing
- historical logs still appear

This is because:
- planned rows are built only from `activeActivities`
- actual rows are built from `activityLogs`

This is an important guardrail:
**planning is controlled by template state; historical truth is controlled by logs**

---

## Current Algorithm in Order

The current flow inside `BuildTodayTimelineUseCase` is:

### Step 1: filter active activity templates
Only active templates participate in planned-row construction.

```kotlin
val activeActivities = activities.filter { it.isActive }
val activityLookup = activeActivities.associateBy { it.id }
```

---

### Step 2: build planned activity timeline rows
For each `ActivityOccurrenceEntity`:
- find its active template
- resolve planned time from `plannedTimeSeconds`
- create a `TimelineItem.ActivityTimelineItem`
- mark it `isCompleted = false`

```kotlin
val plannedActivityItems =
    activityOccurrences.mapNotNull { occurrence ->
        val activity = activityLookup[occurrence.activityId] ?: return@mapNotNull null
        val plannedTime = LocalTime.fromSecondOfDay(occurrence.plannedTimeSeconds)

        TimelineItem.ActivityTimelineItem(
            time = plannedTime,
            occurrenceId = occurrence.id,
            activityId = activity.id,
            title = activity.type.toDisplayLabel(),
            subtitle = activity.notes,
            isWorkout = occurrence.isWorkout,
            scheduledTime = plannedTime,
            isCompleted = false
        )
    }
```

---

### Step 3: build actual logged activity rows
For each `ActivityLog`:
- use `log.start.time` as actual timeline time
- preserve `log.occurrenceId` if present
- otherwise build a synthetic actual-only id
- mark row `isCompleted = true`

```kotlin
val actualActivityItems =
    activityLogs.map { log ->
        val actualTime = log.start.time

        TimelineItem.ActivityTimelineItem(
            time = actualTime,
            occurrenceId = log.occurrenceId ?: buildActualActivityTimelineId(
                activityId = log.activityId,
                time = actualTime
            ),
            activityId = log.activityId ?: -1L,
            title = log.activityType.toDisplayLabel(),
            subtitle = log.notes,
            isWorkout = false,
            scheduledTime = actualTime,
            isCompleted = true
        )
    }
```

---

### Step 4: compute satisfied planned occurrence ids
Only logs that actually carry an occurrenceId participate in suppression.

```kotlin
val satisfiedPlannedActivityOccurrenceIds =
    activityLogs
        .mapNotNull { it.occurrenceId }
        .toSet()
```

---

### Step 5: suppress satisfied planned rows
Any planned row whose occurrenceId is satisfied is removed.

```kotlin
val filteredPlannedActivityItems =
    plannedActivityItems.filterNot { item ->
        item.occurrenceId in satisfiedPlannedActivityOccurrenceIds
    }
```

---

### Step 6: merge with other timeline content
Activities are merged into the final timeline with:
- filtered planned activity rows
- actual activity rows

In the current merged list:

```kotlin
val merged = (
        filteredPlannedSupplementItems +
                supplementDoseLogItems +
                mealItems +
                importedMealItems +
                filteredPlannedActivityItems +
                actualActivityItems
        ).sortedWith(
        compareBy<TimelineItem> { it.time }
            .thenBy { itemTypeSortOrder(it) }
            .thenBy { itemStablePrimaryKey(it) }
            .thenBy { itemStableSecondaryKey(it) }
    )
```

---

## Meaning of `isCompleted` for Activities

The activity merge uses `isCompleted` as a UI-facing state signal.

### Planned activity row
- `isCompleted = false`

Meaning:
- this is still a planned reminder / expectation
- no linked actual log has satisfied it yet

### Actual logged activity row
- `isCompleted = true`

Meaning:
- this row represents historical truth
- the user says this activity happened

This gives the activity system a very clean red vs green distinction in the UI.

---

## Why OccurrenceId Is the Correct Merge Key

The merge algorithm relies on occurrenceId because it is the only stable business identity for:

**this exact planned instance on this exact day**

Using time or title alone would be weaker and eventually wrong.

### Why not match on time?
Because:
- users can log late
- users can log early
- users can edit times
- two different planned occurrences can share similar times

### Why not match on activityId alone?
Because:
- the same activity template can occur multiple times in one day
- one template is not the same thing as one occurrence

### Why occurrenceId works
Because it identifies:
- the specific planned row
- on that specific date
- for that specific instance

This is the correct bridge between planning and logging.

---

## Synthetic Actual IDs

When a log has no occurrenceId, the code builds a synthetic row identity:

```kotlin
private fun buildActualActivityTimelineId(
    activityId: Long?,
    time: LocalTime
): String {
    return buildString {
        append("actual")
        append("|")
        append(activityId ?: "none")
        append("|")
        append(time.toSecondOfDay())
    }
}
```

This is **not** a merge identity.
It is a **timeline row identity / stability helper**.

Purpose:
- keep actual manual rows stable enough for deterministic ordering
- avoid null identity on actual-only rows

It should not be treated as equivalent to a true occurrenceId.

---

## Sorting Policy After Merge

After planned suppression and actual-row construction, all items are sorted by:

1. resolved timeline time
2. cross-type order
3. stable primary identity key
4. stable secondary identity key

For activities specifically, same-time ordering uses stable business meaning instead of hashes.

### Activity primary key
```kotlin
buildString {
    append(item.activityId)
    append("|")
    append(item.occurrenceId)
    append("|")
    append(item.scheduledTime.toSecondOfDay())
}
```

### Activity secondary key
```kotlin
buildString {
    append(item.title)
    append("|")
    append(item.time.toSecondOfDay())
    append("|")
    append(if (item.isWorkout) "W" else "N")
    append("|")
    append(if (item.isCompleted) "C" else "P")
}
```

This makes the final timeline deterministic.

---

## Guardrails Future Devs Must Preserve

### 1. Planned and actual are not the same thing
Do not collapse activity logs back into planned rows.
The system intentionally preserves:
- planned expectation
- actual user-declared truth

### 2. Suppression is occurrence-aware
Do not switch back to heuristic suppression like:
- same title
- same time
- same activity id only

The current correct rule is:
**planned suppression happens only when an actual log carries the same occurrenceId**

### 3. Actual logs remain historical truth
Do not suppress actual logs because a template is inactive.
Inactive should stop future planning, not erase history.

### 4. Manual logs must still appear
If a log has no occurrenceId, it should still produce an actual row.

### 5. `isCompleted` must remain semantically meaningful
For activities:
- planned = false
- actual = true

Do not blur this distinction.

---

## Blueprint for Meals and Supplements Later

The activity path is the cleanest current example of the canonical merge model.

The reusable architecture is:

### Template layer
Reusable user-authored definition

### Occurrence layer
One planned instance for one date/time

### Log layer
Actual user-declared event

### Merge layer
- build planned rows
- build actual rows
- collect satisfied occurrenceIds from actual logs
- suppress planned rows whose occurrenceIds are satisfied
- keep manual actual rows that have no occurrenceId
- sort deterministically

This is the model that meals should eventually adopt if they gain real occurrence-aware planning.

Supplements already use the same conceptual pattern, but activities are currently the best reference for:
- explicit planned row construction
- explicit actual row construction
- explicit occurrence-aware suppression
- explicit UI completed-state semantics

---

## Short Reference Summary

### Planned activity row
- source: `ActivityOccurrenceEntity`
- identity: `occurrence.id`
- completion: false

### Actual activity row
- source: `ActivityLog`
- identity: `log.occurrenceId` when present, otherwise synthetic actual id
- completion: true

### Suppression rule
- suppress planned row when its `occurrenceId` appears in activity logs

### Keep actual manual rows?
- yes

### Keep historical logs when template becomes inactive?
- yes

### Keep inactive templates from creating new planned rows?
- yes

---

## Practical Mental Model

A good mental model is:

- **template** says what can happen repeatedly
- **occurrence** says what was expected today
- **log** says what actually happened
- **timeline merge** decides what the user should see now

And the activity system’s current answer is:

- if an occurrence has not been logged, show the planned row
- if an occurrence has been logged, show the actual row instead
- if something was logged manually with no occurrence, show it as an extra actual event
