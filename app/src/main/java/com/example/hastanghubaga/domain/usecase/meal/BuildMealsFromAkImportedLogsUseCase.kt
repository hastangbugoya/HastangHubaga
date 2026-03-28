package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.dao.meal.AkImportedLogDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedMealDao
import com.example.hastanghubaga.data.local.entity.meal.AkImportedLogEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * BuildMealsFromAkImportedLogsUseCase
 *
 * ## Purpose
 * Groups raw AK imported item logs into HH-style derived meal payloads and
 * materializes them into the dedicated HH imported-meal table.
 *
 * ## Important safety rule`
 * This use case intentionally does NOT persist into HH's native MealEntity table.
 *
 * Reason:
 * - HH MealEntity currently has no source ownership marker / imported-meal identity
 * - blindly writing derived meals there risks colliding with HH user-created meals
 *
 * So this use case owns the safe middle step:
 * - read raw AK logs from AkImportedLogEntity
 * - derive grouped HH-style meals from those logs
 * - persist them into AkImportedMealEntity
 *
 * ## Grouping rules
 * 1) Only logs with a strong AK mealSlot are materialized into imported meals:
 *    - BREAKFAST
 *    - LUNCH
 *    - DINNER
 *    - SNACK
 * 2) Weak / unassigned logs are intentionally NOT materialized:
 *    - null / blank mealSlot
 *    - CUSTOM
 *    - any unsupported / unknown slot
 *
 * These weak logs remain preserved in AkImportedLogEntity only and are not
 * converted into HH imported meals during this phase.
 *
 * ## Nutrition rules
 * - nutrientsJson from AK is parsed as an opaque nutrient map
 * - HH meal totals currently extract/sum only the fields HH already models directly:
 *   calories, protein, carbs, fat, sodium, cholesterol, fiber
 */
