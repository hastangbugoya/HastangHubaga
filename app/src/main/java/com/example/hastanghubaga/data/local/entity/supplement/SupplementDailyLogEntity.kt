package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a historical record of a user's supplement intake for a specific day.
 * Each row captures *one logged event* of taking a supplement — including fractional
 * servings, timestamp, and the unit used at that moment.
 *
 * This table allows the app to:
 * - Track what supplements were actually taken versus scheduled.
 * - Compute daily summaries (e.g., ingredient totals, streaks, compliance rates).
 * - Preserve historical accuracy even if supplement dose units change later.
 *
 * ## Fields
 *
 * @property id Auto-generated primary key.
 *
 * @property supplementId Foreign key reference to `SupplementEntity.id`. Identifies
 * which supplement the user logged as taken.
 *
 * @property date A string representation of the day the dose applies to.
 * Typically formatted as `YYYY-MM-DD` or stored as epoch-day. Multiple logs
 * on the same day are allowed.
 *
 * @property actualServingTaken The amount the user consumed for this log entry.
 * Supports fractional servings (e.g., 0.5, 1.25).
 *
 * @property doseUnit The supplement’s unit at the moment of logging.
 * This ensures logs remain valid even if the supplement's unit changes later.
 *
 * @property timestamp Unix epoch milliseconds representing when the user actually
 * recorded this log. Useful for ordering logs and showing activity history.
 *
 * ## Notes
 * - This table powers daily intake calculations, ingredient aggregation, and streaks.
 * - If multiple logs exist for the same supplement on the same day,
 * they should be aggregated for daily nutrition summaries.
 * - Not all logs must correspond to scheduled doses; users may manually record extra doses.
 *
 * @see SupplementEntity
 * @see SupplementDoseUnit
 */
@Serializable
@Entity(tableName = "supplement_daily_log")
data class SupplementDailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val supplementId: Long,

    val date: String,
    val actualServingTaken: Double,
    val doseUnit: SupplementDoseUnit,

    val timestamp: Long
)

