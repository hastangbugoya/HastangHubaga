package com.example.hastanghubaga.ui.tokens.previews

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.ui.tokens.Motion

@Preview(showBackground = true)
@Composable
fun MotionTokenPreview() {
    var expanded by remember { mutableStateOf(false) }

    val size by animateDpAsState(
        targetValue = if (expanded) 120.dp else 60.dp,
        animationSpec = tween(Motion.Normal),
        label = "motion-preview"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Color.Magenta)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        expanded = !expanded
                    }
                )
        )
        Spacer(Modifier.height(8.dp))
        Text("Tap box to animate")
    }
}
