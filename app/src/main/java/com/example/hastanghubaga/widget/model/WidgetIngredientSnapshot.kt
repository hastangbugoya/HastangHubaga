package com.example.hastanghubaga.widget.model
import kotlinx.serialization.Serializable

@Serializable
data class WidgetIngredientSnapshot(
    val ingredientId: Long,
    val name: String,
    val unit: String,
    val progress: WidgetIngredientProgress?,
    val markers: WidgetIngredientMarkers?,
    val isPlaceholder: Boolean = false
)

fun placeholderIngredientSnapshot(): WidgetIngredientSnapshot =
    WidgetIngredientSnapshot(
        ingredientId = -1L,
        name = "No nutrition logged yet",
        unit = "",
        progress = null,
        markers = null,
        isPlaceholder = true
    )