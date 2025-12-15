package com.example.hastanghubaga.domain.usecase.daytimeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.daytimeline.SupplementTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.TodayTimelineItem
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings

/**
 * Builds a unified, time-ordered timeline of items scheduled for "today".
 *
 * This use case is responsible for combining multiple time-based domains
 * (e.g. supplements, meals, activities) into a single list of
 * [TodayTimelineItem] instances, ordered by time-of-day.
 *
 * ### Current scope
 * - Supplements only
 * - One or more scheduled times per supplement
 *
 * ### Intended future direction
 * This use case is designed to expand to include:
 * - Meals (breakfast, lunch, dinner, snacks)
 * - Activities (workouts, walks, appointments)
 *
 * All timeline entries should:
 * - Expose a single [LocalTime] representing when the item occurs
 * - Be independent of UI concerns
 *
 * ### Design principles
 * - Pure transformation: no database access, side effects, or coroutines
 * - Deterministic output: same inputs always produce the same timeline
 * - Order-by-time as the primary concern (not item type)
 *
 * ### Checklist for adding new timeline item types
 * When introducing a new time-based domain (e.g. meals or activities):
 * - [ ] Create a new `*TimelineItem` implementing [TodayTimelineItem]
 * - [ ] Map the domain model to one or more timeline items
 * - [ ] Assign a clear [LocalTime] for each occurrence
 * - [ ] Add secondary ordering if multiple items share the same time
 * - [ ] Extend tests to cover mixed-type timelines
 *
 * ### Pitfalls to avoid
 * - Do not access repositories or DAOs inside this use case
 * - Do not branch UI logic here (this is not a presentation layer)
 * - Do not special-case supplements as the "primary" item type
 * - Do not rely on system time; all inputs must be explicit
 *
 * This use case exists to establish a stable architectural center for
 * all "today" time-based features as the application grows.
 */
class BuildTodayTimelineUseCase {
    operator fun invoke(
        supplements: List<SupplementWithUserSettings>,
        meals: List<Meal> = emptyList(),
        activities: List<Activity> = emptyList()
    ): List<TodayTimelineItem> {

        return supplements
            .flatMap { supp ->
                supp.scheduledTimes.map { time ->
                    SupplementTimelineItem(time, supp)
                }
            }
            .sortedBy { it.time }
    }
}
