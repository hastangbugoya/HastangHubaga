package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.NutritionGoalsRepositoryImpl
import com.example.hastanghubaga.domain.repository.nutrition.NutritionGoalsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NutritionModule {

    @Binds
    @Singleton
    abstract fun bindNutritionGoalsRepository(
        impl: NutritionGoalsRepositoryImpl
    ): NutritionGoalsRepository
}
