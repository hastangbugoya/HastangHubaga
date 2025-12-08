package com.example.hastanghubaga.data.local.entity.supplement

/**
 * Represents the unit used when recording a supplement's dose.
 *
 * These units describe *how a supplement is taken* rather than how its
 * nutritional ingredients are measured. For example, a capsule may contain
 * 500 mg of Vitamin C, but the *dose unit* is still `CAPSULE`.
 *
 * `SupplementDoseUnit` is stored alongside historical logs
 * (`SupplementDailyLogEntity`) to preserve accuracy even if the dose unit
 * defined in the supplement changes later.
 *
 * ## When this is used
 * - Displaying the recommended serving size (e.g., “2 capsules per day”)
 * - Recording actual intake logs
 * - Converting servings into ingredient amounts
 *
 * ## Why this is separate from IngredientUnit
 * - IngredientUnit describes *nutritional measurement* (mg, mcg, IU, etc.)
 * - SupplementDoseUnit describes *the form the user consumes*
 *
 * Example:
 * A supplement might be taken as **1 capsule**, but contain **500 mg** of Vitamin C.
 *
 * @see com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
 * @see com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
 */
enum class SupplementDoseUnit {
    /** A solid pill contained in a gelatin or vegetable capsule shell. */
    CAPSULE,

    /** A compressed solid tablet form. */
    TABLET,

    /** A soft gelatin capsule containing liquid or gel. */
    SOFTGEL,

    /** Tablespoon measure (approx. 15 mL). */
    SPOON_TBSP,

    /** Teaspoon measure (approx. 5 mL). */
    SPOON_TSP,

    /** Milliliter liquid measurement. */
    ML,

    /** A single drop, typically for liquid supplements like Vitamin D. */
    DROP,

    /** Powder scooped with a standard scoop provided by the manufacturer. */
    SCOOP,

    /** Gram measurement, primarily for loose powders. */
    GRAM,

    /** Milligram measurement, very precise dosing (e.g., creatine). */
    MG,

    /**
     * Catch-all for formulations not covered by the other types.
     * Useful for flexible logging without forcing structural changes.
     */
    OTHER
}
