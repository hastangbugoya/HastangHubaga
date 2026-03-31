package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementNutritionDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleEntity
import com.example.hastanghubaga.data.local.entity.user.ScheduleTypeEntity
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.mappers.toMealNutritionFromNames
import com.example.hastanghubaga.data.local.mappers.toUserSupplementSettings
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.nutrition.DailyIngredientSummary
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementScheduleSpec
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
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
 * Scheduling note:
 * - Persisted user schedule intent now lives in [SupplementUserSettingsEntity] and is exposed as
 *   [SupplementWithUserSettings.scheduleSpec].
 * - Concrete daily times are still resolved later by downstream use cases into
 *   [SupplementWithUserSettings.scheduledTimes] for backward compatibility.
 * - Legacy timing fields on [Supplement] (such as doseAnchorType / offsetMinutes / frequencyType)
 *   are still retained, but should be treated as compatibility or dosage-guidance metadata rather
 *   than the preferred source of new scheduling behavior.
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
    private val supplementUserSettingsDao: SupplementUserSettingsDao,
    private val supplementNutritionDao: SupplementNutritionDao,
    private val supplementScheduleDao: SupplementScheduleDao
) : SupplementRepository, SupplementDoseLogRepository {

    /* ================================================== */
    /* TIMELINE FLOW (PRIMARY CONSUMER PATH)              */
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
     *     → combine(settings + schedules per supplement)
     *     → Flow<List<SupplementWithUserSettings>>
     *
     * Intermediary tables:
     * - supplements
     * - supplement_user_settings
     * - supplement_schedules
     * - supplement_schedule_fixed_times
     * - supplement_schedule_anchored_times
     *
     * Schedule behavior:
     * - Prefer persisted rows from supplement_schedules when present.
     * - Fall back to legacy user-settings CSV schedule intent when no persisted rows exist.
     * - FIXED schedules are converted into scheduledTimes for backward-compatible consumers.
     * - ANCHORED schedules are resolved into scheduledTimes by mapping TimeAnchor -> DoseAnchorType
     *   and reusing the existing event-time system.
     */
    override fun getSupplementsForDate(
        date: String
    ): Flow<List<SupplementWithUserSettings>> {
        return supplementDao.getActiveSupplementsFlow()
            .flatMapLatest { supplements ->
                if (supplements.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val targetDate = LocalDate.parse(date)

                    val supplementFlows: List<Flow<SupplementWithUserSettings>> = supplements.map { supplement ->
                        combine(
                            supplementUserSettingsDao.observeSettings(supplement.id),
                            supplementScheduleDao.observeSchedulesForSupplement(supplement.id)
                        ) { settings, schedules ->
                            settings to schedules
                        }.flatMapLatest { (settings, schedules) ->
                            flow {
                                emit(
                                    buildSupplementWithUserSettings(
                                        supplement = supplement,
                                        settings = settings,
                                        schedules = schedules,
                                        date = targetDate
                                    )
                                )
                            }
                        }
                    }

                    combine(supplementFlows) { results ->
                        results.toList()
                    }
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
            supplementUserSettingsDao.observeSettings(id),
            supplementScheduleDao.observeSchedulesForSupplement(id)
        ) { supplement, settings, schedules ->
            Triple(supplement, settings, schedules)
        }.flatMapLatest { (supplement, settings, schedules) ->
            flow {
                val today = Instant
                    .fromEpochMilliseconds(System.currentTimeMillis())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

                emit(
                    supplement?.let {
                        buildSupplementWithUserSettings(
                            supplement = it,
                            settings = settings,
                            schedules = schedules,
                            date = today
                        )
                    }
                )
            }
        }

    override fun observeSupplementNutritionForDate(dateMillis: Long): Flow<List<MealNutrition>> {
        val (startMillis, endMillis) = dayRangeUtcMillis(dateMillis)

        return supplementNutritionDao
            .observeSupplementLogNutrientsInRange(startMillis, endMillis)
            .map { rows ->
                rows
                    .groupBy { it.logId }
                    .values
                    .map { perLogRows -> perLogRows.toMealNutritionFromNames() }
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
                        ingredientId = item.ingredient.ingredientId,
                        name = key,
                        amount = 0.0,
                        unit = item.ingredient.unit,
                    )
                }

                entry.amount += taken
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
     *
     * Compatibility note:
     * - This still uses legacy frequency fields currently stored on [Supplement].
     * - Recurrence redesign is intentionally out of scope for the current anchor-based
     *   supplement scheduling work.
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

    private fun dayRangeUtcMillis(dateMillis: Long): Pair<Long, Long> {
        val tz = TimeZone.currentSystemDefault()

        val localDate = Instant
            .fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(tz)
            .date

        val startMillis = localDate.atStartOfDayIn(tz).toEpochMilliseconds()
        val endMillis = localDate
            .plus(DatePeriod(days = 1))
            .atStartOfDayIn(tz)
            .toEpochMilliseconds()

        return startMillis to endMillis
    }

    private suspend fun buildSupplementWithUserSettings(
        supplement: SupplementEntity,
        settings: SupplementUserSettingsEntity?,
        schedules: List<SupplementScheduleEntity>,
        date: LocalDate
    ): SupplementWithUserSettings {
        val applicableSchedules = schedules
            .filter { it.isEnabled }
            .filter { it.isActiveOn(date) }

        val scheduledTimes = if (applicableSchedules.isNotEmpty()) {
            buildScheduledTimesFromPersistedSchedules(
                schedules = applicableSchedules,
                date = date
            )
        } else {
            emptyList()
        }

        val scheduleSpec = if (applicableSchedules.isNotEmpty()) {
            buildScheduleSpecFromPersistedSchedules(applicableSchedules)
                ?: settings?.toScheduleSpec()
        } else {
            settings?.toScheduleSpec()
        }

        return SupplementWithUserSettings(
            supplement = supplement.toDomain(),
            userSettings = settings?.toUserSupplementSettings(),
            doseState = MealAwareDoseState.Unknown,
            scheduledTimes = scheduledTimes,
            scheduleSpec = scheduleSpec
        )
    }

    private suspend fun buildScheduledTimesFromPersistedSchedules(
        schedules: List<SupplementScheduleEntity>,
        date: LocalDate
    ): List<LocalTime> {
        val fixedTimes = schedules
            .filter { it.timingType == ScheduleTimingType.FIXED }
            .flatMap { schedule ->
                supplementScheduleDao.getFixedTimesForSchedule(schedule.id)
            }
            .sortedWith(
                compareBy(
                    { it.sortOrder },
                    { it.time.hour },
                    { it.time.minute },
                    { it.id }
                )
            )
            .map { it.time }

        val anchoredTimes = schedules
            .filter { it.timingType == ScheduleTimingType.ANCHORED }
            .flatMap { schedule ->
                supplementScheduleDao.getAnchoredTimesForSchedule(schedule.id)
            }
            .sortedWith(
                compareBy(
                    { it.sortOrder },
                    { it.id }
                )
            )
            .mapNotNull { anchored ->
                resolveAnchoredScheduleTime(
                    anchored = anchored,
                    date = date
                )
            }

        return (fixedTimes + anchoredTimes)
            .distinct()
            .sorted()
    }

    /**
     * Best-effort schedule intent reconstruction from persisted schedule rows.
     *
     * Current mapping behavior:
     * - FIXED schedules -> SupplementScheduleSpec.FixedTimes
     * - ANCHORED schedules are not converted here because persisted rows use TimeAnchor,
     *   while the current domain schedule spec only models MealType-based anchoring.
     *
     * Concrete anchored daily times are still resolved above into scheduledTimes.
     */
    private suspend fun buildScheduleSpecFromPersistedSchedules(
        schedules: List<SupplementScheduleEntity>
    ): SupplementScheduleSpec? {
        val fixedTimes = schedules
            .filter { it.timingType == ScheduleTimingType.FIXED }
            .flatMap { schedule ->
                supplementScheduleDao.getFixedTimesForSchedule(schedule.id)
            }
            .sortedWith(
                compareBy(
                    { it.sortOrder },
                    { it.time.hour },
                    { it.time.minute },
                    { it.id }
                )
            )
            .map { it.time }
            .distinct()

        if (fixedTimes.isNotEmpty()) {
            return SupplementScheduleSpec.FixedTimes(times = fixedTimes)
        }

        return null
    }

    private suspend fun resolveAnchoredScheduleTime(
        anchored: SupplementScheduleAnchoredTimeEntity,
        date: LocalDate
    ): LocalTime? {
        val doseAnchor = anchored.anchor.toDoseAnchorTypeOrNull() ?: return null
        val baseTime = getEventTime(doseAnchor, date) ?: return null
        return baseTime.plusMinutesClamped(anchored.offsetMinutes)
    }

    private fun TimeAnchor.toDoseAnchorTypeOrNull(): DoseAnchorType? {
        return when (this) {
            TimeAnchor.MIDNIGHT -> DoseAnchorType.MIDNIGHT
            TimeAnchor.WAKEUP -> DoseAnchorType.WAKEUP
            TimeAnchor.BREAKFAST -> DoseAnchorType.BREAKFAST
            TimeAnchor.LUNCH -> DoseAnchorType.LUNCH
            TimeAnchor.DINNER -> DoseAnchorType.DINNER
            TimeAnchor.BEFORE_WORKOUT -> DoseAnchorType.BEFORE_WORKOUT
            TimeAnchor.DURING_WORKOUT -> null
            TimeAnchor.AFTER_WORKOUT -> DoseAnchorType.AFTER_WORKOUT
            TimeAnchor.SLEEP -> DoseAnchorType.SLEEP
        }
    }

    private fun LocalTime.plusMinutesClamped(minutesToAdd: Int): LocalTime {
        val totalMinutes = hour * 60 + minute + minutesToAdd
        val clampedMinutes = totalMinutes.coerceIn(0, (23 * 60) + 59)
        val resolvedHour = clampedMinutes / 60
        val resolvedMinute = clampedMinutes % 60
        return LocalTime(hour = resolvedHour, minute = resolvedMinute)
    }

    private fun SupplementScheduleEntity.isActiveOn(date: LocalDate): Boolean {
        if (date < startDate) return false
        if (endDate != null && date > endDate) return false
        if (interval <= 0) return false

        return when (recurrenceType) {
            ScheduleRecurrenceType.DAILY -> {
                val daysBetween = daysBetween(startDate, date)
                daysBetween >= 0 && daysBetween % interval == 0
            }

            ScheduleRecurrenceType.WEEKLY -> {
                val weeklySet = weeklyDays ?: emptyList<DayOfWeek>()
                if (!weeklySet.contains(date.dayOfWeek)) {
                    false
                } else {
                    val daysBetween = daysBetween(startDate, date)
                    val weeksBetween = daysBetween / 7
                    weeksBetween >= 0 && weeksBetween % interval == 0
                }
            }
        }
    }

    private fun daysBetween(start: LocalDate, end: LocalDate): Int {
        val startEpochDay = start.toJavaLocalDate().toEpochDay()
        val endEpochDay = end.toJavaLocalDate().toEpochDay()
        return (endEpochDay - startEpochDay).toInt()
    }

    /**
     * Maps persisted user scheduling settings into the domain-level supplement schedule intent.
     *
     * Rules:
     * - FIXED_TIMES requires at least one parsable time, otherwise returns null
     * - MEAL_ANCHORED requires at least one supported meal type, otherwise returns null
     *
     * Returning null means downstream scheduling code will fall back to the legacy supplement
     * timing fields during the transition period.
     */
    private fun SupplementUserSettingsEntity.toScheduleSpec(): SupplementScheduleSpec? {
        return when (scheduleType) {
            ScheduleTypeEntity.FIXED_TIMES -> {
                val times = parseFixedTimesCsv(fixedTimesCsv)
                if (times.isEmpty()) {
                    null
                } else {
                    SupplementScheduleSpec.FixedTimes(times = times)
                }
            }

            ScheduleTypeEntity.MEAL_ANCHORED -> {
                val mealTypes = parseMealTypesCsv(mealTypesCsv)
                if (mealTypes.isEmpty()) {
                    null
                } else {
                    SupplementScheduleSpec.MealAnchored(
                        mealTypes = mealTypes,
                        offsetMinutes = mealOffsetMinutes ?: 0
                    )
                }
            }
        }
    }

    private fun parseFixedTimesCsv(csv: String?): List<LocalTime> {
        if (csv.isNullOrBlank()) return emptyList()

        return csv.split(",")
            .mapNotNull { token ->
                parseLocalTimeOrNull(token.trim())
            }
            .distinct()
            .sorted()
    }

    private fun parseMealTypesCsv(csv: String?): Set<MealType> {
        if (csv.isNullOrBlank()) return emptySet()

        return csv.split(",")
            .mapNotNull { token ->
                runCatching { MealType.valueOf(token.trim()) }.getOrNull()
            }
            .toSet()
    }

    private fun parseLocalTimeOrNull(value: String): LocalTime? {
        val parts = value.split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23) return null
        if (minute !in 0..59) return null

        return LocalTime(hour = hour, minute = minute)
    }
}