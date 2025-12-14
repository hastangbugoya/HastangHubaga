package com.example.hastanghubaga.ui.tokens.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.ui.tokens.AppColors
import com.example.hastanghubaga.ui.tokens.Shapes
import com.example.hastanghubaga.ui.tokens.UiColors

@Preview(showBackground = true)
@Composable
fun ColorTokenPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ColorRow("Primary", UiColors.Primary())
        ColorRow("Secondary", UiColors.Secondary())
        ColorRow("Background", UiColors.Background())
        ColorRow("Surface", UiColors.Surface())
        ColorRow("Error", UiColors.Error())
    }
}

@Composable
private fun ColorRow(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, Shapes.Medium)
        )
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}
