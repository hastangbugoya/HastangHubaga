package com.example.hastanghubaga.feature.today

import androidx.compose.runtime.Composable
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
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
        val exerciseDraft: ExerciseDraft? = null
    )

    /**
     * User / system intents
     */
    sealed interface Intent {
        data class LoadDate(val date: LocalDate) : Intent
        data object Refresh : Intent
        data class TimelineItemClicked(val item: TimelineItemUiModel) : Intent

        data class ConfirmDose(
            val supplementId: Long,
            val amount: Double,
            val unit: SupplementDoseUnit,
            val scheduledTime: LocalTime?,
            val actualTime: LocalTime?
        ) : Intent

        data class ExerciseTapped(val item: TimelineItemUiModel) : Intent

        data object ExerciseStartPressed : Intent

        data object ExerciseConfirmPressed : Intent

        data class ExerciseNotesChanged(val value: String) : Intent

        data class ExerciseIntensityChanged(val value: Int?) : Intent

        data class ExerciseEndTimeChanged(val value: LocalTime?) : Intent

        data object DismissExerciseSheet : Intent

        data class SupplementLogOptionSelected(
            val supplementId: Long,
            val title: String,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double?,
            val scheduledTime: LocalTime?,
            val option: SupplementLogOption
        ) : Intent

        data class LogMealConfirmed(
            val input: MealLogInput
        ) : Intent

        data class LogMealTapped(
            val mealType: MealType,
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

        data class ShowDoseInputDialog(
            val supplementId: Long,
            val title: String,
            val scheduledTime: LocalTime? = null,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double? = null,
        ) : Effect

        /**
         * Shows a small choice sheet when user taps a scheduled supplement row.
         * Lets the user decide whether they are logging the scheduled dose or an extra/now dose.
         */
        data class ShowSupplementLogChoice(
            val supplementId: Long,
            val title: String,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double?,
            val scheduledTime: LocalTime? = null
        ) : Effect
    }

    sealed interface Destination {
        data class Supplement(val id: Long) : Destination
        data class Meal(val id: Long) : Destination
        data class Activity(val id: Long) : Destination
    }

    data class ExerciseDraft(
        val activityType: ActivityType,
        val startTime: LocalTime,
        val endTime: LocalTime?,
        val notes: String,
        val intensity: Int?,
        val phase: Phase
    ) {
        enum class Phase { Draft, Running }
    }

    enum class SupplementLogOption {
        Scheduled,
        NowExtra
    }

    /**
     * UI-level input for logging a meal.
     * Represents user-entered data, not DB entities.
     */
    data class MealLogInput(
        val mealType: MealType,
        val notes: String? = null,
        val nutrition: NutritionInput? = null,
        val timeUseIntent: TimeUseIntent = TimeUseIntent.ActualNow
    )

    fun MealLogInput.toDomain() = com.example.hastanghubaga.domain.model.meal.LogMealInput(
        mealType = mealType,
        timeUseIntent = timeUseIntent,
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

    fun NutritionInput.toDomain() = com.example.hastanghubaga.domain.model.meal.NutritionInput(
        calories = calories?.toInt() ?: 0,
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
        kotlinx.datetime.Instant
            .fromEpochMilliseconds(utcMillis)
            .toLocalDateTime(DomainTimePolicy.localTimeZone)
}