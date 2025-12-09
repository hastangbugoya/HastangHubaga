package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.meal.MealType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val type: MealType,
    val timestamp: Long, // epoch millis

    val notes: String? = null
)
