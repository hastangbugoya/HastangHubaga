package com.example.hastanghubaga.data.repository

import androidx.room.withTransaction
import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlan
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanGoal
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanWithGoals
import com.example.hastanghubaga.domain.repository.nutrition.NutritionPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionGoalsRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val planDao: NutritionPlanEntityDao,
    private val goalDao: NutrientGoalDao
) : NutritionPlanRepository {

    override fun observeAllPlans(): Flow<List<NutritionPlan>> =
        planDao.observeAllPlans().map { plans ->
            plans.map { it.toDomain() }
        }

    override fun observeActivePlans(): Flow<List<NutritionPlan>> =
        planDao.observeActivePlans().map { plans ->
            plans.map { it.toDomain() }
        }

    override fun observePlan(planId: Long): Flow<NutritionPlan?> =
        planDao.observePlanById(planId).map { it?.toDomain() }

    override fun observePlanWithGoals(planId: Long): Flow<NutritionPlanWithGoals?> =
        combine(
            planDao.observePlanById(planId),
            goalDao.observeGoalsForPlan(planId)
        ) { planEntity, goalEntities ->
            planEntity?.let {
                NutritionPlanWithGoals(
                    plan = it.toDomain(),
                    goals = goalEntities.map { goal -> goal.toDomain() }
                )
            }
        }

    override suspend fun getAllPlans(): List<NutritionPlan> =
        planDao.getAllPlans().map { it.toDomain() }

    override suspend fun getActivePlans(): List<NutritionPlan> =
        planDao.getActivePlans().map { it.toDomain() }

    override suspend fun getPlan(planId: Long): NutritionPlan? =
        planDao.getPlanById(planId)?.toDomain()

    override suspend fun getPlanWithGoals(planId: Long): NutritionPlanWithGoals? {
        val plan = planDao.getPlanById(planId) ?: return null
        val goals = goalDao.getGoalsForPlan(planId).map { it.toDomain() }

        return NutritionPlanWithGoals(
            plan = plan.toDomain(),
            goals = goals
        )
    }

    override suspend fun getPlansByType(type: NutritionGoalType): List<NutritionPlan> =
        planDao.getPlansByType(type.name).map { it.toDomain() }

    override suspend fun getPlansBySourceType(sourceType: String): List<NutritionPlan> =
        planDao.getPlansBySourceType(sourceType).map { it.toDomain() }

    override suspend fun getPlanBySource(
        sourceType: String,
        sourcePlanId: String
    ): NutritionPlan? =
        planDao.getPlanBySource(sourceType, sourcePlanId)?.toDomain()

    override suspend fun getPlansEffectiveOn(dateMillis: Long): List<NutritionPlan> =
        planDao.getPlansEffectiveOn(dateMillis).map { it.toDomain() }

    override suspend fun getActivePlansEffectiveOn(dateMillis: Long): List<NutritionPlan> =
        planDao.getActivePlansEffectiveOn(dateMillis).map { it.toDomain() }

    override suspend fun getGoalsForPlan(planId: Long): List<NutritionPlanGoal> =
        goalDao.getGoalsForPlan(planId).map { it.toDomain() }

    override suspend fun createPlan(
        plan: NutritionPlan,
        goals: List<NutritionPlanGoal>
    ): Long = db.withTransaction {
        validateGoals(goals)

        val newPlanId = planDao.insert(plan.toEntity())

        if (goals.isNotEmpty()) {
            goalDao.insertAll(goals.map { it.toEntity(newPlanId) })
        }

        newPlanId
    }

    override suspend fun updatePlan(plan: NutritionPlan) {
        planDao.update(plan.toEntity())
    }

    override suspend fun replacePlanGoals(
        planId: Long,
        goals: List<NutritionPlanGoal>
    ) {
        db.withTransaction {
            validateGoals(goals)

            goalDao.deleteGoalsForPlan(planId)

            if (goals.isNotEmpty()) {
                goalDao.insertAll(goals.map { it.toEntity(planId) })
            }
        }
    }

    override suspend fun updatePlanWithGoals(
        plan: NutritionPlan,
        goals: List<NutritionPlanGoal>
    ) {
        db.withTransaction {
            validateGoals(goals)

            planDao.update(plan.toEntity())
            goalDao.deleteGoalsForPlan(plan.id)

            if (goals.isNotEmpty()) {
                goalDao.insertAll(goals.map { it.toEntity(plan.id) })
            }
        }
    }

    override suspend fun deletePlan(planId: Long) {
        db.withTransaction {
            val existing = planDao.getPlanById(planId) ?: return@withTransaction
            planDao.delete(existing)
        }
    }

    override suspend fun setPlanActiveState(
        planId: Long,
        isActive: Boolean
    ) {
        planDao.setPlanActiveState(
            planId = planId,
            isActive = isActive,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun upsertPlanGoal(
        planId: Long,
        goal: NutritionPlanGoal
    ) {
        db.withTransaction {
            validateGoal(goal)

            val existing = goalDao.getGoalByPlanIdAndNutrientKey(
                planId = planId,
                nutrientKey = goal.nutrientKey
            )

            if (existing == null) {
                goalDao.insert(goal.toEntity(planId))
            } else {
                goalDao.update(goal.toEntity(planId, existing.id))
            }
        }
    }

    override suspend fun deletePlanGoal(
        planId: Long,
        nutrientKey: String
    ) {
        goalDao.deleteGoalByPlanIdAndNutrientKey(planId, nutrientKey)
    }

    private fun UserNutritionPlanEntity.toDomain(): NutritionPlan =
        NutritionPlan(
            id = id,
            type = type,
            name = name,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            sourceType = sourceType,
            sourcePlanId = sourcePlanId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    private fun NutritionPlan.toEntity(): UserNutritionPlanEntity =
        UserNutritionPlanEntity(
            id = id,
            type = type,
            name = name,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            sourceType = sourceType,
            sourcePlanId = sourcePlanId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    private fun NutrientGoalEntity.toDomain(): NutritionPlanGoal =
        NutritionPlanGoal(
            id = id,
            nutrientKey = nutrientKey,
            minValue = minValue,
            targetValue = targetValue,
            maxValue = maxValue
        )

    private fun NutritionPlanGoal.toEntity(
        planId: Long,
        id: Long = this.id
    ): NutrientGoalEntity =
        NutrientGoalEntity(
            id = id,
            planId = planId,
            nutrientKey = nutrientKey,
            minValue = minValue,
            targetValue = targetValue,
            maxValue = maxValue
        )

    private fun validateGoals(goals: List<NutritionPlanGoal>) {
        val duplicates = goals
            .groupBy { it.nutrientKey.trim() }
            .filterKeys { it.isNotBlank() }
            .filterValues { it.size > 1 }
            .keys

        require(duplicates.isEmpty()) {
            "Duplicate nutrient keys: $duplicates"
        }

        goals.forEach(::validateGoal)
    }

    private fun validateGoal(goal: NutritionPlanGoal) {
        require(goal.nutrientKey.isNotBlank()) {
            "Nutrition plan goal nutrientKey must not be blank."
        }

        require(
            goal.minValue != null ||
                    goal.targetValue != null ||
                    goal.maxValue != null
        ) {
            "Nutrition plan goal '${goal.nutrientKey}' must define at least one of minValue, targetValue, or maxValue."
        }
    }
}