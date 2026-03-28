package com.example.hastanghubaga.ui.main

import com.example.hastanghubaga.feature.calendar.CalendarViewModel
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hastanghubaga.feature.calendar.CalendarScreen
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
import com.example.hastanghubaga.ui.ingredient.IngredientManagerScreen
import com.example.hastanghubaga.ui.screens.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val todayViewModel: TodayScreenViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val supplementsViewModel: SupplementsViewModel = hiltViewModel()

    val supplementsState by supplementsViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }

    var showIngredientManager by remember { mutableStateOf(false) }
    var showSupplements by remember { mutableStateOf(false) }

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
                        title,
                        message,
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
                            navController.navigate(NavItem.HOME.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                            todayViewModel.loadToday(date)
                        }
                    )
                }

                composable(NavItem.SETTINGS.route) {
                    when {
                        showIngredientManager -> {
                            IngredientManagerScreen()
                        }

                        showSupplements -> {
                            SupplementsScreen(
                                items = supplementsState.items,
                                onAddClick = {
                                    supplementsViewModel.onAddClick()
                                },
                                onItemClick = { supplementId ->
                                    supplementsViewModel.onEditClick(supplementId)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            SettingsScreen(
                                onOpenSupplements = {
                                    showSupplements = true
                                    showIngredientManager = false
                                },
                                onOpenNutrients = {
                                    bottomSheetController.show {
                                        Text("Nutrients editor coming later")
                                    }
                                },
                                onOpenActivities = {
                                    bottomSheetController.show {
                                        Text("Activities editor coming later")
                                    }
                                },
                                onOpenIngredients = {
                                    showIngredientManager = true
                                    showSupplements = false
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
                    onSaveClick = supplementsViewModel::onSaveClick,
                    onDeleteClick = supplementsViewModel::onDeleteClick,
                    onDismiss = supplementsViewModel::onDismissEditor
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