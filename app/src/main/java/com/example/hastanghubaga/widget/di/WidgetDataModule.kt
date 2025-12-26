package com.example.hastanghubaga.widget.di

import com.example.hastanghubaga.data.repository.IngredientPreferenceRepositoryImpl
import com.example.hastanghubaga.domain.repository.widget.IngredientPreferenceRepository
import com.example.hastanghubaga.widget.snapshot.WidgetSnapshotStore
import com.example.hastanghubaga.widget.snapshot.WidgetSnapshotStoreImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetDataModule {

    @Binds
    @Singleton
    abstract fun bindIngredientPreferenceRepository(
        impl: IngredientPreferenceRepositoryImpl
    ): IngredientPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindWidgetSnapshotStore(
        impl: WidgetSnapshotStoreImpl
    ): WidgetSnapshotStore

}