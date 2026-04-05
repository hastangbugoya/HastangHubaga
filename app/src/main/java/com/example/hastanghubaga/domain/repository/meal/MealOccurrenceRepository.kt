package com.example.hastanghubaga.domain.repository.meal

import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface MealOccurrenceRepository {

    fun observeOccurrencesForDate(
        date: LocalDate
    ): Flow<List<MealOccurrenceEntity>>

    suspend fun getOccurrencesForDate(
        date: LocalDate
    ): List<MealOccurrenceEntity>

    suspend fun replaceOccurrencesForDate(
        date: LocalDate,
        occurrences: List<MealOccurrenceEntity>
    )
}