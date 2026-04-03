package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor

@Entity(
    tableName = "activity_schedule_anchored_times",
    foreignKeys = [
        ForeignKey(
            entity = ActivityScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class ActivityScheduleAnchoredTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val scheduleId: Long,

    /**
     * Domain anchor this occurrence is relative to.
     *
     * Supports:
     * - meal anchors (BREAKFAST, LUNCH, DINNER, etc.)
     * - workout anchors (BEFORE/DURING/AFTER_WORKOUT)
     * - general anchors (WAKEUP, SLEEP, etc.)
     */
    val anchor: TimeAnchor,

    /**
     * Minutes relative to the anchor. May be negative.
     */
    val offsetMinutes: Int = 0,

    /**
     * Optional user-visible label such as "Before gym" or "After lunch".
     */
    val label: String? = null,

    /**
     * Preserves user-entered ordering when multiple anchored rows exist.
     */
    val sortOrder: Int = 0
)