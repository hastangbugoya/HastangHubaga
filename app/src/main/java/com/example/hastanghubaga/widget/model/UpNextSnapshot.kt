package com.example.hastanghubaga.widget.model

import com.example.hastanghubaga.ui.timeline.TodayUiRowType

data class UpNextSnapshot(
    val type: TodayUiRowType,
    val title: String,
    val subtitle: String?,
    val timeLabel: String,
    val iconKey: String,
    val referenceId: Long
)