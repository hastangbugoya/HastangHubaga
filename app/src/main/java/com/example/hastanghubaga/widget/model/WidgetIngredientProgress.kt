package com.example.hastanghubaga.widget.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetIngredientProgress(
    val current: Double,
    val target: Double?,
    val percent: Double?,
    val status: WidgetIngredientProgressType?,    // UNDER / ON_TRACK / EXCEEDED
    val exceeded: Boolean?
)
