package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.SupplementOccurrenceRepositoryImpl
import com.example.hastanghubaga.data.repository.SupplementRepositoryImpl
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementOccurrenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SupplementDoseLogModule {

    @Binds
    abstract fun bindSupplementDoseLogRepository(
        impl: SupplementRepositoryImpl
    ): SupplementDoseLogRepository

    @Binds
    abstract fun bindSupplementOccurrenceRepository(
        impl: SupplementOccurrenceRepositoryImpl
    ): SupplementOccurrenceRepository
}
