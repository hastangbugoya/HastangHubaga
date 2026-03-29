package com.example.hastanghubaga.di

import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshotUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetSnapshotBindingsModule {

    @Binds
    @Singleton
    abstract fun bindBuildWidgetDailySnapshot(
        impl: BuildWidgetDailySnapshotUseCase
    ): BuildWidgetDailySnapshot
}