package com.example.hastanghubaga.data.local.entity.supplement

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
    CFU,
    CAPSULE,
    TABLET,
    SCOOP,
    DROPS
}