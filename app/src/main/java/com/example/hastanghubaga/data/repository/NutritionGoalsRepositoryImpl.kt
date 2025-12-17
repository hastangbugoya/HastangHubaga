package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoal
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import com.example.hastanghubaga.domain.repository.nutrition.NutritionGoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NutritionGoalsRepositoryImpl @Inject constructor(
    private val dao: UserNutritionGoalsEntityDao
) : NutritionGoalsRepository {

    override fun observeAll(): Flow<List<NutritionGoal>> =
        dao.observeAllGoals().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<NutritionGoal?> =
        dao.observeActiveGoal().map { entity -> entity?.toDomain() }

    override suspend fun getAll(): List<NutritionGoal> =
        dao.getAllGoals().map { it.toDomain() }

    override suspend fun getActive(): NutritionGoal? =
        dao.getActiveGoal()?.toDomain()

    override suspend fun upsert(goal: NutritionGoal): Long =
        dao.upsert(goal.toEntity())

    override suspend fun delete(goal: NutritionGoal) {
        dao.delete(goal.toEntity())
    }

    override suspend fun setActive(goalId: Long) {
        dao.activateGoal(goalId)
    }

    override suspend fun getByType(type: NutritionGoalType): List<NutritionGoal> =
        dao.getGoalsByType(type).map { it.toDomain() }
}
