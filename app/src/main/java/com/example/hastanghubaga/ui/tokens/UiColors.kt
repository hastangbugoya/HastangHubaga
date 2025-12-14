package com.example.hastanghubaga.ui.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color accessors.
 *
 * Do NOT hardcode colors in UI.
 */
object UiColors {

    val Success: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.primary }

    val Warning: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.tertiary }

    val Error: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.error }

    val MutedText: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.onSurfaceVariant }

    val IconDefault: @Composable () -> Color
        get() = { MaterialTheme.colorScheme.onSurface }
}