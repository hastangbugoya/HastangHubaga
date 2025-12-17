package com.example.hastanghubaga.feature.today

import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel


data class TodayScreenState(
    val isLoading: Boolean = false,
    val todaySupplements: List<SupplementWithUserSettings> = emptyList(),
    val errorMessage: String? = null,
    val timelineItems: List<TimelineItemUiModel> = emptyList()
)
