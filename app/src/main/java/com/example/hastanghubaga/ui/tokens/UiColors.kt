package com.example.hastanghubaga.ui.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color accessors.
 *
 * Do NOT hardcode colors in UI.
 *
 * WHY:
 * - Prevents hard-coded colors in UI
 * - Ensures dark mode + dynamic color compatibility
 * - Makes UI intent obvious
 *
 * IMPORTANT:
 * - These represent MEANING, not visual preference
 * - Colors are resolved via MaterialTheme where possible
 *
 * UiColors > Composable
 */
object UiColors {

    @Composable
    fun Primary(): Color = MaterialTheme.colorScheme.primary

    @Composable
    fun Secondary(): Color = MaterialTheme.colorScheme.secondary

    @Composable
    fun Background(): Color = MaterialTheme.colorScheme.background

    @Composable
    fun Surface(): Color = MaterialTheme.colorScheme.surface
    @Composable
    fun Warning(): Color = MaterialTheme.colorScheme.tertiary

    @Composable
    fun Error(): Color = MaterialTheme.colorScheme.error

    @Composable
    fun Success(): Color = MaterialTheme.colorScheme.primary

    @Composable
    fun MutedText(): Color = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun IconDefault(): Color = MaterialTheme.colorScheme.onSurface
}