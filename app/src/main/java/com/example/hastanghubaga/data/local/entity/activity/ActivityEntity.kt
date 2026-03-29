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
     *
     * This is intentionally user-controlled rather than inferred from type,
     * so something like a walk, work shift, or any custom activity can count
     * as a workout if the user wants pre/post workout reminders around it.
     */
    @ColumnInfo(defaultValue = "0")
    val isWorkout: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    val sendAlert: Boolean = false,
    val alertOffsetMinutes: Int? = null
)