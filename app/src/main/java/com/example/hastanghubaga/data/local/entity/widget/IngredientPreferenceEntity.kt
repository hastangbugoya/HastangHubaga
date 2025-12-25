package com.example.hastanghubaga.data.local.entity.widget

import androidx.room.Entity
import androidx.room.Index
import java.time.Instant

@Entity(
    tableName = "ingredient_preferences",
    primaryKeys = ["ingredientId"],
    indices = [
        Index(value = ["ingredientId"])
    ]
)
data class IngredientPreferenceEntity(
    val ingredientId: Long,
    val isFavorite: Boolean = false,
    val updatedAt: Instant = Instant.now()
)
