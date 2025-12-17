package com.example.hastanghubaga.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.DecimalFormat

private val wholeOrDecimal = DecimalFormat("0.#")

fun Double.toUiNumber(): String =
    wholeOrDecimal.format(this)

@Composable
fun Double.asDisplayTextComposable(): String =
    remember(this) {
        if (this % 1.0 == 0.0) {
            this.toInt().toString()
        } else {
            DecimalFormat("0.#").format(this)
        }
    }

fun Double.asDisplayTextNonComposable(): String =
        if (this % 1.0 == 0.0) {
            this.toInt().toString()
        } else {
            DecimalFormat("0.#").format(this)
        }