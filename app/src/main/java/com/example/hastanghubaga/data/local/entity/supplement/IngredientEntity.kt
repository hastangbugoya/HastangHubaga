package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "ingredients",
    indices = [Index(value = ["name"], unique = true)]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val defaultUnit: IngredientUnit,

    val rdaValue: Double? = null,
    val rdaUnit: IngredientUnit? = null,

    val upperLimitValue: Double? = null,
    val upperLimitUnit: IngredientUnit? = null,

    val category: String? = null
)