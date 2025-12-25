package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.mappers.toUserSupplementSettings
import com.example.hastanghubaga.domain.model.widget.DailyIngredientSummary
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinInstant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Central repository for Supplements.
 *
 * Responsibilities:
 * - Timeline construction (active supplements + user settings)
 * - Ingredient aggregation
 * - Dose logging
 * - Scheduling / anchor-time resolution
 *
 * NOTE:
 * - Timeline-related flows are intentionally placed FIRST because they are
 *   performance-sensitive and heavily composed downstream.
 */
class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementEntityDao,
    private val ingredientDao: IngredientEntityDao,
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val dailyStartTimeDao: DailyStartTimeDao,
    private val eventTimeDao: EventTimeDao,
    private val supplementUserSettingsDao: SupplementUserSettingsDao
) : SupplementRepository, SupplementDoseLogRepository {

    /* ================================================== */
    /* TIMELINE FLOW (PRIMARY CONSUMER PATH)               */
    /* ================================================== */

    /**
     * Returns all ACTIVE supplements for a given date,
     * enriched with user settings.
     *
     * Important:
     * - `date` is NOT applied at the SQL level.
     * - Date-based filtering happens later in the use-case:
     *   `shouldTakeToday(date)`
     *
     * Flow shape:
     *   Flow<List<SupplementEntity>>
     *     → flatMapLatest
     *     → combine(settings per supplement)
     *     → Flow<List<SupplementWithUserSettings>>
     *
     * Intermediary tables:
     * - supplements
     * - supplement_user_settings
     */
    override fun getSupplementsForDate(
        date: String
    ): Flow<List<SupplementWithUserSettings>> {
        return supplementDao.getActiveSupplementsFlow()
            .flatMapLatest { supplements ->
                if (supplements.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        supplements.map { supplement ->
                            supplementUserSettingsDao
                                .observeSettings(supplement.id)
                                .map { settings ->
                                    SupplementWithUserSettings(
                                        supplement = supplement.toDomain(),
                                        userSettings = settings?.toUserSupplementSettings(),
                                        doseState = MealAwareDoseState.Unknown
                                    )
                                }
                        }
                    ) { it.toList() }
                }
            }
    }

    /**
     * Observe a single supplement with live user settings.
     * Used by detail screens.
     */
    override fun observeSupplement(id: Long): Flow<SupplementWithUserSettings?> =
        combine(
            supplementDao.observeSupplementById(id),
            supplementUserSettingsDao.observeSettings(id)
        ) { supplement, settings ->
            supplement?.let {
                SupplementWithUserSettings(
                    supplement = it.toDomain(),
                    userSettings = settings?.toUserSupplementSettings(),
                    doseState = MealAwareDoseState.Unknown
                )
            }
        }

    /* ================================================== */
    /* SUPPLEMENT LISTING                                 */
    /* ================================================== */

    override fun getAllSupplements(): Flow<List<Supplement>> =
        supplementDao.getAllSupplementsFlow()
            .map { it.map(SupplementEntity::toDomain) }

    override suspend fun getAllSupplementsOnce(): List<Supplement> =
        supplementDao.getAllSupplementsOnce()
            .map(SupplementEntity::toDomain)

    override suspend fun getActiveSupplementsOrderedByOffset(): List<Supplement> =
        supplementDao.getActiveSupplementsOrderedByOffset()
            .map(SupplementEntity::toDomain)

    override fun getActiveSupplements(): Flow<List<Supplement>> =
        supplementDao.getActiveSupplementsFlow()
            .map { it.map(SupplementEntity::toDomain) }

    override fun isActive(supplement: Supplement): Boolean =
        supplement.isActive

    /* ================================================== */
    /* INGREDIENT AGGREGATION                             */
    /* ================================================== */

    override fun getAllIngredients(): Flow<List<Ingredient>> =
        ingredientDao.getAllIngredientsFlow()
            .map { it.map(IngredientEntity::toDomain) }

    /**
     * Aggregates ingredient totals for a day by joining:
     * - supplement_daily_log
     * - supplements_with_ingredients
     *
     * This is a READ-ONLY aggregation.
     */
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

    /* ================================================== */
    /* DOSE LOGGING                                       */
    /* ================================================== */

    /**
     * Inserts a dose log row.
     *
     * Intermediary table:
     * - supplement_daily_log
     */
    override suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit
    ) {
        val timestamp = date
            .toJavaLocalDate()
            .atTime(time.toJavaLocalTime())
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

    /* ================================================== */
    /* SCHEDULING / EVENT TIMES                           */
    /* ================================================== */

    override suspend fun setHourZero(date: LocalDate, time: LocalTime) {
        dailyStartTimeDao.upsert(
            DailyStartTimeEntity(
                date = date.toString(),
                hourZero = time.toSecondOfDay()
            )
        )
    }

    override suspend fun getHourZero(date: LocalDate): LocalTime? =
        dailyStartTimeDao.getStartTime(date.toString())
            ?.let { LocalTime.fromSecondOfDay(it.hourZero) }

    /**
     * Determines if a supplement should be taken on a date.
     *
     * NOTE:
     * This is intentionally domain-level logic, not SQL.
     */
    override suspend fun shouldTakeToday(
        supplement: Supplement,
        date: LocalDate
    ): Boolean {
        return when (supplement.frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.WEEKLY ->
                supplement.weeklyDays?.contains(date.dayOfWeek) ?: false

            FrequencyType.EVERY_X_DAYS -> {
                val interval = supplement.frequencyInterval ?: return false
                val last = supplement.lastTakenDate?.let(LocalDate::parse)
                val start = supplement.startDate?.let(LocalDate::parse)

                val due = last?.plus(DatePeriod(days = interval)) ?: start
                date == due
            }
        }
    }

    override suspend fun getPredictedNextDoseTime(
        supplement: Supplement,
        date: LocalDate
    ): LocalTime? =
        getHourZero(date)?.let { hz ->
            supplement.offsetMinutes?.let { mins ->
                LocalTime.fromSecondOfDay(
                    hz.toSecondOfDay() + mins * 60
                )
            }
        }

    override suspend fun getNextDoseDateTime(
        supplement: Supplement
    ): Instant? {
        val zone = ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val now = ZonedDateTime.now(zone)

        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            val kxDate = LocalDate.parse(date.toString())

            if (!shouldTakeToday(supplement, kxDate)) continue

            val base = getEventTime(supplement.doseAnchorType, kxDate) ?: continue
            val offset = supplement.offsetMinutes ?: 0

            val candidate = date
                .atTime(base.toJavaLocalTime())
                .plusMinutes(offset.toLong())
                .atZone(zone)

            if (i == 0 && candidate.isBefore(now)) continue
            return candidate.toInstant().toKotlinInstant()
        }

        return null
    }

    override suspend fun getEventTime(
        anchor: DoseAnchorType,
        date: LocalDate
    ): LocalTime? {
        if (anchor == DoseAnchorType.ANYTIME) return null

        eventTimeDao.getOverride(date.toString(), anchor)
            ?.let { return LocalTime.fromSecondOfDay(it.timeSeconds) }

        eventTimeDao.getDefault(anchor)
            ?.let { return LocalTime.fromSecondOfDay(it.timeSeconds) }

        return if (anchor == DoseAnchorType.MIDNIGHT)
            LocalTime.fromSecondOfDay(0)
        else null
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

    override suspend fun removeEventOverride(
        date: LocalDate,
        anchor: DoseAnchorType
    ) {
        eventTimeDao.removeOverride(date.toString(), anchor)
    }

    /* ================================================== */
    /* USER SETTINGS (FUTURE)                             */
    /* ================================================== */

    override suspend fun updateUserPreferredDose(
        supplementId: Long,
        dose: Double,
        unit: SupplementDoseUnit
    ) {
        Log.d(
            "Meow",
            "SupplementRepositoryImpl> updateUserPreferredDose NOT IMPLEMENTED: $supplementId, $dose, $unit"
        )
    }

    override suspend fun setDefaultEventTime(
        anchor: DoseAnchorType,
        time: LocalTime
    ) {
        // Intentionally no-op.
        // Default event times are not user-editable yet.
    }
}
