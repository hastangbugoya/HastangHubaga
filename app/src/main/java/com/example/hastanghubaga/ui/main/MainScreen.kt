package com.example.hastanghubaga.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hastanghubaga.ui.components.BottomNavigationBar
import com.example.hastanghubaga.ui.screens.HomeScreen
import com.example.hastanghubaga.ui.screens.SettingsScreen
import com.example.hastanghubaga.ui.screens.SupplementsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.HOME.route) { HomeScreen() }
            composable(NavItem.SUPPLEMENTS.route) { SupplementsScreen() }
            composable(NavItem.SETTINGS.route) { SettingsScreen() }
        }
    }
}
