package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.domain.model.meal.LogMealInput
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import javax.inject.Inject

class LogMealUseCase @Inject constructor(
    private val repo: MealRepository
) {
    suspend operator fun invoke(
        input: LogMealInput,
        clock: Clock = Clock.System
    ) {
        val (date, time) = DomainTimePolicy.resolveIntent(
            intent = input.timeUseIntent,
            clock = clock
        )

        val timestampMillis = localDateTimeToEpochMillis(date, time)

        repo.logMeal(
            type = input.mealType.toEntity(),
            timestampMillis = timestampMillis,
            notes = input.notes,
            nutrition = input.nutrition
        )
    }

    private fun localDateTimeToEpochMillis(
        date: LocalDate,
        time: LocalTime
    ): Long {
        val ldt = LocalDateTime(date = date, time = time)
        return ldt
            .toInstant(DomainTimePolicy.localTimeZone)
            .toEpochMilliseconds()
    }
}
