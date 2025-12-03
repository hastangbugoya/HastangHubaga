package com.example.hastanghubaga.domain.model

import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit


data class Ingredient(
    val id: Long,
    val name: String,

    val defaultUnit: IngredientUnit,

    val rdaValue: Double? = null,
    val rdaUnit: IngredientUnit? = null,

    val upperLimitValue: Double? = null,
    val upperLimitUnit: IngredientUnit? = null,

    val category: String? = null
)