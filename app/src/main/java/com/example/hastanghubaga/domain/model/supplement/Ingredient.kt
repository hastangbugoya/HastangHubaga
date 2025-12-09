package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit

/**
 * Represents a nutrient or active component found within one or more supplements.
 *
 * This is the *domain-layer* model (not the Room entity), meaning:
 * - It contains only business-logic fields.
 * - It excludes persistence concerns.
 * - It is safe to expose directly to UI or use cases.
 *
 * ## Purpose
 * `Ingredient` provides a normalized description of a nutrient such as:
 * - “Vitamin C”
 * - “Magnesium Glycinate”
 * - “Zinc”
 *
 * Each ingredient may include:
 * - Its standard measurement unit (mg, mcg, IU, etc.)
 * - Optional RDA (Recommended Dietary Allowance)
 * - Optional UL (Upper Limit)
 * - A category for organizing/filtering in the UI (e.g., “Vitamins”, “Minerals”)
 *
 * This model is used when:
 * - Displaying supplement facts
 * - Computing daily nutrient totals
 * - Performing safety checks (e.g., exceeding UL)
 *
 * ## Fields
 * @property id
 * Unique ID for the ingredient.
 *
 * @property name
 * Human-readable ingredient name (e.g., “Vitamin D3”).
 *
 * @property defaultUnit
 * Default measurement unit used for this ingredient across supplements.
 *
 * @property rdaValue
 * Optional daily recommended value based on nutrition guidelines.
 *
 * @property rdaUnit
 * Unit for the RDA value. May differ from [defaultUnit] in rare cases.
 *
 * @property upperLimitValue
 * Optional tolerable upper intake limit. Used for safety calculations.
 *
 * @property upperLimitUnit
 * Unit for the upper limit value.
 *
 * @property category
 * Optional grouping label (e.g., “Mineral”, “Herb”, “Vitamin”).
 *
 * ## Notes
 * - All optional fields may be null if nutrition data is unknown.
 * - For UI safety, logic should treat null UL/RDA as “no defined limit”.
 */
data class Ingredient(
    val id: Long,
    val name: String,

    val defaultUnit: IngredientUnit,

    val rdaValue: Double? = null,
    val rdaUnit: IngredientUnit? = null,

    val upperLimitValue: Double? = null,
    val upperLimitUnit: IngredientUnit? = null,

    val category: String? = null
)