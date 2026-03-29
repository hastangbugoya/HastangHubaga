package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.R
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.MealType
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState

/**
 * Resolves which icon should be shown for a timeline item.
 *
 * This is UI-only logic and intentionally not part of the domain layer.
 */
fun TimelineItemUiModel.icon(): Int =
    when (this) {
        is SupplementUiModel -> supplementIcon()
        is MealUiModel -> mealIcon()
        is ActivityUiModel -> activityIcon()
//        is TimelineItem.SupplementDoseLog -> doseLogIcon()
        is SupplementDoseLogUiModel -> R.drawable.badge_check
        is ImportedMealUiModel -> R.drawable.file_import
    }

private fun SupplementUiModel.supplementIcon(): Int =
    when (doseState) {
        is MealAwareDoseState.PendingMeal -> R.drawable.ic_supplement_with_food_utensils
        is MealAwareDoseState.Ready -> R.drawable.ic_supplement_ready_medicine
        is MealAwareDoseState.PendingEmptyStomach -> R.drawable.ic_supplement_no_food
        else -> R.drawable.ic_supplement_eye_alert
    }

private fun MealUiModel.mealIcon(): Int =
    when (mealType) {
        MealType.BREAKFAST -> R.drawable.ic_meal_breakfast_egg
        MealType.LUNCH -> R.drawable.ic_meal_lunch_sandwich
        MealType.DINNER -> R.drawable.ic_meal_dinner_plate_fork
        MealType.SNACK -> R.drawable.ic_meal_snack_popcorn
        MealType.PRE_WORKOUT -> R.drawable.ic_meal_preworkout_salad
        MealType.POST_WORKOUT -> R.drawable.ic_meal_postworkout_burger
        MealType.CUSTOM -> R.drawable.ic_meal_rice_bowl
    }

private fun ActivityUiModel.activityIcon(): Int =
    when (activityType) {
        ActivityType.STRENGTH_TRAINING -> R.drawable.ic_activity_strength
        ActivityType.WALKING -> R.drawable.ic_activity_walking
        ActivityType.SLEEP -> R.drawable.ic_activity_sleeping
        else -> R.drawable.ic_activity_default
    }

//private fun TimelineItem.SupplementDoseLog.doseLogIcon(): Int =
//    R.drawable.ic_supplement_avoid_caffeine
