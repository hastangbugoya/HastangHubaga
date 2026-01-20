package com.example.hastanghubaga.feature.today

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hastanghubaga.domain.model.activity.isExercise
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.feature.today.ActiveLocalSheet.*
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.*
import com.example.hastanghubaga.ui.common.BannerController
import com.example.hastanghubaga.ui.common.BottomSheetController
import com.example.hastanghubaga.ui.common.ErrorView
import com.example.hastanghubaga.ui.common.LoadingView
import com.example.hastanghubaga.ui.common.SnackbarController
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.MealUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.tokens.Dimens
import com.example.hastanghubaga.ui.tokens.UiColors
import kotlinx.datetime.LocalTime
import kotlin.math.roundToInt

/**
 * Local sheet host:
 * - Dose sheet
 * - Exercise sheet
 *
 * This pattern scales: add another subtype in [ActiveLocalSheet] and handle it in the effect collector.
 */
sealed interface ActiveLocalSheet {
    data class Dose(
        val data: TodayScreenContract.Effect.ShowDoseInputDialog,
        val title: String?
    ) : ActiveLocalSheet

    data class SupplementLogChoice(
        val data: TodayScreenContract.Effect.ShowSupplementLogChoice,
        val title: String?
    ) : ActiveLocalSheet

    data class Exercise(
        val draft: TodayScreenContract.ExerciseDraft,
        val title: String
    ) : ActiveLocalSheet

    data class Meal(
        val input: TodayScreenContract.MealLogInput
    ): ActiveLocalSheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    snackbarController: SnackbarController,
    bannerController: BannerController,
    bottomSheetController: BottomSheetController,
    onNavigate: (TodayScreenContract.Destination) -> Unit,
    viewModel: TodayScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var activeSheet by remember { mutableStateOf<ActiveLocalSheet?>(null) }

