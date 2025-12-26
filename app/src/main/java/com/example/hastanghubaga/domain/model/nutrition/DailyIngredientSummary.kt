package com.example.hastanghubaga.domain.model.nutrition

import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit

data class DailyIngredientSummary(
    val ingredientId: Long,
    val name: String,
    var amount: Double,
    val unit: IngredientUnit,
)