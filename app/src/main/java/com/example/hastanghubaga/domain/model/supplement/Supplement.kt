package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import java.time.DayOfWeek

/**
 * Represents a dietary supplement in the domain layer.
 *
 * This model is used by:
 * - Use cases (e.g., scheduling, dose prediction)
 * - UI screens (displaying supplement details)
 * - Repository outputs (mapping from Room entities)
 *
 * It contains all nutritional, scheduling, and behavioral metadata needed
 * to determine how a supplement should be taken and displayed.
 *
 * ## Purpose
 * `Supplement` provides the domain-level representation of a supplement after:
 * - Joining with its ingredient list
 * - Applying filtering or transformation logic
 *
 * This model separates business logic concerns from persistence-layer
 * implementations (`SupplementEntity`, `SupplementIngredientEntity`, etc.).
 *
 * ## Dosing & Scheduling Fields
 * The following properties determine *when* and *how* the supplement should be taken:
 *
 * - **recommendedServingSize** — Suggested dose per serving, from product label.
 * - **recommendedDoseUnit** — Unit of the serving (capsules, mg, scoops, etc.).
 * - **servingsPerDay** — Manufacturer-recommended number of servings per day.
 * - **doseAnchorType** — Defines the anchor event for the dose schedule
 *   (e.g., WAKEUP, BREAKFAST, BEFORE_WORKOUT).
 * - **frequencyType** — DAILY, WEEKLY, or EVERY_X_DAYS.
 * - **frequencyInterval** — Used for EVERY_X_DAYS patterns.
 * - **weeklyDays** — Days of week when taken (used for WEEKLY pattern).
 * - **offsetMinutes** — Additional offset time from the anchor event.
 *
 * ## Behavioral & Safety Fields
 * - **recommendedWithFood** — Indicates if the supplement should be taken with food.
 * - **recommendedLiquidInOz** — Suggested liquid amount to take with the supplement.
 * - **recommendedTimeBetweenDailyDosesMinutes** — Required spacing between multiple doses.
 * - **avoidCaffeine** — Whether the supplement should not be taken with caffeine.
 *
 * ## Tracking Fields
 * - **startDate** — When the user began taking the supplement.
 * - **lastTakenDate** — When the user last logged a dose.
 * - **isActive** — Whether the supplement is currently enabled by the user.
 *
 * ## Ingredient Information
 * - **ingredients** — A flattened list of domain-level ingredients
 *   contained in the supplement. This is already resolved from Room relations.
 *
 * ## Usage Notes
 * - Scheduling logic should rely on both frequency and dose-anchor fields.
 * - Null values for optional fields indicate "no specific recommendation."
 * - Mapping to/from Room entities should occur in dedicated mapper classes.
 *
 * @property id Unique identifier for the supplement.
 * @property name Common name of the supplement (e.g., "Vitamin D3").
 * @property brand Brand or manufacturer name (optional).
 * @property notes User or system notes associated with this supplement.
 *
 * @property recommendedServingSize Default dose per serving.
 * @property recommendedDoseUnit Unit describing the serving size.
 * @property servingsPerDay Recommended number of servings per day.
 * @property recommendedWithFood Whether the supplement should be taken with food.
 * @property recommendedLiquidInOz Suggested liquid volume in ounces.
 * @property recommendedTimeBetweenDailyDosesMinutes Minimum minutes between doses.
 * @property avoidCaffeine Whether the supplement should not be taken with caffeine.
 *
 * @property doseAnchorType Anchor event used to compute dose timing.
 * @property frequencyType Frequency pattern determining which days it is taken.
 * @property frequencyInterval Interval used for EVERY_X_DAYS schedules.
 * @property weeklyDays Days of the week the supplement is taken, for WEEKLY schedules.
 * @property offsetMinutes Additional minutes added to the computed daily dose time.
 * @property startDate Date the supplement plan began (ISO-8601 string).
 * @property lastTakenDate Most recent date the supplement was taken.
 *
 * @property ingredients List of associated ingredients in domain form.
 * @property isActive Whether the supplement is currently active/enabled.
 */

data class Supplement(
    val id: Long,
    val name: String,
    val brand: String?,
    val notes: String?,

    val recommendedServingSize: Double,
    val recommendedDoseUnit: SupplementDoseUnit,
    val servingsPerDay: Int,
    val recommendedWithFood: Boolean?,
    val recommendedLiquidInOz: Double?,
    val recommendedTimeBetweenDailyDosesMinutes: Int?,
    val avoidCaffeine: Boolean?,
    val doseConditions: Set<DoseCondition> = emptySet(),

    val doseAnchorType: DoseAnchorType,
    val frequencyType: FrequencyType,
    val frequencyInterval: Int?,
    val weeklyDays: List<DayOfWeek>?,
    val offsetMinutes: Int?,
    val startDate: String?,
    val lastTakenDate: String?,

    val ingredients: List<Ingredient>,
    val isActive: Boolean
)