    val localSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Existing “loaded banner” effect remains unchanged
    LaunchedEffect(state.isLoading, state.uiTimelineItems.size) {
        if (!state.isLoading && state.uiTimelineItems.isNotEmpty()) {
            val count = state.uiTimelineItems.size
            bannerController.show(
                if (count == 1) "Loaded 1 timeline item" else "Loaded $count timeline items"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(TodayScreenContract.Intent.LoadToday)
    }

    /**
     * One collector for all Effects.
     *
     * Bottom sheets:
     * 1) Global sheet via BottomSheetController (Effect.ShowBottomSheet)
     * 2) Local dose sheet (ActiveLocalSheet.Dose)
     * 3) Local exercise sheet (ActiveLocalSheet.Exercise) driven by state.exerciseDraft
     *
     * Adding a 4th local sheet = add a new ActiveLocalSheet subtype + set activeSheet here.
     */
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TodayScreenContract.Effect.ShowSnackbar ->
                    snackbarController.show(effect.message)

                is TodayScreenContract.Effect.ShowBanner ->
                    bannerController.show(effect.message)

                is TodayScreenContract.Effect.ShowBottomSheet ->
                    bottomSheetController.show(effect.content)

                is TodayScreenContract.Effect.Navigate ->
                    onNavigate(effect.destination)

                is TodayScreenContract.Effect.ShowError ->
                    bannerController.show(effect.message)

                is TodayScreenContract.Effect.ShowDoseInputDialog -> {
                    // Keep old behavior, but route to the unified local sheet host
                    activeSheet = Dose(
                        data = effect,
                        title = effect.title
                    )
                }

                is TodayScreenContract.Effect.ShowSupplementLogChoice -> {
                    activeSheet = ActiveLocalSheet.SupplementLogChoice(
                        data = effect,
                        title = effect.title
                    )
                }
            }
        }
    }

    TodayScreenContent(
        state = state,
        onItemClick = { item ->
            // Keep existing behavior: VM decides what to do on click (dose, etc.)
            viewModel.onIntent(TodayScreenContract.Intent.TimelineItemClicked(item))

            when (item) {
                is MealUiModel -> {
                    activeSheet = ActiveLocalSheet.Meal(
                        TodayScreenContract.MealLogInput(
                            mealType = item.mealType,
                            timeUseIntent = TimeUseIntent.Scheduled(item.time),
                            notes = null,
                            nutrition = null
                        )
                    )
                }

                is ActivityUiModel -> {
                    if (item.activityType.isExercise) {
                        viewModel.onIntent(TodayScreenContract.Intent.ExerciseTapped(item))
                    }
                }

                else -> Unit
            }

        },
        onRefresh = {
            viewModel.onIntent(TodayScreenContract.Intent.Refresh)
        }
    )
    // Drive exercise sheet from state so it shows even in the Draft/Start-only phase
    LaunchedEffect(state.exerciseDraft) {
        val draft = state.exerciseDraft
        if (draft != null) {
            activeSheet = ActiveLocalSheet.Exercise(
                draft = draft,
                title = draft.activityType.name.replace('_', ' ')
            )
        } else {
            // If draft cleared, close local sheet if it was Exercise
            if (activeSheet is ActiveLocalSheet.Exercise) activeSheet = null
        }
    }

    // One local ModalBottomSheet host for multiple sheet content types
    activeSheet?.let { sheet ->
        ModalBottomSheet(
            sheetState = localSheetState,
            onDismissRequest = {
                when (sheet) {
                    is Dose -> {
                        activeSheet = null
                    }
                    is Exercise -> {
                        viewModel.onIntent(DismissExerciseSheet)
                        activeSheet = null
                    }
                    is SupplementLogChoice -> {
                        activeSheet = null
                    }
                    is Meal -> {
                        activeSheet = null
                    }
                }
            }
        ) {
            when (sheet) {
                is Dose -> {
                    sheet.title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    DoseInputSheetContent(
                        defaultAmount = sheet.data.suggestedDose ?: 0.0,
                        defaultUnit = sheet.data.defaultUnit,
                        onConfirm = { amount, unit ->
                            viewModel.onIntent(
                                ConfirmDose(
                                    supplementId = sheet.data.supplementId,
                                    amount = amount,
                                    unit = unit,
                                    scheduledTime = sheet.data.scheduledTime,
                                    actualTime = null
                                )
                            )
                            activeSheet = null
                        }
                    )
                }

                is Exercise -> {
                    ExerciseBottomSheetContent(
                        title = sheet.title,
                        draft = sheet.draft,
                        onNotesChange = { viewModel.onIntent(ExerciseNotesChanged(it)) },
                        onIntensityChange = { viewModel.onIntent(ExerciseIntensityChanged(it)) },
                        onEndTimeChange = { viewModel.onIntent(ExerciseEndTimeChanged(it)) },
                        onPrimaryAction = {
                            when (sheet.draft.phase) {
                                TodayScreenContract.ExerciseDraft.Phase.Draft ->
                                    viewModel.onIntent(ExerciseStartPressed)

                                TodayScreenContract.ExerciseDraft.Phase.Running ->
                                    viewModel.onIntent(ExerciseConfirmPressed)
                            }
                        }
                    )
                }

                is SupplementLogChoice -> {
                    SupplementLogChoiceSheetContent(
                        title = sheet.title,
                        scheduledTime = sheet.data.scheduledTime,
                        onLogScheduled = {
                            viewModel.onIntent(
                                SupplementLogOptionSelected(
                                    supplementId = sheet.data.supplementId,
                                    title = sheet.data.title,
                                    defaultUnit = sheet.data.defaultUnit,
                                    suggestedDose = sheet.data.suggestedDose,
                                    scheduledTime = sheet.data.scheduledTime,
                                    option = TodayScreenContract.SupplementLogOption.Scheduled
                                )
                            )
                            activeSheet = null
                        },
                        onLogNowExtra = {
                            viewModel.onIntent(
                                SupplementLogOptionSelected(
                                    supplementId = sheet.data.supplementId,
                                    title = sheet.data.title,
                                    defaultUnit = sheet.data.defaultUnit,
                                    suggestedDose = sheet.data.suggestedDose,
                                    scheduledTime = sheet.data.scheduledTime,
                                    option = TodayScreenContract.SupplementLogOption.NowExtra
                                )
                            )
                            activeSheet = null
                        }
                    )
                }
                is Meal -> {
                    MealLogBottomSheetContent(
                        title = sheet.input.mealType.name, // e.g. "Log Lunch"
                        initialNotes = null,
                        initialNutrition = null,
                        onConfirm = { notes, nutrition ->
                            viewModel.onIntent(
                                TodayScreenContract.Intent.LogMealConfirmed(
                                    TodayScreenContract.MealLogInput(
                                        mealType = sheet.input.mealType,
                                        timeUseIntent = TimeUseIntent.ActualNow,
                                        notes = notes,
                                        nutrition = nutrition
                                    )
                                )
                            )
                            activeSheet = null
                        }
                    )
                }
            }
        }
    }
}

