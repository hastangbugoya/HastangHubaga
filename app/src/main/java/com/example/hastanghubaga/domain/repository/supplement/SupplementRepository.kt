package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.DailyIngredientSummary
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class SupplementRepository @Inject constructor(
    private val supplementDao: SupplementEntityDao,
    private val ingredientDao: IngredientEntityDao,
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val dailyStartTimeDao: DailyStartTimeDao
) {

    // ------------------------------------------------------------
    // Basic Queries
    // ------------------------------------------------------------
    fun getAllSupplements() = supplementDao.getAllSupplementsFlow()

    fun getAllIngredients() = ingredientDao.getAllIngredientsFlow()

    fun getLogsForDate(date: LocalDate) =
        supplementDailyLogDao.getDoseLogsForDay(date.toString())

    // ------------------------------------------------------------
    // Hour 0 Handling
    // ------------------------------------------------------------
    suspend fun setHourZero(date: LocalDate, time: LocalTime) {
        dailyStartTimeDao.upsert(
            DailyStartTimeEntity(
                date = date.toString(),
                hourZero = time.toSecondOfDay()
            )
        )
    }

    suspend fun getHourZero(date: LocalDate): LocalTime? {
        val entity = dailyStartTimeDao.getStartTime(date.toString()) ?: return null
        return LocalTime.ofSecondOfDay(entity.hourZero.toLong())
    }

    // ------------------------------------------------------------
    // Dose Logging
    // ------------------------------------------------------------
    suspend fun logSupplementDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit
    ) {

        val timestamp = date
            .atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        supplementDailyLogDao.insertDoseLog(
            SupplementDailyLogEntity(
                supplementId = supplementId,
                date = date.toString(),
                actualServingTaken = fractionTaken,
                doseUnit = doseUnit,
                timestamp = timestamp
            )
        )
    }

    // ------------------------------------------------------------
    // Should-Take Logic
    // ------------------------------------------------------------
    suspend fun shouldTakeToday(supplement: SupplementEntity, date: LocalDate): Boolean {
        return when (supplement.frequencyType) {
            FrequencyType.DAILY -> true

            FrequencyType.EVERY_X_DAYS ->
                supplement.frequencyInterval?.let { interval ->
                    val epoch = date.toEpochDay().toInt()
                    epoch % interval == 0
                } ?: true

            FrequencyType.WEEKLY ->
                supplement.weeklyDays?.contains(date.dayOfWeek) ?: false
        }
    }

    // ------------------------------------------------------------
    // Next Dose Prediction
    // ------------------------------------------------------------
    suspend fun getPredictedNextDoseTime(
        supplement: SupplementEntity,
        date: LocalDate
    ): LocalTime? {
        val hourZero = getHourZero(date) ?: return null

        return supplement.offsetMinutes?.let { offset ->
            hourZero.plusMinutes(offset.toLong())
        }
    }

    // ------------------------------------------------------------
    // Ingredient RDA + Warning Logic
    // ------------------------------------------------------------
    suspend fun getDailyIngredientSummary(date: LocalDate): List<DailyIngredientSummary> {
        val logs = supplementDailyLogDao.getDoseLogsForDayOnce(date.toString())
        val supplements = supplementDao.getAllSupplementsWithIngredients()

        val lookup = supplements.associateBy { it.supplement.id }
        val totals = mutableMapOf<String, DailyIngredientSummary>()

        logs.forEach { log ->
            val supplement = lookup[log.supplementId] ?: return@forEach

            supplement.ingredients.forEach { item ->
                val amountTaken = item.ingredient.amountPerServing * log.actualServingTaken

                val key = item.info.name

                val entry = totals.getOrPut(key) {
                    DailyIngredientSummary(
                        name = key,
                        totalAmount = 0.0,
                        unit = item.ingredient.unit,
                        rda = item.info.rdaValue,
                        upperLimit = item.info.upperLimitValue
                    )
                }

                entry.totalAmount += amountTaken
            }
        }

        return totals.values.toList()
    }

}
