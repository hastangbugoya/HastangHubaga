package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanSuccessCriteriaDao
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
import com.example.hastanghubaga.domain.model.nutrition.DailyNutritionIntake
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateDailyNutritionComplianceUseCaseTest {

    private val evaluateNutrientUseCase = EvaluateNutrientUseCase()
    private val evaluatePlanComplianceUseCase =
        EvaluatePlanComplianceUseCase(evaluateNutrientUseCase)

    // --- Fake DAOs (minimal, inline like your timeline tests) ---

    private class FakePlanDao(
        private val plans: List<UserNutritionPlanEntity>
    ) : NutritionPlanEntityDao by NoOpPlanDao() {
        override suspend fun getActivePlansEffectiveOn(dateMillis: Long) = plans
    }

    private class FakeGoalDao(
        private val goals: List<NutrientGoalEntity>
    ) : NutrientGoalDao by NoOpGoalDao() {
        override suspend fun getGoalsForPlans(planIds: List<Long>) = goals
    }

    private class FakeCriteriaDao :
        NutritionPlanSuccessCriteriaDao by NoOpCriteriaDao()

    // --- Test ---

    @Test
    fun `simple ALL mode plan passes when intake meets goal`() = runTest {

        val plan = UserNutritionPlanEntity(
            id = 1L,
            type = com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType.CUTTING,
            name = "Cutting",
            startDate = 0L,
            endDate = null,
            isActive = true,
            sourceType = "LOCAL",
            sourcePlanId = null,
            successMode = "ALL_TRACKED_GOALS",
            createdAt = 0L,
            updatedAt = 0L
        )

        val goal = NutrientGoalEntity(
            id = 1L,
            planId = 1L,
            nutrientKey = "PROTEIN_G",
            minValue = 100.0,
            targetValue = null,
            maxValue = null
        )

        val intake = DailyNutritionIntake(
            date = 0L,
            nutrients = mapOf("PROTEIN_G" to 120.0)
        )

        val useCase = EvaluateDailyNutritionComplianceUseCase(
            nutritionPlanEntityDao = FakePlanDao(listOf(plan)),
            nutrientGoalDao = FakeGoalDao(listOf(goal)),
            nutritionPlanSuccessCriteriaDao = FakeCriteriaDao(),
            evaluatePlanComplianceUseCase = evaluatePlanComplianceUseCase,
            evaluateNutrientUseCase = evaluateNutrientUseCase
        )

        val result = useCase(intake)

        assertTrue(result.isSuccessful)
        assertTrue(result.planResults.first().isSuccessful)
    }

    @Test
    fun `simple ALL mode plan fails when below min`() = runTest {

        val plan = UserNutritionPlanEntity(
            id = 1L,
            type = com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType.CUTTING,
            name = "Cutting",
            startDate = 0L,
            endDate = null,
            isActive = true,
            sourceType = "LOCAL",
            sourcePlanId = null,
            successMode = "ALL_TRACKED_GOALS",
            createdAt = 0L,
            updatedAt = 0L
        )

        val goal = NutrientGoalEntity(
            id = 1L,
            planId = 1L,
            nutrientKey = "PROTEIN_G",
            minValue = 100.0,
            targetValue = null,
            maxValue = null
        )

        val intake = DailyNutritionIntake(
            date = 0L,
            nutrients = mapOf("PROTEIN_G" to 80.0)
        )

        val useCase = EvaluateDailyNutritionComplianceUseCase(
            nutritionPlanEntityDao = FakePlanDao(listOf(plan)),
            nutrientGoalDao = FakeGoalDao(listOf(goal)),
            nutritionPlanSuccessCriteriaDao = FakeCriteriaDao(),
            evaluatePlanComplianceUseCase = evaluatePlanComplianceUseCase,
            evaluateNutrientUseCase = evaluateNutrientUseCase
        )

        val result = useCase(intake)

        assertFalse(result.isSuccessful)
        assertFalse(result.planResults.first().isSuccessful)
    }

    // --- Minimal no-op DAO stubs to satisfy interface ---

    private open class NoOpPlanDao : NutritionPlanEntityDao {
        override suspend fun insert(plan: UserNutritionPlanEntity) = 0L
        override suspend fun update(plan: UserNutritionPlanEntity) {}
        override suspend fun delete(plan: UserNutritionPlanEntity) {}
        override suspend fun getPlanById(planId: Long) = null
        override fun observePlanById(planId: Long) = throw NotImplementedError()
        override suspend fun getAllPlans() = emptyList<UserNutritionPlanEntity>()
        override fun observeAllPlans() = throw NotImplementedError()
        override suspend fun getActivePlans() = emptyList<UserNutritionPlanEntity>()
        override fun observeActivePlans() = throw NotImplementedError()
        override suspend fun getPlansByType(type: String) = emptyList<UserNutritionPlanEntity>()
        override suspend fun getPlansBySourceType(sourceType: String) = emptyList<UserNutritionPlanEntity>()
        override suspend fun getPlanBySource(sourceType: String, sourcePlanId: String) = null
        override suspend fun setPlanActiveState(planId: Long, isActive: Boolean, updatedAt: Long) {}
        override suspend fun getPlansEffectiveOn(dateMillis: Long) = emptyList<UserNutritionPlanEntity>()
        override suspend fun getActivePlansEffectiveOn(dateMillis: Long) = emptyList<UserNutritionPlanEntity>()
    }

    private open class NoOpGoalDao : NutrientGoalDao {
        override suspend fun insert(goal: NutrientGoalEntity) = 0L
        override suspend fun insertAll(goals: List<NutrientGoalEntity>) = emptyList<Long>()
        override suspend fun update(goal: NutrientGoalEntity) {}
        override suspend fun updateAll(goals: List<NutrientGoalEntity>) {}
        override suspend fun delete(goal: NutrientGoalEntity) {}
        override suspend fun deleteAll(goals: List<NutrientGoalEntity>) {}
        override suspend fun getGoalById(goalId: Long) = null
        override suspend fun getGoalByPlanIdAndNutrientKey(planId: Long, nutrientKey: String) = null
        override suspend fun getGoalsForPlan(planId: Long) = emptyList<NutrientGoalEntity>()
        override fun observeGoalsForPlan(planId: Long) = throw NotImplementedError()
        override suspend fun getGoalsForPlans(planIds: List<Long>) = emptyList<NutrientGoalEntity>()
        override suspend fun getGoalsByNutrientKey(nutrientKey: String) = emptyList<NutrientGoalEntity>()
        override suspend fun getGoalsForPlanByNutrientKeys(planId: Long, nutrientKeys: List<String>) = emptyList<NutrientGoalEntity>()
        override suspend fun deleteGoalsForPlan(planId: Long) {}
        override suspend fun deleteGoalsForPlanExcept(planId: Long, nutrientKeys: List<String>) {}
        override suspend fun deleteGoalByPlanIdAndNutrientKey(planId: Long, nutrientKey: String) {}
    }

    private open class NoOpCriteriaDao : NutritionPlanSuccessCriteriaDao {
        override suspend fun insert(criteria: com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity) = 0L
        override suspend fun insertAll(criteria: List<com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity>) = emptyList<Long>()
        override suspend fun update(criteria: com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity) {}
        override suspend fun updateAll(criteria: List<com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity>) {}
        override suspend fun delete(criteria: com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity) {}
        override suspend fun deleteAll(criteria: List<com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity>) {}
        override suspend fun getById(criteriaId: Long) = null
        override suspend fun getByPlanIdAndNutrientKey(planId: Long, nutrientKey: String) = null
        override suspend fun getForPlan(planId: Long) = emptyList<com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity>()
        override fun observeForPlan(planId: Long) = throw NotImplementedError()
        override suspend fun getForPlans(planIds: List<Long>) = emptyList<com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity>()
        override suspend fun getByNutrientKey(nutrientKey: String) = emptyList<com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity>()
        override suspend fun deleteForPlan(planId: Long) {}
        override suspend fun deleteForPlanExcept(planId: Long, nutrientKeys: List<String>) {}
        override suspend fun deleteByPlanIdAndNutrientKey(planId: Long, nutrientKey: String) {}
    }
}