/**
 * Full Exercise bottom sheet UI.
 *
 * Requirements met:
 * - Start time is shown (prefilled by VM on ExerciseTapped)
 * - End time is shown (from Activity UI model; editable hook included)
 * - Notes + intensity inputs are always visible
 * - Button switches Start -> Save/Confirm based on phase
 * - Shows even when only "Start" exists (Draft phase)
 */
@Composable
private fun ExerciseBottomSheetContent(
    title: String,
    draft: TodayScreenContract.ExerciseDraft,
    onNotesChange: (String) -> Unit,
    onIntensityChange: (Int?) -> Unit,
    onEndTimeChange: (kotlinx.datetime.LocalTime?) -> Unit,
    onPrimaryAction: () -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Start: ${draft.startTime}",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "End: ${draft.endTime ?: "—"}",
            style = MaterialTheme.typography.titleMedium
        )

        // (Optional) later replace with a time picker; for now just a hook point
        // onEndTimeChange(...)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = draft.notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Column {
            Text(
                text = "Intensity",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            val intensityValue = draft.intensity?.toFloat() ?: 5f

            Slider(
                value = intensityValue,
                onValueChange = { newValue ->
                    onIntensityChange(newValue.roundToInt())
                },
                valueRange = 1f..10f,
                steps = 8
            )

            Text(
                text = "Level: ${draft.intensity ?: "—"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        val buttonText =
            when (draft.phase) {
                TodayScreenContract.ExerciseDraft.Phase.Draft -> "Start"
                TodayScreenContract.ExerciseDraft.Phase.Running -> "Save"
            }

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}

@Composable
fun TodayScreenContent(
    state: TodayScreenContract.State,
    onItemClick: (TimelineItemUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(state.errorMessage)
        else -> TimelineList(
            items = state.uiTimelineItems,
            onItemClick = onItemClick
        )
    }
}

@Composable
fun TimelineList(
    items: List<TimelineItemUiModel>,
    onItemClick: (TimelineItemUiModel) -> Unit
) {
    Log.d("TimelineList", "Rendering ${items.size} items")
    items.forEach {
        Log.d("TimelineList", "Rendering item: ${it.title} key:${it.key}")
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = items, key = { it.key }) { item ->
            TimelineRow(item = item, onClick = onItemClick)
        }
    }
}

@Composable
fun TimelineRow(
    item: TimelineItemUiModel,
    onClick: (TimelineItemUiModel) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceS)
            .border(color = UiColors.Primary(), width = 1.dp)
            .padding(Dimens.SpaceS)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = true,
                onClick = { onClick(item) }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = item.time.toString(), style = MaterialTheme.typography.titleMedium)
            Text(text = item.title, style = MaterialTheme.typography.titleLarge)
            item.subtitle?.let { Text(text = it) }
        }
    }
}

