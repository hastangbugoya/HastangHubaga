package com.example.hastanghubaga.domain.model.meal


enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    PRE_WORKOUT,
    POST_WORKOUT,
    CUSTOM
}

fun MealType.toEntity(): com.example.hastanghubaga.data.local.entity.meal.MealType =
    when(this) {
        MealType.BREAKFAST -> com.example.hastanghubaga.data.local.entity.meal.MealType.BREAKFAST
        MealType.LUNCH -> com.example.hastanghubaga.data.local.entity.meal.MealType.LUNCH
        MealType.DINNER -> com.example.hastanghubaga.data.local.entity.meal.MealType.DINNER
        MealType.SNACK -> com.example.hastanghubaga.data.local.entity.meal.MealType.SNACK
        MealType.PRE_WORKOUT -> com.example.hastanghubaga.data.local.entity.meal.MealType.PRE_WORKOUT
        MealType.POST_WORKOUT -> com.example.hastanghubaga.data.local.entity.meal.MealType.POST_WORKOUT
        MealType.CUSTOM -> com.example.hastanghubaga.data.local.entity.meal.MealType.CUSTOM
    }










