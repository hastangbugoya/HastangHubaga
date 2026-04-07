package com.example.hastanghubaga.domain.model.nutrition

import com.example.hastanghubaga.data.local.entity.user.SUCCESS_MODE_ALL

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
 * ---
 * ## Success evaluation
 *
 * A nutrition plan does NOT imply that every tracked nutrient must determine
 * whether the day is considered successful.
 *
 * That behavior is controlled by [successMode].
 *
 * Supported modes:
 * - ALL_TRACKED_GOALS
 * - ANY_TRACKED_GOAL
 * - CUSTOM_SELECTED_GOALS
 * - NONE
 *
 * Notes:
 * - goal rows and success criteria are separate concepts
 * - some nutrients may be tracked but not count toward success
 * - NONE supports tracking-only / gentle reminder behavior
 *
 * ---
 * ## Future AI/dev note
 *
 * Effective nutrient resolution across active plans happens above this model:
 * - effective min = highest min
 * - effective max = lowest max
 * - target is advisory for now
 * - if effective min > effective max, the nutrient is in conflict
 *
 * Do not drop [successMode] when mapping persistence -> domain.
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
    val successMode: String = SUCCESS_MODE_ALL,
    val createdAt: Long,
    val updatedAt: Long
)