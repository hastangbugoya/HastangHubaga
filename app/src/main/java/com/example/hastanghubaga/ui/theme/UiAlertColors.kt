package com.example.hastanghubaga.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Semantic alert colors used for status, warnings, and errors.
 *
 * PURPOSE
 * -------
 * Centralizes alert-related color decisions so UI code
 * never hardcodes meaning ("red", "yellow", etc.).
 *
 * These colors automatically adapt to:
 * - Light / Dark mode
 * - Dynamic color (Android 12+)
 *
 * RULE
 * ----
 * UI should reference alert intent (Error, Warning, Success),
 * not specific colors.
 */
object UiAlertColors {

    /** Critical errors (validation failures, crashes, blocking issues) */
    @Composable
    fun error(): Color = MaterialTheme.colorScheme.error

    /** Warnings (caffeine, timing issues, non-blocking alerts) */
    @Composable
    fun warning(): Color = MaterialTheme.colorScheme.tertiary

    /** Positive confirmation (saved, completed, success states) */
    @Composable
    fun success(): Color = MaterialTheme.colorScheme.primary

    /** Neutral informational messages */
    @Composable
    fun info(): Color = MaterialTheme.colorScheme.secondary
}
