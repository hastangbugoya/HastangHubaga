package com.example.hastanghubaga.domain.model.nutrition

/**
 * Domain model for a nutrition plan in HH.
 *
 * A plan is the parent container for nutrient-level goal rows.
 * It does not directly hold per-nutrient min/target/max values.
 *
 * Multiple plans may be active at the same time.
 * Imported AK plans should live in the same system as local HH plans,
 * differentiated by source metadata rather than by separate schema.
 *
 * Future AI/dev note:
 * Effective nutrient resolution across active plans happens above this model:
 * - effective min = highest min
 * - effective max = lowest max
 * - target is advisory for now
 * - if effective min > effective max, the nutrient is in conflict
 */
data class NutritionPlan(
    val id: Long = 0L,
    val type: NutritionGoalType,
    val name: String,
    val startDate: Long,
    val endDate: Long? = null,
    val isActive: Boolean,
    val sourceType: String,
    val sourcePlanId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)