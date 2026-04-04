# Activity Scheduling System (HastangHubaga) — Detailed Blueprint

## Purpose

This document describes the **activity-specific scheduling system** used in HastangHubaga.

It specializes the general scheduling blueprint into concrete:
- tables
- flows
- merge rules
- use cases

This is the **reference implementation** for:
- future meal scheduling
- supplement scheduling alignment

---

# Data Model (Tables)

## 1. ActivityEntity
Represents reusable activity templates.

Table: `activity`

Key fields:
- `id: Long`
- `type: ActivityType`
- `notes: String?`
- `isActive: Boolean`
- `isWorkout: Boolean`

---

## 2. ActivityScheduleEntity

Table: `activity_schedules`

Defines recurrence rules.

Key fields:
- `id`
- `activityId`
- `recurrenceType` (DAILY, WEEKLY, EVERY_X_DAYS)
- `interval`
- `weeklyDays`
- `startDate`
- `endDate`
- `timingType` (FIXED / ANCHORED)
- `isEnabled`

---

## 3. ActivityScheduleFixedTimeEntity

Table: `activity_schedule_fixed_times`

Defines fixed times.

- `scheduleId`
- `time`

---

## 4. ActivityScheduleAnchoredTimeEntity

Table: `activity_schedule_anchored_times`

Defines anchor-based timing.

- `scheduleId`
- `anchorType`
- `offsetMinutes`

---

## 5. ActivityOccurrenceEntity

Table: `activity_occurrences`

Materialized planned instances.

Key fields:
- `id: String` (**occurrenceId**)
- `activityId`
- `date`
- `plannedTimeSeconds`
- `isWorkout`

---

## 6. ActivityLogEntity

Table: `activity_logs`

Actual logged activities.

Key fields:
- `id`
- `activityId`
- `occurrenceId: String?`
- `startTimestamp`
- `endTimestamp`
- `notes`
- `intensity`

---

# Scheduling Flow

## Step 1 — Load Active Templates

```kotlin
activities.filter { it.isActive }
```

Inactive templates:
- DO NOT produce planned rows
- logs still remain valid

---

## Step 2 — Resolve Schedules

Use:
- recurrence rules
- fixed times
- anchored times

Result:
- determine if activity occurs on date
- resolve planned time

---

## Step 3 — Materialize Occurrences

Use:
`MaterializeActivityOccurrencesForDateUseCase`

Create:
```
ActivityOccurrenceEntity(
    id = occurrenceId,
    activityId,
    date,
    plannedTimeSeconds
)
```

---

## Step 4 — Load Logs

Use:
`ActivityLogRepository.observeActivityLogsForDate(date)`

---

# Timeline Merge (Core Logic)

Use:
`BuildTodayTimelineUseCase`

---

## Step 5 — Build Planned Rows

```kotlin
TimelineItem.ActivityTimelineItem(
    time = plannedTime,
    occurrenceId = occurrence.id,
    isCompleted = false
)
```

---

## Step 6 — Build Actual Rows

```kotlin
TimelineItem.ActivityTimelineItem(
    time = actualTime,
    occurrenceId = log.occurrenceId ?: syntheticId,
    isCompleted = true
)
```

---

## Step 7 — Compute Satisfaction

```kotlin
val satisfied =
    logs.mapNotNull { it.occurrenceId }.toSet()
```

---

## Step 8 — Suppress Planned

```kotlin
planned.filterNot { it.occurrenceId in satisfied }
```

---

## Step 9 — Merge

```
final = plannedRemaining + actual
```

---

## Step 10 — Sort

Sort by:
1. time
2. type
3. stable keys

---

# Merge Behavior Summary

| Scenario | Result |
|--------|-------|
| Planned only | show planned |
| Planned + logged (linked) | show logged only |
| Logged only (no occurrenceId) | show logged |
| Template inactive | hide future planned, keep logs |

---

# OccurrenceId Role

- Primary reconciliation key
- Links planned → actual
- Enables replacement

---

# Upsert Rule

For logs with occurrenceId:
- enforce 1 log per occurrence
- use upsert behavior

---

# UI Semantics

| Type | isCompleted |
|------|------------|
| Planned | false |
| Logged | true |

---

# Key Principles

1. Occurrence is NOT truth
2. Log is truth
3. occurrenceId is the bridge
4. Merge replaces planned with actual
5. Manual logs always appear

---

# Debug Checklist

1. Is activity active?
2. Was schedule resolved?
3. Was occurrence created?
4. Does log have occurrenceId?
5. Is occurrence suppressed?
6. Is actual row present?

---

# Summary

Activity scheduling follows:

Template → Occurrence → Log → Merge

This is the **reference system** for all future scheduling implementations.
