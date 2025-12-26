package com.example.hastanghubaga.widget.snapshot

import android.content.SharedPreferences
import com.example.hastanghubaga.core.backup.SerializationConfig.json
import com.example.hastanghubaga.widget.model.WidgetDailySnapshot
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WidgetSnapshotStoreImpl @Inject constructor(
    private val prefs: SharedPreferences,
    private val json: Json
) : WidgetSnapshotStore {

    override fun save(snapshot: WidgetDailySnapshot) {
        prefs.edit()
            .putString(KEY, Json.encodeToString(
                value = snapshot,
                serializer =
                    WidgetDailySnapshot.serializer()
            ))
            .apply()
    }

    override fun load(): WidgetDailySnapshot? {
        val raw = prefs.getString(KEY, null) ?: return null
        return json.decodeFromString<WidgetDailySnapshot>(raw)
    }

    companion object {
        private const val KEY = "widget_daily_snapshot"
    }
}
