package com.example.hastanghubaga.widget.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.hastanghubaga.R
import com.example.hastanghubaga.ui.main.MainActivity
import dagger.hilt.EntryPoints


class WidgetDebugProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val entryPoint = EntryPoints.get(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val snapshotStore = entryPoint.widgetSnapshotStore()
        val snapshot = snapshotStore.load()

        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_debug
            )

            val text = snapshot?.let {
                buildString {
                    appendLine("Day: ${it.day}")
                    appendLine("Generated: ${it.generatedAt}")
                    appendLine("UpNext: ${it.upNext?.title ?: "ALL DONE"}")
                    appendLine("Ingredients:")
                    it.ingredients.forEach { ing ->
                        appendLine("- ${ing.name}")
                    }
                }
            } ?: "Snapshot is NULL"

            views.setTextViewText(R.id.widget_text, text)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId, // unique per widget instance
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(
                R.id.widget_root,
                pendingIntent
            )
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
