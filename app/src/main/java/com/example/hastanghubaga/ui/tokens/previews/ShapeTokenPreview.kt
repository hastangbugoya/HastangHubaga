package com.example.hastanghubaga.ui.tokens.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.ui.tokens.Shapes

@Preview(showBackground = true)
@Composable
fun ShapeTokenPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShapeBox("Small", Shapes.Small)
        ShapeBox("Medium", Shapes.Medium)
        ShapeBox("Large", Shapes.Large)
        ShapeBox("Pill", Shapes.Pill)
    }
}

@Composable
private fun ShapeBox(label: String, shape: Shape) {
    Column {
        Text(label)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(shape)
                .background(Color.Gray)
        )
    }
}
