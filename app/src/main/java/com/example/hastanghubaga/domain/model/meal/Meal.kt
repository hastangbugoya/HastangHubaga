package com.example.hastanghubaga.domain.model.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType

/**
 * Domain model for a reusable meal template.
 *
 * This mirrors the activity domain model:
 *
 * - This is NOT a point-in-time event
 * - This does NOT carry a timestamp
 * - Time is determined later via scheduling
 *
 * ## Architecture
 *
 * meal (template)
 *   -> schedule (fixed or anchored)
 *   -> (future) occurrence
 *   -> (future) actual log
 *
 * ## Important
 *
 * - Do NOT reintroduce timestamp here
 * - Do NOT treat this as a "logged meal"
 * - This represents WHAT the meal is, not WHEN it happens
 */
data class Meal(
    val id: Long,
    val name: String,
    val type: MealType,
    val treatAsAnchor: MealType? = null,
    val isActive: Boolean,
    val nutrition: MealNutrition?,
    val notes: String? = null
)