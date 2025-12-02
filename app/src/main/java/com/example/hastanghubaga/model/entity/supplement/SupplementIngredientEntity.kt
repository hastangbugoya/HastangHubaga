package com.example.hastanghubaga.model.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplement_ingredients")
data class SupplementIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val supplementId: Long,                // FK to SupplementEntity

    val name: String,                      // e.g., "Magnesium (as Glycinate)"
    val amountPerServing: Double,          // per bottle serving
    val unit: String
)
