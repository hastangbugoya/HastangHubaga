package com.example.hastanghubaga.model.entity.supplement

import androidx.room.Embedded
import androidx.room.Relation

data class SupplementWithIngredients(
    @Embedded val supplement: SupplementEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "supplementId",
        entity = SupplementIngredientEntity::class
    )
    val ingredients: List<SupplementIngredientWithInfo>
)