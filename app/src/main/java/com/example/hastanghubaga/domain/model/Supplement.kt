package com.example.hastanghubaga.domain.model

import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType

data class Supplement(
    val id: Long,
    val name: String,
    val frequencyType: FrequencyType,
    val ingredients: List<Ingredient>, // or however your domain wants it
)
