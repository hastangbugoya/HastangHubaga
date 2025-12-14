package com.example.hastanghubaga.ui.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shape tokens for rounded corners.
 *
 * WHY:
 * - Centralize corner radius decisions
 * - Avoid random dp values in UI
 * - Keep visual language consistent
 */
object Shapes {

    /** Small UI elements (chips, tags, icons) */
    val Small = RoundedCornerShape(4.dp)

    /** Buttons, cards, list rows */
    val Medium = RoundedCornerShape(8.dp)

    /** Large cards, dialogs, sheets */
    val Large = RoundedCornerShape(16.dp)

    /** Fully rounded (pills, avatars) */
    val Pill = RoundedCornerShape(50)
}
