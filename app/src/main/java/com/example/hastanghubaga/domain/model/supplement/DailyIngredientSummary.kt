package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

/**
 * Represents a single nutrient’s total intake and safety status for a given day.
 *
 * This summary model is typically created after aggregating all supplement doses
 * taken within the same day. It provides convenience values such as percent of
 * the Recommended Daily Allowance (RDA) and whether the user exceeds the
 * tolerable upper limit (UL) for that ingredient.
 *
 * ### Typical use cases:
 * - Showing a “daily nutrient summary” screen
 * - Highlighting nutrients where intake is too low or too high
 * - Providing insights into supplement planning or scheduling
 *
 * @property name The ingredient's display name, e.g. `"Vitamin C"`.
 * @property totalAmount The accumulated total amount consumed for the day, in the given unit.
 * @property unit The measurement unit (e.g., `"mg"`, `"mcg"`, `"IU"`).
 * @property rda The Recommended Daily Allowance for this nutrient, if defined.
 * @property upperLimit The tolerable Upper Limit (UL) for safe intake, if defined.
 */
data class DailyIngredientSummary(
    val name: String,
    var totalAmount: Double,
    val unit: IngredientUnit,

    val rda: Double? = null,
    val upperLimit: Double? = null
) {

    /**
     * Percentage of the RDA consumed today, or `null` if RDA is not defined.
     *
     * Example:
     * ```
     * totalAmount = 500 mg
     * rda = 90 mg
     * percentRda = 555.5%
     * ```
     */
    val percentRda: Double?
        get() = if (rda != null) (totalAmount / rda) * 100 else null

    /**
     * Indicates whether today's total intake exceeds the tolerable upper limit (UL),
     * if an upper limit is defined. Returns `false` if no UL exists.
     */
    val exceedsUpperLimit: Boolean
        get() = upperLimit != null && totalAmount > upperLimit
}