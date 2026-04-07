package com.example.hastanghubaga.domain.model.nutrition

/**
 * Represents high-level classifications of nutrition plans.
 *
 * Includes a built-in [displayName] to avoid UI-layer `when()` mapping.
 *
 * Future AI/dev note:
 * - Keep this enum UI-friendly but not UI-dependent.
 * - displayName should remain simple and stable (no localization logic here).
 * - If localization is needed later, this can be replaced by a string key instead.
 */
enum class NutritionGoalType(
    val displayName: String
) {
    BULKING("Bulking"),
    CUTTING("Cutting"),
    MAINTENANCE("Maintenance"),
    RECOMP("Recomposition"),
    PERFORMANCE("Performance"),
    KETO("Keto"),
    CUSTOM("Custom")
}