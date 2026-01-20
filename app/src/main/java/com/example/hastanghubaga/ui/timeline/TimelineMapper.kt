package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.meal.MealType
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.ui.util.UiFormatter
import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable
import com.example.hastanghubaga.ui.timeline.SupplementUiModel
import com.example.hastanghubaga.ui.timeline.MealUiModel
import com.example.hastanghubaga.ui.timeline.ActivityUiModel

fun SupplementWithUserSettings.toPreviewTimelineItems(): List<TimelineItem> =
    scheduledTimes.map { time ->
        TimelineItem.SupplementTimelineItem(
            time = time,
            supplement = this,
        )
    }

fun TimelineItem.toTimelineItemUiModel(): TimelineItemUiModel =
    when (this) {

        is TimelineItem.SupplementTimelineItem -> {
            SupplementUiModel(
                id = supplement.supplement.id,
                time = time,
                title = supplement.supplement.name,
                subtitle =
                    "${supplement.effectiveServingSize.asDisplayTextNonComposable()} " +
                            supplement.effectiveDoseUnit.toDisplayCase(
                                supplement.effectiveServingSize
                            ),

                // MealAwareDoseState does NOT represent "taken" — it represents readiness/advice.
                // Until we wire dose logs into the timeline, this must not be inferred here.
                isCompleted = false,

                supplementId = supplement.supplement.id,
                scheduledTime = time,
                doseState = supplement.doseState,
                defaultUnit = supplement.supplement.recommendedDoseUnit,
                suggestedDose = supplement.supplement.recommendedServingSize
            )
        }

        is TimelineItem.MealTimelineItem ->
            MealUiModel(
                id = meal.id,
                time = meal.timestamp.time,
                title = meal.type.name,
                subtitle = meal.notes,
                isCompleted = true, // meals exist only if logged (for now)

                mealId = meal.id,
                mealType = meal.type.toDomain()
            )

        is TimelineItem.ActivityTimelineItem ->
            ActivityUiModel(
                id = activity.id,
                time = activity.start.time,
                title = activity.type.name,
                subtitle = UiFormatter.formatTimeRange(
                    start = activity.start,
                    end = activity.end
                ),
                isCompleted = activity.end != null,

                activityId = activity.id,
                activityType = activity.type,
                startTime = activity.start.time,
                endTime = activity.end?.time,
                intensity = activity.intensity
            )

        is TimelineItem.SupplementDoseLogTimelineItem ->
            SupplementDoseLogUiModel(
            id = this.doseLogId, /* doseLogId WHEN AVAILABLE, else temporary synthetic */
            time = time,
            title = title,
            subtitle = buildString {
                amount?.let { append(it) }
                unit?.let {
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
                scheduledTime?.let {
                    if (isNotEmpty()) append(" ")
                    append("(scheduled $it)")
                }
            }.ifBlank { null },
            isCompleted = true,

            supplementId = supplementId,
            scheduledTime = scheduledTime,
            amountText = amount?.toString(),
            unitText = unit
        )
    }


fun List<TimelineItem>.toTimelineItemUiModels(): List<TimelineItemUiModel> =
    map { it.toTimelineItemUiModel() }

private fun com.example.hastanghubaga.data.local.entity.meal.MealType.toDomain(): MealType =
    when (this) {
        com.example.hastanghubaga.data.local.entity.meal.MealType.BREAKFAST ->
            MealType.BREAKFAST

        com.example.hastanghubaga.data.local.entity.meal.MealType.LUNCH ->
            MealType.LUNCH

        com.example.hastanghubaga.data.local.entity.meal.MealType.DINNER ->
            MealType.DINNER

        com.example.hastanghubaga.data.local.entity.meal.MealType.SNACK ->
            MealType.SNACK

        com.example.hastanghubaga.data.local.entity.meal.MealType.PRE_WORKOUT ->
            MealType.PRE_WORKOUT

        com.example.hastanghubaga.data.local.entity.meal.MealType.POST_WORKOUT ->
            MealType.POST_WORKOUT

        com.example.hastanghubaga.data.local.entity.meal.MealType.CUSTOM ->
            MealType.CUSTOM
    }

//fun List<TimelineItem>.toTimelineItemUiModelsWithIsCompleted(): List<TimelineItemUiModel> =
//    map { domainItem ->
//        when (domainItem) {
//
//            is TimelineItem.SupplementTimelineItem -> {
//                val supplement = domainItem.supplement
//                val time = domainItem.time
//
//                SupplementUiModel(
//                    id = supplement.supplement.id,
//                    time = time,
//                    title = supplement.supplement.name,
//                    subtitle =
//                        "${supplement.effectiveServingSize.asDisplayTextNonComposable()} " +
//                                supplement.effectiveDoseUnit.toDisplayCase(
//                                    supplement.effectiveServingSize
//                                ),
//
//                    // ✅ this now works because isTaken exists
//                    isCompleted = domainItem.isTaken,
//
//                    supplementId = supplement.supplement.id,
//                    scheduledTime = time,
//                    doseState = supplement.doseState,
//                    defaultUnit = supplement.supplement.recommendedDoseUnit,
//                    suggestedDose = supplement.supplement.recommendedServingSize
//                )
//            }
//
//            is TimelineItem.MealTimelineItem -> {
//                val meal = domainItem.meal
//                MealUiModel(
//                    id = meal.id,
//                    time = domainItem.time,
//                    title = meal.type.name, // or however you display it
//                    subtitle = meal.notes,
//                    // ✅ if meal row exists, it’s done
//                    isCompleted = true,
//
//                    mealId = meal.id,
//                    mealType = meal.type.toDomain()
//                )
//            }
//
//            is TimelineItem.ActivityTimelineItem -> {
//                val activity = domainItem.activity
//                ActivityUiModel(
//                    id = activity.id,
//                    time = domainItem.time,
//                    title = activity.type.name, // or display mapping
//                    subtitle = "${activity.start.time} - ${activity.end?.time ?: "…"}",
//                    // ✅ done when ended
//                    isCompleted = activity.end != null,
//
//                    activityId = activity.id,
//                    activityType = activity.type,
//                    intensity = activity.intensity,
//                    startTime = activity.start.time,
//                    endTime = null
//                )
//            }
//
//            is TimelineItem.SupplementDoseLogTimelineItem -> {
//                // If you’re not ready to show “extra dose” rows yet,
//                // do NOT leave TODO() (it will crash if one appears).
//                SupplementDoseLogUiModel(
//                    id = -1L,
//                    time = domainItem.time,
//                    title = domainItem.title,
//                    subtitle = buildString {
//                        domainItem.amount?.let { append(it) }
//                        domainItem.unit?.let { append(" "); append(it) }
//                        domainItem.scheduledTime?.let { append(" (scheduled "); append(it); append(")") }
//                    }.ifBlank { null },
//                    isCompleted = true,
//                    supplementId = domainItem.supplementId,
//                    scheduledTime = TODO(),
//                    amountText = TODO(),
//                    unitText = TODO()
//                )
//            }
//        }
//    }
