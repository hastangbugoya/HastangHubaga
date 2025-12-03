package com.example.hastanghubaga.domain.model

import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit


data class Ingredient(
    val id: Long,
    val name: String,
    val amountPerServing: Double,
    val unit: IngredientUnit,
    val rdaValue: Double?,
    val upperLimitValue: Double?
)