@ViewModelScoped
class BuildMealsFromAkImportedLogsUseCase @Inject constructor(
    private val akImportedLogDao: AkImportedLogDao,
    private val akImportedMealDao: AkImportedMealDao
) {

    data class DerivedMeal(
        val groupingKey: String,
        val type: MealType,
        val timestamp: Long,
        val logDateIso: String,
        val notes: String?,
        val sourceLogStableIds: List<String>,
        val totalCalories: Int,
        val totalProtein: Double,
        val totalCarbs: Double,
        val totalFat: Double,
        val totalSodium: Double?,
        val totalCholesterol: Double?,
        val totalFiber: Double?
    )

    suspend operator fun invoke(
        logDateIso: String
    ): List<DerivedMeal> {
        val logs = akImportedLogDao.getForDate(logDateIso)

        if (logs.isEmpty()) {
            akImportedMealDao.replaceForDate(
                logDateIso = logDateIso,
                meals = emptyList()
            )
            return emptyList()
        }

        val strongSlotLogs = logs.filter { it.hasStrongMealSlot() }

        val derived = strongSlotLogs
            .groupBy { GroupKey(it.logDateIso, normalizeStrongMealSlot(it.mealSlot)) }
            .toSortedMap(compareBy<GroupKey> { it.logDateIso }.thenBy { it.mealType.name })
            .values
            .map { grouped ->
                val type = normalizeStrongMealSlot(grouped.first().mealSlot)
                buildDerivedMeal(
                    groupingKey = "${grouped.first().logDateIso}:${type.name}",
                    type = type,
                    logs = grouped,
                    notes = buildGroupedNotes(grouped)
                )
            }
            .sortedBy { it.timestamp }

        akImportedMealDao.replaceForDate(
            logDateIso = logDateIso,
            meals = derived.map { it.toEntity() }
        )

        return derived
    }

    private fun AkImportedLogEntity.hasStrongMealSlot(): Boolean {
        return when (mealSlot?.trim()?.uppercase()) {
            "BREAKFAST", "LUNCH", "DINNER", "SNACK" -> true
            else -> false
        }
    }

    private fun buildDerivedMeal(
        groupingKey: String,
        type: MealType,
        logs: List<AkImportedLogEntity>,
        notes: String?
    ): DerivedMeal {
        require(logs.isNotEmpty()) { "Cannot build a derived meal from an empty log list." }

        val totals = logs.fold(NutritionTotals()) { acc, log ->
            acc + parseNutrition(log.nutrientsJson)
        }

        return DerivedMeal(
            groupingKey = groupingKey,
            type = type,
            timestamp = logs.minOf { it.timestamp },
            logDateIso = logs.first().logDateIso,
            notes = notes,
            sourceLogStableIds = logs.map { it.stableId }.sorted(),
            totalCalories = totals.calories.roundToIntSafe(),
            totalProtein = totals.protein,
            totalCarbs = totals.carbs,
            totalFat = totals.fat,
            totalSodium = totals.sodium.nullIfZeroAndUnset(),
            totalCholesterol = totals.cholesterol.nullIfZeroAndUnset(),
            totalFiber = totals.fiber.nullIfZeroAndUnset()
        )
    }

    private fun buildGroupedNotes(logs: List<AkImportedLogEntity>): String {
        return logs
            .sortedBy { it.timestamp }
            .joinToString(separator = ", ") { it.itemName }
    }

    private fun normalizeStrongMealSlot(raw: String?): MealType {
        return when (raw?.trim()?.uppercase()) {
            "BREAKFAST" -> MealType.BREAKFAST
            "LUNCH" -> MealType.LUNCH
            "DINNER" -> MealType.DINNER
            "SNACK" -> MealType.SNACK
            else -> error("normalizeStrongMealSlot called with non-strong slot: $raw")
        }
    }

    private fun parseNutrition(nutrientsJson: String): NutritionTotals {
        val obj = Json.parseToJsonElement(nutrientsJson).jsonObject

        return NutritionTotals(
            calories = obj.doubleValue("CALORIES_KCAL"),
            protein = obj.doubleValue("PROTEIN_G"),
            carbs = obj.doubleValue("CARBS_G"),
            fat = obj.doubleValue("FAT_G"),
            sodium = obj.doubleValue("SODIUM_MG"),
            cholesterol = obj.doubleValue("CHOLESTEROL_MG"),
            fiber = obj.doubleValue("FIBER_G")
        )
    }

    private fun JsonObject.doubleValue(key: String): Double {
        val primitive = this[key] as? JsonPrimitive ?: return 0.0
        return primitive.content.toDoubleOrNull() ?: 0.0
    }

    private fun DerivedMeal.toEntity(): AkImportedMealEntity {
        return AkImportedMealEntity(
            groupingKey = groupingKey,
            logDateIso = logDateIso,
            type = type,
            timestamp = timestamp,
            notes = notes,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            totalSodium = totalSodium,
            totalCholesterol = totalCholesterol,
            totalFiber = totalFiber
        )
    }

    private data class GroupKey(
        val logDateIso: String,
        val mealType: MealType
    )

    private data class NutritionTotals(
        val calories: Double = 0.0,
        val protein: Double = 0.0,
        val carbs: Double = 0.0,
        val fat: Double = 0.0,
        val sodium: Double = 0.0,
        val cholesterol: Double = 0.0,
        val fiber: Double = 0.0
    ) {
        operator fun plus(other: NutritionTotals): NutritionTotals {
            return NutritionTotals(
                calories = calories + other.calories,
                protein = protein + other.protein,
                carbs = carbs + other.carbs,
                fat = fat + other.fat,
                sodium = sodium + other.sodium,
                cholesterol = cholesterol + other.cholesterol,
                fiber = fiber + other.fiber
            )
        }
    }

    private fun Double.roundToIntSafe(): Int {
        return kotlin.math.round(this).toInt()
    }

    /**
     * Keeps nullable nutrition fields nullable when the aggregate is effectively "not present".
     *
     * For this first pass, 0.0 is treated as absent for optional fields because AK may omit a field
     * entirely, and HH optional meal nutrition columns are nullable.
     */
    private fun Double.nullIfZeroAndUnset(): Double? {
        return if (this == 0.0) null else this
    }
}