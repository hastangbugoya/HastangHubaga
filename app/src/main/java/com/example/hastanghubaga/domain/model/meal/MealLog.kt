package com.example.hastanghubaga.domain.model.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import kotlinx.datetime.LocalDateTime

/**
 * Domain representation of one actual logged HH meal.
 *
 * Canonical meal model:
 * - MealEntity = reusable template / definition
 * - MealOccurrenceEntity = planned occurrence for a date/time
 * - MealLogEntity = actual consumed meal
 *
 * This model represents reality, not planner intent.
 *
 * Reconciliation contract:
 * - if [occurrenceId] is non-null, this log fulfills that planned occurrence
 * - timeline builders may suppress the matching planned card and show this log
 * - if [occurrenceId] is null, this is an extra / unplanned meal log
 *
 * Snapshot rule:
 * - [mealType] is stored on the log so history remains stable even if the
 *   template later changes
 */
data class MealLog(
    val id: Long,

    /**
     * Optional reference to the template meal this log came from.
     *
     * Nullable for forward flexibility and extra/unplanned logging cases.
     */
    val mealId: Long? = null,

    /**
     * Optional planned occurrence fulfilled by this actual log.
     *
     * - non-null = planned meal was logged
     * - null = extra / unplanned meal log
     */
    val occurrenceId: String? = null,

    /**
     * Snapshot of the meal type at log time.
     */
    val mealType: MealType,

    /**
     * Actual consumption start datetime in local/domain time.
     */
    val start: LocalDateTime,

    /**
     * Actual consumption end datetime in local/domain time.
     *
     * Nullable for forward compatibility while the logging model stays minimal.
     */
    val end: LocalDateTime? = null,

    /**
     * Optional user-entered note for this actual meal.
     */
    val notes: String? = null,

    /**
     * Optional nutrition snapshot captured at log time.
     */
    val calories: Int? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val sodiumMg: Double? = null,
    val cholesterolMg: Double? = null,
    val fiberGrams: Double? = null
)