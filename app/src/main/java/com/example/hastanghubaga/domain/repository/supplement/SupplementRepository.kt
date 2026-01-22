package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementWithSettings
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.nutrition.DailyIngredientSummary
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.supplement.UserSupplementSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Instant

/**
 * Repository interface for managing ${CLASS_NAME}.
 *
 * Responsibilities:
 * - TODO: Add responsibilities
 *
 * Implementations:
 * - TODO: Add implementation notes
 */
interface SupplementRepository {

    fun getAllSupplements(): Flow<List<Supplement>>

    fun getAllIngredients(): Flow<List<Ingredient>>

    fun getActiveSupplements(): Flow<List<Supplement>>

    suspend fun getAllSupplementsOnce(): List<Supplement>

    suspend fun getActiveSupplementsOrderedByOffset(): List<Supplement>

    suspend fun shouldTakeToday(supplement: Supplement, date: LocalDate): Boolean

    suspend fun getPredictedNextDoseTime(
        supplement: Supplement,
        date: LocalDate
    ): LocalTime?

    suspend fun getDailyIngredientSummary(date: LocalDate): List<DailyIngredientSummary>

    suspend fun setHourZero(date: LocalDate, time: LocalTime)

    suspend fun getHourZero(date: LocalDate): LocalTime?

    // Should this supplement be considered active (user takes it)?
    fun isActive(supplement: Supplement): Boolean


    // Returns the absolute next dose datetime
    suspend fun getNextDoseDateTime(
        supplement: Supplement
    ): Instant?

    suspend fun setDefaultEventTime(anchor: DoseAnchorType, time: LocalTime)

    suspend fun overrideEventTime(
        date: LocalDate,
        anchor: DoseAnchorType,
        time: LocalTime
    )
    suspend fun removeEventOverride(date: LocalDate, anchor: DoseAnchorType)

    suspend fun getEventTime(
        anchor: DoseAnchorType,
        date: LocalDate
    ): LocalTime?

    suspend fun updateUserPreferredDose(
        supplementId: Long,
        dose: Double,
        unit: SupplementDoseUnit
    )

    fun getSupplementsForDate(
        date: String
    ): Flow<List<SupplementWithUserSettings>>

    fun observeSupplement(
        supplementId: Long
    ): Flow<SupplementWithUserSettings?>

    fun observeSupplementNutritionForDate(dateMillis: Long): Flow<List<MealNutrition>>

}