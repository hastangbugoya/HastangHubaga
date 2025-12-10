package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents the relationship between a supplement and an ingredient,
 * defining **how much of a specific nutrient is contained in one serving**
 * of a supplement.
 *
 * This table is the backbone of nutrient calculation. Each row maps:
 * - one supplement → one ingredient
 * - with a quantity measured in mg, mcg, IU, etc.
 *
 * ### Purpose
 * `SupplementIngredientEntity` enables:
 * - computing daily nutrient totals
 * - validating RDA / upper limit thresholds
 * - generating ingredient summaries
 * - displaying accurate nutritional breakdowns on supplement detail screens
 *
 * Supplements may contain **multiple ingredients**, and the same ingredient
 * may appear in many supplements. This entity forms a classic many-to-many
 * relationship with a payload.
 *
 * ### Data Fields
 * - `amountPerServing`: The nutrient quantity per *one serving* of this supplement
 * - `unit`: Human-readable unit ("mg", "mcg", "IU", etc.)
 * - `displayName`: Label name exactly as printed on the bottle (may differ slightly
 *   from canonical ingredient names, and therefore preserved as-is)
 *
 * ### Foreign Keys
 * - `supplementId` → links to `SupplementEntity`
 * - `ingredientId` → links to `IngredientEntity`
 *
 * Both use `CASCADE` deletion to ensure related rows are cleaned up automatically.
 *
 * @property id Primary key.
 * @property supplementId ID of the supplement this ingredient belongs to.
 * @property ingredientId ID of the nutrient/ingredient being referenced.
 * @property displayName Label text used for UI presentation (as shown on supplement packaging).
 * @property amountPerServing How much of the ingredient is in *one serving* of the supplement.
 * @property unit Display unit corresponding to `amountPerServing`.
 *
 * @see SupplementEntity
 * @see IngredientEntity
 */
@Serializable
@Entity(
    tableName = "supplement_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = SupplementEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplementId"), Index("ingredientId")]
)
data class SupplementIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val supplementId: Long,
    val ingredientId: Long,

    // Bottle label text (optional but useful)
    val displayName: String,

    val amountPerServing: Double,
    val unit: IngredientUnit
)