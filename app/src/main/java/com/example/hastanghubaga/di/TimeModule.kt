package com.example.hastanghubaga.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.datetime.Clock
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.System
}