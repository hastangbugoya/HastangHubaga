package com.example.hastanghubaga.ui.components

import androidx.compose.runtime.Composable

data class BottomSheetState(
    val isVisible: Boolean = false,
    val content: (@Composable () -> Unit)? = null
)


