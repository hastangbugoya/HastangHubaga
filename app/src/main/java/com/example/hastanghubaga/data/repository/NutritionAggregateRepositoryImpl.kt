package com.example.hastanghubaga.data.repository


import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementNutritionDao
import com.example.hastanghubaga.domain.model.meal.DailyNutritionSummary
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class NutritionAggregateRepositoryImpl @Inject constructor(
    private val mealNutritionDao: MealNutritionDao,
    private val supplementNutritionDao: SupplementNutritionDao
) : NutritionAggregateRepository {

    override fun observeDailyNutritionSummariesInRange(
        startMillis: Long,
        endMillis: Long,
        timeZone: TimeZone
    ): Flow<List<DailyNutritionSummary>> {

        val mealsFlow = mealNutritionDao.observeNutritionForMealsInRange(startMillis, endMillis)
            .map { rows ->
                rows.map { r ->
                    TimedNutrition(
                        timestamp = r.timestamp,
                        nutrition = MealNutrition(
                            protein = r.protein,
                            carbs = r.carbs,
                            fat = r.fat,
                            calories = r.calories.toDouble(),
                            sodium = r.sodium,
                            cholesterol = r.cholesterol,
                            fiber = r.fiber
                        )
                    )
                }
            }

        val supplementsFlow = supplementNutritionDao.observeSupplementNutritionInRange(startMillis, endMillis)
            .map { rows ->
                rows.map { r ->
                    TimedNutrition(
                        timestamp = r.timestamp,
                        nutrition = MealNutrition(
                            protein = r.protein.zeroToNull(),
                            carbs = r.carbs.zeroToNull(),
                            fat = r.fat.zeroToNull(),
                            calories = r.calories.zeroToNull(),
                            sodium = r.sodium.zeroToNull(),
                            cholesterol = r.cholesterol.zeroToNull(),
                            fiber = r.fiber.zeroToNull()
                        )
                    )
                }
            }

        return combine(mealsFlow, supplementsFlow) { meals, supplements ->
            val all = meals + supplements

            val byDay: Map<LocalDate, List<MealNutrition>> =
                all.groupBy { it.timestamp.toLocalDate(timeZone) }
                    .mapValues { (_, timed) -> timed.map { it.nutrition } }

            byDay.entries
                .map { (day, nutritions) ->
                    val total = nutritions.sumNutrition()
                    DailyNutritionSummary(
                        date = day.toString(),
                        totalProtein = total.protein ?: 0.0,
                        totalFat = total.fat ?: 0.0,
                        totalCarbs = total.carbs ?: 0.0,
                        totalCalories = total.calories,
                        sodium = total.sodium,
                        cholesterol = total.cholesterol,
                        fiber = total.fiber
                    )
                }
                .sortedBy { it.date }
        }
    }

    private data class TimedNutrition(
        val timestamp: Long,
        val nutrition: MealNutrition
    )

    private fun Long.toLocalDate(tz: TimeZone): LocalDate =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(tz).date

    private fun Double.zeroToNull(): Double? = if (this == 0.0) null else this

    private fun List<MealNutrition>.sumNutrition(): MealNutrition {
        fun sum(selector: (MealNutrition) -> Double?): Double? {
            val values = this.mapNotNull(selector)
            return if (values.isEmpty()) null else values.sum()
        }

        return MealNutrition(
            protein = sum { it.protein },
            carbs = sum { it.carbs },
            fat = sum { it.fat },
            calories = sum { it.calories },
            sodium = sum { it.sodium },
            cholesterol = sum { it.cholesterol },
            fiber = sum { it.fiber }
        )
    }
}
