# Daily Timeline System – Developer Onboarding Guide

## Overview

The Today timeline is built by combining multiple domain sources into a single chronological list:

- Supplements
- Activities
- Meals
- Imported meals (AK)

This process is orchestrated primarily by:

- `TodayScreenViewModel`
- `BuildTodayTimelineUseCase`

---

# 1. High-Level Pipeline

```
TodayScreenViewModel.observeTimeline()

→ combine(
    getSupplementsForDate(date),
    getMealsForDate(date),
    getImportedMealsForDate(date),
    getActivitiesForDate(date)
)

→ BuildTodayTimelineUseCase

→ List<TimelineItem>

→ TimelineMapper → TimelineItemUiModel

→ UI Render
```

---

# 2. Supplements – How They Enter the Timeline

## Source

```
GetSupplementsWithUserSettingsForDateUseCase
```

Returns:
```
List<SupplementWithUserSettings>
```

## Step 1 – Determine if supplement is active

Inside repository:

- `schedule.isEnabled`
- `schedule.isActiveOn(date)`

## Step 2 – Convert schedule → times

From:

```
SupplementScheduleSpec
```

Types:

### Fixed Times
```
08:00, 13:00, 20:00
```

### Anchored
```
BEFORE_WORKOUT (+ offset)
AFTER_WORKOUT
```

## Step 3 – Produce timeline rows

```
scheduledTimes.map { time ->
    TimelineItem.SupplementTimelineItem(...)
}
```

Each time = ONE row

---

# 3. Activities – How They Enter the Timeline

## Source

```
GetActivitiesForDateUseCase
```

Returns:
```
List<Activity>
```

## Step 1 – Activities are already concrete

No schedule expansion needed.

Each activity has:

- start time
- optional end time

## Step 2 – Timeline conversion

```
TimelineItem.ActivityTimelineItem(
    time = activity.start.time
)
```

---

# 4. Meals – How They Enter the Timeline

## Source

```
GetMealsForDateUseCase
GetImportedMealsForDateUseCase
```

## Step 1 – Time resolution

Meals use:

1. Logged timestamp (primary)
2. Fallback anchor (future use)

## Step 2 – Timeline rows

Native meals:

```
TimelineItem.MealTimelineItem
```

Imported meals:

```
TimelineItem.ImportedMealTimelineItem
```

---

# 5. Time Resolution Rules (Important)

Each type determines time differently:

| Type        | Time Source |
|------------|------------|
| Supplement | Schedule or anchor |
| Activity   | start time |
| Meal       | logged timestamp |
| Imported   | stored timestamp |

---

# 6. Timeline Merge Process

Inside:

```
BuildTodayTimelineUseCase
```

### Step 1 – Build independent lists

```
supplementItems
activityItems
mealItems
importedMealItems
```

### Step 2 – Combine

```
val allItems =
    supplementItems +
    activityItems +
    mealItems +
    importedMealItems
```

### Step 3 – Sort

```
allItems.sortedBy { it.time }
```

---

# 7. Current Behavior (Important)

✔ ALL items are included  
✔ No filtering  
✔ No deduplication  
✔ No occurrence system yet  

This means:

- Multiple supplements show as multiple rows
- Activities always show
- Meals always show

---

# 8. Known Limitation (Current System)

Supplements are:

❌ Schedule-based  
❌ NOT occurrence-based  

Meaning:

- No unique identity per dose
- Cannot reconcile logs cleanly
- Extra doses are separate log rows

---

# 9. Future Direction (Already In Progress)

You are moving toward:

## Occurrence-based system

```
Schedule → Occurrence → Log
```

### Benefits:

- Multiple daily doses
- Extra doses become timeline items
- One-to-one log linkage
- Stable identity

---

# 10. Key Takeaways

- Timeline is a **merged view of multiple independent domains**
- Supplements are currently **expanded from schedules**
- Activities & meals are **already concrete**
- Merge is **simple + sorted**
- Future system will be **occurrence-driven**

---

# 11. Mental Model

Think of timeline as:

```
[Generate domain items]
        ↓
[Normalize to TimelineItem]
        ↓
[Merge all sources]
        ↓
[Sort by time]
        ↓
[Render UI]
```

---

# 12. Where to Look in Code

Core files:

- `TodayScreenViewModel.kt`
- `BuildTodayTimelineUseCase.kt`
- `TimelineMapper.kt`
- `TimelineItem.kt`

Supplement-specific:

- `GetSupplementsWithUserSettingsForDateUseCase.kt`
- `SupplementRepositoryImpl.kt`

---

---

# 13. Design Philosophy

The timeline is intentionally designed as a **composition layer**, not a source of truth.

Key principles:

- Each domain (supplements, meals, activities) owns its own data
- The timeline does not mutate or persist data
- The timeline is a **derived view**
- All items are treated equally once converted to `TimelineItem`

This allows:

- independent evolution of each domain
- simple merging logic
- minimal coupling between features

---

# 14. Current vs Future Model

## Current (v1)
- Supplements are schedule-based
- Timeline rows are derived from schedules
- Logs are separate and loosely linked

## Future (v2 – in progress)
- Supplements become occurrence-based
- Each timeline row represents a concrete occurrence
- Logs link directly to occurrences
- Extra doses create new occurrences

This document intentionally reflects the **current system**, not the final one.
# End