@Composable
private fun SupplementLogChoiceSheetContent(
    title: String?,
    scheduledTime: LocalTime?,
    onLogScheduled: () -> Unit,
    onLogNowExtra: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title ?: "Log supplement dose",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Choose how you want to log:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Option: scheduled
        val scheduledLabel = scheduledTime?.toString() ?: "scheduled time"
        Text(
            text = "Log scheduled dose ($scheduledLabel)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogScheduled() }
                .padding(vertical = 12.dp)
        )

        // Option: now/extra
        Text(
            text = "Log now / extra dose",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogNowExtra() }
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
fun MealLogBottomSheetContent(
    title: String,
    initialNotes: String? = null,
    initialNutrition: TodayScreenContract.NutritionInput? = null,
    onConfirm: (notes: String?, nutrition: TodayScreenContract.NutritionInput?) -> Unit
) {
    var notesText by remember { mutableStateOf(initialNotes.orEmpty()) }

    // Keep as text while editing; convert to Double? on confirm.
    var calories by remember { mutableStateOf(initialNutrition?.calories?.toString().orEmpty()) }
    var protein by remember { mutableStateOf(initialNutrition?.proteinGrams?.toString().orEmpty()) }
    var carbs by remember { mutableStateOf(initialNutrition?.carbsGrams?.toString().orEmpty()) }
    var fat by remember { mutableStateOf(initialNutrition?.fatGrams?.toString().orEmpty()) }
    var sodium by remember { mutableStateOf(initialNutrition?.sodiumMg?.toString().orEmpty()) }
    var cholesterol by remember { mutableStateOf(initialNutrition?.cholesterolMg?.toString().orEmpty()) }
    var fiber by remember { mutableStateOf(initialNutrition?.fiberGrams?.toString().orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )

        NutrientNumberField(label = "Calories", value = calories, onValueChange = { calories = it })
        NutrientNumberField(label = "Protein (g)", value = protein, onValueChange = { protein = it })
        NutrientNumberField(label = "Carbs (g)", value = carbs, onValueChange = { carbs = it })
        NutrientNumberField(label = "Fat (g)", value = fat, onValueChange = { fat = it })
        NutrientNumberField(label = "Sodium (mg)", value = sodium, onValueChange = { sodium = it })
        NutrientNumberField(label = "Cholesterol (mg)", value = cholesterol, onValueChange = { cholesterol = it })
        NutrientNumberField(label = "Fiber (g)", value = fiber, onValueChange = { fiber = it })

        Spacer(Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val parsed = TodayScreenContract.NutritionInput(
                    calories = calories.toDoubleOrNull(),
                    proteinGrams = protein.toDoubleOrNull(),
                    carbsGrams = carbs.toDoubleOrNull(),
                    fatGrams = fat.toDoubleOrNull(),
                    sodiumMg = sodium.toDoubleOrNull(),
                    cholesterolMg = cholesterol.toDoubleOrNull(),
                    fiberGrams = fiber.toDoubleOrNull()
                )

                // If ALL nutrition fields are empty, treat nutrition as null.
                val nutritionOrNull =
                    if (parsed.isAllNull()) null else parsed

                val notesOrNull = notesText.trim().ifEmpty { null }

                onConfirm(notesOrNull, nutritionOrNull)
            }
        ) {
            Text("Confirm")
        }
    }
}

@Composable
private fun NutrientNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            // Allow digits + decimal point; keep it forgiving.
            onValueChange(new.filter { it.isDigit() || it == '.' })
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

private fun TodayScreenContract.NutritionInput.isAllNull(): Boolean =
    calories == null &&
            proteinGrams == null &&
            carbsGrams == null &&
            fatGrams == null &&
            sodiumMg == null &&
            cholesterolMg == null &&
            fiberGrams == null

