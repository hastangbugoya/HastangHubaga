package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.domain.model.supplement.DailyIngredientSummary
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementWithSettings
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.Supplement
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

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

    suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit
    )

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

    fun nextDoseDate(supp: SupplementEntity): LocalDate

    // Returns the absolute next dose datetime
    suspend fun getNextDoseDateTime(
        supplement: Supplement
    ): ZonedDateTime?

//    suspend fun getAnchorTime(anchor: DoseAnchorType, date: LocalDate): LocalTime?

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

    suspend fun getSupplementWithUserSettings(id: Long): SupplementWithSettings?

    fun observeSupplementWithUserSettings(id: Long): Flow<SupplementWithSettings?>

    suspend fun updateUserPreferredDose(
        supplementId: Long,
        dose: Double,
        unit: SupplementDoseUnit
    )
}