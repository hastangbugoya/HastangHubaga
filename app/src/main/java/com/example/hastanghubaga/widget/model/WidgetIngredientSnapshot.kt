package com.example.hastanghubaga.widget.model
import kotlinx.serialization.Serializable

@Serializable
data class WidgetIngredientSnapshot(
    val ingredientId: Long,
    val name: String,
    val unit: String,
    val progress: WidgetIngredientProgress?,
    val markers: WidgetIngredientMarkers?
)
