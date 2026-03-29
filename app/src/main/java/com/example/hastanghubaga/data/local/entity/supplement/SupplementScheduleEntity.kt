package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "supplement_schedules",
    foreignKeys = [
        ForeignKey(
            entity = SupplementEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplementId")]
)
data class SupplementScheduleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val supplementId: Long,

    // -------------------------
    // Recurrence
    // -------------------------
    val recurrenceType: ScheduleRecurrenceType,
    val interval: Int,

    val weeklyDays: List<DayOfWeek>?,

    // -------------------------
    // Window
    // -------------------------
    val startDate: LocalDate,
    val endDate: LocalDate?,

    // -------------------------
    // Timing type
    // -------------------------
    val timingType: ScheduleTimingType,

    // -------------------------
    // State
    // -------------------------
    val isEnabled: Boolean = true
)

enum class ScheduleRecurrenceType {
    DAILY,
    WEEKLY
}

enum class ScheduleTimingType {
    FIXED,
    ANCHORED
}
