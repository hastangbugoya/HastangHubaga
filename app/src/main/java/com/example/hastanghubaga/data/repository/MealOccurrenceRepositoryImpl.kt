package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.meal.MealOccurrenceDao
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import com.example.hastanghubaga.domain.repository.meal.MealOccurrenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class MealOccurrenceRepositoryImpl @Inject constructor(
    private val occurrenceDao: MealOccurrenceDao
) : MealOccurrenceRepository {

    override fun observeOccurrencesForDate(
        date: LocalDate
    ): Flow<List<MealOccurrenceEntity>> =
        occurrenceDao.observeOccurrencesForDate(date.toString())

    override suspend fun getOccurrencesForDate(
        date: LocalDate
    ): List<MealOccurrenceEntity> =
        occurrenceDao.getOccurrencesForDate(date.toString())

    override suspend fun replaceOccurrencesForDate(
        date: LocalDate,
        occurrences: List<MealOccurrenceEntity>
    ) {
        val dateString = date.toString()

        occurrenceDao.deleteScheduledOccurrencesForDate(dateString)

        if (occurrences.isNotEmpty()) {
            occurrenceDao.upsertOccurrences(occurrences)
        }
    }
}