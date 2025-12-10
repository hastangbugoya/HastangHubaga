package com.example.hastanghubaga.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealNutritionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MealNutritionDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.mealNutritionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ------------------------------------------------------
    // INSERT + QUERY
    // ------------------------------------------------------
    @Test
    fun insertAndRetrieveNutrition() = runTest {
        val entity = MealNutritionEntity(
            mealId = 10L,
            protein = 25.0,
            carbs = 40.0,
            fat = 10.0,
            calories = 350.0,
            sodium = 120.0,
            cholesterol = 30.0,
            fiber = 5.0
        )

        dao.insertNutrition(entity)

        val result = dao.getNutritionForMeal(10L)

        assertNotNull(result)
        assertEquals(25.0, result!!.protein, 0.0)
        assertEquals(40.0, result.carbs, 0.0)
        assertEquals(10.0, result.fat, 0.0)
    }

    // ------------------------------------------------------
    // QUERY NON-EXISTENT
    // ------------------------------------------------------
    @Test
    fun getNutrition_nonExistent_returnsNull() = runTest {
        val result = dao.getNutritionForMeal(999L)
        assertNull(result)
    }

    // ------------------------------------------------------
    // REPLACE ON CONFLICT
    // ------------------------------------------------------
    @Test
    fun insertNutrition_replacesExisting() = runTest {
        val original = MealNutritionEntity(
            mealId = 5L,
            protein = 10.0,
            carbs = 20.0,
            fat = 5.0,
            calories = 200.0,
            sodium = 50.0,
            cholesterol = 10.0,
            fiber = 2.0
        )

        val updated = original.copy(
            protein = 30.0,
            carbs = 10.0
        )

        dao.insertNutrition(original)
        dao.insertNutrition(updated)

        val result = dao.getNutritionForMeal(5L)

        assertNotNull(result)
        assertEquals(30.0, result!!.protein, 0.0)  // updated value
        assertEquals(10.0, result.carbs, 0.0)
    }

    // ------------------------------------------------------
    // DELETE
    // ------------------------------------------------------
    @Test
    fun deleteNutrition_removesEntry() = runTest {
        val entity = MealNutritionEntity(
            mealId = 7L,
            protein = 15.0,
            carbs = 30.0,
            fat = 8.0,
            calories = 280.0,
            sodium = 100.0,
            cholesterol = 20.0,
            fiber = 4.0
        )

        dao.insertNutrition(entity)

        dao.deleteNutrition(7L)

        val result = dao.getNutritionForMeal(7L)

        assertNull(result)
    }
}
