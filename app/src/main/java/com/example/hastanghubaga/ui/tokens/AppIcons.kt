package com.example.hastanghubaga.ui.tokens

import androidx.annotation.DrawableRes
import com.example.hastanghubaga.R

/**
 * Central registry of app icons.
 *
 * WHY:
 * - Prevent scattered drawable references
 * - Easier refactors / theming
 */
object AppIcons {

    @DrawableRes
    val AvoidCaffeine = R.drawable.ic_supplement_avoid_caffeine

    @DrawableRes
    val WithFood = R.drawable.ic_meal_rice_bowl

    @DrawableRes
    val Warning = R.drawable.ic_supplement_eye_alert
}
