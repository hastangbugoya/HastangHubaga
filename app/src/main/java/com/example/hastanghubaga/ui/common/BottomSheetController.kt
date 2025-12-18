package com.example.hastanghubaga.ui.common

import androidx.compose.runtime.Composable

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
    fun showTimelineInfoSheet(
        title: String,
        subtitle: String,
        time: String,
        key: String,
        onClose: () -> Unit
    )
    fun hide()
}