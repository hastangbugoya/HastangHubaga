package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


/**
 * Represents a nutritional ingredient that can appear in one or more supplements.
 *
 * Each ingredient stores metadata such as its default unit (e.g., mg, mcg),
 * recommended daily allowance (RDA), tolerable upper intake limit (UL), and an
 * optional category (e.g., "Vitamin", "Mineral", "Amino Acid").
 *
 * This table is referenced by:
 * - `supplement_ingredients` (junction table linking supplements to ingredients)
 * - Daily summary calculations (e.g., aggregating a user's intake vs RDA)
 *
 * ## Schema Notes
 * - The `name` column is indexed and unique to prevent duplicate ingredient entries.
 * - Units are represented using the `IngredientUnit` enum for consistency.
 * - RDA and upper limit values are optional because many ingredients do not have
 *   officially defined recommended dosages.
 *
 * @property id Auto-generated primary key for the ingredient.
 * @property name The human-readable name of the ingredient (e.g., "Vitamin C").
 * @property defaultUnit The standard unit used for this ingredient (mg, mcg, IU, etc.).
 *
 * @property rdaValue Optional Recommended Daily Allowance amount.
 * @property rdaUnit Optional unit used for the RDA value. If set, it should match
 * the unit context for comparisons.
 *
 * @property upperLimitValue Optional tolerable upper limit amount. This is used
 * to warn the user if daily scheduled supplements exceed safe intake.
 * @property upperLimitUnit Optional unit for the upper limit.
 *
 * @property category Optional grouping label such as "Vitamin", "Mineral",
 * "Amino Acid", "Herb", etc. Used for sorting and filtering in UI.
 */
@Serializable
@Entity(
    tableName = "ingredients",
    indices = [Index(value = ["name"], unique = true)]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val defaultUnit: IngredientUnit,

    val rdaValue: Double? = null,
    val rdaUnit: IngredientUnit? = null,

    val upperLimitValue: Double? = null,
    val upperLimitUnit: IngredientUnit? = null,

    val category: String? = null
)
