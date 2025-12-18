package com.example.hastanghubaga.ui.common

import androidx.compose.runtime.Composable

@Composable
fun ScreenState(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        isLoading ->
            LoadingView()

        errorMessage != null ->
            ErrorView(
                message = errorMessage
            )

        else ->
            content()
    }
}
