package com.example.hastanghubaga.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.example.hastanghubaga.domain.model.supplement.Supplement
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate


class SupplementWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val names = SupplementWidgetPrefs.loadTodaySupplements(context)

        provideContent {
            SupplementWidgetUI(names)
        }
    }
}

@Composable
private fun SupplementWidgetUI(supps: List<String>) {
    Column(modifier = GlanceModifier.padding(16.dp)) {
        Text("Today's Supplements")
        supps.forEach {
            Text("• ${it}")
        }
    }
}