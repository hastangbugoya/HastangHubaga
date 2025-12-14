package com.example.hastanghubaga.ui.tokens

/**
 * Motion & animation timing tokens.
 *
 * WHY:
 * - Prevent inconsistent animation speeds
 * - Improve perceived polish
 * - Easier UX tuning later
 */
object Motion {

    /** Micro interactions (icon tint, press) */
    const val Fast = 100

    /** Standard UI transitions */
    const val Normal = 300

    /** Larger transitions (screens, sheets) */
    const val Slow = 500
}
