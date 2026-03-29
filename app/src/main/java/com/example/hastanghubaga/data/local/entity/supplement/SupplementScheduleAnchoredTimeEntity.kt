package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor

@Entity(
    tableName = "supplement_schedule_anchored_times",
    foreignKeys = [
        ForeignKey(
            entity = SupplementScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class SupplementScheduleAnchoredTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val scheduleId: Long,

    /**
     * Domain anchor this occurrence is relative to.
     */
    val anchor: TimeAnchor,

    /**
     * Minutes relative to the anchor. May be negative.
     */
    val offsetMinutes: Int = 0,

    /**
     * Optional user-visible label such as "Before breakfast".
     */
    val label: String? = null,

    /**
     * Preserves user-entered ordering when multiple anchored rows exist.
     */
    val sortOrder: Int = 0
)
