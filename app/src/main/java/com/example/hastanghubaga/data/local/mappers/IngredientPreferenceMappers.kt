package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.widget.IngredientPreferenceEntity
import com.example.hastanghubaga.domain.model.widget.IngredientPreference

fun IngredientPreferenceEntity.toDomain(): IngredientPreference =
    IngredientPreference(
        ingredientId = ingredientId,
        isFavorite = isFavorite
    )

 fun IngredientPreference.toEntity(): IngredientPreferenceEntity =
    IngredientPreferenceEntity(
        ingredientId = ingredientId,
        isFavorite = isFavorite
    )