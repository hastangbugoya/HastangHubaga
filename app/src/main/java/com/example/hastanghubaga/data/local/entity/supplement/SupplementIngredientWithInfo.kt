package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Embedded
import androidx.room.Relation

data class SupplementIngredientWithInfo(
    @Embedded val ingredient: SupplementIngredientEntity,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val info: IngredientEntity
)
