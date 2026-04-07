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
 * - SUCCESS EVALUATION STRATEGY (see [successMode])
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
 * ## SUCCESS EVALUATION DESIGN (IMPORTANT)
 *
 * A nutrition plan defines nutrient goals, but DOES NOT automatically define
 * how "success" is evaluated for a day.
 *
 * This is handled by [successMode] + optional child table:
 *   -> nutrition_plan_success_criteria
 *
 * ### Why this exists:
 * - A user may track many nutrients
 * - But only care about SOME for "success"
 * - Or none at all (gentle reminder mode)
 *
 * ---
 * ## successMode behavior
 *
 * ### ALL_TRACKED_GOALS
 * - All nutrient goals in this plan must pass
 *
 * ### ANY_TRACKED_GOAL
 * - At least one nutrient goal must pass
 *
 * ### CUSTOM_SELECTED_GOALS
 * - Only nutrients listed in nutrition_plan_success_criteria are evaluated
 * - If none selected → treat as success (intentional)
 *
 * ### NONE
 * - Always successful regardless of goal values
 * - Used for "tracking only / non-punitive mode"
 *
 * ---
 * ## Future AI/dev note
 *
 * DO NOT:
 * - infer success from nutrient goals alone
 * - assume all nutrients are required
 *
 * ALWAYS:
 * - check [successMode]
 * - if CUSTOM → query nutrition_plan_success_criteria
 *
 * ---
 * ## Effective nutrient computation
 *
 * Effective ranges are computed across ACTIVE plans:
 * - effective min = highest min
 * - effective max = lowest max
 * - conflicts when min > max
 */
@Entity(tableName = "nutrition_plan")
data class UserNutritionPlanEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val type: NutritionGoalType,

    val name: String,

    val startDate: Long,

    val endDate: Long? = null,

    val isActive: Boolean,

    val sourceType: String,

    val sourcePlanId: String? = null,

    /**
     * Defines how success is evaluated for this plan.
     *
     * See KDoc above for full behavior.
     *
     * Stored as String for forward compatibility.
     */
    val successMode: String = SUCCESS_MODE_ALL,

    val createdAt: Long,

    val updatedAt: Long
)

/**
 * Success mode constants.
 *
 * Kept as String instead of enum for:
 * - migration safety
 * - forward compatibility with AK
 */
const val SUCCESS_MODE_ALL = "ALL_TRACKED_GOALS"
const val SUCCESS_MODE_ANY = "ANY_TRACKED_GOAL"
const val SUCCESS_MODE_CUSTOM = "CUSTOM_SELECTED_GOALS"
const val SUCCESS_MODE_NONE = "NONE"