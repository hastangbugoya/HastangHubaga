package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.SupplementRepositoryImpl
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import dagger.hilt.components.SingletonComponent
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn

@Module
@InstallIn(SingletonComponent::class)
abstract class SupplementDoseLogModule {

    @Binds
    abstract fun bindSupplementDoseLogRepository(
        impl: SupplementRepositoryImpl
    ): SupplementDoseLogRepository
}
