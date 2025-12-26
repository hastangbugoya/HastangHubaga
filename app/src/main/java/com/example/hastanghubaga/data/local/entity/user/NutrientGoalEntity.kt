package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a nutrient-level goal associated with a nutrition plan.
 *
 * Each row defines a constraint for a single nutritional dimension
 * (ingredient / nutrient), such as:
 * - Calories
 * - Protein
 * - Sodium
 * - Vitamin D
 *
 * This entity represents *constraints*, not consumption.
 *
 * Design principles:
 * - One nutrient = one row
 * - No hard-coded nutrients
 * - Fully extensible without schema changes
 *
 * Nutrient goals are evaluated against actual consumption
 * (from NutritionTotalsRepository) by a separate calculator layer.
 */
@Entity(
    tableName = "nutrient_goals",
    indices = [
        Index("planId"),
        Index("ingredientId")
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
     * Auto-generated primary key for this nutrient goal.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * ID of the nutrition plan this goal belongs to.
     *
     * Multiple nutrient goals may belong to the same plan.
     * Deleting a plan will cascade-delete its goals.
     */
    val planId: Long,

    /**
     * Identifier of the ingredient / nutrient this goal applies to.
     *
     * This should match the IDs used by nutrition aggregation
     * (NutritionTotalsRepository).
     */
    val ingredientId: Long,

    /**
     * Desired daily target amount for this nutrient.
     *
     * Examples:
     * - Calories: 2200
     * - Protein: 150
     * - Vitamin D: 600
     */
    val target: Double,

    /**
     * Optional upper limit for this nutrient.
     *
     * Used for nutrients where excessive intake should be flagged
     * (e.g. sodium, vitamin A).
     *
     * Null indicates no upper bound.
     */
    val upperLimit: Double?,

    /**
     * Unit of measurement for both target and upper limit.
     *
     * Must match the unit used in nutrition aggregation to ensure
     * valid comparisons.
     */
    val unit: String,

    /**
     * Whether this nutrient goal is currently enabled.
     *
     * Allows temporarily disabling individual goals without
     * removing them from the plan.
     */
    val isEnabled: Boolean
)

