package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a joined view of a supplement ingredient and its corresponding
 * ingredient metadata. This data class is used when loading a supplement along
 * with full nutritional details about each ingredient.
 *
 * ### Purpose
 * `SupplementIngredientWithInfo` combines:
 * - the *amount per serving* and label-specific details stored in
 *   [SupplementIngredientEntity], and
 * - the canonical nutrient information stored in [IngredientEntity]
 *
 * This joined structure is typically used for:
 * - displaying a supplement’s full ingredient breakdown
 * - computing total daily nutrient intake across supplements
 * - mapping Room results into domain models
 *
 * ### Fields
 * - `ingredient`: The specific ingredient as declared in the supplement bottle
 *   (amount per serving, label display name, unit)
 * - `info`: The canonical ingredient entry in the ingredients table
 *   (RDA, upper limits, classification, etc.)
 *
 * ### Notes
 * This is a **Room relation class**, used strictly to receive combined rows
 * from the database. It is not meant to be stored in a table.
 *
 * @property ingredient The supplement-ingredient row representing the amount per serving.
 * @property info The canonical ingredient metadata describing nutrient properties.
 *
 * @see SupplementIngredientEntity
 * @see IngredientEntity
 */
data class SupplementIngredientWithInfo(
    @Embedded val ingredient: SupplementIngredientEntity,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val info: IngredientEntity
)
