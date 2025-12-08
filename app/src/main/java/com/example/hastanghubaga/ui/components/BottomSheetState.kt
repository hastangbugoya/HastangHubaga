package com.example.hastanghubaga.ui.components

import androidx.compose.runtime.Composable

data class BottomSheetState(
    val isVisible: Boolean = false,
    val content: (@Composable () -> Unit)? = null
)


/**
 * Type-safe controller interface.
 */

interface BottomSheetController {
    fun show(content: @Composable () -> Unit)
    fun showSuccess(title: String, message: String)
    fun showError(title: String, message: String)
    fun showConfirm(
        title: String,
        message: String,
        onYes: () -> Unit,
        onNo: () -> Unit = {}
    )
    fun hide()
}