package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.ActivityRepositoryImpl
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityModule {

    @Binds
    abstract fun bindActivityRepository(
        impl: ActivityRepositoryImpl
    ): ActivityRepository
}