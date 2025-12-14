package com.example.hastanghubaga.ui.tokens.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.ui.tokens.Elevation

@Preview(showBackground = true)
@Composable
fun ElevationTokenPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevationCard("Low", Elevation.Low)
        ElevationCard("Medium", Elevation.Medium)
        ElevationCard("High", Elevation.High)
    }
}

@Composable
private fun ElevationCard(label: String, elevation: Dp) {
    Card(
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        Text(label, modifier = Modifier.padding(16.dp))
    }
}
