package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import kotlinx.coroutines.flow.Flow

/**
 * Repository for canonical ingredient master data.
 *
 * This repository owns CRUD access to IngredientEntity records.
 * Supplement-specific ingredient composition does NOT belong here; that belongs
 * to SupplementIngredientEntity / SupplementIngredientDao.
 *
 * Design rules:
 * - Ingredients are reusable canonical definitions
 * - UI typically observes ingredients sorted by name
 * - Search should be lightweight and name-based for picker/checklist flows
 */
interface IngredientRepository {

    fun observeAllIngredients(): Flow<List<IngredientEntity>>

    fun searchIngredients(query: String): Flow<List<IngredientEntity>>

    suspend fun getAllIngredients(): List<IngredientEntity>

    suspend fun getIngredientById(id: Long): IngredientEntity?

    suspend fun getIngredientByName(name: String): IngredientEntity?

    suspend fun upsertIngredient(ingredient: IngredientEntity): Long

    suspend fun upsertIngredients(ingredients: List<IngredientEntity>): List<Long>

    suspend fun updateIngredient(ingredient: IngredientEntity)

    suspend fun deleteIngredient(ingredient: IngredientEntity)

    suspend fun updateRda(
        id: Long,
        rdaValue: Double?,
        rdaUnit: IngredientUnit?
    )

    suspend fun updateUpperLimit(
        id: Long,
        upperLimitValue: Double?,
        upperLimitUnit: IngredientUnit?
    )

    suspend fun updateCategory(
        id: Long,
        category: String?
    )

    suspend fun clearAllIngredients()
}