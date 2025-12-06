package com.example.hastanghubaga.widget

import android.content.Context
import org.json.JSONArray

object SupplementWidgetPrefs {
    private const val PREFS_NAME = "supplement_widget_prefs"
    private const val KEY_TODAY_LIST = "today_supplements"

    fun saveTodaySupplements(context: Context, suppNames: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONArray(suppNames).toString()
        prefs.edit().putString(KEY_TODAY_LIST, json).apply()
    }

    fun loadTodaySupplements(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TODAY_LIST, "[]")
        val arr = JSONArray(json)
        return List(arr.length()) { i -> arr.getString(i) }
    }
}
