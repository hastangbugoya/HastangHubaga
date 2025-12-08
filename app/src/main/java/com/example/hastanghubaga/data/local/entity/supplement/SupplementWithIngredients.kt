package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a Room relational projection that joins a [SupplementEntity]
 * with all of its associated ingredient entries and their corresponding
 * ingredient metadata.
 *
 * ## Purpose
 * This data class is used by Room to return a fully populated supplement:
 * - The main supplement row from `supplements`
 * - All ingredient rows from `supplement_ingredients`
 * - Each ingredient enriched with its canonical nutrient information
 *   from the `ingredients` table.
 *
 * ## Typical Use Cases
 * - Displaying detailed supplement profiles in the UI
 * - Performing nutrient calculations (e.g., total mg of Vitamin C per serving)
 * - Building domain models that require both supplement and ingredient data
 *
 * ## Notes
 * This is a **data-layer DTO**, not a domain model.
 * It should normally be mapped to a domain model before being used in the rest
 * of the app to maintain clean architecture boundaries.
 */
data class SupplementWithIngredients(
    @Embedded val supplement: SupplementEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "supplementId",
        entity = SupplementIngredientEntity::class
    )
    val ingredients: List<SupplementIngredientWithInfo>
)