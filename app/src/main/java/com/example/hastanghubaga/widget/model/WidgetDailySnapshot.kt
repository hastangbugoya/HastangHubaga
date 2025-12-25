package com.example.hastanghubaga.widget.model

data class WidgetDailySnapshot(
    val schemaVersion: Int,
    val generatedAt: String,
    val day: String,
    val summary: WidgetDailySummary,
    val upNext: UpNextSnapshot?,           // ← nullable by design
    val ingredients: List<WidgetIngredientSnapshot>
)