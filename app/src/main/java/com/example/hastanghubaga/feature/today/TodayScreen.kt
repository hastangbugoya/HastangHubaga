package com.example.hastanghubaga.feature.today

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hastanghubaga.domain.model.activity.isExercise
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.feature.today.ActiveLocalSheet.Dose
import com.example.hastanghubaga.feature.today.ActiveLocalSheet.Exercise
import com.example.hastanghubaga.feature.today.ActiveLocalSheet.ForceLogSupplementPicker
import com.example.hastanghubaga.feature.today.ActiveLocalSheet.Meal
import com.example.hastanghubaga.feature.today.ActiveLocalSheet.SupplementLogChoice
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ConfirmDose
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.DismissExerciseSheet
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ExerciseConfirmPressed
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ExerciseDateChanged
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ExerciseEndTimeChanged
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ExerciseIntensityChanged
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ExerciseNotesChanged
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ExerciseStartTimeChanged
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ForceLogSupplementSelected
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.ForceLogSupplementTapped
import com.example.hastanghubaga.feature.today.TodayScreenContract.Intent.SupplementLogOptionSelected
import com.example.hastanghubaga.ui.common.BannerController
import com.example.hastanghubaga.ui.common.BottomSheetController
import com.example.hastanghubaga.ui.common.ErrorView
import com.example.hastanghubaga.ui.common.LoadingView
import com.example.hastanghubaga.ui.common.SnackbarController
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.ImportedMealUiModel
import com.example.hastanghubaga.ui.timeline.MealUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.icon
import com.example.hastanghubaga.ui.tokens.Dimens
import com.example.hastanghubaga.ui.tokens.UiColors
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.math.roundToInt

sealed interface ActiveLocalSheet {
    data class Dose(
        val data: TodayScreenContract.Effect.ShowDoseInputDialog,
        val title: String?
    ) : ActiveLocalSheet

    data class SupplementLogChoice(
        val data: TodayScreenContract.Effect.ShowSupplementLogChoice,
        val title: String?
    ) : ActiveLocalSheet

    data class ForceLogSupplementPicker(
        val supplements: List<Supplement>
    ) : ActiveLocalSheet

    data class Exercise(
        val draft: TodayScreenContract.ExerciseDraft,
        val title: String
    ) : ActiveLocalSheet

