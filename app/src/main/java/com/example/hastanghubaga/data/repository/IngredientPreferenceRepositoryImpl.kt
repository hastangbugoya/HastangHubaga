package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.widget.IngredientPreferenceDao
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.repository.widget.IngredientPreferenceRepository
import com.example.hastanghubaga.domain.model.widget.IngredientPreference
import javax.inject.Inject

class IngredientPreferenceRepositoryImpl @Inject constructor(
    private val dao: IngredientPreferenceDao
) : IngredientPreferenceRepository {

    override suspend fun getForIngredientIds(
        ingredientIds: List<Long>
    ): Map<Long, IngredientPreference> {
        return dao
            .getForIngredientIds(ingredientIds)
            .associate{ entity -> entity.ingredientId to
                    entity.toDomain()
            }
    }
}
