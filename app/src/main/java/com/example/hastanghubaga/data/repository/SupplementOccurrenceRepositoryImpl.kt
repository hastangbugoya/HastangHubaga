package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.supplement.SupplementOccurrenceDao
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.domain.repository.supplement.SupplementOccurrenceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Room-backed repository for planned supplement occurrences.
 *
 * This repository is the canonical PLANNED-side persistence layer for supplement
 * timeline/planner rows.
 *
 * Architectural role:
 * - [SupplementOccurrenceEntity] represents one intended supplement occurrence
 *   for a specific date/time
 * - actual logged intake events remain separate in supplement_daily_log
 * - linkage between planned and actual is via stable occurrence ID
 *
 * Notes:
 * - date-scoped rebuilds intentionally replace the planned snapshot for that day
 * - soft-delete remains available for targeted planner item removal while
 *   preserving historical linkage possibilities for actual logs
 */
class SupplementOccurrenceRepositoryImpl @Inject constructor(
    private val occurrenceDao: SupplementOccurrenceDao
) : SupplementOccurrenceRepository {

    override fun observeOccurrencesForDate(
        date: LocalDate
    ): Flow<List<SupplementOccurrenceEntity>> =
        occurrenceDao.observeOccurrencesForDate(date.toString())

    override suspend fun getOccurrencesForDate(
        date: LocalDate
    ): List<SupplementOccurrenceEntity> =
        occurrenceDao.getOccurrencesForDate(date.toString())

    override suspend fun replaceOccurrencesForDate(
        date: LocalDate,
        occurrences: List<SupplementOccurrenceEntity>
    ) {
        val dateString = date.toString()

        // Day snapshot rebuild:
        // remove existing planned rows for the target date, then write the
        // replacement set. This keeps the observable planned-day state aligned
        // with the resolved schedule output for that date.
        occurrenceDao.deleteOccurrencesForDate(dateString)

        if (occurrences.isNotEmpty()) {
            occurrenceDao.insertOccurrences(occurrences)
        }
    }

    override suspend fun insertOccurrence(
        occurrence: SupplementOccurrenceEntity
    ) {
        occurrenceDao.insertOccurrence(occurrence)
    }

    override suspend fun softDeleteOccurrence(
        occurrenceId: String
    ) {
        occurrenceDao.softDeleteOccurrence(occurrenceId)
    }
}