package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.serialization.Serializable

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
    val alertOffsetMinutes: Int? = null
)