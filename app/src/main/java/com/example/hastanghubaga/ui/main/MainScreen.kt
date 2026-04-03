package com.example.hastanghubaga.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hastanghubaga.feature.activities.ui.ActivitiesScreen
import com.example.hastanghubaga.feature.activities.ui.ActivitiesViewModel
import com.example.hastanghubaga.feature.activities.ui.ActivityEditorSheet
import com.example.hastanghubaga.feature.calendar.CalendarScreen
import com.example.hastanghubaga.feature.calendar.CalendarViewModel
import com.example.hastanghubaga.feature.ingredients.ui.IngredientEditorSheet
import com.example.hastanghubaga.feature.ingredients.ui.IngredientsScreen
import com.example.hastanghubaga.feature.ingredients.ui.IngredientsViewModel
import com.example.hastanghubaga.feature.meals.ui.MealEditorSheet
import com.example.hastanghubaga.feature.meals.ui.MealsScreen
import com.example.hastanghubaga.feature.meals.ui.MealsViewModel
import com.example.hastanghubaga.feature.settings.eventtimes.DefaultEventTimesScreen
import com.example.hastanghubaga.feature.settings.eventtimes.DefaultEventTimesViewModel
import com.example.hastanghubaga.feature.supplements.ui.SupplementEditorSheet
import com.example.hastanghubaga.feature.supplements.ui.SupplementsScreen
import com.example.hastanghubaga.feature.supplements.ui.SupplementsViewModel
import com.example.hastanghubaga.feature.today.TodayScreen
import com.example.hastanghubaga.feature.today.TodayScreenContract
import com.example.hastanghubaga.feature.today.TodayScreenViewModel
import com.example.hastanghubaga.ui.common.BannerController
import com.example.hastanghubaga.ui.common.BottomSheetController
import com.example.hastanghubaga.ui.common.SnackbarController
import com.example.hastanghubaga.ui.common.sheets.SheetConfirmContent
import com.example.hastanghubaga.ui.common.sheets.SheetSuccessContent
import com.example.hastanghubaga.ui.common.sheets.TimelineItemInfoSheet
import com.example.hastanghubaga.ui.components.BottomNavigationBar
import com.example.hastanghubaga.ui.components.TopBanner
import com.example.hastanghubaga.ui.screens.SettingsScreen
import com.example.hastanghubaga.ui.screens.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

private enum class SettingsSubscreen {
    ROOT,
    SUPPLEMENTS,
    ACTIVITIES,
    INGREDIENTS,
    MEALS,
    EVENT_TIMES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val todayViewModel: TodayScreenViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val supplementsViewModel: SupplementsViewModel = hiltViewModel()
    val activitiesViewModel: ActivitiesViewModel = hiltViewModel()
    val ingredientsViewModel: IngredientsViewModel = hiltViewModel()
    val mealsViewModel: MealsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val defaultEventTimesViewModel: DefaultEventTimesViewModel = hiltViewModel()

