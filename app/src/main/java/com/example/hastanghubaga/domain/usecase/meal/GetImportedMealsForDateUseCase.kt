package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.dao.meal.AkImportedMealDao
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Returns AK imported meals materialized for a given date.
 *
 * Important:
 * - This reads from ak_imported_meals only
 * - It does NOT convert imported meals into HH native MealEntity rows
 * - It does NOT merge imported meals with HH native meals
 * - It does NOT assign CUSTOM / weak-slot logs
 *
 * This is intentionally a read-only bridge for the current integration phase.
 */
class GetImportedMealsForDateUseCase @Inject constructor(
    private val akImportedMealDao: AkImportedMealDao
) {

    operator fun invoke(date: LocalDate): Flow<List<AkImportedMealEntity>> {
        return akImportedMealDao.observeForDate(
            logDateIso = date.toString()
        )
    }
}