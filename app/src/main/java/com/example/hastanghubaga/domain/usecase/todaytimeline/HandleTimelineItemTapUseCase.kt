package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.ui.timeline.SupplementUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import javax.inject.Inject

/**
 * Resolves the domain meaning of a tap on a Today timeline item.
 *
 * This use case is pure decision logic only.
 */
class HandleTimelineItemTapUseCase @Inject constructor() {

    fun resolve(item: TimelineItemUiModel): TimelineTapAction {
        return when (item) {
            is SupplementUiModel ->
                TimelineTapAction.RequestDoseInput(
                    supplementId = item.id,
                    defaultUnit = item.defaultUnit,
                    suggestedDose = item.suggestedDose,
                    title = item.title,
                    scheduledTime = item.scheduledTime,
                    occurrenceId = item.occurrenceId
                )

            else ->
                TimelineTapAction.NoOp
        }
    }
}