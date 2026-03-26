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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    // Navigation
    val navController = rememberNavController()

    // ✅ FIX 1: hoist viewmodels at MainScreen scope (activity-level)
    val todayViewModel: TodayScreenViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Banner state
    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }

    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }

    // Controllers (UI side-effects)
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
                        // NOTE: your existing impl closes sheet only
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

    // IMPORTANT: Overlays must be conditionally composed to avoid input blocking
    LaunchedEffect(bannerVisible, bannerMessage) {
        if (bannerVisible && !bannerMessage.isNullOrBlank()) {
            delay(3_000)
            bannerVisible = false
        }
    }

    // ROOT UI LAYER
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                // ✅ FIX 2: ensure bottom nav uses save/restore so screens keep state
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
                        // ✅ pass hoisted VM
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
                        // ✅ pass hoisted VM
                        viewModel = calendarViewModel,

                        modifier = Modifier.fillMaxSize(),
                        snackbarHostState = snackbarHostState,

                        // ✅ wire Calendar -> Today
                        onNavigateToDate = { date: LocalDate ->
                            // go to HOME tab without nuking back stack state
                            navController.navigate(NavItem.HOME.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }

                            // tell Today to display that date:
                            // replace with your real MVI event if needed
                            todayViewModel.loadToday(date)
                        }
                    )
                }

                composable(NavItem.SETTINGS.route) {
                    IngredientManagerScreen()
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




//package com.example.hastanghubaga.ui.main
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.hastanghubaga.feature.calendar.CalendarScreen
//import com.example.hastanghubaga.feature.calendar.ui.MonthCalendarPanel
//import com.example.hastanghubaga.feature.today.TodayScreen
//import com.example.hastanghubaga.feature.today.TodayScreenContract
//import com.example.hastanghubaga.ui.common.BannerController
//import com.example.hastanghubaga.ui.common.BottomSheetController
//import com.example.hastanghubaga.ui.common.SnackbarController
//import com.example.hastanghubaga.ui.common.sheets.SheetConfirmContent
//import com.example.hastanghubaga.ui.common.sheets.SheetSuccessContent
//import com.example.hastanghubaga.ui.common.sheets.TimelineItemInfoSheet
//import com.example.hastanghubaga.ui.components.BottomNavigationBar
//import com.example.hastanghubaga.ui.components.TopBanner
//import com.example.hastanghubaga.ui.screens.SettingsScreen
//import kotlinx.coroutines.delay
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainScreen() {
//    // Navigation
//    val navController = rememberNavController()
//    // Snackbar
//    val snackbarHostState = remember { SnackbarHostState() }
//    // Banner state
//    var bannerMessage by remember { mutableStateOf<String?>(null) }
//    var bannerVisible by remember { mutableStateOf(false) }
//    // Bottom sheet state
//    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var sheetContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
//    var sheetVisible by remember { mutableStateOf(false) }
//    // Controllers (UI side-effects)
//    val snackbarController: SnackbarController = remember {
//        object : SnackbarController {
//            override suspend fun show(message: String) {
//                snackbarHostState.showSnackbar(message)
//            }
//        }
//    }
//    val bannerController: BannerController = remember {
//        object : BannerController {
//            override fun show(message: String) {
//                bannerMessage = message
//                bannerVisible = true
//            }
//
//            override fun hide() {
//                bannerVisible = false
//            }
//        }
//    }
//    val bottomSheetController: BottomSheetController = remember {
//        object : BottomSheetController {
//            override fun show(content: @Composable () -> Unit) {
////                Log.d("Meow", "SheetDebug> show() called")
//                sheetContent = content
//                sheetVisible = true
//            }
//
//            override fun showSuccess(title: String, message: String) {
//                show {
//                    SheetSuccessContent(title, message, onClose = { hide() })
//                }
//
//            }
//
//            override fun showError(title: String, message: String) {
//                show {
//                    com.example.hastanghubaga.ui.common.sheets.SheetErrorContent(
//                        title,
//                        message,
//                        onClose = { hide() })
//                }
//            }
//
//            override fun showConfirm(
//                title: String,
//                message: String,
//                onYes: () -> Unit,
//                onNo: () -> Unit
//            ) {
//                show {
//                    SheetConfirmContent(
//                        title,
//                        message,
//                        onYes = { hide() },
//                        onNo = { hide() })
//                }
//            }
//
//            override fun showTimelineInfoSheet(
//                title: String,
//                subtitle: String,
//                time: String,
//                key: String,
//                onClose: () -> Unit
//            ) {
//                show {
//                    TimelineItemInfoSheet(
//                        title = title,
//                        subtitle = subtitle,
//                        time = time,
//                        key = key,
//                        onClose = {
//                            hide()
//                        }
//                    )
//                }
//            }
//
//            override fun hide() {
////                Log.d("Meow", "SheetDebug> hide() called")
//                sheetVisible = false
//                sheetContent = null
//            }
//        }
//    }
//    // IMPORTANT: Overlays must be conditionally composed to avoid input blocking
//    LaunchedEffect(bannerVisible, bannerMessage) {
//        if (bannerVisible && !bannerMessage.isNullOrBlank()) {
//            delay(3_000)
//            bannerVisible = false
//        }
//    }
//    // ROOT UI LAYER
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            snackbarHost = { SnackbarHost(snackbarHostState) },
//            bottomBar = {
//                BottomNavigationBar(navController)
//            }
//        ) { innerPadding ->
//
//            NavHost(
//                navController = navController,
//                startDestination = NavItem.HOME.route,
//                modifier = Modifier.padding(innerPadding)
//            ) {
//                composable(NavItem.HOME.route) {
//                    TodayScreen(
//                        snackbarController = snackbarController,
//                        bannerController = bannerController,
//                        bottomSheetController = bottomSheetController,
//                        onNavigate = { destination ->
//                            navController.navigate(destination.toRoute())
//                        }
//                    )
//                }
//                composable(NavItem.MANAGE.route) {
//                    CalendarScreen(
//                        modifier = Modifier.fillMaxSize(),
//                        snackbarHostState = snackbarHostState,
//                        onNavigateToDate = {
//
//                        }
//                    )
//                }
//                composable(NavItem.SETTINGS.route) {
//                    SettingsScreen {}
//                }
//            }
//        }
//
//        if (sheetVisible && sheetContent != null) {
//            ModalBottomSheet(
//                sheetState = bottomSheetState,          // ✅ REQUIRED
//                onDismissRequest = { bottomSheetController.hide() }
//            ) {
//                sheetContent?.invoke()
//            }
//        }
//        if (bannerVisible && !bannerMessage.isNullOrBlank()) {
//            TopBanner(
//                message = bannerMessage,
//                isVisible = true
//            )
//        }
//
//    }
//}
//
//private fun TodayScreenContract.Destination.toRoute(): String =
//    when (this) {
//        is TodayScreenContract.Destination.Supplement ->
//            com.example.hastanghubaga.navigation.NavItem.SUPPLEMENT_DETAIL.route(id)
//
//        is TodayScreenContract.Destination.Meal ->
//            com.example.hastanghubaga.navigation.NavItem.MEAL_DETAIL.route(id)
//
//        is TodayScreenContract.Destination.Activity ->
//            com.example.hastanghubaga.navigation.NavItem.ACTIVITY_DETAIL.route(id)
//    }