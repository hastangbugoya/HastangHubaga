package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.activity.ActivityOccurrenceDao
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.domain.repository.activity.ActivityOccurrenceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Room-backed repository for planned activity occurrences.
 *
 * This repository is the canonical PLANNED-side persistence layer for activity
 * timeline/planner rows.
 *
 * Architectural role:
 * - [ActivityOccurrenceEntity] represents one intended activity occurrence
 *   for a specific date/time
 * - actual logged activity events remain separate in ActivityLogEntity (future)
 * - linkage between planned and actual is via stable occurrence ID
 *
 * Notes:
 * - date-scoped rebuilds intentionally replace the planned snapshot for that day
 * - soft-delete remains available for targeted planner item removal while
 *   preserving future historical linkage possibilities for actual logs
 */
class ActivityOccurrenceRepositoryImpl @Inject constructor(
    private val occurrenceDao: ActivityOccurrenceDao
) : ActivityOccurrenceRepository {

    override fun observeOccurrencesForDate(
        date: LocalDate
    ): Flow<List<ActivityOccurrenceEntity>> =
        occurrenceDao.observeOccurrencesForDate(date.toString())

    override suspend fun getOccurrencesForDate(
        date: LocalDate
    ): List<ActivityOccurrenceEntity> =
        occurrenceDao.getOccurrencesForDate(date.toString())

    override suspend fun getWorkoutOccurrencesForDate(
        date: LocalDate
    ): List<ActivityOccurrenceEntity> =
        occurrenceDao.getWorkoutOccurrencesForDate(date.toString())

    override suspend fun replaceOccurrencesForDate(
        date: LocalDate,
        occurrences: List<ActivityOccurrenceEntity>
    ) {
        val dateString = date.toString()

        // Day snapshot rebuild:
        // remove existing scheduled rows for the target date, then write the
        // replacement set. This keeps the observable planned-day state aligned
        // with the resolved schedule output for that date while preserving
        // ad-hoc rows.
        occurrenceDao.deleteScheduledOccurrencesForDate(dateString)

        if (occurrences.isNotEmpty()) {
            occurrenceDao.upsertOccurrences(occurrences)
        }
    }

    override suspend fun insertOccurrence(
        occurrence: ActivityOccurrenceEntity
    ) {
        occurrenceDao.upsertOccurrence(occurrence)
    }

    override suspend fun softDeleteOccurrence(
        occurrenceId: String
    ) {
        occurrenceDao.softDeleteOccurrence(occurrenceId)
    }

    override suspend fun updateOccurrenceWorkoutFlag(
        occurrenceId: String,
        isWorkout: Boolean
    ) {
        occurrenceDao.updateOccurrenceWorkoutFlag(
            occurrenceId = occurrenceId,
            isWorkout = isWorkout
        )
    }
}