package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class GetSupplementsWithUserSettingsForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository,
    private val eventTimeDao: EventTimeDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        date: LocalDate
    ): Flow<List<SupplementWithUserSettings>> =
        supplementRepository
            .getSupplementsForDate(date.toString())
            .mapLatest { supplements ->
                supplements
                    .filter { it.shouldTakeOn(date) }
                    .map { it.withScheduleFor(date) }
            }

    // ---------------------------------------------------------------------
    // Scheduling
    // ---------------------------------------------------------------------

    private suspend fun SupplementWithUserSettings.withScheduleFor(
        date: LocalDate
    ): SupplementWithUserSettings {

        val dosesPerDay = resolveDosesPerDay()
        val offsetMinutes = supplement.offsetMinutes ?: 0

        val baseTime = resolveAnchorTime(supplement.doseAnchorType)

        val scheduledTimes =
            if (baseTime == null) emptyList()
            else List(dosesPerDay) { index ->
                baseTime.plusMinutes((index * offsetMinutes).toLong())
            }

        return copy(scheduledTimes = scheduledTimes)
    }

    private fun SupplementWithUserSettings.resolveDosesPerDay(): Int =
        userSettings?.preferredServingsPerDay
            ?: supplement.servingsPerDay

    /**
     * Resolves anchor time from DB defaults or fallbacks.
     */
    private suspend fun resolveAnchorTime(
        anchor: DoseAnchorType
    ): LocalTime? =
        eventTimeDao.getDefault(anchor)
            ?.let { LocalTime.ofSecondOfDay(it.timeSeconds.toLong()) }
            ?: fallbackAnchorTime(anchor)

    // ---------------------------------------------------------------------
    // Frequency rules
    // ---------------------------------------------------------------------

    private fun SupplementWithUserSettings.shouldTakeOn(
        date: LocalDate
    ): Boolean =
        when (supplement.frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.WEEKLY ->
                supplement.weeklyDays?.contains(date.dayOfWeek) == true
            FrequencyType.EVERY_X_DAYS ->
                shouldTakeEveryXDays(date)
        }

    private fun SupplementWithUserSettings.shouldTakeEveryXDays(
        date: LocalDate
    ): Boolean {
        val lastTaken = supplement.lastTakenDate?.let(LocalDate::parse)
        val interval = supplement.frequencyInterval ?: return false
        return lastTaken == null || lastTaken.plusDays(interval.toLong()) == date
    }

    // ---------------------------------------------------------------------
    // Anchor fallbacks
    // ---------------------------------------------------------------------

    private fun fallbackAnchorTime(anchor: DoseAnchorType): LocalTime? =
        when (anchor) {
            DoseAnchorType.MIDNIGHT -> LocalTime.MIDNIGHT
            DoseAnchorType.WAKEUP -> LocalTime.of(7, 0)
            DoseAnchorType.BREAKFAST -> LocalTime.of(8, 0)
            DoseAnchorType.LUNCH -> LocalTime.of(12, 0)
            DoseAnchorType.DINNER -> LocalTime.of(18, 0)
            DoseAnchorType.BEFORE_WORKOUT -> LocalTime.of(16, 30)
            DoseAnchorType.AFTER_WORKOUT -> LocalTime.of(17, 45)
            else -> null
        }
}
