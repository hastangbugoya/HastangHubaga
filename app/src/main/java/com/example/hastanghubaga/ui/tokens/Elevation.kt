package com.example.hastanghubaga.ui.tokens

import androidx.compose.ui.unit.dp

/**
 * Elevation tokens (z-axis).
 *
 * WHY:
 * - Consistent depth hierarchy
 * - Easy global tuning
 */
object Elevation {

    /** Flat surfaces */
    val None = 0.dp

    /** Cards, list items */
    val Low = 2.dp

    /** Floating buttons, raised cards */
    val Medium = 6.dp

    /** Dialogs, bottom sheets */
    val High = 12.dp
}
