package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import com.example.hastanghubaga.domain.repository.meal.MealOccurrenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetMealOccurrencesForDateUseCase @Inject constructor(
    private val repository: MealOccurrenceRepository
) {
    operator fun invoke(
        date: LocalDate
    ): Flow<List<MealOccurrenceEntity>> {
        return repository.observeOccurrencesForDate(date)
    }
}