package com.example.hastanghubaga.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home),
    SUPPLEMENTS("supplements", "Supplements", Icons.Filled.CheckCircle),
    SETTINGS("settings", "Settings", Icons.Filled.Settings)
}