package com.example.hastanghubaga.ui.main

import SheetErrorContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hastanghubaga.feature.today.TodayScreen
import com.example.hastanghubaga.feature.today.TodayScreenViewModel
import com.example.hastanghubaga.ui.components.BottomNavigationBar
import com.example.hastanghubaga.ui.components.BottomSheetController
import com.example.hastanghubaga.ui.components.BottomSheetState
import com.example.hastanghubaga.ui.components.SheetConfirmContent
import com.example.hastanghubaga.ui.components.SheetSuccessContent
import com.example.hastanghubaga.ui.components.TopBanner
import com.example.hastanghubaga.ui.screens.SettingsScreen
import com.example.hastanghubaga.ui.screens.SupplementsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    todayScreenViewModel: TodayScreenViewModel
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
        todayScreenViewModel.events.collect { event ->
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
                    TodayScreen(
                        showBottomSheet = { bottomSheetController.show(it) },
                        snackbarData = snackbarHostState,
                        viewModel = todayScreenViewModel
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
