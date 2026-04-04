package com.example.hastanghubaga.domain.model.activity

import kotlinx.datetime.LocalDateTime

/**
 * Domain representation of an activity used by the planner and scheduler.
 *
 * Canonical activity model:
 *
 * - ActivityEntity (data layer) → template definition
 * - ActivityOccurrenceEntity → planned instance (date/time)
 * - ActivityLogEntity → actual performed session (NEW)
 *
 * This model represents:
 * - template-derived activity data
 * - occurrence-projected activity data (for timeline planning)
 *
 * ⚠️ IMPORTANT:
 * This model is NOT a log.
 *
 * Actual performed activity sessions must be represented by ActivityLogEntity
 * and must NOT be mapped into this model.
 *
 * Why this matters:
 * - templates generate occurrences
 * - occurrences drive planned timeline rows
 * - logs represent actual behavior
 * - logs reconcile against occurrences using occurrenceId
 *
 * 🔒 Non-negotiable:
 * This model must never be used as a storage or transport model for actual logs.
 */
data class Activity(
    val id: Long,
    val type: ActivityType,

    /**
     * For occurrences:
     * - represents planned time for the day
     *
     * For templates:
     * - may be ignored or used as a reference default
     */
    val start: LocalDateTime,

    /**
     * Planned activities typically do not have an end.
     * This may be null for most planner use cases.
     */
    val end: LocalDateTime?,

    val notes: String? = null,
    val intensity: Int? = null,

    /**
     * Indicates whether this activity should be treated as a workout anchor.
     *
     * For occurrences:
     * - this is snapshot data from ActivityOccurrenceEntity
     *
     * For templates:
     * - this is the default value
     */
    val isWorkout: Boolean = false,

    /**
     * Mirrors ActivityEntity.isActive.
     *
     * Planner contract:
     * - false = excluded from scheduling + occurrence generation
     * - true = normal behavior
     */
    val isActive: Boolean = true,

    val sendAlert: Boolean = false,
    val alertOffsetMinutes: Int? = null
)