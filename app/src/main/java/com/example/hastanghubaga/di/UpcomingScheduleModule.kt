package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.UpcomingScheduleRepositoryImpl
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UpcomingScheduleModule {

    @Binds
    abstract fun bindUpcomingScheduleRepository(
        impl: UpcomingScheduleRepositoryImpl
    ): UpcomingScheduleRepository
}
