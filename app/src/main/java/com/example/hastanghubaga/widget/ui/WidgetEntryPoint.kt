package com.example.hastanghubaga.widget.ui

import com.example.hastanghubaga.widget.snapshot.WidgetSnapshotStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetSnapshotStore(): WidgetSnapshotStore
}