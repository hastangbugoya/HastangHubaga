package com.example.hastanghubaga.domain.repository.widget

import com.example.hastanghubaga.domain.model.widget.IngredientPreference

interface IngredientPreferenceRepository {
    suspend fun getForIngredientIds(
        ingredientIds: List<Long>
    ): Map<Long, IngredientPreference>
}