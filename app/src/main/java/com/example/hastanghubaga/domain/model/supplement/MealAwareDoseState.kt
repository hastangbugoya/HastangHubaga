package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.meal.MealType


sealed class MealAwareDoseState {

    /** Dose can be taken normally */
    object Ready : MealAwareDoseState()

    /** Suggest waiting until a meal occurs */
    data class PendingMeal(
        val reason: String
    ) : MealAwareDoseState()

    /** Suggest waiting until stomach is empty */
    data class PendingEmptyStomach(
        val reason: String
    ) : MealAwareDoseState()

    /** Informational warning only */
    data class Advisory(
        val reason: String
    ) : MealAwareDoseState()

    object Unknown : MealAwareDoseState()
}