    data class Meal(
        val input: TodayScreenContract.MealLogInput
    ) : ActiveLocalSheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    snackbarController: SnackbarController,
    bannerController: BannerController,
    bottomSheetController: BottomSheetController,
    onNavigate: (TodayScreenContract.Destination) -> Unit,
    initialDate: LocalDate = DomainTimePolicy.todayLocal(),
    viewModel: TodayScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var activeSheet by remember { mutableStateOf<ActiveLocalSheet?>(null) }
    val localSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.isLoading, state.uiTimelineItems.size, state.selectedDate) {
        if (!state.isLoading && state.uiTimelineItems.isNotEmpty()) {
            val count = state.uiTimelineItems.size
            bannerController.show(
                if (count == 1) {
                    "Loaded 1 timeline item for ${state.selectedDate}"
                } else {
                    "Loaded $count timeline items for ${state.selectedDate}"
                }
            )
        }
    }

    LaunchedEffect(initialDate) {
        viewModel.onIntent(TodayScreenContract.Intent.LoadDate(initialDate))
    }

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
                    activeSheet = Dose(
                        data = effect,
                        title = effect.title
                    )
                }

                is TodayScreenContract.Effect.ShowSupplementLogChoice -> {
                    activeSheet = SupplementLogChoice(
                        data = effect,
                        title = effect.title
                    )
                }

                is TodayScreenContract.Effect.ShowForceLogSupplementPicker -> {
                    activeSheet = ForceLogSupplementPicker(
                        supplements = effect.supplements
                    )
                }
            }
        }
    }

    TodayScreenContent(
        state = state,
        onItemClick = { item ->
            viewModel.onIntent(TodayScreenContract.Intent.TimelineItemClicked(item))

            when (item) {
                is MealUiModel -> {
                    activeSheet = Meal(
                        TodayScreenContract.MealLogInput(
                            mealType = item.mealType,
                            timeUseIntent = TimeUseIntent.Scheduled(item.time),
                            notes = null,
                            nutrition = null
                        )
                    )
                }

                is ImportedMealUiModel -> Unit

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
        },
        onForceLogSupplement = {
            viewModel.onIntent(ForceLogSupplementTapped)
        }
    )

    LaunchedEffect(state.exerciseDraft) {
        val draft = state.exerciseDraft
        if (draft != null) {
            activeSheet = Exercise(
                draft = draft,
                title = draft.activityType.name.replace('_', ' ')
            )
        } else {
            if (activeSheet is Exercise) activeSheet = null
        }
    }

    activeSheet?.let { sheet ->
        ModalBottomSheet(
            sheetState = localSheetState,
            onDismissRequest = {
                when (sheet) {
                    is Dose -> activeSheet = null
                    is Exercise -> {
                        viewModel.onIntent(DismissExerciseSheet)
                        activeSheet = null
                    }
                    is SupplementLogChoice -> activeSheet = null
                    is ForceLogSupplementPicker -> activeSheet = null
                    is Meal -> activeSheet = null
                }
            }
        ) {
            when (sheet) {
                is Dose -> {
                    var actualDate by remember(sheet.data) {
                        mutableStateOf(sheet.data.initialActualDate)
                    }
                    var actualTime by remember(sheet.data) {
                        mutableStateOf(
                            sheet.data.initialActualTime
                                ?: sheet.data.scheduledTime
                                ?: DomainTimePolicy.nowLocalDateTime(Clock.System).time
                        )
                    }

                    sheet.title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    DoseDateTimeSection(
                        actualDate = actualDate,
                        actualTime = actualTime,
                        onDateChange = { actualDate = it },
                        onTimeChange = { actualTime = it }
                    )

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
                                    actualDate = actualDate,
                                    actualTime = actualTime,
                                    occurrenceId = sheet.data.occurrenceId
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
                        onDateChange = { viewModel.onIntent(ExerciseDateChanged(it)) },
                        onStartTimeChange = { viewModel.onIntent(ExerciseStartTimeChanged(it)) },
                        onEndTimeChange = { viewModel.onIntent(ExerciseEndTimeChanged(it)) },
                        onNotesChange = { viewModel.onIntent(ExerciseNotesChanged(it)) },
                        onIntensityChange = { viewModel.onIntent(ExerciseIntensityChanged(it)) },
                        onPrimaryAction = {
                            viewModel.onIntent(ExerciseConfirmPressed)
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
                                    occurrenceId = sheet.data.occurrenceId,
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
                                    occurrenceId = sheet.data.occurrenceId,
                                    option = TodayScreenContract.SupplementLogOption.NowExtra
                                )
                            )
                            activeSheet = null
                        }
                    )
                }

                is ForceLogSupplementPicker -> {
                    ForceLogSupplementPickerSheetContent(
                        supplements = sheet.supplements,
                        onSupplementSelected = { supplement ->
                            viewModel.onIntent(
                                ForceLogSupplementSelected(
                                    supplementId = supplement.id,
                                    title = supplement.name,
                                    defaultUnit = supplement.recommendedDoseUnit,
                                    suggestedDose = supplement.recommendedServingSize
                                )
                            )
                            activeSheet = null
                        }
                    )
                }

                is Meal -> {
                    MealLogBottomSheetContent(
                        title = sheet.input.mealType.name,
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

@Composable
private fun DoseDateTimeSection(
    actualDate: LocalDate,
    actualTime: LocalTime,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Actual intake",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateChange(
                                LocalDate(
                                    year = year,
                                    monthNumber = month + 1,
                                    dayOfMonth = dayOfMonth
                                )
                            )
                        },
                        actualDate.year,
                        actualDate.monthNumber - 1,
                        actualDate.dayOfMonth
                    ).show()
                }
            ) {
                Text("Date: $actualDate")
            }

            TextButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onTimeChange(
                                LocalTime(
                                    hour = hourOfDay,
                                    minute = minute
                                )
                            )
                        },
                        actualTime.hour,
                        actualTime.minute,
                        false
                    ).show()
                }
            ) {
                Text("Time: ${actualTime.toDisplayText()}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ExerciseBottomSheetContent(
    title: String,
    draft: TodayScreenContract.ExerciseDraft,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onNotesChange: (String) -> Unit,
    onIntensityChange: (Int?) -> Unit,
    onPrimaryAction: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Actual activity",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateChange(
                                LocalDate(
                                    year = year,
                                    monthNumber = month + 1,
                                    dayOfMonth = dayOfMonth
                                )
                            )
                        },
                        draft.logDate.year,
                        draft.logDate.monthNumber - 1,
                        draft.logDate.dayOfMonth
                    ).show()
                }
            ) {
                Text("Date: ${draft.logDate}")
            }

            TextButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onStartTimeChange(
                                LocalTime(
                                    hour = hourOfDay,
                                    minute = minute
                                )
                            )
                        },
                        draft.startTime.hour,
                        draft.startTime.minute,
                        false
                    ).show()
                }
            ) {
                Text("Start: ${draft.startTime.toDisplayText()}")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onEndTimeChange(
                                LocalTime(
                                    hour = hourOfDay,
                                    minute = minute
                                )
                            )
                        },
                        draft.endTime.hour,
                        draft.endTime.minute,
                        false
                    ).show()
                }
            ) {
                Text("End: ${draft.endTime.toDisplayText()}")
            }
        }

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

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

