# 📱 HastangHubaga

**HastangHubaga (HH)** is a planner-first health tracking app focused on
**timeline-driven daily execution** across:

-   🧪 Supplements\
-   🏃 Activities\
-   🍽 Meals

------------------------------------------------------------------------

# 🧠 Core Philosophy

HH follows a **planner-first architecture**:

Template → Schedule → Occurrence → Log → Timeline

------------------------------------------------------------------------

# ⚙️ Architecture Overview

## Templates

-   MealEntity
-   ActivityEntity
-   SupplementEntity

## Schedules

-   Daily / Weekly / Anchored / Fixed time

## Occurrences

-   MealOccurrenceEntity
-   ActivityOccurrenceEntity
-   SupplementOccurrenceEntity

## Logs

-   MealLogEntity
-   ActivityLogEntity
-   SupplementDailyLogEntity

------------------------------------------------------------------------

# 🔁 Shared Scheduling Engine

All domains use a shared scheduling engine:

materializeXOccurrencesForDate(date)

------------------------------------------------------------------------

# ⚠️ Critical Rule

Occurrences must be materialized before they can be observed.

------------------------------------------------------------------------

# 🧩 Timeline Merge

-   Planned occurrences
-   Logged items
-   Merge by occurrenceId

------------------------------------------------------------------------

# ✅ Recent Fix

Meal occurrences were not materialized on future date open.

Fix: - Added MaterializeMealOccurrencesForDateUseCase to
TodayScreenViewModel

------------------------------------------------------------------------

# 🚀 Tech Stack

-   Kotlin
-   Jetpack Compose
-   Room
-   Hilt
-   Coroutines / Flow
