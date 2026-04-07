package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Defines which nutrients count toward "success" for a nutrition plan.
 *
 * This table is ONLY used when the parent [UserNutritionPlanEntity.successMode]
 * is [SUCCESS_MODE_CUSTOM].
 *
 * One row = one nutrient key selected as part of the plan's success basis.
 *
 * ---
 * ## Why this table exists
 *
 * Nutrition goals and success evaluation are related, but not identical.
 *
 * A plan may contain many nutrient goals for:
 * - guidance
 * - warnings
 * - monitoring
 * - future reporting
 *
 * But the user may only want SOME of those nutrients to determine whether the day
 * is considered a "success".
 *
 * Examples:
 * - Protein + calories determine success
 * - Sodium is tracked for warning only
 * - Fiber is tracked but not required
 *
 * That is why success selection is modeled separately instead of adding a boolean
 * directly onto [NutrientGoalEntity].
 *
 * ---
 * ## Behavior
 *
 * This table should be ignored unless the parent plan uses:
 * - [SUCCESS_MODE_CUSTOM]
 *
 * If the parent plan is custom mode and this table contains:
 * - 1+ rows -> only those nutrient keys count toward success
 * - 0 rows -> treat as success by default
 *
 * That empty-selection behavior is intentional so the app can support:
 * - "tracking only"
 * - "gentle reminder"
 * - "non-punitive" plans
 *
 * ---
 * ## Future AI/dev note
 *
 * DO NOT assume:
 * - every goal row must count toward success
 * - success criteria must mirror all nutrient goals
 *
 * ALWAYS:
 * - check the parent plan's successMode first
 * - if CUSTOM, evaluate only nutrients listed here
 *
 * ---
 * ## Storage model
 *
 * - [planId] points to the parent nutrition plan
 * - [nutrientKey] must match the canonical nutrient key space used elsewhere
 *   in HH/AK (examples: PROTEIN_G, CARBS_G, FAT_G, CALORIES_KCAL)
 *
 * The unique (planId, nutrientKey) index prevents duplicate selections.
 */
@Entity(
    tableName = "nutrition_plan_success_criteria",
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
data class UserNutritionPlanSuccessCriteriaEntity(

    /**
     * Surrogate primary key.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Parent nutrition plan ID.
     */
    val planId: Long,

    /**
     * Canonical nutrient key selected as part of the plan's success basis.
     *
     * This should match the same canonical key space used by:
     * - NutrientGoalEntity.nutrientKey
     * - HH seeded ingredient codes
     * - AK CsvNutrientCatalog codes
     */
    val nutrientKey: String
)