package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.domain.repository.supplement.IngredientRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Default Room-backed implementation of IngredientRepository.
 */
@Singleton
class IngredientRepositoryImpl @Inject constructor(
    private val ingredientEntityDao: IngredientEntityDao
) : IngredientRepository {

    override fun observeAllIngredients(): Flow<List<IngredientEntity>> =
        ingredientEntityDao.observeAllIngredients()

    override fun searchIngredients(query: String): Flow<List<IngredientEntity>> =
        ingredientEntityDao.searchIngredients(query)

    override suspend fun getAllIngredients(): List<IngredientEntity> =
        ingredientEntityDao.getAllIngredients()

    override suspend fun getIngredientById(id: Long): IngredientEntity? =
        ingredientEntityDao.getIngredientById(id)

    override suspend fun getIngredientByName(name: String): IngredientEntity? =
        ingredientEntityDao.getIngredientByName(name)

    override suspend fun upsertIngredient(ingredient: IngredientEntity): Long =
        ingredientEntityDao.insertIngredient(ingredient)

    override suspend fun upsertIngredients(ingredients: List<IngredientEntity>): List<Long> =
        ingredientEntityDao.insertIngredients(ingredients)

    override suspend fun updateIngredient(ingredient: IngredientEntity) {
        ingredientEntityDao.updateIngredient(ingredient)
    }

    override suspend fun deleteIngredient(ingredient: IngredientEntity) {
        ingredientEntityDao.deleteIngredient(ingredient)
    }

    override suspend fun updateRda(
        id: Long,
        rdaValue: Double?,
        rdaUnit: IngredientUnit?
    ) {
        ingredientEntityDao.updateRda(
            id = id,
            rdaValue = rdaValue,
            rdaUnit = rdaUnit
        )
    }

    override suspend fun updateUpperLimit(
        id: Long,
        upperLimitValue: Double?,
        upperLimitUnit: IngredientUnit?
    ) {
        ingredientEntityDao.updateUpperLimit(
            id = id,
            upperLimitValue = upperLimitValue,
            upperLimitUnit = upperLimitUnit
        )
    }

    override suspend fun updateCategory(
        id: Long,
        category: String?
    ) {
        ingredientEntityDao.updateCategory(
            id = id,
            category = category
        )
    }

    override suspend fun clearAllIngredients() {
        ingredientEntityDao.clearAllIngredients()
    }
}