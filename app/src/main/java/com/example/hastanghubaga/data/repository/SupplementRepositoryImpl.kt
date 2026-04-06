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
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.user.ScheduleTypeEntity
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.mappers.toMealNutritionFromNames
import com.example.hastanghubaga.data.local.mappers.toUserSupplementSettings
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.nutrition.DailyIngredientSummary
import com.example.hastanghubaga.domain.model.supplement.AnchoredRow
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.ResolvedSupplementScheduleEntry
import com.example.hastanghubaga.domain.model.supplement.ResolvedSupplementTimingType
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.model.supplement.SupplementScheduleSpec
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogReadRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
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

class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementEntityDao,
    private val ingredientDao: IngredientEntityDao,
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val dailyStartTimeDao: DailyStartTimeDao,
    private val eventTimeDao: EventTimeDao,
    private val supplementUserSettingsDao: SupplementUserSettingsDao,
    private val supplementNutritionDao: SupplementNutritionDao,
    private val supplementScheduleDao: SupplementScheduleDao
) : SupplementRepository, SupplementDoseLogRepository, SupplementDoseLogReadRepository {

    override fun getSupplementsForDate(
        date: String
    ): Flow<List<SupplementWithUserSettings>> {
        return supplementDao.getActiveSupplementsFlow()
            .flatMapLatest { supplements ->
                if (supplements.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val targetDate = LocalDate.parse(date)

                    val supplementFlows: List<Flow<SupplementWithUserSettings>> =
                        supplements.map { supplement ->
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

    override fun observeDoseLogsForDate(
        date: LocalDate
    ): Flow<List<SupplementDoseLog>> {
        return supplementDailyLogDao
            .getDoseLogsForDay(date.toString())
            .map { rows ->
                rows.map(SupplementDailyLogEntity::toDomain)
            }
    }

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

    override fun getAllIngredients(): Flow<List<Ingredient>> =
        ingredientDao.observeAllIngredients()
            .map { it.map(IngredientEntity::toDomain) }

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

    override suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit,
        occurrenceId: String?
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
                timestamp = timestamp,
                occurrenceId = occurrenceId
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

    override suspend fun getHourZero(date: LocalDate): LocalTime? =
        dailyStartTimeDao.getStartTime(date.toString())
            ?.let { LocalTime.fromSecondOfDay(it.hourZero) }

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

        val persistedFixedEntries = buildResolvedFixedEntriesFromPersistedSchedules(
            supplementId = supplement.id,
            schedules = applicableSchedules,
            date = date
        )

        val persistedScheduleSpec = buildPersistedScheduleSpecFromSchedules(
            schedules = applicableSchedules
        )

        val fallbackScheduleSpec =
            if (applicableSchedules.isEmpty()) {
                settings?.toScheduleSpec()
            } else {
                persistedScheduleSpec ?: settings?.toScheduleSpec()
            }

        val fallbackResolvedEntries =
            if (persistedFixedEntries.isEmpty()) {
                buildResolvedEntriesFromFallbackScheduleSpec(
                    supplementId = supplement.id,
                    scheduleSpec = fallbackScheduleSpec,
                    date = date
                )
            } else {
                emptyList()
            }

        val resolvedEntries = (persistedFixedEntries + fallbackResolvedEntries)
            .distinctBy { entry ->
                listOf(
                    entry.scheduleId,
                    entry.sourceRowId,
                    entry.time.toSecondOfDay(),
                    entry.timingType,
                    entry.anchor,
                    entry.label,
                    entry.sortOrder
                )
            }
            .sortedWith(
                compareBy<ResolvedSupplementScheduleEntry>({ it.time })
                    .thenBy { it.sortOrder }
                    .thenBy { it.label ?: "" }
                    .thenBy { it.scheduleId ?: Long.MAX_VALUE }
                    .thenBy { it.sourceRowId ?: Long.MAX_VALUE }
            )

        val scheduledTimes = resolvedEntries
            .map { it.time }
            .distinct()

        return SupplementWithUserSettings(
            supplement = supplement.toDomain(),
            userSettings = settings?.toUserSupplementSettings(),
            doseState = MealAwareDoseState.Unknown,
            scheduledTimes = scheduledTimes,
            scheduleSpec = fallbackScheduleSpec,
            resolvedScheduleEntries = resolvedEntries
        )
    }

    /**
     * Builds concrete same-day fixed-time outputs from persisted schedule rows.
     *
     * This preserves schedule identity and child-row identity instead of flattening
     * everything into a single plain time list too early.
     */
    private suspend fun buildResolvedFixedEntriesFromPersistedSchedules(
        supplementId: Long,
        schedules: List<SupplementScheduleEntity>,
        date: LocalDate
    ): List<ResolvedSupplementScheduleEntry> {
        val fixedSchedules = schedules
            .filter { it.timingType == ScheduleTimingType.FIXED }

        if (fixedSchedules.isEmpty()) return emptyList()

        val fixedRows = fixedSchedules
            .flatMap { schedule ->
                supplementScheduleDao
                    .getFixedTimesForSchedule(schedule.id)
                    .map { row -> schedule to row }
            }
            .sortedWith(
                compareBy<Pair<SupplementScheduleEntity, SupplementScheduleFixedTimeEntity>>(
                    { it.first.id },
                    { it.second.sortOrder },
                    { it.second.time.hour },
                    { it.second.time.minute },
                    { it.second.id }
                )
            )

        return fixedRows.map { (schedule, row) ->
            ResolvedSupplementScheduleEntry(
                scheduleId = schedule.id,
                sourceRowId = row.id,
                occurrenceId = buildOccurrenceId(
                    date = date,
                    supplementId = supplementId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = row.time,
                    sortOrder = row.sortOrder
                ),
                time = row.time,
                timingType = ResolvedSupplementTimingType.FIXED,
                anchor = null,
                label = row.label,
                sortOrder = row.sortOrder
            )
        }
    }

    /**
     * Builds persisted schedule intent without collapsing anchored child rows into
     * a shared-offset representation.
     *
     * Important:
     * - Fixed persisted schedules already flow through [resolvedScheduleEntries].
     * - Anchored persisted schedules are emitted as [SupplementScheduleSpec.AnchoredRows]
     *   so the use case can resolve them later with anchor context while preserving
     *   row-level offset/label/order fidelity.
     */
    private suspend fun buildPersistedScheduleSpecFromSchedules(
        schedules: List<SupplementScheduleEntity>
    ): SupplementScheduleSpec? {
        val anchoredSchedules = schedules
            .filter { it.timingType == ScheduleTimingType.ANCHORED }

        if (anchoredSchedules.isEmpty()) {
            return null
        }

        val anchoredRows = anchoredSchedules
            .flatMap { schedule ->
                supplementScheduleDao.getAnchoredTimesForSchedule(schedule.id)
            }
            .sortedWith(
                compareBy<SupplementScheduleAnchoredTimeEntity>(
                    { it.scheduleId },
                    { it.sortOrder },
                    { it.id }
                )
            )

        if (anchoredRows.isEmpty()) {
            return null
        }

        return SupplementScheduleSpec.AnchoredRows(
            rows = anchoredRows.map { row ->
                AnchoredRow(
                    anchor = row.anchor,
                    offsetMinutes = row.offsetMinutes,
                    label = row.label,
                    sortOrder = row.sortOrder
                )
            }
        )
    }

    private fun buildResolvedEntriesFromFallbackScheduleSpec(
        supplementId: Long,
        scheduleSpec: SupplementScheduleSpec?,
        date: LocalDate
    ): List<ResolvedSupplementScheduleEntry> {
        return when (scheduleSpec) {
            is SupplementScheduleSpec.FixedTimes -> {
                scheduleSpec.times
                    .distinct()
                    .sorted()
                    .mapIndexed { index, time ->
                        ResolvedSupplementScheduleEntry(
                            scheduleId = null,
                            sourceRowId = null,
                            occurrenceId = buildOccurrenceId(
                                date = date,
                                supplementId = supplementId,
                                scheduleId = null,
                                sourceRowId = null,
                                time = time,
                                sortOrder = index
                            ),
                            time = time,
                            timingType = ResolvedSupplementTimingType.LEGACY,
                            anchor = null,
                            label = null,
                            sortOrder = index
                        )
                    }
            }

            else -> emptyList()
        }
    }

    private fun buildOccurrenceId(
        date: LocalDate,
        supplementId: Long,
        scheduleId: Long?,
        sourceRowId: Long?,
        time: LocalTime,
        sortOrder: Int
    ): String {
        return listOf(
            date.toString(),
            supplementId.toString(),
            scheduleId?.toString() ?: "ns",
            sourceRowId?.toString() ?: "nr",
            time.toSecondOfDay().toString(),
            sortOrder.toString()
        ).joinToString(separator = "|")
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