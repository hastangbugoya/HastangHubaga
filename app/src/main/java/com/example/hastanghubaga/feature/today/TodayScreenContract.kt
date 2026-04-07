package com.example.hastanghubaga.feature.today

import androidx.compose.runtime.Composable
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.LogMealInput
import com.example.hastanghubaga.domain.model.nutrition.DailyComplianceResult
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.ui.timeline.MealUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime

object TodayScreenContract {

    /**
     * UI State – single source of truth
     */
    data class State(
        val selectedDate: LocalDate = DomainTimePolicy.todayLocal(),
        val isLoading: Boolean = false,
        val uiTimelineItems: List<TimelineItemUiModel> = emptyList(),
        val domainTimelineItems: List<TimelineItem> = emptyList(),
        val errorMessage: String? = null,
        val exerciseDraft: ExerciseDraft? = null,
        val mealDraft: MealLogInput? = null,

        /**
         * Daily nutrition compliance result for the selected date.
         *
         * Phase 1:
         * - Computed and stored on state
         * - Not yet required to render in Today UI
         *
         * Future:
         * - Today header summary
         * - Calendar/day coloring
         * - Drill-down into plan and nutrient comparisons
         */
        val dailyCompliance: DailyComplianceResult? = null
    )

    /**
     * User / system intents
     */
    sealed interface Intent {
        data class LoadDate(val date: LocalDate) : Intent
        data object Refresh : Intent
        data class TimelineItemClicked(val item: TimelineItemUiModel) : Intent

        /**
         * Opens the temporary force-log supplement picker.
         *
         * This is intentionally separate from tapping a scheduled supplement row.
         * It allows the user to log an actual supplement dose even when the
         * supplement is not scheduled on the current timeline.
         */
        data object ForceLogSupplementTapped : Intent

        /**
         * Result from the temporary force-log supplement picker.
         *
         * The selected supplement should then flow into the existing dose input
         * dialog with:
         * - scheduledTime = null
         * - occurrenceId = null
         *
         * This preserves the distinction between:
         * - planned/scheduled supplement occurrences
         * - actual manual/force-logged supplement events
         */
        data class ForceLogSupplementSelected(
            val supplementId: Long,
            val title: String,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double?
        ) : Intent

        /**
         * Confirms a supplement dose log from the Today screen.
         *
         * [occurrenceId] is optional because:
         * - a scheduled supplement row may map to a concrete planned occurrence
         * - an extra / manual supplement log may not yet be linked to an occurrence
         *
         * [actualDate] and [actualTime] represent the user-entered truth of when
         * the intake happened. These are intentionally separate from planned
         * schedule context.
         *
         * This keeps the UI contract forward-compatible with occurrence-aware
         * reconciliation while still supporting current manual logging flows.
         */
        data class ConfirmDose(
            val supplementId: Long,
            val amount: Double,
            val unit: SupplementDoseUnit,
            val scheduledTime: LocalTime?,
            val actualDate: LocalDate?,
            val actualTime: LocalTime?,
            val occurrenceId: String? = null
        ) : Intent

        /**
         * User tapped an activity card on the timeline and wants to log the
         * planned activity occurrence.
         */
        data class ExerciseTapped(val item: TimelineItemUiModel) : Intent

        /**
         * Saves the current activity log draft as an actual logged activity session.
         */
        data object ExerciseConfirmPressed : Intent

        data class ExerciseDateChanged(val value: LocalDate) : Intent

        data class ExerciseStartTimeChanged(val value: LocalTime) : Intent

        data class ExerciseEndTimeChanged(val value: LocalTime) : Intent

        data class ExerciseNotesChanged(val value: String) : Intent

        data class ExerciseIntensityChanged(val value: Int?) : Intent

        data object DismissExerciseSheet : Intent

        /**
         * User tapped a native HH meal row on the timeline and wants to log the
         * exact planned meal occurrence represented by that row.
         *
         * Important:
         * - The tapped [MealUiModel] is the source of truth.
         * - Do not reconstruct meal logging state from meal type alone.
         * - This preserves row-level planner identity such as [MealUiModel.occurrenceId]
         *   and exact placement time from the tapped timeline item.
         */
        data class LogMealTapped(
            val item: MealUiModel
        ) : Intent

        /**
         * Saves the current meal log draft as an actual logged meal.
         */
        data class LogMealConfirmed(
            val input: MealLogInput
        ) : Intent

        /**
         * Dismisses the meal sheet and clears any in-progress meal draft state.
         */
        data object DismissMealSheet : Intent

        /**
         * Result from the small supplement choice sheet shown when the user taps
         * a supplement row.
         *
         * [occurrenceId] identifies the concrete planned occurrence when the tap
         * came from a scheduled supplement item. It remains nullable for extra/now
         * flows and for the current incremental transition toward occurrence-aware
         * logging.
         */
        data class SupplementLogOptionSelected(
            val supplementId: Long,
            val title: String,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double?,
            val scheduledTime: LocalTime?,
            val occurrenceId: String? = null,
            val option: SupplementLogOption
        ) : Intent
    }

