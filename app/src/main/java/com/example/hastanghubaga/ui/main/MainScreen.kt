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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hastanghubaga.feature.today.TodayScreen
import com.example.hastanghubaga.feature.today.TodayScreenContract
import com.example.hastanghubaga.ui.common.BannerController
import com.example.hastanghubaga.ui.common.BottomSheetController
import com.example.hastanghubaga.ui.common.SnackbarController
import com.example.hastanghubaga.ui.components.BottomNavigationBar
import com.example.hastanghubaga.ui.components.BottomSheetState
import com.example.hastanghubaga.ui.components.SheetConfirmContent
import com.example.hastanghubaga.ui.components.SheetSuccessContent
import com.example.hastanghubaga.ui.components.TopBanner
import com.example.hastanghubaga.ui.screens.SettingsScreen
import com.example.hastanghubaga.ui.screens.SupplementsScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    var sheetData by remember { mutableStateOf(BottomSheetState()) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarConfig = topBarConfigs[currentRoute] ?: TopBarConfig()

    val bannerController = remember {
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


    val snackbarController = remember {
        object : SnackbarController {
            override suspend fun show(message: String) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

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
                    SheetSuccessContent(title, message) { hide() }
                }
            }

            override fun showError(title: String, message: String) {
                show {
                    SheetErrorContent(title, message) { hide() }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar(navController) },
        topBar = {
            DynamicTopBar(
                config = topBarConfig,
                onBack = { navController.navigateUp() }
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
                        snackbarController = snackbarController,
                        bannerController = bannerController,
                        bottomSheetController = bottomSheetController,
                        onNavigate = { destination ->
                            navController.navigate(destination.toRoute())
                        }
                    )
                }

                composable(NavItem.MANAGE.route) {
                    SupplementsScreen {}
                }

                composable(NavItem.SETTINGS.route) {
                    SettingsScreen {}
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

private fun TodayScreenContract.Destination.toRoute(): String =
    when (this) {
        is TodayScreenContract.Destination.Supplement ->
            com.example.hastanghubaga.navigation.NavItem.SUPPLEMENT_DETAIL.route(id)

        is TodayScreenContract.Destination.Meal ->
            com.example.hastanghubaga.navigation.NavItem.MEAL_DETAIL.route(id)

        is TodayScreenContract.Destination.Activity ->
            com.example.hastanghubaga.navigation.NavItem.ACTIVITY_DETAIL.route(id)
    }