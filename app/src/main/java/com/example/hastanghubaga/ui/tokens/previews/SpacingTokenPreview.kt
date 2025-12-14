package com.example.hastanghubaga.ui.tokens.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.ui.tokens.Dimens

@Preview(showBackground = true)
@Composable
fun SpacingTokenPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        SpacerBar("XS", Dimens.SpaceXS)
        SpacerBar("S", Dimens.SpaceS)
        SpacerBar("M", Dimens.SpaceM)
        SpacerBar("L", Dimens.SpaceL)
        SpacerBar("XL", Dimens.SpaceXL)
    }
}

@Composable
private fun SpacerBar(label: String, space: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(40.dp))
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(space)
                .background(Color.Blue)
        )
    }
}
