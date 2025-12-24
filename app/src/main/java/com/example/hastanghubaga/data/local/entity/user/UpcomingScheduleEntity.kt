package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hastanghubaga.ui.timeline.TodayUiRowType

@Entity(
    tableName = "upcoming_schedule",
    indices = [
        Index("scheduledAt"),
        Index("type", "referenceId")
    ]
)
data class UpcomingScheduleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** SUPPLEMENT | MEAL | ACTIVITY */
    val type: TodayUiRowType,

    /** FK to supplementId / mealId / activityId */
    val referenceId: Long,

    /** Epoch millis (UTC) */
    val scheduledAt: Long,

    /** Optional – used by widgets & notifications */
    val title: String,

    /** Optional – e.g. “500 mg”, “30 min walk” */
    val subtitle: String?,

    /** Used to suppress alerts if already taken */
    val isCompleted: Boolean = false
)