    /**
     * One-off effects (not state)
     */
    sealed interface Effect {
        data class ShowSnackbar(
            val message: String
        ) : Effect

        data class ShowBanner(
            val message: String
        ) : Effect

        data class ShowBottomSheet(
            val content: @Composable () -> Unit
        ) : Effect

        data class ShowError(
            val message: String
        ) : Effect

        data class Navigate(
            val destination: Destination
        ) : Effect

        /**
         * Shows the temporary supplement picker for force-log flows.
         *
         * The picker should present active supplements only.
         * Selecting one should lead into the existing dose input dialog with
         * no scheduled time / no occurrence linkage.
         */
        data class ShowForceLogSupplementPicker(
            val supplements: List<Supplement>
        ) : Effect

        /**
         * Shows the supplement dose input dialog.
         *
         * [occurrenceId] is optional and allows the dialog confirmation path to
         * preserve which concrete planned supplement occurrence is being logged.
         *
         * [initialActualDate] and [initialActualTime] are editable user-facing
         * defaults for the actual intake timestamp. UI may present them via simple
         * text buttons that open pickers.
         */
        data class ShowDoseInputDialog(
            val supplementId: Long,
            val title: String,
            val scheduledTime: LocalTime? = null,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double? = null,
            val occurrenceId: String? = null,
            val initialActualDate: LocalDate = DomainTimePolicy.todayLocal(),
            val initialActualTime: LocalTime? = null
        ) : Effect

        /**
         * Shows a small choice sheet when user taps a scheduled supplement row.
         * Lets the user decide whether they are logging the scheduled dose or an extra/now dose.
         *
         * [occurrenceId] identifies the concrete scheduled planner item when available.
         */
        data class ShowSupplementLogChoice(
            val supplementId: Long,
            val title: String,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double?,
            val scheduledTime: LocalTime? = null,
            val occurrenceId: String? = null
        ) : Effect
    }

    sealed interface Destination {
        data class Supplement(val id: Long) : Destination
        data class Meal(val id: Long) : Destination
        data class Activity(val id: Long) : Destination
    }

    /**
     * UI draft for logging an activity session.
     *
     * This represents user-editable logging data, not a live in-progress timer.
     *
     * [activityId] preserves the template identity when the log came from a known
     * activity definition.
     *
     * [occurrenceId] preserves the linkage to the planned activity occurrence so
     * the timeline can later treat that occurrence as fulfilled and show only
     * the logged card.
     */
    data class ExerciseDraft(
        val activityId: Long? = null,
        val activityType: ActivityType,
        val logDate: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val notes: String,
        val intensity: Int?,
        val occurrenceId: String? = null
    )

    enum class SupplementLogOption {
        Scheduled,
        NowExtra
    }

    /**
     * UI-level draft for logging a meal.
     *
     * This represents user-editable meal logging state on the Today screen.
     * It intentionally mirrors the role that [ExerciseDraft] plays for activities.
     *
     * [occurrenceId] preserves linkage to the planned meal occurrence so the
     * actual meal log can later fulfill/suppress the matching planned row.
     *
     * [endTime] is optional because the current minimum requirement is primarily
     * "I ate this meal at this time", while still leaving room for a meal window.
     */
    data class MealLogInput(
        val mealType: com.example.hastanghubaga.data.local.entity.meal.MealType,
        val logDate: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime? = null,
        val notes: String? = null,
        val nutrition: NutritionInput? = null,
        val occurrenceId: String? = null
    )

    fun MealLogInput.toDomain(): LogMealInput =
        LogMealInput(
            mealType = mealType,
            timeUseIntent = TimeUseIntent.Explicit(logDate, startTime),
            occurrenceId = occurrenceId,
            notes = notes,
            nutrition = nutrition?.toDomain()
        )

    /**
     * Optional nutrition input from UI.
     */
    data class NutritionInput(
        val calories: Double?,
        val proteinGrams: Double?,
        val carbsGrams: Double?,
        val fatGrams: Double?,
        val sodiumMg: Double?,
        val cholesterolMg: Double?,
        val fiberGrams: Double?
    )

    fun NutritionInput.toDomain(): com.example.hastanghubaga.domain.model.meal.NutritionInput =
        com.example.hastanghubaga.domain.model.meal.NutritionInput(
            calories = calories?.toInt(),
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            sodiumMg = sodiumMg,
            cholesterolMg = cholesterolMg,
            fiberGrams = fiberGrams
        )

    fun epochMillisToLocalDateTime(
        utcMillis: Long
    ): kotlinx.datetime.LocalDateTime =
        Instant
            .fromEpochMilliseconds(utcMillis)
            .toLocalDateTime(DomainTimePolicy.localTimeZone)
}