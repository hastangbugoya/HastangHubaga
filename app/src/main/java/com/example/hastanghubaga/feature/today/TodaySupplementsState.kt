package com.example.hastanghubaga.feature.today

import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings

data class TodaySupplementsState(
    val isLoading: Boolean = false,
    val todaySupplements: List<SupplementWithUserSettings> = emptyList(),
    val errorMessage: String? = null
)
