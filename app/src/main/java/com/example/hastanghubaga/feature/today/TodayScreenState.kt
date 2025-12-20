package com.example.hastanghubaga.feature.today

import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel


data class TodayScreenState(
    val isLoading: Boolean = false,
    val todaySupplements: List<SupplementWithUserSettings> = emptyList(),
    val errorMessage: String? = null,
    val uiTimelineItems: List<TimelineItemUiModel> = emptyList(),
    val domainTimelineItems: List<TimelineItem> = emptyList()
)
