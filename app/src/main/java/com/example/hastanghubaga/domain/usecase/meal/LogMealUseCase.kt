package com.example.hastanghubaga.domain.usecase.meal

import android.util.Log
import com.example.hastanghubaga.domain.model.meal.LogMealInput
import com.example.hastanghubaga.domain.repository.meal.MealLogRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant

/**
 * Saves one actual logged meal.
 *
 * Canonical meal model:
 * - MealEntity = template
 * - MealOccurrenceEntity = planned occurrence
 * - MealLogEntity = actual consumed meal
 *
 * This use case writes only to the meal log layer.
 * It must NOT create or mutate template rows.
 *
 * Planned logging contract:
 * - if [LogMealInput.occurrenceId] is non-null, it identifies one specific
 *   planned meal occurrence
 * - repeated logging for the same non-null occurrenceId must update/replace the
 *   existing persisted log row rather than create a duplicate
 * - if [LogMealInput.occurrenceId] is null, this is treated as an ad-hoc /
 *   force-logged meal and may insert as a new independent row
 *
 * Important:
 * - [LogMealInput.mealId] should be preserved whenever the log came from a known
 *   HH meal template, including force-log picker flows
 * - [LogMealInput.endTime] is optional and should persist when supplied
 * - nutrition values should pass through unchanged when supplied
 */
class LogMealUseCase @Inject constructor(
    private val repo: MealLogRepository
) {
    suspend operator fun invoke(
        input: LogMealInput,
        clock: Clock = Clock.System
    ): Long {
        val (date, time) = DomainTimePolicy.resolveIntent(
            intent = input.timeUseIntent,
            clock = clock
        )

        val startTimestamp = localDateTimeToEpochMillis(date, time)
        val endTimestamp =
            input.endTime?.let { endTime ->
                localDateTimeToEpochMillis(date, endTime)
            }

        Log.d("MEAL_RECON", "LogMealUseCase> input=$input")
        Log.d(
            "MEAL_RECON",
            "LogMealUseCase> resolved mealId=${input.mealId} occurrenceId=${input.occurrenceId} start=$startTimestamp end=$endTimestamp calories=${input.nutrition?.calories}"
        )

        return repo.insertMealLog(
            mealId = input.mealId,
            occurrenceId = input.occurrenceId,
            mealType = input.mealType,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            notes = input.notes,
            calories = input.nutrition?.calories,
            proteinGrams = input.nutrition?.proteinGrams,
            carbsGrams = input.nutrition?.carbsGrams,
            fatGrams = input.nutrition?.fatGrams,
            sodiumMg = input.nutrition?.sodiumMg,
            cholesterolMg = input.nutrition?.cholesterolMg,
            fiberGrams = input.nutrition?.fiberGrams
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