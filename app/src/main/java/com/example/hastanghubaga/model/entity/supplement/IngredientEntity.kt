package com.example.hastanghubaga.model.entity.supplement

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

    // Names like "Vitamin C", "Zinc", "Magnesium Glycinate"
    val name: String,

    // "mg", "mcg", "IU", "g", etc.
    val defaultUnit: String,

    // Optional: Recommended Daily Allowance (may be null)
    val rdaValue: Double? = null,

    // Unit of RDA (mg/mcg/IU/etc.) — nullable because some have no standard RDA
    val rdaUnit: String? = null,

    // Optional but useful later: tolerable upper limit (UL)
    val upperLimitValue: Double? = null,
    val upperLimitUnit: String? = null,

    // Optional: category (vitamin, mineral, herb, amino acid, probiotic)
    val category: String? = null
)