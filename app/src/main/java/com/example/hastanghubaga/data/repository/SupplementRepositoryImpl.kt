package com.example.hastanghubaga.data.repository

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
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.Ingredient
import com.example.hastanghubaga.domain.model.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementEntityDao,
    private val ingredientDao: IngredientEntityDao,
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val dailyStartTimeDao: DailyStartTimeDao
) : SupplementRepository {

    override fun getAllSupplements(): Flow<List<Supplement>> =
        supplementDao.getAllSupplementsFlow()
            .map { list -> list.map { it.toDomain() } }

    override fun getAllIngredients(): Flow<List<Ingredient>> =
        ingredientDao.getAllIngredientsFlow()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit
    ) {
        val timestamp = date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

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

    override suspend fun setHourZero(date: LocalDate, time: LocalTime) {
        dailyStartTimeDao.upsert(
            DailyStartTimeEntity(
                date = date.toString(),
                hourZero = time.toSecondOfDay()
            )
        )
    }

    override suspend fun getHourZero(date: LocalDate): LocalTime? {
        return dailyStartTimeDao.getStartTime(date.toString())
            ?.let { LocalTime.ofSecondOfDay(it.hourZero.toLong()) }
    }

    override suspend fun shouldTakeToday(supplement: Supplement, date: LocalDate): Boolean {
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

    override suspend fun getPredictedNextDoseTime(
        supplement: Supplement,
        date: LocalDate
    ): LocalTime? {
        val hourZero = getHourZero(date) ?: return null
        return supplement.offsetMinutes?.let { hourZero.plusMinutes(it.toLong()) }
    }

    override suspend fun getDailyIngredientSummary(
        date: LocalDate
    ): List<DailyIngredientSummary> {

        val logs = supplementDailyLogDao.getDoseLogsForDayOnce(date.toString())
        val supplements = supplementDao.getAllSupplementsWithIngredients()

        val lookup = supplements.associateBy { it.supplement.id }
        val totals = mutableMapOf<String, DailyIngredientSummary>()

        logs.forEach { log ->
            val supplement = lookup[log.supplementId] ?: return@forEach

            supplement.ingredients.forEach { item ->
                val taken = item.ingredient.amountPerServing * log.actualServingTaken

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

                entry.totalAmount += taken
            }
        }

        return totals.values.toList()
    }
}
