package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.domain.model.supplement.DailyIngredientSummary
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
import com.example.hastanghubaga.data.local.mappers.toSupplementSettings
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.data.local.mappers.toUserSupplementSettings
import com.example.hastanghubaga.data.local.models.toDomainSafe
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.supplement.UserSupplementSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
/**
 * Concrete implementation of [SupplementRepository] responsible for coordinating all
 * supplement-related persistence, scheduling logic, dosage logs, ingredient aggregation,
 * and user-customizable supplement settings.
 *
 * ## Purpose
 * This repository acts as the *single source of truth* for supplement data. It merges:
 *
 * - Room DAOs (`SupplementEntityDao`, `IngredientEntityDao`, etc.)
 * - User customization (`SupplementUserSettingsDao`)
 * - Timing systems (event anchors, overrides, hour-zero rules)
 * - Dose logging and historical intake
 * - Daily nutrient aggregation (e.g., mg of Vitamin C consumed today)
 *
 * Higher-level features such as:
 * - Today screen
 * - Next-dose prediction
 * - Ingredient tracking
 * - Personalized supplement schedules
 *
 * all rely on this repository.
 *
 * ## Major Responsibilities
 *
 * ### 1. Supplement Management
 * - Fetch all supplements (active or inactive)
 * - Merge settings via [SupplementWithSettings]
 * - Provide domain models instead of Room entities
 *
 * ### 2. Ingredient System
 * - Return ingredient lists
 * - Compute per-day totals via [DailyIngredientSummary]
 *
 * ### 3. Dose Logging
 * - Insert historical dose entries with timestamps
 * - Query logs per day
 * - Support accuracy for analytics and prediction
 *
 * ### 4. Scheduling & Timing Logic
 * - Frequency types: DAILY, WEEKLY, EVERY_X_DAYS
 * - Dose anchor types (MIDNIGHT, BREAKFAST, CAFFEINE, etc.)
 * - Per-day "hour zero" override
 * - Per-event override + global defaults
 * - Predict next dose date/time
 *
 * ### 5. User Customization
 * - Store preferred serving size, units, and active state
 * - Override bottle-label recommendations
 * - Support dynamic customization per supplement
 *
 * ## Relationships
 * This repository depends on multiple DAOs:
 *
 * - [SupplementEntityDao] — core supplement records
 * - [IngredientEntityDao] — ingredient master table
 * - [SupplementDailyLogDao] — logs for daily actual intake
 * - [DailyStartTimeDao] — per-day "starting hour" anchor
 * - [EventTimeDao] — default & daily override event timing
 * - [SupplementUserSettingsDao] — per-supplement user preferences
 *
 * All public methods return:
 * - Domain models (`Supplement`, `Ingredient`, `DailyIngredientSummary`)
 * - `Flow<T>` for observable values
 * - Suspend functions for database updates
 *
 * ## Usage
 * Typically injected through Hilt:
 *
 * ```kotlin
 * @Inject lateinit var repository: SupplementRepository
 * ```
 *
 * and consumed by:
 *
 * - ViewModels
 * - Use cases
 * - Widgets
 * - The daily schedule engine
 *
 * ## Threading
 * All DB operations run inside Room’s dispatcher. Some lightweight transformations
 * occur on the calling coroutine context.
 *
 * ## Implementation Notes
 * - Does *not* expose Room entities to upper layers.
 * - All mapping to domain is handled via extension functions (`toDomain`, `toDomainSafe`).
 * - Frequency and event logic must remain consistent across viewmodels and UIs.
 *
 * DATA FLOW OVERVIEW
 *
 * [Room DAOs]
 *      ↓
 * [Repository (this class)]
 *      ↓
 * [Domain Models]
 *      ↓
 * [UseCases]
 *      ↓
 * [ViewModels]
 *      ↓
 * [Compose UI]
 *
 * This class is the ONLY place where:
 * - Multiple tables are joined
 * - Time-based rules are applied
 * - Domain models are assembled
 */

