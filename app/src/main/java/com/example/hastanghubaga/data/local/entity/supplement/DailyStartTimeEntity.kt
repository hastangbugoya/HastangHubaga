package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Represents the user's "start of the day" anchor for a specific calendar date.
 *
 * This value determines how dose scheduling is calculated. For example,
 * if `hourZero = 3600`, then the day is treated as beginning at 1:00 AM,
 * and all dose-time offsets (e.g., +30 minutes from MIDNIGHT) are added
 * relative to this value.
 *
 * ## Why this exists
 * Some users wake up late, work night shifts, or otherwise have non-midnight
 * daily cycles. Instead of recomputing the app's global schedule logic, this
 * table stores the day's base timestamp — allowing dose anchors (MIDNIGHT,
 * WAKEUP, BEFORE_WORKOUT, etc.) to remain consistent across days.
 *
 * ## Notes
 * - `date` is the primary key and uses ISO-8601 format (`YYYY-MM-DD`).
 * - `hourZero` is the number of seconds after midnight when the app
 *   considers the "start" of the user's day.
 * - This table normally stores **one entry per day** and is seeded at
 *   database creation.
 *
 * @property date The calendar date in ISO-8601 format. Serves as the PK.
 * @property hourZero Number of seconds past midnight representing the start
 * of the user’s day (e.g., 0 = midnight, 3600 = 1:00 AM).
 */
@Entity(tableName = "daily_start_time")
data class DailyStartTimeEntity(
    @PrimaryKey val date: String,
    val hourZero: Int
)

