package com.example.hastanghubaga.widget.model

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import com.example.hastanghubaga.ui.util.format12Hour

fun UpcomingSchedule.toUpNextSnapshot(): UpNextSnapshot =
    UpNextSnapshot(
            title = title,
            referenceId = id,
            subtitle = subtitle, // aleady agreed: preformatted is OK
            iconKey = type.name,
            type = type,
            timeLabel = scheduledAt.format12Hour(),
        )