package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Child row for a nutrition plan.
 *
 * One row = one nutrient constraint inside one plan.
 *
 * This is intentionally normalized so HH can support:
 * - multiple plans
 * - multiple active plans
 * - overlapping nutrient rules across plans
 * - future import of AK plans without requiring a separate schema
 *
 * Important design choice:
 * - We store a canonical [nutrientKey] string rather than an ingredient ID.
 * - HH can later map AK payload names/keys into this canonical key space during import.
 * - That keeps the storage model stable even if external payloads differ from HH naming.
 *
 * Value semantics:
 * - [minValue] = lower bound, nullable
 * - [targetValue] = advisory target, nullable
 * - [maxValue] = upper bound, nullable
 *
 * At least one of min/target/max should be non-null. That rule is expected to be enforced
 * by repository / use case validation rather than by the entity alone.
 *
 * Future AI/dev note:
 * Effective active-plan resolution should happen above the persistence layer:
 * - effective min = highest active min
 * - effective max = lowest active max
 * - target remains advisory for now
 * - if effective min > effective max, that nutrient is in conflict
 */
@Entity(
    tableName = "nutrition_plan_goals",
    indices = [
        Index(value = ["planId"]),
        Index(value = ["nutrientKey"]),
        Index(value = ["planId", "nutrientKey"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserNutritionPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NutrientGoalEntity(

    /**
     * Surrogate primary key for this plan-goal row.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Parent nutrition plan ID.
     */
    val planId: Long,

    /**
     * Canonical HH nutrient identifier for this goal row.
     *
     * Examples might later include keys such as:
     * - calories
     * - protein
     * - carbs
     * - fat
     * - sodium
     *
     * Do not store raw AK source field names here unless they are first normalized
     * into HH's canonical key space.
     */
    val nutrientKey: String,

    /**
     * Optional minimum desired amount for this nutrient.
     */
    val minValue: Double? = null,

    /**
     * Optional advisory target for this nutrient.
     */
    val targetValue: Double? = null,

    /**
     * Optional maximum allowed amount for this nutrient.
     */
    val maxValue: Double? = null
)