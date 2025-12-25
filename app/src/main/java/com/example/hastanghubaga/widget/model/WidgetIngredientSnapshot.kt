package com.example.hastanghubaga.widget.model

data class WidgetIngredientSnapshot(
    val ingredientId: Long,
    val name: String,
    val unit: String,
    val progress: WidgetIngredientProgress,
    val markers: WidgetIngredientMarkers?
)
