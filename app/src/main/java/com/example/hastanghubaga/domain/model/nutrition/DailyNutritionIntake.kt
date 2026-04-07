package com.example.hastanghubaga.domain.model.nutrition

/**
 * Represents the nutrient intake totals for a single calendar day.
 *
 * This is the input model for HH daily nutrition compliance evaluation.
 *
 * The compliance engine should evaluate:
 * - which plans are active for the date
 * - which nutrient goals apply
 * - which tracked nutrients count toward success
 * - whether the day is successful based on plan successMode
 *
 * This model is intentionally small and source-agnostic.
 *
 * Why this exists:
 * - HH daily compliance should not depend directly on AK snapshot DTOs
 * - local/manual intake, imported AK intake, or future merged sources should all
 *   be convertible into this neutral domain model
 * - monthly/calendar compliance can later reuse this same daily shape
 *
 * Nutrient map rules:
 * - key = canonical nutrient key aligned with HH/AK nutrient catalog naming
 * - value = total intake amount for that day in the nutrient's canonical unit
 * - omitted key means no intake value was provided for that nutrient
 *
 * Important:
 * - Missing nutrient data is NOT the same as zero unless a caller explicitly
 *   normalizes it that way before constructing this model
 * - Compliance logic should decide how missing vs zero is interpreted
 *
 * Future AI/dev note:
 * - Keep this model source-neutral
 * - Do not add plan-specific or UI-specific fields here
 * - Do not couple this directly to persistence entities or AK transport models
 */
data class DailyNutritionIntake(
    val date: Long,
    val nutrients: Map<String, Double>
)