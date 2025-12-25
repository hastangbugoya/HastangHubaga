package com.example.hastanghubaga.widget.snapshot

import com.example.hastanghubaga.widget.model.WidgetDailySnapshot

interface WidgetSnapshotStore {
    fun save(snapshot: WidgetDailySnapshot)
    fun load(): WidgetDailySnapshot?
}
