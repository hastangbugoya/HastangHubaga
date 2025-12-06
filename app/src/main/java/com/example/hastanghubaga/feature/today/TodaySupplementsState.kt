package com.example.hastanghubaga.feature.today

import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity

data class TodaySupplementsState(
    val isLoading: Boolean = false,
    val todaySupplements: List<SupplementEntity> = emptyList(),
    val errorMessage: String? = null
)
