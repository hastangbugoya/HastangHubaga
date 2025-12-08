package com.example.hastanghubaga.ui.main

import SheetErrorContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.hastanghubaga.feature.today.TodaySupplementsScreen
import com.example.hastanghubaga.feature.today.TodaySupplementsViewModel
import com.example.hastanghubaga.ui.components.*
import com.example.hastanghubaga.ui.components.BottomSheetState
import com.example.hastanghubaga.ui.screens.SettingsScreen
import com.example.hastanghubaga.ui.screens.SupplementsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    todaySupplementsViewModel: TodaySupplementsViewModel
) {

    val navController = rememberNavController()
    rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var sheetData by remember { mutableStateOf(BottomSheetState()) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarConfig = topBarConfigs[currentRoute] ?: TopBarConfig()

    // ───────────────────────────────────────────────
    // GLOBAL BOTTOM SHEET CONTROLLER
    // ───────────────────────────────────────────────
    val bottomSheetController = remember {
        object : BottomSheetController {
            override fun show(content: @Composable () -> Unit) {
                sheetData = BottomSheetState(true, content)
            }
            override fun hide() {
                sheetData = BottomSheetState(false, null)
            }
            override fun showSuccess(title: String, message: String) {
                show {
                    SheetSuccessContent(title, message) {
                        hide()
                    }
                }
            }
            override fun showError(title: String, message: String) {
                show {
                    SheetErrorContent(title, message) {
                        hide()
                    }
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
                        onYes = {
                            hide()
                            onYes()
                        },
                        onNo = {
                            hide()
                            onNo()
                        }
                    )
                }
            }
        }
    }
    // COLLECT VIEWMODEL UI EVENTS (MINIMAL ADD)
    LaunchedEffect(Unit) {
        todaySupplementsViewModel.events.collect { event ->
            when (event) {

                is MainScreenIntent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is MainScreenIntent.ShowBanner -> {
                    bannerMessage = event.message
                    bannerVisible = true
                    launch {
                        kotlinx.coroutines.delay(3000)
                        bannerVisible = false
                    }
                }

                is MainScreenIntent.ShowBottomSheet -> {
                    bottomSheetController.show(event.content)
                }

                is MainScreenIntent.HideBottomSheet -> {
                    bottomSheetController.hide()
                }

                is MainScreenIntent.Navigate -> {
                    navController.navigate(event.route)
                }

                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar(navController) },
        topBar = {
           DynamicTopBar(
               topBarConfig,
               {navController.navigateUp()}
           )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxWidth()) {
            TopBanner(
                message = bannerMessage,
                isVisible = bannerVisible
            )
            NavHost(
                navController = navController,
                startDestination = NavItem.HOME.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavItem.HOME.route) {
                    TodaySupplementsScreen(
                        showBottomSheet = { bottomSheetController.show(it) },
                        snackbarData = snackbarHostState,
                        viewModel = todaySupplementsViewModel
                    )
                }
                composable(NavItem.SUPPLEMENTS.route) {
                    SupplementsScreen({})
                }
                composable(NavItem.SETTINGS.route) {
                    SettingsScreen({})
                }
            }
        }
        if (sheetData.isVisible && sheetData.content != null) {
            ModalBottomSheet(
                onDismissRequest = { bottomSheetController.hide() },
                sheetState = bottomSheetState
            ) {
                sheetData.content?.invoke()
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