    val supplementsState by supplementsViewModel.state.collectAsState()
    val activitiesState by activitiesViewModel.state.collectAsState()
    val ingredientsState by ingredientsViewModel.uiState.collectAsState()
    val mealsState by mealsViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }

    var settingsSubscreen by rememberSaveable { mutableStateOf(SettingsSubscreen.ROOT) }

    var selectedHomeDateIso by rememberSaveable {
        mutableStateOf(
            LocalDate.parse(
                kotlinx.datetime.Clock.System.now()
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                    .date
                    .toString()
            ).toString()
        )
    }

    val snackbarController: SnackbarController = remember {
        object : SnackbarController {
            override suspend fun show(message: String) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val bannerController: BannerController = remember {
        object : BannerController {
            override fun show(message: String) {
                bannerMessage = message
                bannerVisible = true
            }

            override fun hide() {
                bannerVisible = false
            }
        }
    }

    val bottomSheetController: BottomSheetController = remember {
        object : BottomSheetController {
            override fun show(content: @Composable () -> Unit) {
                sheetContent = content
                sheetVisible = true
            }

            override fun showSuccess(title: String, message: String) {
                show { SheetSuccessContent(title, message, onClose = { hide() }) }
            }

            override fun showError(title: String, message: String) {
                show {
                    com.example.hastanghubaga.ui.common.sheets.SheetErrorContent(
                        title = title,
                        message = message,
                        onClose = { hide() }
                    )
                }
            }

            override fun showConfirm(
                title: String,
                message: String,
                onYes: () -> Unit,
                onNo: () -> Unit
            ) {
                show {
                    SheetConfirmContent(
                        title = title,
                        message = message,
                        onYes = { hide() },
                        onNo = { hide() }
                    )
                }
            }

            override fun showTimelineInfoSheet(
                title: String,
                subtitle: String,
                time: String,
                key: String,
                onClose: () -> Unit
            ) {
                show {
                    TimelineItemInfoSheet(
                        title = title,
                        subtitle = subtitle,
                        time = time,
                        key = key,
                        onClose = { hide() }
                    )
                }
            }

            override fun hide() {
                sheetVisible = false
                sheetContent = null
            }
        }
    }

    LaunchedEffect(bannerVisible, bannerMessage) {
        if (bannerVisible && !bannerMessage.isNullOrBlank()) {
            delay(3_000)
            bannerVisible = false
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    BackHandler(
        enabled = settingsSubscreen != SettingsSubscreen.ROOT &&
                navController.currentDestination?.route == NavItem.SETTINGS.route
    ) {
        settingsSubscreen = SettingsSubscreen.ROOT
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavItem.HOME.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavItem.HOME.route) {
                    TodayScreen(
                        initialDate = LocalDate.parse(selectedHomeDateIso),
                        viewModel = todayViewModel,
                        snackbarController = snackbarController,
                        bannerController = bannerController,
                        bottomSheetController = bottomSheetController,
                        onNavigate = { destination ->
                            navController.navigate(destination.toRoute())
                        }
                    )
                }

                composable(NavItem.MANAGE.route) {
                    CalendarScreen(
                        viewModel = calendarViewModel,
                        modifier = Modifier.fillMaxSize(),
                        snackbarHostState = snackbarHostState,
                        onNavigateToDate = { date: LocalDate ->
                            selectedHomeDateIso = date.toString()
                            navController.navigate(NavItem.HOME.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        }
                    )
                }

                composable(NavItem.SETTINGS.route) {
                    when (settingsSubscreen) {
                        SettingsSubscreen.INGREDIENTS -> {
                            IngredientsScreen(
                                items = ingredientsState.ingredients,
                                onAddClick = { ingredientsViewModel.onAddClick() },
                                onItemClick = { id ->
                                    ingredientsViewModel.onIngredientClick(id)
                                },
                                onBackClick = {
                                    settingsSubscreen = SettingsSubscreen.ROOT
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SettingsSubscreen.SUPPLEMENTS -> {
                            SupplementsScreen(
                                items = supplementsState.items,
                                onAddClick = { supplementsViewModel.onAddClick() },
                                onItemClick = { supplementId ->
                                    supplementsViewModel.onEditClick(supplementId)
                                },
                                onBackClick = {
                                    settingsSubscreen = SettingsSubscreen.ROOT
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SettingsSubscreen.ACTIVITIES -> {
                            ActivitiesScreen(
                                items = activitiesState.items,
                                onAddClick = { activitiesViewModel.onAddClick() },
                                onItemClick = { activityId ->
                                    activitiesViewModel.onEditClick(activityId)
                                },
                                onBackClick = {
                                    settingsSubscreen = SettingsSubscreen.ROOT
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SettingsSubscreen.MEALS -> {
                            MealsScreen(
                                items = mealsState.items,
                                onAddClick = { mealsViewModel.onAddClick() },
                                onItemClick = { mealId ->
                                    mealsViewModel.onEditClick(mealId)
                                },
                                onBackClick = {
                                    settingsSubscreen = SettingsSubscreen.ROOT
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SettingsSubscreen.EVENT_TIMES -> {
                            DefaultEventTimesScreen(
                                viewModel = defaultEventTimesViewModel,
                                onBackClick = {
                                    settingsSubscreen = SettingsSubscreen.ROOT
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SettingsSubscreen.ROOT -> {
                            SettingsScreen(
                                onImportFromAdobongKangkong = {
                                    settingsViewModel.onImportFromAdobongKangkongClick()
                                },
                                onOpenSupplements = {
                                    settingsSubscreen = SettingsSubscreen.SUPPLEMENTS
                                },
                                onOpenNutrients = {
                                    bottomSheetController.show {
                                        Text("Nutrients editor coming later")
                                    }
                                },
                                onOpenActivities = {
                                    settingsSubscreen = SettingsSubscreen.ACTIVITIES
                                },
                                onOpenIngredients = {
                                    settingsSubscreen = SettingsSubscreen.INGREDIENTS
                                },
                                onOpenMeals = {
                                    settingsSubscreen = SettingsSubscreen.MEALS
                                },
                                onOpenEventTimes = {
                                    settingsSubscreen = SettingsSubscreen.EVENT_TIMES
                                }
                            )
                        }
                    }
                }
            }
        }

        if (sheetVisible && sheetContent != null) {
            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = { bottomSheetController.hide() }
            ) {
                sheetContent?.invoke()
            }
        }

        val supplementEditor = supplementsState.editor
        if (supplementEditor != null) {
            ModalBottomSheet(
                onDismissRequest = { supplementsViewModel.onDismissEditor() }
            ) {
                SupplementEditorSheet(
                    state = supplementEditor,
                    onNameChanged = supplementsViewModel::onNameChanged,
                    onBrandChanged = supplementsViewModel::onBrandChanged,
                    onNotesChanged = supplementsViewModel::onNotesChanged,
                    onIsActiveChanged = supplementsViewModel::onIsActiveChanged,
                    onAddScheduleClick = supplementsViewModel::onAddScheduleClick,
                    onRemoveScheduleClick = supplementsViewModel::onRemoveScheduleClick,
                    onScheduleAction = supplementsViewModel::onScheduleAction,
                    onSaveClick = supplementsViewModel::onSaveClick,
                    onDeleteClick = supplementsViewModel::onDeleteClick,
                    onDismiss = supplementsViewModel::onDismissEditor
                )
            }
        }

        val activityEditor = activitiesState.editor
        if (activityEditor != null) {
            ModalBottomSheet(
                onDismissRequest = { activitiesViewModel.onDismissEditor() }
            ) {
                ActivityEditorSheet(
                    state = activityEditor,
                    onTypeChanged = activitiesViewModel::onTypeChanged,
                    onNotesChanged = activitiesViewModel::onNotesChanged,
                    onIntensityChanged = activitiesViewModel::onIntensityChanged,
                    onIsWorkoutChanged = activitiesViewModel::onIsWorkoutChanged,
                    onIsActiveChanged = activitiesViewModel::onIsActiveChanged,
                    onAddScheduleClick = activitiesViewModel::onAddScheduleClick,
                    onRemoveScheduleClick = activitiesViewModel::onRemoveScheduleClick,
                    onScheduleAction = activitiesViewModel::onScheduleAction,
                    onSaveClick = activitiesViewModel::onSaveClick,
                    onDeleteClick = activitiesViewModel::onDeleteClick,
                    onDismiss = activitiesViewModel::onDismissEditor
                )
            }
        }

        val mealEditor = mealsState.editor
        if (mealEditor != null) {
            ModalBottomSheet(
                onDismissRequest = { mealsViewModel.onDismissEditor() }
            ) {
                MealEditorSheet(
                    state = mealEditor,
                    onNameChanged = mealsViewModel::onNameChanged,
                    onTypeChanged = mealsViewModel::onTypeChanged,
                    onTreatAsAnchorChanged = mealsViewModel::onTreatAsAnchorChanged,
                    onNotesChanged = mealsViewModel::onNotesChanged,
                    onTimestampChanged = mealsViewModel::onTimestampChanged,
                    onSaveClick = mealsViewModel::onSaveClick,
                    onDeleteClick = mealsViewModel::onDeleteClick,
                    onDismiss = mealsViewModel::onDismissEditor
                )
            }
        }

        val ingredientEditor = ingredientsState.editor
        if (ingredientEditor.isVisible) {
            ModalBottomSheet(
                onDismissRequest = { ingredientsViewModel.onDismissEditor() }
            ) {
                IngredientEditorSheet(
                    state = ingredientEditor,
                    onCodeChanged = ingredientsViewModel::onCodeChanged,
                    onNameChanged = ingredientsViewModel::onNameChanged,
                    onDefaultUnitChanged = ingredientsViewModel::onDefaultUnitChanged,
                    onRdaValueChanged = ingredientsViewModel::onRdaValueChanged,
                    onRdaUnitChanged = ingredientsViewModel::onRdaUnitChanged,
                    onUpperLimitValueChanged = ingredientsViewModel::onUpperLimitValueChanged,
                    onUpperLimitUnitChanged = ingredientsViewModel::onUpperLimitUnitChanged,
                    onCategoryChanged = ingredientsViewModel::onCategoryChanged,
                    onSaveClick = ingredientsViewModel::onSaveClick,
                    onDeleteClick = ingredientsViewModel::onDeleteClick,
                    onDismiss = ingredientsViewModel::onDismissEditor
                )
            }
        }

        if (bannerVisible && !bannerMessage.isNullOrBlank()) {
            TopBanner(
                message = bannerMessage,
                isVisible = true
            )
        }
    }
}

private fun TodayScreenContract.Destination.toRoute(): String =
    when (this) {
        is TodayScreenContract.Destination.Supplement ->
            com.example.hastanghubaga.navigation.NavItem.SUPPLEMENT_DETAIL.route(id)

        is TodayScreenContract.Destination.Meal ->
            com.example.hastanghubaga.navigation.NavItem.MEAL_DETAIL.route(id)

        is TodayScreenContract.Destination.Activity ->
            com.example.hastanghubaga.navigation.NavItem.ACTIVITY_DETAIL.route(id)
    }