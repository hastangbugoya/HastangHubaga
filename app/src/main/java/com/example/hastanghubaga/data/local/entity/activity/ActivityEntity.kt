package com.example.hastanghubaga.data.local.entity.activity

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
    val intensity: Int? = null
)