class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementEntityDao,
    private val ingredientDao: IngredientEntityDao,
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val dailyStartTimeDao: DailyStartTimeDao,
    private val eventTimeDao: EventTimeDao,
    private val supplementUserSettingsDao: SupplementUserSettingsDao
) : SupplementRepository {

    /**
     * Returns a live stream of all supplements in the system.
     *
     * @return Flow emitting the full list of supplements mapped to domain models.
     */
    override fun getAllSupplements(): Flow<List<Supplement>> =
        supplementDao.getAllSupplementsFlow()
            .map { list -> list.map { it.toSupplementSettings() } }

    /**
     * Fetches all supplements once (non-reactive).
     * Useful for background work, exports, or one-time calculations.
     *
     * @return List of all supplements as domain models.
     */
    override suspend fun getAllSupplementsOnce(): List<Supplement> {
        return supplementDao.getAllSupplementsOnce()
            .map { it.toSupplementSettings() }
    }

    /**
     * Returns all active supplements ordered by their configured offset time.
     * This is typically used for "today's schedule" ordering.
     *
     * @return List of active supplements sorted by dose offset.
     */
    override suspend fun getActiveSupplementsOrderedByOffset(): List<Supplement> =
        supplementDao.getActiveSupplementsOrderedByOffset()
            .map { it.toSupplementSettings() }

    /**
     * Observes only supplements that are currently active.
     *
     * @return Flow emitting active supplements as domain models.
     */
    override fun getActiveSupplements(): Flow<List<Supplement>> =
        supplementDao.getActiveSupplementsFlow()
            .map { list -> list.map { it.toSupplementSettings() } }

    /**
     * Observes all ingredients stored in the database.
     *
     * @return Flow emitting ingredient domain models.
     */
    override fun getAllIngredients(): Flow<List<Ingredient>> =
        ingredientDao.getAllIngredientsFlow()
            .map { list -> list.map { it.toSupplementSettings() } }

    /**
     * Logs that a supplement dose was taken at a specific date and time.
     * This preserves historical accuracy for analytics and summaries.
     *
     * @param supplementId ID of the supplement taken.
     * @param date Calendar date the dose was taken.
     * @param time Local time the dose was taken.
     * @param fractionTaken Portion of the recommended serving (e.g. 0.5, 1.0).
     * @param doseUnit Unit used when recording the dose.
     */
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

    /**
     * Sets the "hour zero" for a given day.
     * Hour zero is the baseline used for calculating anchored dose times.
     *
     * @param date The day being configured.
     * @param time The time considered as the day's starting reference.
     */
    override suspend fun setHourZero(date: LocalDate, time: LocalTime) {
        dailyStartTimeDao.upsert(
            DailyStartTimeEntity(
                date = date.toString(),
                hourZero = time.toSecondOfDay()
            )
        )
    }

    /**
     * Retrieves the configured hour zero for a given day.
     *
     * @param date The date to query.
     * @return LocalTime if set, or null if not configured.
     */
    override suspend fun getHourZero(date: LocalDate): LocalTime? {
        return dailyStartTimeDao.getStartTime(date.toString())
            ?.let { LocalTime.ofSecondOfDay(it.hourZero.toLong()) }
    }

    /**
     * Indicates whether a supplement is currently active.
     * Convenience helper for UI and scheduling logic.
     *
     * @param supplement Domain supplement model.
     * @return True if active, false otherwise.
     */
    override fun isActive(supplement: Supplement): Boolean = supplement.isActive

    /**
     * Determines whether a supplement should be taken on a given date
     * based on its frequency rules (daily, weekly, every X days).
     *
     * @param supplement Domain supplement model.
     * @param date Date to evaluate.
     * @return True if the supplement is due on that day.
     */
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

    /**
     * Calculates the predicted dose time for a supplement on a given day
     * using hour-zero and offset rules.
     *
     * @param supplement Domain supplement model.
     * @param date Date to calculate for.
     * @return LocalTime of the predicted dose, or null if not applicable.
     */
    override suspend fun getPredictedNextDoseTime(
        supplement: Supplement,
        date: LocalDate
    ): LocalTime? {
        val hourZero = getHourZero(date) ?: return null
        return supplement.offsetMinutes?.let { hourZero.plusMinutes(it.toLong()) }
    }

    /**
     * Aggregates all ingredient intake for a given day by summing
     * logged supplement doses and their ingredient compositions.
     *
     * @param date Day to summarize.
     * @return List of ingredient totals including RDA and upper limits.
     */
    override suspend fun getDailyIngredientSummary(
        date: LocalDate
    ): List<DailyIngredientSummary> {
        // ------------------------------------------------------------
        // INGREDIENT AGGREGATION
        //
        // This logic answers the question:
        // "How much of each nutrient did the user actually consume today?"
        //
        // Data sources:
        // • SupplementDailyLog (what was taken)
        // • SupplementIngredient (what each supplement contains)
        // • Ingredient RDA / UL (safety thresholds)
        // ------------------------------------------------------------

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

    /**
     * Determines the next upcoming date and time when a supplement
     * should be taken, looking up to 7 days ahead.
     *
     * @param supplement Domain supplement model.
     * @return ZonedDateTime of the next dose, or null if none found.
     */
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

    /**
     * Computes the next scheduled date for a supplement based on
     * its last taken date or configured start date.
     *
     * @param supp Database supplement entity.
     * @return The next due date.
     */
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

    /**
     * Sets the default time for a given dose anchor (e.g. breakfast).
     *
     * @param anchor The anchor type being configured.
     * @param time The default time for that anchor.
     */
    override suspend fun setDefaultEventTime(anchor: DoseAnchorType, time: LocalTime) {
        eventTimeDao.upsertDefault(
            EventDefaultTimeEntity(
                anchor = anchor,
                timeSeconds = time.toSecondOfDay()
            )
        )
    }

    /**
     * Resolves the effective event time for an anchor on a given date.
     * Priority order:
     * 1) Daily override
     * 2) Global default
     * 3) Midnight fallback (for MIDNIGHT anchor)
     *
     * @param anchor Anchor type to resolve.
     * @param date Date being evaluated.
     * @return LocalTime if resolved, otherwise null.
     */
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

    /**
     * Overrides the event time for a specific date and anchor.
     *
     * @param date Date to override.
     * @param anchor Anchor type.
     * @param time New time for that date.
     */
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

    /**
     * Removes a previously set daily override for an anchor.
     *
     * @param date Date to clear.
     * @param anchor Anchor type.
     */
    override suspend fun removeEventOverride(date: LocalDate, anchor: DoseAnchorType) {
        eventTimeDao.removeOverride(date.toString(), anchor)
    }

    /**
     * Observes a supplement merged with its user-specific settings.
     * Emits updates whenever either side changes.
     *
     * @param id Supplement ID.
     * @return Flow emitting the merged supplement + settings model.
     */
    override fun observeSupplementWithUserSettings(id: Long): Flow<SupplementWithSettings?> =
        supplementDao.observeSupplementWithSettings(id)
            .map { join -> join.toDomainSafe() }

    /**
     * Fetches a supplement merged with its user settings once.
     *
     * @param id Supplement ID.
     * @return SupplementWithSettings or null if not found.
     */
    override suspend fun getSupplementWithUserSettings(id: Long): SupplementWithSettings? =
    // ------------------------------------------------------------
    // INGREDIENT AGGREGATION
    //
    // This logic answers the question:
    // "How much of each nutrient did the user actually consume today?"
    //
    // Data sources:
    // • SupplementDailyLog (what was taken)
    // • SupplementIngredient (what each supplement contains)
    // • Ingredient RDA / UL (safety thresholds)
    // ------------------------------------------------------------
        supplementDao.getSupplementWithSettings(id)?.toDomainSafe()

    /**
     * Updates the user's preferred serving size and unit for a supplement.
     * Overrides the bottle-recommended values.
     *
     * @param supplementId Target supplement ID.
     * @param dose Preferred serving size.
     * @param unit Unit associated with the preferred dose.
     */
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

    /**
     * Returns all active supplements for the given date,
     * combined with any user-specific settings.
     *
     * PURPOSE:
     * --------
     * This is the primary data source for supplement list screens.
     * It merges:
     * - The base supplement definition (recommended dose, schedule)
     * - Optional user preferences (preferred dose, enabled/disabled)
     *
     * into a single, UI-ready domain model.
     *
     * @param date
     * ISO-8601 date string (yyyy-MM-dd).
     * Currently used for future scheduling logic; not all supplements
     * may depend on the date yet.
     *
     * @return
     * A Flow emitting a list of [SupplementWithUserSettings].
     *
     * FLOW BEHAVIOR:
     * --------------
     * - Emits when supplements change
     * - Emits when user settings change
     * - User settings may be null if the user has not customized a supplement
     *
     * NOTES:
     * ------
     * - This method deliberately lives in the repository to keep ViewModels simple.
     * - Initial emissions may occur before user settings exist.
     */
    override fun getSupplementsForDate(
        date: String
    ): Flow<List<SupplementWithUserSettings>> =
        combine(
            supplementDao.getActiveSupplementsFlow(),
            supplementUserSettingsDao.observeAllSettings()
        ) { supplements, settings ->
            supplements.map { entity ->
                val domainSupplement = entity.toSupplementSettings()

                val userSettings = settings
                    .firstOrNull { it.supplementId == entity.id }
                    ?.toSupplementSettings()

                SupplementWithUserSettings(
                    supplement = domainSupplement,
                    userSettings = userSettings?.toSupplementSettings()
                )
            }
        }

    /**
     * Observes a single supplement and its user-specific settings.
     *
     * PURPOSE:
     * --------
     * Used for detail/edit screens where live updates are required
     * when either:
     * - The supplement definition changes, or
     * - The user's preferences change
     *
     * @param supplementId
     * Database ID of the supplement to observe.
     *
     * @return
     * A Flow emitting a [SupplementWithUserSettings], or null if the
     * supplement does not exist.
     *
     * FLOW BEHAVIOR:
     * --------------
     * - Emits on supplement updates
     * - Emits on user settings updates
     * - User settings may be null if not yet created
     *
     * NOTES:
     * ------
     * - Combines multiple data sources using Flow.combine
     * - Safe to collect indefinitely in a ViewModel
     */
    override fun observeSupplement(
        supplementId: Long
    ): Flow<SupplementWithUserSettings?> =
        combine(
            supplementDao.observeSupplementById(supplementId),
            supplementUserSettingsDao.observeSettings(supplementId)
        ) { supplementEntity, settingsEntity ->
            supplementEntity?.let {
                SupplementWithUserSettings(
                    supplement = it.toSupplementSettings(),
                    userSettings = settingsEntity?.toUserSupplementSettings()
                )
            }
        }

    /**
     * Persists user-specific preferences for a supplement.
     *
     * PURPOSE:
     * --------
     * Saves overrides such as preferred dose, enabled/disabled state,
     * or other user-defined settings without modifying the base
     * supplement definition.
     *
     * @param settings
     * Domain model containing user preferences.
     *
     * @param supplementId
     * ID of the supplement these settings apply to.
     *
     * NOTES:
     * ------
     * - This performs an upsert operation.
     * - If settings do not exist, they will be created.
     * - If settings exist, they will be replaced.
     *
     * IMPORTANT:
     * ----------
     * This method intentionally does NOT update the supplement itself.
     * User preferences and supplement definitions have separate lifecycles.
     */
    override suspend fun saveUserSettings(
        settings: UserSupplementSettings,
        supplementId: Long
    ) {
        supplementUserSettingsDao.upsert(
            settings.toEntity(supplementId)
        )
    }

    /**
     * Resolves the effective time for a supplement anchor on a given date.
     *
     * Resolution priority:
     * 1) Explicit date override
     * 2) Day-of-week override
     * 3) Global default anchor time
     * 4) Anchor fallback (MIDNIGHT / null)
     *
     * @return LocalTime if the anchor represents a fixed time, or null for ANYTIME
     */
    private suspend fun resolveAnchorTime(
        anchor: DoseAnchorType,
        date: LocalDate
    ): LocalTime? {

        // ANYTIME means no fixed time
        if (anchor == DoseAnchorType.ANYTIME) {
            return null
        }

        val dayOfWeek = date.dayOfWeek

        // --------------------------------------------------
        // 1️⃣ Explicit per-date override
        // --------------------------------------------------
        eventTimeDao.getOverride(date.toString(), anchor)
            ?.let { override ->
                return LocalTime.ofSecondOfDay(override.timeSeconds.toLong())
            }

        // --------------------------------------------------
        // 2️⃣ Day-of-week override (NEW)
        // --------------------------------------------------
        eventTimeDao.getDayOfWeekOverride(anchor, dayOfWeek)
            ?.let { dow ->
                return LocalTime.ofSecondOfDay(dow.timeSeconds.toLong())
            }

        // --------------------------------------------------
        // 3️⃣ Global default anchor time
        // --------------------------------------------------
        eventTimeDao.getDefault(anchor)
            ?.let { def ->
                return LocalTime.ofSecondOfDay(def.timeSeconds.toLong())
            }

        // --------------------------------------------------
        // 4️⃣ Fallback rules
        // --------------------------------------------------
        return when (anchor) {
            DoseAnchorType.MIDNIGHT -> LocalTime.MIDNIGHT
            else -> null
        }
    }

}
