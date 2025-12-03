package com.example.hastanghubaga.model.entity.supplement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supplement_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = SupplementEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplementId"), Index("ingredientId")]
)
data class SupplementIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val supplementId: Long,
    val ingredientId: Long,

    // Bottle label text (optional but useful)
    val displayName: String,

    val amountPerServing: Double,
    val unit: String
)