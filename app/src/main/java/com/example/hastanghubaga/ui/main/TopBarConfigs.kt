package com.example.hastanghubaga.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

val topBarConfigs = mapOf(
    NavItem.HOME.route to TopBarConfig(
        title = "Today",
        actions = {
            IconButton(onClick = { /* refresh */ }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ),

    NavItem.MANAGE.route to TopBarConfig(
        title = "Supplements",
        actions = {
            IconButton(onClick = { /* add supplement */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ),

    NavItem.SETTINGS.route to TopBarConfig(
        title = "Settings"
    )
)
