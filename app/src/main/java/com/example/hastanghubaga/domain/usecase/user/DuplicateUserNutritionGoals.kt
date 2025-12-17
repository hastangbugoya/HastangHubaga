package com.example.hastanghubaga.domain.usecase.user

import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import javax.inject.Inject

class DuplicateUserNutritionGoals @Inject constructor(){
    operator fun invoke(
        goal: UserNutritionGoalsEntity,
        newName: String,
        newStartDate: Long,
        newEndDate: Long?,
        setToActive: Boolean
    ): UserNutritionGoalsEntity {
        return UserNutritionGoalsEntity(
            name = newName,
            startDate = newStartDate,
            endDate = newEndDate,
            type = goal.type,
            dailyProteinTarget = goal.dailyProteinTarget,
            dailyFatTarget = goal.dailyFatTarget,
            dailyCarbTarget = goal.dailyCarbTarget,
            dailyCalorieTarget = goal.dailyCalorieTarget,
            sodiumLimitMg = goal.sodiumLimitMg,
            cholesterolLimitMg = goal.cholesterolLimitMg,
            fiberTargetGrams = goal.fiberTargetGrams,
            isActive = setToActive
        )
    }
}