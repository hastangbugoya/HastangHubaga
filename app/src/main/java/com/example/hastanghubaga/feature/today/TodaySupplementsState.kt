package com.example.hastanghubaga.feature.today

import com.example.hastanghubaga.domain.model.supplement.Supplement

data class TodaySupplementsState(
    val isLoading: Boolean = false,
    val todaySupplements: List<Supplement> = emptyList(),
    val errorMessage: String? = null
)
