package com.example.hastanghubaga.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
@RunWith(AndroidJUnit4::class)
class MealEntityDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    private lateinit var dao: MealEntityDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = db.mealEntityDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ------------------------------------------------------------
    // INSERT + READ (joined result)
    // ------------------------------------------------------------
    @Test
    fun insertMeal_andNutrition_andObserveJoined() = runTest {
        val mealId = dao.insertMeal(
            MealEntity(type = MealType.BREAKFAST, timestamp = 1_000L, notes = "Eggs")
        )

        dao.insertNutrition(
            MealNutritionEntity(
                mealId = mealId,
                protein = 25.0,
                carbs = 10.0,
                fat = 5.0,
                calories = 200.0,
                sodium = 500.0,
                cholesterol = 100.0,
                fiber = 2.0
            )
        )

        val joined = dao.observeMeal(mealId).first()
        assertThat(joined).isNotNull()

        joined!!
        assertThat(joined.meal.id).isEqualTo(mealId)
        assertThat(joined.nutrition?.protein).isEqualTo(25.0)
        assertThat(joined.nutrition?.calories).isEqualTo(200.0)
    }

    // ------------------------------------------------------------
    // observeAllMeals() ordering
    // ------------------------------------------------------------
    @Test
    fun observeAllMeals_ordersByTimestampDesc() = runTest {
        dao.insertMeal(MealEntity(type =MealType.LUNCH, timestamp = 2000L))
        dao.insertMeal(MealEntity(type = MealType.DINNER, timestamp = 5000L))

        val items = dao.observeAllMeals().first()

        assertThat(items.size).isEqualTo(2)
        assertThat(items.first().meal.type).isEqualTo(MealType.DINNER)
        assertThat(items.last().meal.type).isEqualTo(MealType.LUNCH)
    }

    // ------------------------------------------------------------
    // getMealsForDate()
    // ------------------------------------------------------------
    @Test
    fun getMealsForDate_returnsOnlyThatDate() = runTest {
        // 2025-01-01
        dao.insertMeal(MealEntity(type = MealType.SNACK, timestamp = 1735689600000L))

        // 2025-01-02
        dao.insertMeal(MealEntity(type = MealType.SNACK, timestamp = 1735776000000L))

        val meals = dao.getMealsForDate("2025-01-01")
        assertThat(meals.size).isEqualTo(1)
    }

    // ------------------------------------------------------------
    // deleteMeal()
    // ------------------------------------------------------------
    @Test
    fun deleteMeal_removesIt() = runTest {
        val id = dao.insertMeal(MealEntity(type = MealType.BREAKFAST, timestamp = 3000L))

        val stored = dao.observeMeal(id).first()
        assertThat(stored).isNotNull()

        dao.deleteMeal(stored!!.meal)

        val after = dao.observeMeal(id).first()
        assertThat(after).isNull()
    }

    // ------------------------------------------------------------
    // deleteMealById()
    // ------------------------------------------------------------
    @Test
    fun deleteMealById_works() = runTest {
        val id = dao.insertMeal(MealEntity(type = MealType.LUNCH, timestamp = 4000L))

        dao.deleteMealById(id)

        val result = dao.observeMeal(id).first()
        assertThat(result).isNull()
    }

    // ------------------------------------------------------------
    // deleteNutrition()
    // ------------------------------------------------------------
    @Test
    fun deleteNutrition_removesNutritionOnly() = runTest {
        val mealId = dao.insertMeal(MealEntity(type = MealType.DINNER, timestamp = 9000L))

        dao.insertNutrition(
            MealNutritionEntity(
                mealId = mealId,
                protein = 20.0,
                carbs = 10.0,
                fat = 5.0,
                calories = 150.0,
                sodium = 300.0,
                cholesterol = 50.0,
                fiber = 1.0
            )
        )

        dao.deleteNutrition(mealId)

        val joined = dao.observeMeal(mealId).first()
        assertThat(joined?.nutrition).isNull()
        assertThat(joined?.meal).isNotNull()
    }

    // ------------------------------------------------------------
    // getMealsByType()
    // ------------------------------------------------------------
    @Test
    fun getMealsByType_returnsCorrectMeals() = runTest {
        dao.insertMeal(MealEntity(type =MealType.BREAKFAST, timestamp = 1L))
        dao.insertMeal(MealEntity(type =MealType.LUNCH, timestamp = 2L))
        dao.insertMeal(MealEntity(type =MealType.BREAKFAST, timestamp = 3L))

        val breakfasts = dao.getMealsByType(MealType.BREAKFAST)

        assertThat(breakfasts.size).isEqualTo(2)
        assertThat(breakfasts.all { it.type == MealType.BREAKFAST }).isTrue()
    }
}
