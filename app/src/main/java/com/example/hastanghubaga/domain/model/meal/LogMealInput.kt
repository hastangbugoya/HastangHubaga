package com.example.hastanghubaga.domain.model.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.time.TimeUseIntent
import kotlinx.datetime.LocalTime

data class LogMealInput(
    /**
     * Optional template linkage.
     *
     * This should be populated when the logged meal originated from a known
     * HH meal template, including:
     * - tapping a planned/native HH meal row
     * - force-logging an HH meal from the Today screen picker
     *
     * This stays nullable so truly ad-hoc/manual meal logs can still exist even
     * when they are not tied to a specific MealEntity.
     */
    val mealId: Long? = null,

    /**
     * Optional linkage to a specific planned meal occurrence.
     *
     * Non-null means this log fulfills one concrete planner-generated meal
     * occurrence. Null means this is an ad-hoc / extra / force-logged meal.
     */
    val occurrenceId: String? = null,

    val mealType: MealType,
    val timeUseIntent: TimeUseIntent,

    /**
     * Optional actual end time for the meal event on the same local day as the
     * resolved [timeUseIntent] date.
     *
     * Current use:
     * - persisted into MealLogEntity.endTimestamp when provided
     *
     * This remains nullable because the minimum valid meal log is still
     * essentially "I ate this meal at this time."
     */
    val endTime: LocalTime? = null,

    val notes: String? = null,

    /**
     * Optional nutrition captured at log time.
     *
     * If null, the meal log still exists as an actual meal event but carries no
     * explicit nutrition payload.
     */
    val nutrition: NutritionInput? = null
)

data class NutritionInput(
    val calories: Int? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val sodiumMg: Double? = null,
    val cholesterolMg: Double? = null,
    val fiberGrams: Double? = null
)