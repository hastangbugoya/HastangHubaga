package com.example.hastanghubaga.ui.main

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class TopBarConfig(
    val title: String = "",
    val showBackButton: Boolean = false,
    val actions: @Composable RowScope.() -> Unit = {}
)

