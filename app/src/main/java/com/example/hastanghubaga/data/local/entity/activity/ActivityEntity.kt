package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.serialization.Serializable

/**
 * ActivityEntity represents the base/template definition of an activity.
 *
 * In HH architecture, this sits at the TEMPLATE layer:
 * - ActivityEntity = default definition (what)
 * - ActivityOccurrenceEntity = scheduled instance (when)
 * - ActivityLogEntity = actual execution (what actually happened)
 *
 * LOCATION MODEL (NEW):
 * This entity provides the DEFAULT location for the activity.
 *
 * Location resolution (highest → lowest priority):
 * 1. ActivityLogEntity override
 * 2. ActivityOccurrenceEntity override
 * 3. ActivityEntity (this)
 * 4. none
 *
 * DUAL SOURCE RULE:
 * Location can come from ONE of:
 * - savedAddressId (preferred, reusable location)
 * - addressAsRawString (fallback, user-entered)
 *
 * App logic MUST ensure only one is set at a time.
 *
 * DESIGN INTENT:
 * - Allow reusable favorite locations
 * - Allow raw fallback for non-mappable places
 * - Support future map-intent launching
 * - Avoid forcing strict address structure too early
 *
 * IMPORTANT:
 * This entity does NOT enforce location correctness.
 * Validation and conflict resolution are handled at the UI / use-case layer.
 *
 * FUTURE AI / DEV REMINDERS:
 * - Do NOT auto-convert raw strings into saved addresses silently
 * - Do NOT assume raw strings are mappable
 * - Consider adding lat/lng or placeId ONLY when map integration is stable
 * - If foreign keys are added later, prefer ON DELETE SET NULL
 */
@Serializable
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val type: ActivityType,

    // store epoch millis for timezone-safe history
    val startTimestamp: Long,
    val endTimestamp: Long?,

    val notes: String? = null,

    /**
     * Optional user-reported intensity for the activity.
     *
     * Convention (example, you can document this later):
     * 1 = very light
     * 5 = moderate
     * 10 = maximal effort
     *
     * Nullable because:
     * - many activities won’t have intensity
     * - historical data won’t have it
     */
    val intensity: Int? = null,

    /**
     * Gentle user-defined flag indicating that this activity should be treated
     * as a workout anchor source for supplement timing.
     */
    @ColumnInfo(defaultValue = "0")
    val isWorkout: Boolean = false,

    /**
     * Controls whether this activity participates in planning/scheduling.
     *
     * Parallels Supplement.isActive:
     * - false = hidden from planner + no occurrences generated
     * - true = eligible for scheduling + timeline
     *
     * Does NOT delete historical logs/occurrences.
     */
    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(defaultValue = "0")
    val sendAlert: Boolean = false,
    val alertOffsetMinutes: Int? = null,

    /**
     * Optional reference to a reusable saved address.
     *
     * Acts as the DEFAULT location for this activity.
     * Can be overridden by occurrence or log layers.
     *
     * IMPORTANT:
     * App logic should ensure only ONE of:
     * - savedAddressId
     * - addressAsRawString
     * is populated at a time.
     */
    val savedAddressId: Long? = null,

    /**
     * Optional raw/free-text location fallback.
     *
     * Used when no saved address is selected or when the user provides
     * a non-mappable/custom location string.
     *
     * IMPORTANT:
     * App logic should ensure only ONE of:
     * - savedAddressId
     * - addressAsRawString
     * is populated at a time.
     */
    val addressAsRawString: String? = null
)