package com.example.hastanghubaga.ui.tokens.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
/**
 * DESIGN SYSTEM PREVIEW
 *
 * Purpose:
 * --------
 * Single entry point to visually inspect all UI tokens:
 * colors, spacing, shapes, elevation, and motion.
 *
 * How to use:
 * -----------
 * - Open this file first when tweaking UI
 * - Adjust token values and see changes instantly
 * - Use as living documentation
 *
 * Rules:
 * ------
 * - No business logic here
 * - No app state
 * - No ViewModels
 *
 * If something looks off in the app,
 * check it here first.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DesignSystemPreviewScreen() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Section("Colors") { ColorTokenPreview() }
        }
        item {
            Section("Spacing") { SpacingTokenPreview() }
        }
        item {
            Section("Shapes") { ShapeTokenPreview() }
        }
        item {
            Section("Elevation") { ElevationTokenPreview() }
        }
        item {
            Section("Motion") { MotionTokenPreview() }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}
