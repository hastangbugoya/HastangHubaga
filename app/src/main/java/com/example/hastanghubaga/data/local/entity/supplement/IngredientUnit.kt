package com.example.hastanghubaga.data.local.entity.supplement

import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable

/**
 * Defines the supported measurement units used to represent ingredient quantities
 * in supplements. Units cover both nutritional amounts (e.g., mg, mcg, IU) and
 * physical serving units (e.g., capsule, scoop).
 *
 * This enum is used by:
 * - `IngredientEntity` to declare the default measurement unit
 * - `SupplementIngredientEntity` to represent serving-based ingredient amounts
 * - Daily intake calculators, which may convert between compatible units
 *
 * ## Notes
 * - Enum values are stored as strings in Room by default (using their name), which
 *   ensures forward compatibility as long as values are not renamed.
 * - Some units (e.g., `CAPSULE`, `TABLET`, `SCOOP`) represent *forms* rather than
 *   mass/volume. These are treated as opaque units unless conversion logic is added.
 * - Conversions are only meaningful for compatible units (e.g., mg → g). IU does not
 *   convert uniformly across ingredients and should be handled ingredient-by-ingredient.
 *
 * @see IngredientEntity
 * @see SupplementIngredientEntity
 */
enum class IngredientUnit {
    MG,
    MCG,
    G,
    IU,
    ML,
    OZ,
    KCAL,
    CFU,
    CAPSULE,
    TABLET,
    SCOOP,
    DROPS
}

fun IngredientUnit.DisplayCase(size: Double): String {
    val isSingular = size == 1.0

    return when (this) {
        // Mass
        IngredientUnit.MG ->
            if (isSingular) "mg" else "mg"
        IngredientUnit.MCG ->
            if (isSingular) "mcg" else "mcg"
        IngredientUnit.G ->
            if (isSingular) "g" else "g"

        // Volume / measure
        IngredientUnit.ML ->
            if (isSingular) "mL" else "mL"
        IngredientUnit.OZ ->
            if (isSingular) "oz" else "oz"
        IngredientUnit.DROPS ->
            if (isSingular) "drop" else "drops"

        // Count / dose
        IngredientUnit.IU ->
            if (isSingular) "IU" else "IU"
        IngredientUnit.CFU ->
            if (isSingular) "CFU" else "CFU"

        IngredientUnit.CAPSULE ->
            if (isSingular) "capsule" else "capsules"
        IngredientUnit.TABLET ->
            if (isSingular) "tablet" else "tablets"
        IngredientUnit.SCOOP ->
            if (isSingular) "scoop" else "scoops"

        IngredientUnit.KCAL -> "kcal"
    }
}