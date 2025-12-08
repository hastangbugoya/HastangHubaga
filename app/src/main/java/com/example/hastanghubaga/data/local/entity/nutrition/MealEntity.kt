package com.example.hastanghubaga.data.local.entity.nutrition

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String,     // YYYY-MM-DD
    val timestamp: Long,  // exact moment eaten

    val type: MealType,
    val anchor: DoseAnchorType?, // Optional link to supplement anchors

    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val calories: Double? = null,

    val sodium: Double? = null,
    val cholesterol: Double? = null,
    val fiber: Double? = null
)