@Composable
fun TodayScreenContent(
    state: TodayScreenContract.State,
    onItemClick: (TimelineItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onForceLogSupplement: () -> Unit
) {
    when {
        state.isLoading -> LoadingView()
        state.errorMessage != null -> ErrorView(state.errorMessage)
        else -> Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = state.selectedDate.toString(),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Button(
                onClick = onForceLogSupplement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Force log supplement")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TimelineList(
                modifier = Modifier.weight(1f),
                items = state.uiTimelineItems,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun TimelineList(
    modifier: Modifier = Modifier,
    items: List<TimelineItemUiModel>,
    onItemClick: (TimelineItemUiModel) -> Unit
) {
    Log.d("TimelineList", "Rendering ${items.size} items")
    items.forEach {
        Log.d("TimelineList", "Rendering item: ${it.title} key:${it.key}")
    }
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(
            items = items,
            key = { index, item -> "${item.key}-$index" }
        ) { _, item ->
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
            .background(color = if(item.isCompleted) Color.Green else Color.Red)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.time.toDisplayText(),
                    style = MaterialTheme.typography.labelMedium
                )

                androidx.compose.material3.Icon(
                    painter = androidx.compose.ui.res.painterResource(id = item.icon()),
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            item.subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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

        val scheduledLabel = scheduledTime?.toDisplayText() ?: "scheduled time"
        Text(
            text = "Log scheduled dose ($scheduledLabel)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogScheduled() }
                .padding(vertical = 12.dp)
        )

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
private fun ForceLogSupplementPickerSheetContent(
    supplements: List<Supplement>,
    onSupplementSelected: (Supplement) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Force log supplement",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Choose an active supplement to log.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = supplements,
                key = { it.id }
            ) { supplement ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSupplementSelected(supplement) }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = supplement.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    val subtitle =
                        "${supplement.recommendedServingSize} ${supplement.recommendedDoseUnit.name}"

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
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

private fun LocalTime.toDisplayText(): String {
    val hourText = hour.toString().padStart(2, '0')
    val minuteText = minute.toString().padStart(2, '0')
    return "$hourText:$minuteText"
}