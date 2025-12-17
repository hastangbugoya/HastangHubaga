package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import java.time.LocalTime

interface TimelineItemUiModel {
    val key: String
    val id: Long
    val time: LocalTime
    val title: String
    val subtitle: String?
    val rowType: TodayUiRowType

    data class Supplement(
        override val id: Long,
        override val time: LocalTime,
        override val title: String,
        override val subtitle: String?,
        val doseState: MealAwareDoseState?,
        ) : TimelineItemUiModel {
            override val rowType: TodayUiRowType = TodayUiRowType.SUPPLEMENT
        override val key = "${rowType.name}-${id}-$time"
        }

    data class Meal(
        override val id: Long,
        override val time: LocalTime,
        override val title: String,
        override val subtitle: String?,
        val type: MealType
    ) : TimelineItemUiModel {
        override val rowType: TodayUiRowType = TodayUiRowType.MEAL
        override val key = "${rowType.name}-${id}-$time"
    }

    data class Activity(
        override val id: Long,
        override val time: LocalTime,
        override val title: String,
        override val subtitle: String?,
    ) : TimelineItemUiModel {
        override val rowType: TodayUiRowType = TodayUiRowType.ACTIVITY
        override val key = "${rowType.name}-${id}-$time"
    }
}
