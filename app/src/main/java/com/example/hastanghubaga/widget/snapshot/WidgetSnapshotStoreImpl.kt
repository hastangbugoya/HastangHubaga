package com.example.hastanghubaga.widget.snapshot

import android.content.SharedPreferences
import com.example.hastanghubaga.widget.model.WidgetDailySnapshot
import javax.inject.Inject

class WidgetSnapshotStoreImpl @Inject constructor(
    private val prefs: SharedPreferences,
    private val moshi: Moshi
) : WidgetSnapshotStore {

    override fun save(snapshot: WidgetDailySnapshot) {
        prefs.edit()
            .putString(KEY, moshi.adapter(WidgetDailySnapshot::class.java).toJson(snapshot))
            .apply()
    }

    override fun load(): WidgetDailySnapshot? {
        val json = prefs.getString(KEY, null) ?: return null
        return moshi.adapter(WidgetDailySnapshot::class.java).fromJson(json)
    }

    companion object {
        private const val KEY = "widget_daily_snapshot"
    }
}
