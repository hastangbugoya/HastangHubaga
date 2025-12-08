package com.example.hastanghubaga.ui.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable

sealed class MainScreenIntent {

    // ────────────────────────────
    // SNACKBAR
    // ────────────────────────────
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : MainScreenIntent()

    // ────────────────────────────
    // BOTTOM SHEET
    // ────────────────────────────
    data class ShowBottomSheet(
        val content: @Composable () -> Unit
    ) : MainScreenIntent()

    data class ShowSuccessSheet(
        val title: String,
        val message: String
    ) : MainScreenIntent()

    data class ShowErrorSheet(
        val title: String,
        val message: String
    ) : MainScreenIntent()

    data class ShowConfirmSheet(
        val title: String,
        val message: String,
        val onYes: () -> Unit,
        val onNo: () -> Unit
    ) : MainScreenIntent()

    object HideBottomSheet : MainScreenIntent()

    // ────────────────────────────
    // NAVIGATION
    // ────────────────────────────
    data class Navigate(val route: String) : MainScreenIntent()
    object NavigateBack : MainScreenIntent()

    // ────────────────────────────
    // TOAST BANNER
    // ────────────────────────────
    data class ShowBanner(
        val message: String,
        val isError: Boolean = false
    ) : MainScreenIntent()

    // ────────────────────────────
    // LOADING
    // ────────────────────────────
    data class SetLoading(val visible: Boolean) : MainScreenIntent()
}