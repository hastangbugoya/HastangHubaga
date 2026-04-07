package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType

/**
 * Represents a nutrition plan in HH.
 *
 * A plan is a container for nutrient-level constraints (see [NutrientGoalEntity]).
 *
 * This entity is intentionally focused on:
 * - identity
 * - lifecycle
 * - activation state
 * - source metadata (local vs imported)
 *
 * It does NOT contain nutrient values.
 *
 * ---
 * ## Key design decisions
 *
 * ### 1. Multi-plan + multi-active support
 * - Multiple plans can exist simultaneously
 * - Multiple plans can be active at the same time
 * - Conflict resolution happens at the domain layer (NOT here)
 *
 * ### 2. Source-aware storage
 * - Imported AK plans are stored in the SAME table as local plans
 * - Differentiated using [sourceType] and [sourcePlanId]
 *
 * ### 3. No "single active plan" assumption
 * - We intentionally DO NOT enforce only one active plan
 *
 * ### 4. Time-bound plans
 * - startDate / endDate allow historical and phased plans
 *
 * ---
 * ## Future AI/dev note
 * Effective nutrient ranges are computed across ACTIVE plans:
 * - effective min = highest min
 * - effective max = lowest max
 * - conflicts when min > max
 */
@Entity(tableName = "nutrition_plan")
data class UserNutritionPlanEntity(

    /**
     * Auto-generated primary key.
     *
     * IMPORTANT:
     * We no longer force a single plan (no id = 1L).
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Classification of the plan (cut, bulk, etc.).
     */
    val type: NutritionGoalType,

    /**
     * Human-readable name.
     */
    val name: String,

    /**
     * Plan start date (epoch millis).
     */
    val startDate: Long,

    /**
     * Optional end date (null = ongoing).
     */
    val endDate: Long? = null,

    /**
     * Whether this plan is currently active.
     *
     * NOTE:
     * Multiple plans may be active simultaneously.
     */
    val isActive: Boolean,

    /**
     * Source type of the plan.
     *
     * Suggested values:
     * - "LOCAL"
     * - "AK_IMPORTED"
     *
     * Kept as String for flexibility and forward compatibility.
     */
    val sourceType: String,

    /**
     * External source identifier (e.g., AK plan ID).
     *
     * Null for local plans.
     */
    val sourcePlanId: String? = null,

    /**
     * Creation timestamp (epoch millis).
     */
    val createdAt: Long,

    /**
     * Last updated timestamp (epoch millis).
     */
    val updatedAt: Long
)