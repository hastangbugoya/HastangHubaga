package com.example.hastanghubaga.model.entity.supplement


data class DailyIngredientSummary(
    val name: String,            // e.g., "Vitamin C"
    var totalAmount: Double,     // accumulated mg/mcg/IU/etc. for the day
    val unit: String,            // "mg", "mcg", etc.

    val rda: Double? = null,     // Recommended Daily Allowance
    val upperLimit: Double? = null // Tolerable Upper Limit
) {
    // Derived properties (optional convenience)
    val percentRda: Double?
        get() = if (rda != null) (totalAmount / rda) * 100 else null

    val exceedsUpperLimit: Boolean
        get() = upperLimit != null && totalAmount > upperLimit
}