package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ak_imported_logs")
data class AkImportedLogEntity(
    @PrimaryKey
    val stableId: String,

    val modifiedAt: Long,
    val timestamp: Long,
    val logDateIso: String,

    val mealSlot: String?,   // maps to MealType later
    val itemName: String,
    val nutrientsJson: String
)