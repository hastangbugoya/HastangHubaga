package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ak_imported_meals")
data class AkImportedMealEntity(
    @PrimaryKey
    val groupingKey: String,   // e.g. "2026-03-27:BREAKFAST" or "single:<stableId>"

    val logDateIso: String,
    val type: MealType,
    val timestamp: Long,
    val notes: String?,

    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalSodium: Double? = null,
    val totalCholesterol: Double? = null,
    val totalFiber: Double? = null
)