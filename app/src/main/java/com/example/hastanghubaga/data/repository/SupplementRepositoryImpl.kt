package com.example.hastanghubaga.data.repository

import androidx.room.util.joinIntoString
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.entity.supplement.DailyIngredientSummary
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementWithSettings
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.models.SupplementJoinedRoom
import com.example.hastanghubaga.data.local.models.toDomainSafe
import com.example.hastanghubaga.domain.model.Ingredient
import com.example.hastanghubaga.domain.model.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementEntityDao,
    private val ingredientDao: IngredientEntityDao,
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val dailyStartTimeDao: DailyStartTimeDao,
    private val eventTimeDao: EventTimeDao,
    private val supplementUserSettingsDao: SupplementUserSettingsDao
) : SupplementRepository {

    override fun getAllSupplements(): Flow<List<Supplement>> =
        supplementDao.getAllSupplementsFlow()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getAllSupplementsOnce(): List<Supplement> {
        return supplementDao.getAllSupplementsOnce()
            .map { it.toDomain() }
    }

    override fun getActiveSupplements(): Flow<List<Supplement>> =
        supplementDao.getActiveSupplementsFlow()
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

    override fun isActive(supplement: Supplement): Boolean = supplement.isActive

    override suspend fun shouldTakeToday(
        supplement: Supplement,
        date: LocalDate
    ): Boolean {

        return when (supplement.frequencyType) {

            FrequencyType.DAILY -> true

            FrequencyType.EVERY_X_DAYS -> {
                val interval = supplement.frequencyInterval ?: return false

                val last = supplement.lastTakenDate?.let { LocalDate.parse(it) }
                val start = supplement.startDate?.let { LocalDate.parse(it) }

                val dueDate = when {
                    last != null -> last.plusDays(interval.toLong())
                    start != null -> start
                    else -> return false
                }

                date == dueDate
            }

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

    override suspend fun getNextDoseDateTime(
        supplement: Supplement
    ): ZonedDateTime? {

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val now = ZonedDateTime.now(zone)

        val anchor = supplement.doseAnchorType
        val offset = supplement.offsetMinutes ?: 0

        for (i in 0 until 7) {
            val date = today.plusDays(i.toLong())

            // Should take on this day?
            if (!shouldTakeToday(supplement, date))
                continue

            val baseTime = getEventTime(anchor, date)
                ?: continue

            var doseDateTime = date
                .atTime(baseTime)
                .plusMinutes(offset.toLong())
                .atZone(zone)

            if (i == 0 && doseDateTime.isBefore(now)) {
                continue // today's time passed → try tomorrow
            }

            return doseDateTime
        }

        return null
    }


//    override suspend fun getAnchorTime(
//        anchor: DoseAnchorType,
//        date: LocalDate
//    ): LocalTime? {
//        TODO("Not yet implemented")
//    }

    override fun nextDoseDate(supp: SupplementEntity): LocalDate {
        val interval = supp.frequencyInterval ?: 1

        // If user has taken at least once → use sliding schedule
        supp.lastTakenDate?.let {
            val last = LocalDate.parse(it)
            return last.plusDays(interval.toLong())
        }

        // If never taken but startDate exists → next due = startDate
        supp.startDate?.let {
            val start = LocalDate.parse(it)
            return start
        }

        // Neither is set → cannot schedule yet
        return LocalDate.MAX // or null
    }

    override suspend fun setDefaultEventTime(anchor: DoseAnchorType, time: LocalTime) {
        eventTimeDao.upsertDefault(
            EventDefaultTimeEntity(
                anchor = anchor,
                timeSeconds = time.toSecondOfDay()
            )
        )
    }

    override suspend fun getEventTime(
        anchor: DoseAnchorType,
        date: LocalDate
    ): LocalTime? {
        if (anchor == DoseAnchorType.ANYTIME)
            return null

        // 1. Daily override exists?
        eventTimeDao.getOverride(date.toString(), anchor)?.let { override ->
            return LocalTime.ofSecondOfDay(override.timeSeconds.toLong())
        }

        // 2. Default time exists?
        eventTimeDao.getDefault(anchor)?.let { def ->
            return LocalTime.ofSecondOfDay(def.timeSeconds.toLong())
        }

        // 3. MIDNIGHT fallback
        return if (anchor == DoseAnchorType.MIDNIGHT) LocalTime.MIDNIGHT else null
    }

    override suspend fun overrideEventTime(
        date: LocalDate,
        anchor: DoseAnchorType,
        time: LocalTime
    ) {
        eventTimeDao.upsertOverride(
            EventDailyOverrideEntity(
                date = date.toString(),
                anchor = anchor,
                timeSeconds = time.toSecondOfDay()
            )
        )
    }

    override suspend fun removeEventOverride(date: LocalDate, anchor: DoseAnchorType) {
        eventTimeDao.removeOverride(date.toString(), anchor)
    }

//    override suspend fun getSupplementWithUserSettings(id: Long): SupplementWithSettings {
//        val base = supplementDao.getSupplementById(id)
//        val settings = supplementUserSettingsDao.getSettings(id)
//
//        return SupplementWithSettings(base!!, settings)
//    }

    override fun observeSupplementWithUserSettings(id: Long): Flow<SupplementWithSettings?> =
        supplementDao.observeSupplementWithSettings(id)
            .map { join -> join.toDomainSafe() }

    override suspend fun getSupplementWithUserSettings(id: Long): SupplementWithSettings? =
        supplementDao.getSupplementWithSettings(id)?.toDomainSafe()

    override suspend fun updateUserPreferredDose(
        supplementId: Long,
        dose: Double,
        unit: SupplementDoseUnit
    ) {
        supplementUserSettingsDao.upsert(
            SupplementUserSettingsEntity(
                supplementId = supplementId,
                preferredServingSize = dose,
                preferredUnit = unit
            )
        )
    }

}
