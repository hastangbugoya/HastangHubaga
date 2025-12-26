package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType

/**
 * Represents a high-level user-defined nutrition strategy or plan.
 *
 * A nutrition plan defines *intent*, not consumption or progress.
 * It acts as a container for a set of nutrient-level goals
 * (see [NutrientGoalEntity]).
 *
 * Examples of plans:
 * - "Cut"
 * - "Maintenance"
 * - "Bulk"
 * - "Low Sodium"
 *
 * This entity is intentionally minimal:
 * - It does NOT contain nutrient targets
 * - It does NOT contain totals or progress
 * - It does NOT contain widget or UI state
 *
 * Nutrient targets are normalized into [NutrientGoalEntity] and
 * associated via a foreign key relationship.
 *
 * Design rationale:
 * - Allows unlimited nutrients without schema changes
 * - Enables multiple plans in the future
 * - Keeps Room migrations simple and predictable
 * - Separates strategy (plan) from constraints (goals)
 */
@Entity(tableName = "nutrition_plan")
data class UserNutritionPlanEntity(

    /**
     * Primary key for the nutrition plan.
     *
     * Using a stable ID (default = 1L) allows a single active plan
     * today, while remaining forward-compatible with multiple plans
     * in the future.
     */
    @PrimaryKey val id: Long = 1L,
    val type: NutritionGoalType,
    /**
     * Human-readable name of the plan.
     *
     * Examples: "Cut", "Maintenance", "Lean Bulk"
     */
    val name: String,

    /**
     * Whether this plan is currently active.
     *
     * Only one plan should be active at a time.
     * This flag allows plans to be stored, switched,
     * or archived without deletion.
     */
    val isActive: Boolean,

    /**
     * ISO-8601 timestamp indicating when the plan was created.
     *
     * Stored as a string to keep this entity persistence-only
     * and avoid time-zone logic at the data layer.
     */
    val createdAt: String
)
