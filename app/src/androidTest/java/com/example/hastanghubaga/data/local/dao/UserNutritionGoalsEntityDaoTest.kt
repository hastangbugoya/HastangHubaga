package com.example.hastanghubaga.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.db.TestAppDatabase
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class UserNutritionGoalsEntityDaoTest {

    private lateinit var db: TestAppDatabase
    private lateinit var dao: UserNutritionGoalsEntityDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TestAppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.userNutritionGoalsEntityDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private fun makeGoal(
        id: Long = 0,
        type: NutritionGoalType,
        isActive: Boolean = false
    ) = UserNutritionGoalsEntity(
        id = id,
        type = type,
        dailyCalorieTarget = 2500.0,
        dailyProteinTarget = 180.0,
        dailyCarbTarget = 250.0,
        dailyFatTarget = 70.0,
        isActive = isActive,
        name = "Test",
        sodiumLimitMg = 2300.0,
        cholesterolLimitMg = 300.0,
        fiberTargetGrams = 30.0,
    )

    // ---------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------

    @Test
    fun upsert_insertsGoal() = runBlocking {
        dao.upsert(makeGoal(type = NutritionGoalType.BULKING))

        val all = dao.getAllGoals()
        Assert.assertEquals(1, all.size)
        Assert.assertEquals(NutritionGoalType.BULKING, all.first().type)
    }

    @Test
    fun observeAllGoals_emitsGoals() = runTest {
        dao.upsert(makeGoal(type = NutritionGoalType.BULKING))
        dao.upsert(makeGoal(type = NutritionGoalType.CUTTING))

        val emitted = dao.observeAllGoals().first()

        Assert.assertEquals(2, emitted.size)
    }

    @Test
    fun activateGoal_marksOnlyOneActive() = runBlocking {
        val bulkId = dao.upsert(makeGoal(type = NutritionGoalType.BULKING))
        val cutId = dao.upsert(makeGoal(type = NutritionGoalType.CUTTING))

        dao.activateGoal(bulkId)

        var active = dao.getActiveGoal()
        Assert.assertEquals(NutritionGoalType.BULKING, active?.type)

        dao.activateGoal(cutId)

        active = dao.getActiveGoal()
        Assert.assertEquals(NutritionGoalType.CUTTING, active?.type)
    }

    @Test
    fun observeActiveGoal_emitsCorrectGoal() = runTest {
        val bulkId = dao.upsert(makeGoal(type = NutritionGoalType.BULKING))
        dao.activateGoal(bulkId)

        val active = dao.observeActiveGoal().first()

        Assert.assertNotNull(active)
        Assert.assertEquals(NutritionGoalType.BULKING, active!!.type)
    }

    @Test
    fun getGoalsByType_filtersCorrectly() = runBlocking {
        dao.upsert(makeGoal(type = NutritionGoalType.BULKING))
        dao.upsert(makeGoal(type = NutritionGoalType.CUTTING))
        dao.upsert(makeGoal(type = NutritionGoalType.BULKING))

        val bulking = dao.getGoalsByType(NutritionGoalType.BULKING)

        Assert.assertEquals(2, bulking.size)
    }

    @Test
    fun delete_removesGoal() = runBlocking {
        val id = dao.upsert(makeGoal(type = NutritionGoalType.MAINTENANCE))
        val goal = dao.getAllGoals().first { it.id == id }

        dao.delete(goal)

        val remaining = dao.getAllGoals()
        Assert.assertTrue(remaining.isEmpty())
    }
}
